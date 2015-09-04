package ch.unil.fcrepo4.spring.data.core.query.sparql;

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
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFDataMgr;
import org.junit.Test;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

import static ch.unil.fcrepo4.assertj.Assertions.assertThat;
import static ch.unil.fcrepo4.spring.data.core.Constants.TEST_FEDORA_URI_NAMESPACE;
import static org.assertj.core.api.Assertions.assertThat;
import static org.fcrepo.kernel.RdfLexicon.CREATED_BY;
import static org.fcrepo.kernel.RdfLexicon.CREATED_DATE;
import static org.fcrepo.kernel.RdfLexicon.HAS_PRIMARY_IDENTIFIER;
import static org.fcrepo.kernel.RdfLexicon.REPOSITORY_NAMESPACE;

/**
 * @author gushakov
 */
public class SparqlSelectQueryBuilderTest {

    private static final String REPO_URL = "http://localhost:9090/rest";

    @Test
    public void testEmptySelect() throws Exception {
        Query query = new SparqlSelectQueryBuilder().build();
        assertThat(compress(query)).isEqualTo("SELECT ?_ WHERE { }");
    }

    @Test
    public void testSelectWithVar() throws Exception {
        Query query = new SparqlSelectQueryBuilder().select("s").build();
        assertThat(compress(query)).isEqualTo("SELECT ?s WHERE { }");
    }

    @Test
    public void testSelectWithFrom() throws Exception {
        Query query = new SparqlSelectQueryBuilder()
                .select("s")
                .from("s", "rdf:type", "Class")
                .build();
//        assertThat(compress(query)).isEqualTo("SELECT ?s WHERE { }");
    }

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
        Model model = ModelFactory.createModelForGraph(graph);
        RDFDataMgr.write(System.out, model, Lang.TURTLE);

        Query query = new SparqlSelectQueryBuilder(new PrefixMap().addPrefix("fdr", REPOSITORY_NAMESPACE))
                .select("s")
                .from("s", "fdr:" + HAS_PRIMARY_IDENTIFIER.getLocalName(), uuid)
                .build();
        System.out.println(query);

        try(QueryExecution queryExecution = QueryExecutionFactory.create(query, model)){
            ResultSet results = queryExecution.execSelect();
            assertThat(results.hasNext()).isTrue();
            assertThat(results.next().getResource("s").getURI()).isEqualTo(sUri);
        }
    }

    @Test
    public void testCount() throws Exception {
        Graph graph = new GraphMem();
        graph.add(new Triple(
                NodeFactory.createURI(REPO_URL + "/foo/bar/1"),
                NodeFactory.createURI(TEST_FEDORA_URI_NAMESPACE + "number"),
                NodeFactory.createLiteral("1", XSDDatatype.XSDinteger)
        ));
        graph.add(new Triple(
                NodeFactory.createURI(REPO_URL + "/foo/bar/2"),
                NodeFactory.createURI(TEST_FEDORA_URI_NAMESPACE + "number"),
                NodeFactory.createLiteral("2", XSDDatatype.XSDinteger)
        ));

        Model model = ModelFactory.createModelForGraph(graph);
        RDFDataMgr.write(System.out, model, Lang.TURTLE);


        Query query = new SparqlSelectQueryBuilder(new PrefixMap().addPrefix("tst", TEST_FEDORA_URI_NAMESPACE))
                .count(true)
                .from("s", "tst:number", "?v")
                .build();
        System.out.println(query);

        try(QueryExecution queryExecution = QueryExecutionFactory.create(query, model)) {
            ResultSet results = queryExecution.execSelect();
            assertThat(results.hasNext()).isTrue();
            assertThat(results.next().get("count").asNode())
                    .isLiteral()
                    .hasLiteralValue(2)
            ;
        }
    }

    @Test
    public void testQueryByCreatedDate() throws Exception {
        String created = "2015-08-25T08:27:13.660Z";
        ZonedDateTime dateTime = ZonedDateTime.parse(created, DateTimeFormatter.ISO_OFFSET_DATE_TIME);
        String sUri = REPO_URL + "/foo/bar/1440491233444";
        Graph graph = new GraphMem();
        graph.add(new Triple(
                NodeFactory.createURI(sUri),
                NodeFactory.createURI(CREATED_DATE.getURI()),
                NodeFactory.createLiteral(created, XSDDatatype.XSDdateTime)
        ));
        Model model = ModelFactory.createModelForGraph(graph);
        RDFDataMgr.write(System.out, model, Lang.TURTLE);

        Query query = new SparqlSelectQueryBuilder(new PrefixMap().addPrefix("fdr", REPOSITORY_NAMESPACE))
                .select("s")
                .from("s", "fdr:" + CREATED_DATE.getLocalName(), dateTime)
                .build();
        System.out.println(query);

        try(QueryExecution queryExecution = QueryExecutionFactory.create(query, model)) {
            ResultSet results = queryExecution.execSelect();
            assertThat(results.hasNext()).isTrue();
            assertThat(results.next().getResource("s").getURI()).isEqualTo(sUri);
        }

    }

    @Test
    public void testSelectAnd() throws Exception {
        String created = "2015-08-25T08:27:13.660Z";
        String createdBy = "bypassAdmin";
        ZonedDateTime dateTime = ZonedDateTime.parse(created, DateTimeFormatter.ISO_OFFSET_DATE_TIME);
        String sUri = REPO_URL + "/foo/bar/1440491233444";
        Graph graph = new GraphMem();
        graph.add(new Triple(
                NodeFactory.createURI(sUri),
                NodeFactory.createURI(CREATED_DATE.getURI()),
                NodeFactory.createLiteral(created, XSDDatatype.XSDdateTime)
        ));
        graph.add(new Triple(
                NodeFactory.createURI(sUri),
                NodeFactory.createURI(CREATED_BY.getURI()),
                NodeFactory.createLiteral("bypassAdmin", XSDDatatype.XSDstring)
        ));
        Model model = ModelFactory.createModelForGraph(graph);
        RDFDataMgr.write(System.out, model, Lang.TURTLE);

        Query query = new SparqlSelectQueryBuilder(new PrefixMap().addPrefix("fdr", REPOSITORY_NAMESPACE))
                .select("s")
                .from("s", "fdr:" + CREATED_DATE.getLocalName(), dateTime)
                .and("s", "fdr:" + CREATED_BY.getLocalName(), createdBy)
//                .where(null)
                .build();
        System.out.println(query);

        try(QueryExecution queryExecution = QueryExecutionFactory.create(query, model)) {
            ResultSet results = queryExecution.execSelect();
            assertThat(results.hasNext()).isTrue();
            assertThat(results.next().getResource("s").getURI()).isEqualTo(sUri);
        }

    }

    private String compress(Query query) {
        return query.toString().replaceAll("\\n", " ").replaceAll("\\s+", " ").trim();
    }

}
