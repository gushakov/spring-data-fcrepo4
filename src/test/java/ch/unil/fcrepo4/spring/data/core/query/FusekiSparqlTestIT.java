package ch.unil.fcrepo4.spring.data.core.query;

import ch.unil.fcrepo4.assertj.TripleUtils;
import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.sparql.core.BasicPattern;
import com.hp.hpl.jena.sparql.expr.E_Cast;
import com.hp.hpl.jena.sparql.expr.E_LessThan;
import com.hp.hpl.jena.sparql.syntax.ElementFilter;
import com.hp.hpl.jena.sparql.syntax.ElementGroup;
import com.hp.hpl.jena.sparql.syntax.ElementTriplesBlock;
import org.assertj.core.api.Assertions;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.env.Environment;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.ArrayList;
import java.util.List;

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

    @Test
    public void testSelectQuery() throws Exception {
        Query query = QueryFactory.make();
        query.setQuerySelectType();
        query.addResultVar("s");
        ElementGroup pattern = new ElementGroup();
        query.setQueryPattern(pattern);
        List<ElementTriplesBlock> fromBlocks = new ArrayList<>();
        ElementTriplesBlock from1 = new ElementTriplesBlock();
        from1.addTriple(TripleUtils.triple("s:s1 p:p1 o:o1"));
        from1.addTriple(TripleUtils.triple("s:s2 p:p2 o:o2"));
        fromBlocks.add(from1);


//        ElementTriplesBlock from2 = new ElementTriplesBlock();
//        from2.addTriple(TripleUtils.triple("s:s2 p:p2 o:o2"));
//        fromBlocks.add(from2);

        for (ElementTriplesBlock fromBlock : fromBlocks){
            pattern.addElement(fromBlock);
        }

        System.out.println(query);

        try (QueryExecution queryExecution = QueryExecutionFactory.sparqlService(env.getProperty("triplestore.sparql.query.url"), "ASK {}")) {
            boolean result = queryExecution.execAsk();
            assertThat(result).isTrue();
        }

    }

}
