package ch.unil.fcrepo4.spring.data.core.query.sparql;

import ch.unil.fcrepo4.utils.Utils;
import com.hp.hpl.jena.query.*;
import com.hp.hpl.jena.rdf.model.Resource;
import org.apache.http.client.utils.URIBuilder;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author gushakov
 */

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {SparqlQueryTestIT.TestConfig.class})

public class SparqlQueryTestIT {

    @Configuration
    @PropertySource("classpath:fcrepo4.properties")
    public static class TestConfig {
    }

    @Value("#{environment.getProperty('fedora.host')}")
    private String fedoraHost;

    @Value("#{environment.getProperty('fedora.port')}")
    private int fedoraPort;

    @Value("#{environment.getProperty('fedora.path')}")
    private String fedoraPath;

    @Value("#{environment.getProperty('triplestore.host')}")
    private String triplestoreHost;

    @Value("#{environment.getProperty('triplestore.port')}")
    private int triplestorePort;

    @Value("#{environment.getProperty('triplestore.path')}")
    private String triplestorePath;

    @Value("#{environment.getProperty('triplestore.db')}")
    private String triplestoreDb;

    @Test
    public void testAskQuery() throws Exception {

        try (QueryExecution queryExecution = QueryExecutionFactory.sparqlService(getQueryServiceUrl(), "ASK {}")) {
            boolean result = queryExecution.execAsk();
            assertThat(result).isTrue();
        }
    }

//    @Test
    public void testSelectQuery() throws Exception {

        String sparqlQuery = "SELECT  ?ch_unil_fcrepo4_spring_data_repository_Vehicle\n" +
                "WHERE\n" +
                "  { ?ch_unil_fcrepo4_spring_data_repository_Vehicle <info:fedora/test/make> \"Ford\"^^<http://www.w3.org/2001/XMLSchema#string>}";
        Query query = QueryFactory.create(sparqlQuery);

        try (QueryExecution queryExecution = QueryExecutionFactory.sparqlService(getQueryServiceUrl(), query)) {
            ResultSet results = queryExecution.execSelect();
//            assertThat(results.hasNext());
            assertThat(results).isNotNull();
            while (results.hasNext()) {
                List<String> resultVars = query.getResultVars();
                Resource queryResultResource = getFirstAvailableResource(results.next(), resultVars);
                if (queryResultResource == null) {
                    throw new IllegalStateException("Query solution has no resource for variables " + Arrays.toString(resultVars.toArray(new String[resultVars.size()])));
                }
                System.out.println(queryResultResource.getURI());
            }
        }
    }

    private Resource getFirstAvailableResource(QuerySolution querySolution, List<String> varNames) {
        Resource resource = null;
        for (String varName : varNames) {
            resource = querySolution.getResource(varName);
            if (resource != null) {
                break;
            }
        }
        return resource;
    }

    private String getFedoraUrl(){
        String fedoraUrl = new URIBuilder().setScheme("http")
                .setHost(fedoraHost)
                .setPort(fedoraPort)
                .setPath(fedoraPath+"/rest").toString();

        System.out.println(fedoraUrl);
        return fedoraUrl;
    }

    private String getQueryServiceUrl(){
        String queryUrl = new URIBuilder().setScheme("http")
                .setHost(triplestoreHost)
                .setPort(triplestorePort)
                .setPath(triplestorePath+"/"+triplestoreDb+"/query").toString();

        System.out.println(queryUrl);
        return queryUrl;
    }

    private String getDataServiceUrl(){
        String dataUrl = new URIBuilder().setScheme("http")
                .setHost(triplestoreHost)
                .setPort(triplestorePort)
                .setPath(triplestorePath+"/"+triplestoreDb+"/data").toString();

        System.out.println(dataUrl);
        return dataUrl;
    }

}
