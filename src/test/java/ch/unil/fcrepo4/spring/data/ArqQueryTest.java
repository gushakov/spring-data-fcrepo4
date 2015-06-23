package ch.unil.fcrepo4.spring.data;

import com.hp.hpl.jena.graph.Graph;
import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.NodeFactory;
import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.query.*;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.sparql.core.Var;
import com.hp.hpl.jena.sparql.syntax.ElementGroup;
import org.fcrepo.client.FedoraRepository;
import org.fcrepo.client.impl.FedoraObjectImpl;
import org.fcrepo.client.impl.FedoraRepositoryImpl;
import org.junit.Test;

/**
 * @author gushakov
 */
public class ArqQueryTest {

    @Test
    public void testQuery() throws Exception {

        // from https://jena.apache.org/documentation/query/app_api.html

        FedoraRepository repository = new FedoraRepositoryImpl("http://localhost:9090/rest");

        FedoraObjectImpl fo = (FedoraObjectImpl) repository.findOrCreateObject("/test");
        Graph graph = fo.getGraph();

        Model model = ModelFactory.createModelForGraph(graph);

        Query query = QueryFactory.create("SELECT ?x\n" +
                "WHERE { ?x  <http://fedora.info/definitions/v4/repository#uuid>  \"4e2ed909-3c35-4b49-9c09-f07e7d076af4\" }");

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

}
