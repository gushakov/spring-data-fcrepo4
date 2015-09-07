package ch.unil.fcrepo4.spring.data.core.query.sparql;

import ch.unil.fcrepo4.spring.data.core.convert.rdf.ExtendedXsdDatatypeConverter;
import ch.unil.fcrepo4.spring.data.core.convert.rdf.RdfDatatypeConverter;
import com.hp.hpl.jena.graph.NodeFactory;
import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.sparql.core.BasicPattern;
import com.hp.hpl.jena.sparql.expr.E_GreaterThan;
import com.hp.hpl.jena.sparql.expr.E_LogicalAnd;
import com.hp.hpl.jena.sparql.expr.E_Regex;
import com.hp.hpl.jena.sparql.expr.ExprVar;
import com.hp.hpl.jena.sparql.syntax.ElementFilter;
import com.hp.hpl.jena.sparql.syntax.ElementGroup;
import com.hp.hpl.jena.sparql.syntax.ElementTriplesBlock;
import com.hp.hpl.jena.sparql.syntax.ElementUnion;
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

        ElementTriplesBlock triples1 = new ElementTriplesBlock();
        triples1.addTriple(new Triple(NodeFactory.createVariable("v1"),
                NodeFactory.createURI("tst:prop1"), NodeFactory.createVariable("v2")));


        ElementGroup group1 = new ElementGroup();
        group1.addElement(triples1);


        RdfDatatypeConverter rdfTypes = new ExtendedXsdDatatypeConverter();

        E_GreaterThan expr1 = new E_GreaterThan(new ExprVar("v2"),
                rdfTypes.encodeExpressionValue(1000));

        E_Regex expr2 = new E_Regex(new ExprVar("v3"), "foo", "i");

        E_LogicalAnd andExpr = new E_LogicalAnd(expr1, expr2);

        ElementFilter filter = new ElementFilter(andExpr);
        group1.addElementFilter(filter);


        ElementTriplesBlock triples2 = new ElementTriplesBlock();
        triples2.addTriple(new Triple(NodeFactory.createVariable("v4"),
                NodeFactory.createURI("tst:prop2"), rdfTypes.encodeLiteralValue("foo")));

        ElementGroup group2 = new ElementGroup();
        group2.addElement(triples2);

        ElementUnion union = new ElementUnion();
        union.addElement(group1);
        union.addElement(group2);
        query.setQueryPattern(union);
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
