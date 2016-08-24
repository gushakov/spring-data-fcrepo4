package ch.unil.fcrepo4.client;

import ch.unil.fcrepo4.spring.data.core.convert.rdf.ExtendedXsdDatatypeConverter;
import com.hp.hpl.jena.graph.Triple;
import org.apache.commons.io.IOUtils;
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

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.List;
import java.util.UUID;

import static ch.unil.fcrepo4.assertj.Assertions.assertThat;
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
        public FedoraClientRepository fedoraClientRepository() {
            return new FedoraClientRepositoryImpl(String.format("http://%s:%d%s/rest",
                    env.getProperty("fedora.host"),
                    env.getProperty("fedora.port", Integer.class),
                    env.getProperty("fedora.path")));
        }

    }

    @Autowired
    private FedoraClientRepository fedoraClientRepository;

    @Autowired
    private FedoraClientRepository clientRepository;

    @Test
    public void testGetRepositoryUrl() throws Exception {
        System.out.println(fedoraClientRepository.getRepositoryUrl());
    }

    @Test
    public void testExists() throws Exception {
        final String uuid = UUID.randomUUID().toString();
        final String path = "/" + uuid;
        clientRepository.createObject(path);
        assertThat(clientRepository.exists(path)).isTrue();
    }

    @Test
    public void testCreateObject() throws Exception {
        final String uuid = UUID.randomUUID().toString();
        final String path = "/" + uuid;
        final FedoraObject fedoraObject = clientRepository.createObject(path);
        assertThat(fedoraObject.getName()).isEqualTo(uuid);
        assertThat(fedoraObject.getPath()).isEqualTo(path);
        assertThat(fedoraObject.getProperties()).isNotEmpty();
    }

    @Test
    public void testCreateDatastream() throws Exception {
        final ExtendedXsdDatatypeConverter rdfConverter = new ExtendedXsdDatatypeConverter();
        final String uuid = UUID.randomUUID().toString();
        final String path = "/" + uuid;
        try (InputStream inputStream = new ByteArrayInputStream("foobar".getBytes())) {
            final FedoraContent fedoraContent = new FedoraContent();
            fedoraContent.setContent(inputStream);
            fedoraContent.setContentType("text/plain");
            fedoraContent.setFilename("foobar.txt");
            FedoraDatastream datastream = clientRepository.createDatastream(path, fedoraContent);
            assertThat(datastream.getProperties())
                    .containsPredicateWithObjectValue("http://www.ebu.ch/metadata/ontologies/ebucore/ebucore#hasMimeType",
                            rdfConverter.serializeLiteralValue("text/plain"));
        }
    }

    @Test
    public void testFetchDatastreamContent() throws Exception {
        final String uuid = UUID.randomUUID().toString();
        final String path = "/" + uuid;
        try (InputStream inputStream = new ByteArrayInputStream("foobar".getBytes())) {
            final FedoraContent fedoraContent = new FedoraContent();
            fedoraContent.setContent(inputStream);
            fedoraContent.setContentType("text/plain");
            fedoraContent.setFilename("foobar.txt");
            FedoraDatastream datastream = clientRepository.createDatastream(path, fedoraContent);
            InputStream content = datastream.getContent();
            assertThat(IOUtils.toString(content, "UTF-8")).isEqualTo("foobar");
        }
    }

    @Test
    public void testForceDelete() throws Exception {
        final String uuid = UUID.randomUUID().toString();
        final String path = "/" + uuid;
        clientRepository.createObject(path);
        clientRepository.forceDelete(path);
        assertThat(clientRepository.exists(path)).isFalse();
    }

    @Test
    public void testUpdateProperties() throws Exception {
        final ExtendedXsdDatatypeConverter rdfConverter = new ExtendedXsdDatatypeConverter();
        final String sparqlUpdate = "PREFIX dc: <http://purl.org/dc/elements/1.1/>\n" +
                "INSERT {\n" +
                "  <> dc:title \"foobar\" .\n" +
                "}\n" +
                "WHERE { }";
        final String uuid = UUID.randomUUID().toString();
        final String path = "/" + uuid;
        clientRepository.createObject(path);
        clientRepository.updateProperties(path, sparqlUpdate);
        assertThat(clientRepository.getObject(path).getProperties())
                .containsPredicateWithObjectValue("http://purl.org/dc/elements/1.1/title", rdfConverter.serializeLiteralValue("foobar"));
    }

    @Test
    public void testUpdateDatastreamContent() throws Exception {
        final ExtendedXsdDatatypeConverter rdfConverter = new ExtendedXsdDatatypeConverter();
        final String uuid = UUID.randomUUID().toString();
        final String path = "/" + uuid;
        FedoraDatastream datastream;
        try (InputStream inputStream = new ByteArrayInputStream("foobar".getBytes())) {
            final FedoraContent firstContent = new FedoraContent();
            firstContent.setContent(inputStream);
            firstContent.setContentType("text/plain");
            firstContent.setFilename("foobar.txt");
            datastream = clientRepository.createDatastream(path, firstContent);
        }

        try (InputStream inputStream = new ByteArrayInputStream("<wam>baz</wam>".getBytes("UTF-8"))){
            final FedoraContent secondContent = new FedoraContent();
            secondContent.setContent(inputStream);
            secondContent.setContentType("text/xml");
            secondContent.setFilename("wambaz.xml");
            clientRepository.updateDatastreamContent(path, secondContent);
        }

        assertThat(IOUtils.toString(datastream.getContent(), "UTF-8")).isEqualTo("<wam>baz</wam>");
        assertThat(datastream.getProperties())
                .containsPredicateWithObjectValue("http://www.ebu.ch/metadata/ontologies/ebucore/ebucore#hasMimeType",
                        rdfConverter.serializeLiteralValue("text/xml"));

    }

}
