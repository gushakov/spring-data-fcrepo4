package ch.unil.fcrepo4.spring.data.repository;

import ch.unil.fcrepo4.spring.data.core.FedoraTemplate;
import ch.unil.fcrepo4.spring.data.repository.config.EnableFedoraRepositories;
import ch.unil.fcrepo4.spring.data.transaction.FedoraRepositoryTransactionManager;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.event.ContextClosedEvent;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.core.env.Environment;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.DefaultTransactionDefinition;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author gushakov
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {EnableTransactionManagementTestIT.TestConfig.class},
initializers = {EnableTransactionManagementTestIT.ContextEventsListener.class})
public class EnableTransactionManagementTestIT {


    @Configuration
    @PropertySource("classpath:fcrepo4.properties")
    @EnableFedoraRepositories
    @EnableTransactionManagement
    public static class TestConfig {

        @Autowired
        private Environment env;

        //@formatter:off
        @Bean
        public FedoraTemplate fedoraTemplate() {
            return new FedoraTemplate(env.getProperty("fedora.host"),
                    env.getProperty("fedora.port", Integer.class),
                    env.getProperty("fedora.path"),
                    env.getProperty("triplestore.host"),
                    env.getProperty("triplestore.port", Integer.class),
                    env.getProperty("triplestore.path"),
                    env.getProperty("triplestore.db"));
        }
        //@formatter:on

        @Bean
        public PlatformTransactionManager transactionManager() {
            return new FedoraRepositoryTransactionManager(fedoraTemplate().getRepository());
        }

    }

    public static class ContextEventsListener implements ApplicationContextInitializer<ConfigurableApplicationContext>{
        private VehicleCrudRepository vehicleRepo;

        @Override
        public void initialize(ConfigurableApplicationContext applicationContext) {
            applicationContext.addApplicationListener(event -> {
                if (event instanceof ContextRefreshedEvent){
                    vehicleRepo = applicationContext.getBean(VehicleCrudRepository.class);
                }
                else if (event instanceof ContextClosedEvent) {
                    // if @Transactional did work, this bean should have been rolled back
                    assertThat(vehicleRepo.exists(1L)).isFalse();
                }
            });
        }
    }

    @SuppressWarnings("SpringJavaAutowiringInspection")
    @Autowired
    private VehicleCrudRepository vehicleRepo;

    @Autowired
    private PlatformTransactionManager transactionManager;

    @Test
    @Transactional
    public void testCreateWithRollback() throws Exception {
        vehicleRepo.save(new Vehicle(1L, "Ford", "light green", 1000, 6.5f));
        // should exist in the current transaction
        assertThat(vehicleRepo.exists(1L)).isTrue();
    }

    @Test
    public void testRollbackProgrammatically() throws Exception {
        DefaultTransactionDefinition def = new DefaultTransactionDefinition(TransactionDefinition.PROPAGATION_REQUIRED);
        final TransactionStatus status = transactionManager.getTransaction(def);
        vehicleRepo.save(new Vehicle(2L, "Toyota", "red", 15000, 4.5f));
        transactionManager.rollback(status);
        assertThat(vehicleRepo.exists(2L)).isFalse();
    }



}
