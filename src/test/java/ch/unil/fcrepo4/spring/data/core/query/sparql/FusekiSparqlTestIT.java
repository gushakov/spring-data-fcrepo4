package ch.unil.fcrepo4.spring.data.core.query.sparql;

import ch.unil.fcrepo4.spring.data.core.convert.rdf.ExtendedXsdDatatypeConverter;
import ch.unil.fcrepo4.spring.data.core.convert.rdf.RdfDatatypeConverter;
import com.hp.hpl.jena.graph.NodeFactory;
import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.sparql.expr.E_GreaterThan;
import com.hp.hpl.jena.sparql.expr.ExprVar;
import com.hp.hpl.jena.sparql.syntax.ElementFilter;
import com.hp.hpl.jena.sparql.syntax.ElementGroup;
import com.hp.hpl.jena.sparql.syntax.ElementTriplesBlock;
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
    public void testSelectQuery() throws Exception {

        Query query = QueryFactory.make();
        query.setQuerySelectType();
        query.addResultVar("v1");

        ElementTriplesBlock triples = new ElementTriplesBlock();
        triples.addTriple(new Triple(NodeFactory.createVariable("v1"),
                NodeFactory.createURI("tst:prop1"), NodeFactory.createVariable("v2")));


        ElementGroup group = new ElementGroup();
        group.addElement(triples);

        RdfDatatypeConverter rdfDatatypeConverter = new ExtendedXsdDatatypeConverter();

        ElementFilter filter = new ElementFilter(new E_GreaterThan(new ExprVar("v2"),
                rdfDatatypeConverter.encodeExpressionValue(1000)));

        group.addElementFilter(filter);

        query.setQueryPattern(group);
        System.out.println(query);


    }

    @Test
    public void testAskQuery() throws Exception {

        try (QueryExecution queryExecution = QueryExecutionFactory.sparqlService(env.getProperty("triplestore.sparql.query.url"), "ASK {}")) {
            boolean result = queryExecution.execAsk();
            assertThat(result).isTrue();
        }

    }

}
