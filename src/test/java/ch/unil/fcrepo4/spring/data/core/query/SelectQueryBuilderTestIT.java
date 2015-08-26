package ch.unil.fcrepo4.spring.data.core.query;

import ch.unil.fcrepo4.utils.Utils;
import com.hp.hpl.jena.datatypes.xsd.XSDDateTime;
import com.hp.hpl.jena.graph.Graph;
import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFDataMgr;
import org.fcrepo.client.FedoraException;
import org.fcrepo.client.FedoraObject;
import org.fcrepo.client.FedoraRepository;
import org.fcrepo.client.impl.FedoraObjectImpl;
import org.fcrepo.client.impl.FedoraRepositoryImpl;
import org.fcrepo.kernel.RdfLexicon;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.env.Environment;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.time.ZonedDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.fcrepo.kernel.RdfLexicon.CREATED_DATE;
import static org.fcrepo.kernel.RdfLexicon.REPOSITORY_NAMESPACE;

/**
 * @author gushakov
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {SelectQueryBuilderTestIT.TestConfig.class})
public class SelectQueryBuilderTestIT {

    @Configuration
    @PropertySource("classpath:fcrepo4.properties")
    public static class TestConfig {
        @Autowired
        private Environment env;

        @Bean
        public FedoraRepository fedoraRepository() throws FedoraException {
            String repoUrl = env.getProperty("fedora.repository.url");
            return new FedoraRepositoryImpl(repoUrl);
        }
    }

    @Value("#{environment.getProperty('fedora.repository.url')}")
    private String repoUrl;

    @Autowired
    private FedoraRepository fedoraRepository;

    @Test
    public void testQueryByCreatedDateWithZonedDateTime() throws Exception {
        System.out.println(repoUrl);
        String collectionPath = "/foo/" + System.currentTimeMillis();
        FedoraObject fedoraObject = fedoraRepository.findOrCreateObject(collectionPath + "/1");

        XSDDateTime xsdDateTime = (XSDDateTime) Utils.getLiteralValue(fedoraObject.getProperties(), RdfLexicon.CREATED_DATE.getURI());
        System.out.println(xsdDateTime.toString());
        ZonedDateTime zonedDateTime = ZonedDateTime.parse(xsdDateTime.toString());

        String sUri = repoUrl + fedoraObject.getPath();

        Query query = new SelectQueryBuilder(new PrefixMap().addPrefix("f", REPOSITORY_NAMESPACE))
                .select("s")
                .from("s", "f:" + CREATED_DATE.getLocalName(), zonedDateTime)
                .build();
        System.out.println(query);

        FedoraObjectImpl fo = (FedoraObjectImpl) fedoraRepository.getObject(collectionPath);
        Graph graph = fo.getGraph();
        Model model = ModelFactory.createModelForGraph(graph);
        RDFDataMgr.write(System.out, model, Lang.TURTLE);

        try(QueryExecution queryExecution = QueryExecutionFactory.create(query, model)){
            ResultSet results = queryExecution.execSelect();
            assertThat(results.next().getResource("s").getURI()).isEqualTo(sUri);
        }

    }
}
