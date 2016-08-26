package ch.unil.fcrepo4.client;

import ch.unil.fcrepo4.spring.data.core.convert.rdf.ExtendedXsdDatatypeConverter;
import org.apache.commons.io.IOUtils;
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
import java.util.Date;
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
        public FedoraClientRepository fcrepo() {
            return new FedoraClientRepositoryImpl(String.format("http://%s:%d%s/rest",
                    env.getProperty("fedora.host"),
                    env.getProperty("fedora.port", Integer.class),
                    env.getProperty("fedora.path")));
        }

    }
    
    @Autowired
    private FedoraClientRepository repository;

    @Test
    public void testGetRepositoryUrl() throws Exception {
        System.out.println(repository.getRepositoryUrl());
    }

    @Test
    public void testExists() throws Exception {
        final String path = "/" + UUID.randomUUID().toString();
        try {
            repository.createObject(path);
            assertThat(repository.exists(path)).isTrue();
        } finally {
            repository.forceDelete(path);
        }
    }

    @Test
    public void testGetCreatedDate() throws Exception {
        final String path = "/" + UUID.randomUUID().toString();
        try {
            final FedoraObject resource = repository.createObject(path);
            assertThat(resource.getCreatedDate()).isEqualToIgnoringSeconds(new Date(System.currentTimeMillis()));
        } finally {
            repository.forceDelete(path);
        }
    }

    @Test
    public void testCreateObject() throws Exception {
        final String uuid = UUID.randomUUID().toString();
        final String path = "/" + uuid;
        try {
            final FedoraObject fedoraObject = repository.createObject(path);
            assertThat(fedoraObject.getName()).isEqualTo(uuid);
            assertThat(fedoraObject.getPath()).isEqualTo(path);
            assertThat(fedoraObject.getProperties()).isNotEmpty();
        } finally {
            repository.forceDelete(path);
        }
    }

    @Test
    public void testCreateDatastream() throws Exception {
        final ExtendedXsdDatatypeConverter rdfConverter = new ExtendedXsdDatatypeConverter();
        final String path = "/" + UUID.randomUUID().toString();
        try (InputStream inputStream = new ByteArrayInputStream("foobar".getBytes())) {
            final FedoraContent fedoraContent = new FedoraContent();
            fedoraContent.setContent(inputStream);
            fedoraContent.setContentType("text/plain");
            fedoraContent.setFilename("foobar.txt");
            FedoraDatastream datastream = repository.createDatastream(path, fedoraContent);
            assertThat(datastream.getProperties())
                    .containsPredicateWithObjectValue("http://www.ebu.ch/metadata/ontologies/ebucore/ebucore#hasMimeType",
                            rdfConverter.serializeLiteralValue("text/plain"));
        } finally {
            repository.forceDelete(path);
        }
    }

    @Test
    public void testFetchDatastreamContent() throws Exception {
        final String path = "/" + UUID.randomUUID().toString();
        try (InputStream inputStream = new ByteArrayInputStream("foobar".getBytes())) {
            final FedoraContent fedoraContent = new FedoraContent();
            fedoraContent.setContent(inputStream);
            fedoraContent.setContentType("text/plain");
            fedoraContent.setFilename("foobar.txt");
            FedoraDatastream datastream = repository.createDatastream(path, fedoraContent);
            InputStream content = datastream.getContent();
            assertThat(IOUtils.toString(content, "UTF-8")).isEqualTo("foobar");
        } finally {
            repository.forceDelete(path);
        }
    }

    @Test
    public void testForceDelete() throws Exception {
        final String path = "/" + UUID.randomUUID().toString();
        repository.createObject(path);
        repository.forceDelete(path);
    }

    @Test
    public void testUpdateProperties() throws Exception {
        final ExtendedXsdDatatypeConverter rdfConverter = new ExtendedXsdDatatypeConverter();
        final String sparqlUpdate = "PREFIX dc: <http://purl.org/dc/elements/1.1/>\n" +
                "INSERT {\n" +
                "  <> dc:title \"foobar\" .\n" +
                "}\n" +
                "WHERE { }";
        final String path = "/" + UUID.randomUUID().toString();
        try {
            repository.createObject(path);
            repository.updateProperties(path, sparqlUpdate);
            assertThat(repository.getObject(path).getProperties())
                    .containsPredicateWithObjectValue("http://purl.org/dc/elements/1.1/title", rdfConverter.serializeLiteralValue("foobar"));
        } finally {
            repository.forceDelete(path);
        }
    }

    @Test
    public void testUpdateDatastreamProperties() throws Exception {
        final ExtendedXsdDatatypeConverter rdfConverter = new ExtendedXsdDatatypeConverter();
        final String path = "/" + UUID.randomUUID().toString();
        final String sparqlUpdate = "PREFIX dc: <http://purl.org/dc/elements/1.1/>\n" +
                "INSERT {\n" +
                "  <> dc:title \"foobar\" .\n" +
                "}\n" +
                "WHERE { }";
        try (InputStream inputStream = new ByteArrayInputStream("foobar".getBytes())) {
            final FedoraContent fedoraContent = new FedoraContent();
            fedoraContent.setContent(inputStream);
            fedoraContent.setContentType("text/plain");
            fedoraContent.setFilename("foobar.txt");
            FedoraDatastream datastream = repository.createDatastream(path, fedoraContent);
            datastream.updateProperties(sparqlUpdate);
            assertThat(repository.getDatastream(path).getProperties())
                    .containsPredicateWithObjectValue("http://purl.org/dc/elements/1.1/title", rdfConverter.serializeLiteralValue("foobar"));
        }
        finally {
            repository.forceDelete(path);
        }
    }

    @Test
    public void testUpdateDatastreamContent() throws Exception {
        final ExtendedXsdDatatypeConverter rdfConverter = new ExtendedXsdDatatypeConverter();
        final String path = "/" + UUID.randomUUID().toString();
        try {
            FedoraDatastream datastream;
            try (InputStream inputStream = new ByteArrayInputStream("foobar".getBytes())) {
                final FedoraContent firstContent = new FedoraContent();
                firstContent.setContent(inputStream);
                firstContent.setContentType("text/plain");
                firstContent.setFilename("foobar.txt");
                datastream = repository.createDatastream(path, firstContent);
            }

            try (InputStream inputStream = new ByteArrayInputStream("<wam>baz</wam>".getBytes("UTF-8"))){
                final FedoraContent secondContent = new FedoraContent();
                secondContent.setContent(inputStream);
                secondContent.setContentType("text/xml");
                secondContent.setFilename("wambaz.xml");
                repository.updateDatastreamContent(path, secondContent);
            }

            assertThat(IOUtils.toString(datastream.getContent(), "UTF-8")).isEqualTo("<wam>baz</wam>");
            assertThat(datastream.getProperties())
                    .containsPredicateWithObjectValue("http://www.ebu.ch/metadata/ontologies/ebucore/ebucore#hasMimeType",
                            rdfConverter.serializeLiteralValue("text/xml"));
        } finally {
           repository.forceDelete(path);
        }

    }

}
