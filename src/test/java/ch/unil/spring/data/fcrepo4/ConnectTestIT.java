package ch.unil.spring.data.fcrepo4;

import org.fcrepo.client.FedoraObject;
import org.fcrepo.client.FedoraRepository;
import org.fcrepo.client.impl.ReadOnlyFedoraRepositoryImpl;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.env.Environment;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import static ch.unil.spring.data.fcrepo4.assertj.Assertions.assertThat;


/**
 * @author gushakov
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration
public class ConnectTestIT {

    @Configuration
    @PropertySource("classpath:fcrepo4.properties")
    static class TestConfig {

    }

    @Autowired
    private Environment env;

    @Test
    public void testReadObject() throws Exception {
        // read an existing object from Fedora
        String repoUrl = env.getProperty("fedora.repository.url");
        // read-only access
        FedoraRepository repository = new ReadOnlyFedoraRepositoryImpl(repoUrl);
        FedoraObject fo = repository.findOrCreateObject("/test");
        assertThat(fo)
                .isNotNull()
                .hasName("test")
                .hasPath("/test")
        ;
    }

}
