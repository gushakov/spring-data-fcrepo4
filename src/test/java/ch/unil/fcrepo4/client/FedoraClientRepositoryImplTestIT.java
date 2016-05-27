package ch.unil.fcrepo4.client;

import ch.unil.fcrepo4.utils.Utils;
import org.apache.commons.lang3.StringUtils;
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
@ContextConfiguration(classes = {FedoraClientRepositoryImplTestIT.TestConfig.class})
public class FedoraClientRepositoryImplTestIT {

    @Configuration
    @PropertySource("classpath:fcrepo4.properties")
    public static class TestConfig {

        @Autowired
        private Environment env;

        @Bean
        public String fedoraUrl() {
            return String.format("http://localhost:%d/fcrepo/rest", env.getProperty("fedora.port", Integer.class));
        }

        @Bean
        public FedoraClientRepository fedoraClientRepository() {
            return new FedoraClientRepositoryImpl(fedoraUrl());
        }

    }

    @Autowired
    private String fedoraUrl;

    @Autowired
    private FedoraClientRepository clientRepository;

    @Test
    public void testGetRepositoryUrl() throws Exception {
        assertThat(clientRepository.getRepositoryUrl()).isEqualTo(fedoraUrl);
    }

    @Test
    public void testExists() throws Exception {
        final String path = "/test_" + System.currentTimeMillis();
        clientRepository.createObject(path);
        assertThat(clientRepository.exists(path)).isTrue();
    }

    @Test
    public void testCreateObject() throws Exception {
        final String path = "/test_" + System.currentTimeMillis();
        final FedoraResource fedoraResource = clientRepository.createObject(path);
        assertThat(fedoraResource.getName()).isEqualTo(StringUtils.stripStart(path, "/"));
        assertThat(fedoraResource.getPath()).isEqualTo(path);
        assertThat(fedoraResource.getProperties()).isNotEmpty();
        assertThat(fedoraResource.getProperties().next().getSubject().getURI())
                .isEqualTo(Utils.concatenate(clientRepository.getRepositoryUrl(), path, false));
    }

/*
    @Test
    public void testCreateDatastream() throws Exception {
        final String path = "/test_" + System.currentTimeMillis();
        try (InputStream inputStream = new ByteArrayInputStream("foobar".getBytes())) {
            clientRepository.createDatastream(path, inputStream, "text/plain");
        }
    }
*/

}
