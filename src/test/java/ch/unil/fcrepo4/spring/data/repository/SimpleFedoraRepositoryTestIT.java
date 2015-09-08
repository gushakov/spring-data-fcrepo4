package ch.unil.fcrepo4.spring.data.repository;

// based on code from org.springframework.data.solr.repository.ITestSolrRepositoryOperations

import ch.unil.fcrepo4.beans.Vehicle;
import ch.unil.fcrepo4.spring.data.core.FedoraTemplate;
import ch.unil.fcrepo4.spring.data.repository.config.EnableFedoraRepositories;
import org.fcrepo.client.*;
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
            return new FedoraTemplate(env.getProperty("fedora.repository.url"), env.getProperty("triplestore.sparql.query.url"));
        }
    }

    @SuppressWarnings("SpringJavaAutowiringInspection")
    @Autowired
    private VehicleCrudRepository repository;

    @Test
    public void testWireVehicleRepository() throws Exception {
        assertThat(repository).isNotNull();
    }

    @Test
    public void testSaveOne() throws Exception {
        Vehicle vehicle = new Vehicle(1L, "Ford", "Green", 15000, 6.5f);
        assertThat(repository.save(vehicle)).isEqualTo(vehicle);
        assertThat(repository.exists(1L));
    }

    @Test
    public void testFindOne() throws Exception {
        Vehicle vehicle = new Vehicle(1L, "Ford", "Green", 15000, 6.5f);
        repository.save(vehicle);
        assertThat(repository.findOne(1L)).isNotNull();
    }

    @Test
    public void testFindByMake() throws Exception {
        Vehicle vehicle = new Vehicle(1L, "Ford", "Green", 15000, 6.5f);
        repository.save(vehicle);
        List<Vehicle> vehicles = repository.findByMake("Ford");
        assertThat(vehicles)
                .hasSize(1)
                .extracting("id", "make", "color", "miles", "consumption")
                .contains(tuple(1L, "Ford", "Green", 15000, 6.5f))
        ;
    }

}
