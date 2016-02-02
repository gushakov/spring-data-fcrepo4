package ch.unil.fcrepo4.spring.data.repository;

import ch.unil.fcrepo4.spring.data.core.FedoraTemplate;
import ch.unil.fcrepo4.spring.data.repository.config.EnableFedoraRepositories;
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
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.List;

import static ch.unil.fcrepo4.assertj.Assertions.assertThat;

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
            Vehicle greenFord = new Vehicle(1L, "Ford", "light green", 1000, 6.5f);
            vehicleRepo.save(greenFord);
        }

        if (!vehicleRepo.exists(2L)) {
            Vehicle redToyota = new Vehicle(2L, "Toyota", "red", 15000, 4.5f);
            vehicleRepo.save(redToyota);
        }
    }

    @Test
    public void testFindByMake() throws Exception {
        List<Vehicle> vehicles = vehicleRepo.findByMake("Ford");
        assertThat(vehicles).isNotEmpty();
        assertThat(vehicles.get(0)).hasMake("Ford");
        System.out.println(vehicles.get(0).getCreated());
    }

    @Test
    public void testFindByMilesGreaterThan() throws Exception {
       List<Vehicle> vehicles = vehicleRepo.findByMilesGreaterThan(14000);
       assertThat(vehicles).withMilesGreaterThan(14000)
               .extracting("id").containsExactly(2L);
    }

    @Test
    public void testFindByMakeAndColor() throws Exception {
        List<Vehicle> vehicles = vehicleRepo.findByMakeAndColor("Ford", "light green");
        assertThat(vehicles).isNotEmpty();
        assertThat(vehicles.get(0))
                .hasMake("Ford")
                .hasColor("light green");
    }

    @Test
    public void testFindByColorLike() throws Exception {
        List<Vehicle> vehicles = vehicleRepo.findByColorLike("green");
        assertThat(vehicles).isNotEmpty();
        assertThat(vehicles.get(0)).hasColorLike("green");
    }

    @Test
    public void testFindByMakeOrColor() throws Exception {
        List<Vehicle> vehicles = vehicleRepo.findByMakeOrColorLike("Ford", "red");
        assertThat(vehicles).extracting("id").containsOnly(1L, 2L);
    }

    @Test
    public void testFindByCreatedGreaterThan() throws Exception {
        List<Vehicle> vehicles = vehicleRepo.findByCreatedGreaterThan(DateTime.now().minusDays(2).toDate());
        assertThat(vehicles).isNotEmpty();
    }
}
