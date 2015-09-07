package ch.unil.fcrepo4.spring.data.repository;

// based on code from org.springframework.data.solr.repository.ITestSolrRepositoryOperations

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
        Vehicle bean = new Vehicle(1L);
        assertThat(repository.save(bean)).isEqualTo(bean);
        assertThat(repository.exists(1L));
    }

    @Test
    public void testFindOne() throws Exception {
        Vehicle bean = new Vehicle(1L);
        assertThat(repository.save(bean));
        assertThat(repository.findOne(1L)).isNotNull();
    }

    @Test
    public void testFindByMake() throws Exception {
        repository.findByMake("Ford");
    }

    @Test
    public void testFindByMakeAndMilesGreaterThan() throws Exception {
        repository.findByMakeAndMilesGreaterThan("Ford", 10000);
    }

    @Test
    public void testFindByMakeAndMilesGreaterThanAndConsumptionGreaterThan() throws Exception {
        repository.findByMakeAndMilesGreaterThanAndConsumptionGreaterThan("Ford", 10000, 6.5f);
    }

    @Test
    public void testFindByMakeAndMilesOrColorAndConsumption() throws Exception {
        repository.findByMakeAndMilesOrColorAndConsumption("Ford", 10000, "green", 6.5f);
    }


    @Test
    public void testFindByMakeLike() throws Exception {
        repository.findByMakeLike("toto");
    }

}
