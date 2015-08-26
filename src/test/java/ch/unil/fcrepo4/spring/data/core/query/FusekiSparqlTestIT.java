package ch.unil.fcrepo4.spring.data.core.query;

import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import org.assertj.core.api.Assertions;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.env.Environment;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import static org.assertj.core.api.Assertions.assertThat;

// code from https://github.com/apache/jena/blob/master/jena-fuseki2/jena-fuseki-core/src/test/java/org/apache/jena/fuseki/TestQuery.java


/**
 * @author gushakov
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {FusekiSparqlTestIT.TestConfig.class})
public class FusekiSparqlTestIT {
    @Configuration
    @PropertySource("classpath:fcrepo4.properties")
    public static class TestConfig {

    }

    @Autowired
    private Environment env;

    @Test
    public void testQuery() throws Exception {

        try (QueryExecution queryExecution = QueryExecutionFactory.sparqlService(env.getProperty("triplestore.sparql.query.url"), "ASK {}")) {
            boolean result = queryExecution.execAsk();
            assertThat(result).isTrue();
        }

    }
}
