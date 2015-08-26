package ch.unil.fcrepo4.spring.data.core.query;

import com.hp.hpl.jena.datatypes.xsd.XSDDatatype;
import com.hp.hpl.jena.graph.Graph;
import com.hp.hpl.jena.graph.NodeFactory;
import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.mem.GraphMem;
import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.fcrepo.kernel.RdfLexicon.HAS_PRIMARY_IDENTIFIER;
import static org.fcrepo.kernel.RdfLexicon.REPOSITORY_NAMESPACE;

/**
 * @author gushakov
 */
public class SelectQueryBuilderTest {

    private static final String REPO_URL = "http://localhost:9090/rest";

    @Test
    public void testSelectByUuid() throws Exception {
        String uuid = "b2c934fc-e358-4ddd-af9b-f30900422a6a";
        String sUri = REPO_URL + "/foo/bar/1440491233444";
        Graph graph = new GraphMem();
        graph.add(new Triple(
                NodeFactory.createURI(sUri),
                NodeFactory.createURI(HAS_PRIMARY_IDENTIFIER.getURI()),
                NodeFactory.createLiteral(uuid, XSDDatatype.XSDstring)
        ));
        System.out.println(graph);

        Query query = new SelectQueryBuilder(new PrefixMap().addPrefix("f", REPOSITORY_NAMESPACE))
                .select("s")
                .from("s", "f:" + HAS_PRIMARY_IDENTIFIER.getLocalName(), uuid)
                .build();
        System.out.println(query);

        Model model = ModelFactory.createModelForGraph(graph);

        QueryExecution queryExecution = QueryExecutionFactory.create(query, model);
        ResultSet results = queryExecution.execSelect();
        assertThat(results.hasNext());
        assertThat(results.next().getResource("s").getURI()).isEqualTo(sUri);
    }

}
