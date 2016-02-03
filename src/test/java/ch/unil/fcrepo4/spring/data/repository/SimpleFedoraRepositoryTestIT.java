package ch.unil.fcrepo4.spring.data.repository;

import ch.unil.fcrepo4.spring.data.core.FedoraTemplate;
import ch.unil.fcrepo4.spring.data.core.query.FedoraPageRequest;
import ch.unil.fcrepo4.spring.data.repository.config.EnableFedoraRepositories;
import org.assertj.core.api.Assertions;
import org.fcrepo.client.FedoraException;
import org.joda.time.DateTime;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.env.Environment;
import org.springframework.data.domain.Page;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.List;

import static ch.unil.fcrepo4.assertj.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThat;

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
            return new FedoraTemplate(env.getProperty("fedora.host"),
                    env.getProperty("fedora.port", Integer.class));
        }

    }

    @SuppressWarnings("SpringJavaAutowiringInspection")
    @Autowired
    private VehicleCrudRepository vehicleRepo;

    @Before
    public void setUp() throws Exception {

        if (!vehicleRepo.exists(1L)) {
            vehicleRepo.save(new Vehicle(1L, "Ford", "light green", 1000, 6.5f));
            vehicleRepo.save(new Vehicle(2L, "Toyota", "red", 15000, 4.5f));
            vehicleRepo.save(new Vehicle(3L, "Ferrari", "red", 10000, 7.5f));
            vehicleRepo.save(new Vehicle(4L, "Volkswagen", "blue", 20000, 5.5f));
            vehicleRepo.save(new Vehicle(5L, "Honda", "green", 1500, 5.0f));
            vehicleRepo.save(new Vehicle(6L, "Lexus", "light brown", 3000, 7.0f));
            vehicleRepo.save(new Vehicle(7L, "Citroën", "silver", 1000, 4.0f));
            vehicleRepo.save(new Vehicle(8L, "Dodge", "red", 1500, 6.0f));
        }
    }

    @Test
    public void testFindByMake() throws Exception {
        List<Vehicle> vehicles = vehicleRepo.findByMake("Ford");
//        List<Vehicle> vehicles = vehicleRepo.findByMake("Citroën");
        ch.unil.fcrepo4.assertj.Assertions.assertThat(vehicles).isNotEmpty();
//        assertThat(vehicles.get(0)).hasMake("Citroën");
        assertThat(vehicles.get(0)).hasMake("Ford");
    }

    @Test
    public void testFindByMilesGreaterThan() throws Exception {
        List<Vehicle> vehicles = vehicleRepo.findByMilesGreaterThan(14000);
        ch.unil.fcrepo4.assertj.Assertions.assertThat(vehicles).withMilesGreaterThan(14000)
                .extracting("id").containsExactly(2L, 4L);
    }

    @Test
    public void testFindByMakeAndColor() throws Exception {
        List<Vehicle> vehicles = vehicleRepo.findByMakeAndColor("Ford", "light green");
        ch.unil.fcrepo4.assertj.Assertions.assertThat(vehicles).isNotEmpty();
        assertThat(vehicles.get(0))
                .hasMake("Ford")
                .hasColor("light green");
    }

    @Test
    public void testFindByColorLike() throws Exception {
        List<Vehicle> vehicles = vehicleRepo.findByColorLike("green");
        ch.unil.fcrepo4.assertj.Assertions.assertThat(vehicles).isNotEmpty();
        assertThat(vehicles.get(0)).hasColorLike("green");
    }

    @Test
    public void testFindByMakeOrColor() throws Exception {
        List<Vehicle> vehicles = vehicleRepo.findByMakeOrColorLike("Ford", "red");
        ch.unil.fcrepo4.assertj.Assertions.assertThat(vehicles).extracting("id").containsOnly(1L, 2L, 3L, 8L);
    }

    @Test
    public void testFindByCreatedGreaterThan() throws Exception {
        List<Vehicle> vehicles = vehicleRepo.findByCreatedGreaterThan(new DateTime(0L).toDate());
        ch.unil.fcrepo4.assertj.Assertions.assertThat(vehicles).isNotEmpty();
    }

    @Test
    public void testFindByMilesGreaterThanPage() throws Exception {
        Page<Vehicle> firstPage = vehicleRepo.findByMilesGreaterThan(0, new FedoraPageRequest(0, 4));
        assertThat(firstPage.getNumberOfElements()).isEqualTo(4);
        assertThat(firstPage.isFirst()).isTrue();
        assertThat(firstPage.hasNext()).isTrue();
        Page<Vehicle> secondPage = vehicleRepo.findByMilesGreaterThan(0, firstPage.nextPageable());
        assertThat(secondPage.getNumberOfElements()).isEqualTo(4);
        assertThat(secondPage.isFirst()).isFalse();
        assertThat(secondPage.hasNext()).isTrue();
        Page<Vehicle> thirdPage = vehicleRepo.findByMilesGreaterThan(0, secondPage.nextPageable());
        assertThat(thirdPage.getNumberOfElements()).isEqualTo(0);
        assertThat(thirdPage.isFirst()).isFalse();
        assertThat(thirdPage.hasNext()).isFalse();
    }

    @Test
    public void testFindAllByPage() throws Exception {
        assertThat(vehicleRepo.findAll(new FedoraPageRequest(0, Integer.MAX_VALUE)).getNumberOfElements())
                .isEqualTo(8);
    }
}
