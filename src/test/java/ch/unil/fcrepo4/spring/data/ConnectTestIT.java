package ch.unil.fcrepo4.spring.data;

import ch.unil.fcrepo4.utils.Utils;
import org.fcrepo.client.FedoraObject;
import org.fcrepo.client.FedoraRepository;
import org.fcrepo.client.impl.FedoraRepositoryImpl;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.env.Environment;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.time.LocalDateTime;
import java.time.ZoneOffset;

import static ch.unil.fcrepo4.assertj.Assertions.assertThat;

// Based on org.fcrepo.client.impl.FedoraRepositoryImplIT in fcrepo4-client

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
    public void testCreateObject() throws Exception {
        // read an existing object from Fedora
        String repoUrl = env.getProperty("fedora.repository.url");
        // read-only access
        FedoraRepository repository = new FedoraRepositoryImpl(repoUrl);
        FedoraObject fo = repository.findOrCreateObject("/test");
        assertThat(fo)
                .isNotNull()
                .hasName("test")
                .createdBefore(LocalDateTime.now().toInstant(ZoneOffset.UTC))
        ;
        Utils.triplesStream(fo.getProperties()).forEach(System.out::println);
    }

    @Test
    public void testIndex() throws Exception {
        // read an existing object from Fedora
        String repoUrl = env.getProperty("fedora.repository.url");
        // read-only access
        FedoraRepository repository = new FedoraRepositoryImpl(repoUrl);
        FedoraObject fo = repository.findOrCreateObject("/test/foobar");
        fo.updateProperties("PREFIX dc: <http://purl.org/dc/elements/1.1/>\n" +
                "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\n" +
                "PREFIX indexing: <http://fedora.info/definitions/v4/indexing#>\n" +
                "  \n" +
                "DELETE { }\n" +
                "INSERT { \n" +
                "  <> indexing:hasIndexingTransformation \"default\"; \n" +
                "  rdf:type indexing:Indexable; \n" +
                "  dc:title \"Indexing title foobar\" }\n" +
                "WHERE { }");
    }

}
