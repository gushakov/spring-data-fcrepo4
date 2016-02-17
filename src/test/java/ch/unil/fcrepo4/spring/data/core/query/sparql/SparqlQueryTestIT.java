package ch.unil.fcrepo4.spring.data.core.query.sparql;

import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.sparql.algebra.Op;
import com.hp.hpl.jena.sparql.algebra.OpAsQuery;
import com.hp.hpl.jena.sparql.algebra.op.OpBGP;
import com.hp.hpl.jena.sparql.algebra.op.OpFilter;
import com.hp.hpl.jena.sparql.algebra.op.OpProject;
import com.hp.hpl.jena.sparql.core.BasicPattern;
import com.hp.hpl.jena.sparql.core.Var;
import com.hp.hpl.jena.sparql.expr.E_LessThan;
import com.hp.hpl.jena.sparql.expr.Expr;
import com.hp.hpl.jena.sparql.expr.ExprVar;
import com.hp.hpl.jena.sparql.expr.nodevalue.NodeValueInteger;
import org.apache.http.client.utils.URIBuilder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.env.Environment;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.Arrays;

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


    @Autowired
    private Environment env;

    @Test
    public void testAskQuery() throws Exception {

        String queryUrl = new URIBuilder().setScheme("http")
                .setHost(env.getProperty("fedora.host"))
                .setPort(env.getProperty("triplestore.port", Integer.class))
                .setPath("/test/query").toString();

        System.out.println(queryUrl);

        try (QueryExecution queryExecution = QueryExecutionFactory.sparqlService(queryUrl, "ASK {}")) {
            boolean result = queryExecution.execAsk();
            assertThat(result).isTrue();
        }
    }

    @Test
    public void testAlgebra() throws Exception {
        // from https://jena.apache.org/documentation/query/manipulating_sparql_using_arq.html

        // ?s ?p ?o .
        Triple pattern =
                Triple.create(Var.alloc("s"), Var.alloc("p"), Var.alloc("o"));
        // ( ?s < 20 )
        Expr e = new E_LessThan(new ExprVar("s"), new NodeValueInteger(20));

        Op op;
        BasicPattern pat = new BasicPattern();                 // Make a pattern
        pat.add(pattern);                                      // Add our pattern match
        op = new OpBGP(pat);                                   // Make a BGP from this pattern
        System.out.println(op);
        op = OpFilter.filter(e, op);                           // Filter that pattern with our expression
        System.out.println(op);
        op = new OpProject(op, Arrays.asList(Var.alloc("s"))); // Reduce to just ?s
        System.out.println(op);
        Query q = OpAsQuery.asQuery(op);                       // Convert to a query
        q.setQuerySelectType();

        System.out.println(q);
    }

}
