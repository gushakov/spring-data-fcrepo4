package ch.unil.fcrepo4.utils;

import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

/**
 * @author gushakov
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {GraphExporterTest.TestConfig.class})
@Ignore
public class GraphExporterTest {

    @Configuration
    @PropertySource("classpath:fcrepo4.properties")
    public static class TestConfig {

    }

    @Value("#{environment.getProperty('fedora.repository.url')}")
    private String repoUrl;

    @Value("#{environment.getProperty('triplestore.sparql.query.url')}")
    private String sparqlQueryUrl;

    @Value("#{environment.getProperty('triplestore.sparql.data.url')}")
    private String sparqlDataUrl;

    @Value("#{environment.getProperty('maven.profile.id')}")
    private String profileId;

    @Test
    public void testExportToFile() throws Exception {
        if (profileId != null && profileId.equals("localhost")) {
            GraphExporter.getInstance().exportToFile("/vehicle", repoUrl, "c:\\tmp");
        }
    }

    @Test
    public void testExportToFuseki() throws Exception {
        if (profileId != null && profileId.equals("localhost")) {
            GraphExporter.getInstance().exportToFuseki("/vehicle", repoUrl, sparqlDataUrl);
        }
    }
}
