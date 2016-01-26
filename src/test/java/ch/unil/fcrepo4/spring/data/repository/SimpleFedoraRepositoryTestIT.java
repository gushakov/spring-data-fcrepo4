package ch.unil.fcrepo4.spring.data.repository;

// based on code from org.springframework.data.solr.repository.ITestSolrRepositoryOperations

import ch.unil.fcrepo4.assertj.Assertions;
import ch.unil.fcrepo4.beans.Vehicle;
import ch.unil.fcrepo4.beans.VehicleDescription;
import ch.unil.fcrepo4.beans.VehiclePicture;
import ch.unil.fcrepo4.spring.data.core.FedoraTemplate;
import ch.unil.fcrepo4.spring.data.core.query.FedoraRdfQueryPageRequest;
import ch.unil.fcrepo4.spring.data.repository.config.EnableFedoraRepositories;
import org.assertj.core.api.Condition;
import org.fcrepo.client.FedoraException;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.env.Environment;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.domain.Page;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.io.ByteArrayInputStream;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;


/**
 * @author gushakov
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {SimpleFedoraRepositoryTestIT.TestConfig.class})
public class SimpleFedoraRepositoryTestIT {

    @Configuration
    @PropertySource("classpath:fcrepo4.properties")
    @EnableFedoraRepositories
    public static class TestConfig {
        @Autowired
        private Environment env;

        @Bean
        public FedoraTemplate fedoraTemplate() throws FedoraException {
            return new FedoraTemplate(env.getProperty("fedora.host"), env.getProperty("fedora.port", Integer.class));
        }

    }

    @SuppressWarnings("SpringJavaAutowiringInspection")
    @Autowired
    private VehicleCrudRepository vehicleRepo;

    @Value("#{environment.getProperty('fedora.repository.url')}")
    private String repoUrl;

    @Value("#{environment.getProperty('triplestore.sparql.data.url')}")
    private String sparqlDataUrl;

    private boolean initialized = false;

    @Before
    public void setUp() throws Exception {

        if (!initialized) {

            Vehicle vehicle1 = new Vehicle(1L, "Ford", "Green", 15000, 6.5f);
            vehicle1.setDescription(new VehicleDescription(new ByteArrayInputStream("<car>lorem ipsum</car>".getBytes())));
            vehicleRepo.save(vehicle1);

            Vehicle vehicle2 = new Vehicle(2L, "Toyota", "Light-green", 10000, 7.5f);
            vehicle2.setPicture(new VehiclePicture(new ClassPathResource("picture.png").getInputStream()));
            vehicleRepo.save(vehicle2);

            vehicleRepo.save(new Vehicle(3L, "Honda", "Yellow", 20000, 8.5f));
            vehicleRepo.save(new Vehicle(4L, "Volkswagen", "Red", 30000, 5.0f));
            vehicleRepo.save(new Vehicle(5L, "BMW", "Gray", 5000, 7.5f));

            initialized = true;
        }
    }

    @Test
    public void testWireRepository() throws Exception {
        assertThat(vehicleRepo).isNotNull();
    }

    @Test
    public void testExists() throws Exception {
        assertThat(vehicleRepo.exists(1L));
    }

    @Test
    public void testFindOne() throws Exception {
        assertThat(vehicleRepo.findOne(1L)).isNotNull();
    }

    @Test
    public void testFindByMake() throws Exception {
        assertThat(vehicleRepo.findByMake("Ford"))
                .hasSize(1)
                .extracting("id", "make", "color", "miles", "consumption")
                .contains(tuple(1L, "Ford", "Green", 15000, 6.5f))
        ;
    }

    @Test
    public void testFindByMilesGreaterThan() throws Exception {
        assertThat(vehicleRepo.findByMilesGreaterThan(15000))
                .extracting("miles", Integer.class).have(greaterThan(15000));
    }

    @Test
    public void testFindByMilesGreaterThanAndConsumptionGreaterThan() throws Exception {
        List<Vehicle> vehicles = vehicleRepo.findByMilesGreaterThanAndConsumptionGreaterThan(5000, 7.0f);
        assertThat(vehicles)
                .extracting("miles", Integer.class).have(greaterThan(5000));
        assertThat(vehicles)
                .extracting("consumption", Float.class).have(greaterThan(7.0f));

    }

    @Test
    public void testFindByColorLike() throws Exception {
        List<Vehicle> vehicles = vehicleRepo.findByColorLike("green");
        assertThat(vehicles).extracting("color", String.class)
                .are(like("green"));
    }

    @Test
    public void testFindByMilesGreaterThanInPageable() throws Exception {
        Page<Vehicle> page1 = vehicleRepo.findByMilesGreaterThan(0, new FedoraRdfQueryPageRequest(0, 3));
        assertThat(page1.getNumber()).isEqualTo(0);
        assertThat(page1.isFirst());
        assertThat(page1.getNumberOfElements()).isEqualTo(3);
        assertThat(page1.getTotalElements()).isEqualTo(5);
        assertThat(page1.hasNext()).isTrue();
        assertThat(page1.getContent()).extracting("miles", Integer.class).have(greaterThan(0));
        Page<Vehicle> page2 = vehicleRepo.findByMilesGreaterThan(0, page1.nextPageable());
        assertThat(page2.getNumber()).isEqualTo(1);
        assertThat(page2.isLast()).isTrue();
        assertThat(page2.getNumberOfElements()).isEqualTo(2);
    }

    @Test
    public void testLoadDatastream() throws Exception {
        Vehicle vehicle = vehicleRepo.findOne(1L);
        Assertions.assertThat(vehicle.getDescription().getDesc()).hasXmlContentEquivalentTo("<car>lorem ipsum</car>");
    }

    @Test
    public void testAccessResourceProperties() throws Exception {
        Vehicle vehicle = vehicleRepo.findOne(1L);
        assertThat(vehicle.getUuid()).isNotNull();
        assertThat(vehicle.getCreatedDate()).isNotNull();
    }

    private <T> Condition<Comparable<T>> greaterThan(T checkValue) {
        return new Condition<Comparable<T>>() {
            @Override
            public boolean matches(Comparable<T> value) {
                return value.compareTo(checkValue) > 0;
            }
        };
    }

    private Condition<String> like(String text){
        return new Condition<String>(){
            @Override
            public boolean matches(String value) {
                return value.toLowerCase().contains(text.toLowerCase());
            }
        };
    }

}
