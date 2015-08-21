package ch.unil.fcrepo4.readme;

import ch.unil.fcrepo4.spring.data.core.FedoraTemplate;
import org.fcrepo.client.FedoraException;
import org.fcrepo.client.FedoraRepository;
import org.fcrepo.client.impl.FedoraRepositoryImpl;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.env.Environment;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

/**
 * @author gushakov
 */

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {ReadMeTest.TestConfig.class})
public class ReadMeTest {

    @Configuration
    @PropertySource("classpath:fcrepo4.properties")
    static class TestConfig {

        @Autowired
        private Environment env;

        @Bean
        public FedoraRepository fedoraRepository() throws FedoraException {
            String repoUrl = env.getProperty("fedora.repository.url");
            return new FedoraRepositoryImpl(repoUrl);
        }

        @Bean
        public FedoraTemplate fedoraTemplate() throws FedoraException {
            return new FedoraTemplate(fedoraRepository());
        }

    }

    @Autowired
    private FedoraTemplate fedoraTemplate;

    @Test
    public void testSaveWithPhoto() throws Exception {
        fedoraTemplate.save(new Vehicle());
    }

    @Test
    public void testCustomPathCreator() throws Exception {
       fedoraTemplate.save(new Person());
    }

}
