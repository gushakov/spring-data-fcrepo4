package ch.unil.fcrepo4.client;

import ch.unil.fcrepo4.spring.data.repository.config.EnableFedoraRepositories;
import org.fcrepo.client.FedoraException;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.env.Environment;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author gushakov
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {TransactionalFedoraRepositoryImplTestIT.TestConfig.class})
public class TransactionalFedoraRepositoryImplTestIT {

    @Configuration
    @PropertySource("classpath:fcrepo4.properties")
    @EnableFedoraRepositories
    public static class TestConfig {
        @Autowired
        private Environment env;

        @Bean
        public TransactionalFedoraRepository fedoraRepository() throws FedoraException {
            return new TransactionalFedoraRepositoryImpl(env.getProperty("fedora.repository.url"));
        }
    }

    @Autowired
    private TransactionalFedoraRepository fedoraRepository;

    @Test
    public void testStartRollbackTransaction() throws Exception {
        String txId = fedoraRepository.startTransaction();
        assertThat(txId).isNotEmpty();
        fedoraRepository.rollbackTransaction();
    }

    @Test
    public void testCommitTransaction() throws Exception {
        String txId = fedoraRepository.startTransaction();
        assertThat(txId).isNotEmpty();
        fedoraRepository.commitTransaction();
    }

    @Test
    public void testCreateResourceAndRollback() throws Exception {
        fedoraRepository.startTransaction();
        String path = "/toto" + System.currentTimeMillis();
        fedoraRepository.createObject(path);
        fedoraRepository.rollbackTransaction();
        assertThat(fedoraRepository.exists(path)).isFalse();
    }

    @Test
    public void testMultiThreaded() throws Exception {
        final Set<String> txIds = new HashSet<>();

        // based on code from http://stackoverflow.com/a/1250655
        ExecutorService threadPool = Executors.newFixedThreadPool(4);
        for (int i = 0; i < 10; i++){
            threadPool.execute(() -> {
                try {
                    String txId = fedoraRepository.startTransaction();
                    if (txIds.contains(txId)) {
                        throw new AssertionError("Duplicate transaction detected");
                    }
                    txIds.add(txId);
                    fedoraRepository.rollbackTransaction();
                } catch (FedoraException e) {
                    throw new AssertionError(e);
                }
            });
        }
        threadPool.shutdown();
        threadPool.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
    }

}
