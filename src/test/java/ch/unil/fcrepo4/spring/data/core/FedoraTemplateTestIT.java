package ch.unil.fcrepo4.spring.data.core;

import ch.unil.fcrepo4.spring.data.core.mapping.annotation.FedoraObject;
import ch.unil.fcrepo4.spring.data.core.mapping.annotation.Path;
import org.fcrepo.client.FedoraException;
import org.fcrepo.client.FedoraRepository;
import org.fcrepo.client.impl.FedoraRepositoryImpl;
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
@ContextConfiguration(classes = {FedoraTemplateTestIT.TestConfig.class})
public class FedoraTemplateTestIT {

    @Configuration
    @PropertySource("classpath:fcrepo4.properties")
    static class TestConfig {

        @Autowired
        private Environment env;

        @Bean
        public FedoraRepository fedoraRepository() throws FedoraException {
            String repoUrl = env.getProperty("fedora.repository.url");
            FedoraRepository repository = new FedoraRepositoryImpl(repoUrl);
            org.fcrepo.client.FedoraObject fo = repository.findOrCreateObject("/test/foobar");
            return repository;
        }

        @Bean
        public FedoraTemplate fedoraTemplate() throws FedoraException {
            return new FedoraTemplate(fedoraRepository());
        }

    }

    @FedoraObject
    static class Bean1 {
        @Path
        String path = "foo-bar-101";
    }

    @FedoraObject
    static class Bean2 {
        @Path
        String path = "102|103";
    }

    @Autowired
    private FedoraTemplate fedoraTemplate;

    @Test
    public void testSave() throws Exception {
         fedoraTemplate.save(new Bean1());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSave2() throws Exception {
         fedoraTemplate.save(new Bean2());
    }

}
