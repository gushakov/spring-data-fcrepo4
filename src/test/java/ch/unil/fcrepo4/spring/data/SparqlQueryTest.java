package ch.unil.fcrepo4.spring.data;

import static ch.unil.fcrepo4.spring.data.core.query.SelectQueryBuilder.*;

import ch.unil.fcrepo4.spring.data.core.query.PrefixMap;
import ch.unil.fcrepo4.spring.data.core.query.SelectQueryBuilder;
import com.hp.hpl.jena.graph.Graph;
import com.hp.hpl.jena.graph.NodeFactory;
import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.query.*;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.sparql.core.Var;
import com.hp.hpl.jena.sparql.expr.E_LessThan;
import com.hp.hpl.jena.sparql.expr.Expr;
import com.hp.hpl.jena.sparql.expr.ExprVar;
import com.hp.hpl.jena.sparql.expr.nodevalue.NodeValueInteger;
import com.hp.hpl.jena.sparql.syntax.ElementGroup;
import org.fcrepo.client.FedoraRepository;
import org.fcrepo.client.impl.FedoraObjectImpl;
import org.fcrepo.client.impl.FedoraRepositoryImpl;
import org.junit.Test;

/**
 * @author gushakov
 */
public class SparqlQueryTest {

    @Test
    public void testQuery() throws Exception {

        // from https://jena.apache.org/documentation/query/app_api.html

        FedoraRepository repository = new FedoraRepositoryImpl("http://localhost:9090/rest");

        FedoraObjectImpl fo = (FedoraObjectImpl) repository.findOrCreateObject("/test");
        Graph graph = fo.getGraph();

        Model model = ModelFactory.createModelForGraph(graph);

        Query query = QueryFactory.create("SELECT ?x\n" +
                "WHERE { ?x  <http://fedora.info/definitions/v4/repository#uuid>  \"969de1b6-b91d-44e2-80a8-eb03edf49ac0\" }");

        try(QueryExecution queryExecution = QueryExecutionFactory.create(query, model)){
            ResultSet results = queryExecution.execSelect();
            System.out.println(results.hasNext());
        }

    }

    @Test
    public void testQueryBuilder() throws Exception {
        Triple triple = Triple.create(Var.alloc("s"), NodeFactory.createURI("info:fedora/test/foo"), NodeFactory.createLiteral("bar"));
        Query query = QueryFactory.make();
        query.setQuerySelectType();
        query.addResultVar("s");
        ElementGroup elementGroup = new ElementGroup();

        elementGroup.addTriplePattern(triple);
        query.setQueryPattern(elementGroup);

        System.out.println(query);

        FedoraRepository repository = new FedoraRepositoryImpl("http://localhost:9090/rest");

        FedoraObjectImpl fo = (FedoraObjectImpl) repository.findOrCreateObject("/test");
        Graph graph = fo.getGraph();

        Model model = ModelFactory.createModelForGraph(graph);

        try(QueryExecution queryExecution = QueryExecutionFactory.create(query, model)){
            ResultSet results = queryExecution.execSelect();
            System.out.println(results.hasNext());
            QuerySolution querySolution = results.nextSolution();
            RDFNode s = querySolution.get("s");
            System.out.println(s);
        }
    }

    @Test
    public void testSparqlQuery() throws Exception {
/*
        Triple triple1 = Triple.create(Var.alloc("p"), NodeFactory.createURI("http://foobar#name"), NodeFactory.createLiteral("George", new XSDBaseStringType("string")));
        Expr expr = new E_LessThan(new ExprVar("o"), new NodeValueInteger(10));
        Query query = new SelectQueryBuilder()
                .select("p")
                .from(triple1)
//                .where(expr)
                .build();
        System.out.println(query);

        // test with local Fuseki server

        try(QueryExecution queryExecution = QueryExecutionFactory.sparqlService("http://localhost:3030/ds/sparql", query.toString())){
            ResultSet results = queryExecution.execSelect();
            while (results.hasNext()){
                QuerySolution querySolution = results.nextSolution();
                RDFNode s = querySolution.get("p");
                System.out.println(s);
            }
        }
*/

    }

    @Test
    public void testSparqlQueryCount() throws Exception {
        Triple triple1 = Triple.create(Var.alloc("s"), NodeFactory.createURI("http://foobar#id"), Var.alloc("v"));
        Triple triple2 = Triple.create(Var.alloc("t"), NodeFactory.createURI("http://foobar#id"), Var.alloc("u"));
        Expr expr1 = new E_LessThan(new ExprVar("v"), new NodeValueInteger(5));
        Query query = new SelectQueryBuilder(new PrefixMap().addPrefix("t", "http://foobar#"))
                .select("s")
                .from("s", "t:foo", 5)
                .from("u", "t:bar", "wam")
                .from("u", "s:toto", "waz")
                .where(expr1)
                .build();
        System.out.println(query);

/*
        Triple triple1 = Triple.create(Var.alloc("s"), NodeFactory.createURI("http://foobar#id"), Var.alloc("v"));
        Query query = new SelectQueryBuilder()
                .count(true)
                .from(triple1)
                .where(new E_LessThan(new ExprVar("v"), new NodeValueInteger(5)))

                .build();

        System.out.println(query);

        // test with local Fuseki server

        try(QueryExecution queryExecution = QueryExecutionFactory.sparqlService("http://localhost:3030/ds/sparql", query.toString())){
            ResultSet results = queryExecution.execSelect();
            while (results.hasNext()){
                QuerySolution querySolution = results.nextSolution();
                RDFNode s = querySolution.get("count");
                System.out.println(s);
            }
        }
*/

    }

}
