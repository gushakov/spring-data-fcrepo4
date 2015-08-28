package ch.unil.fcrepo4.utils;

import com.hp.hpl.jena.graph.Graph;
import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFDataMgr;
import org.fcrepo.client.FedoraException;
import org.fcrepo.client.FedoraObject;
import org.fcrepo.client.FedoraRepository;
import org.fcrepo.client.FedoraResource;
import org.fcrepo.client.impl.FedoraObjectImpl;
import org.fcrepo.client.impl.FedoraRepositoryImpl;
import org.fcrepo.client.impl.ReadOnlyFedoraRepositoryImpl;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.io.FileOutputStream;
import java.util.Collection;

/**
 * @author gushakov
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {GraphExporterTest.TestConfig.class})
public class GraphExporterTest {

    @Configuration
    @PropertySource("classpath:fcrepo4.properties")
    public static class TestConfig {

    }

    @Value("#{environment.getProperty('fedora.repository.url')}")
    private String repoUrl;

    @Value("#{environment.getProperty('triplestore.sparql.query.url')}")
    private String sparqlUrl;



    @Test
    @Ignore
    public void testExportAsTurtle() throws Exception {
        FedoraRepository fedoraRepository = new FedoraRepositoryImpl(repoUrl);
        FedoraObjectImpl fedoraObject = (FedoraObjectImpl) fedoraRepository.getObject("/foo");
        Graph graph = fedoraObject.getGraph();
        addTriples(graph, fedoraObject.getChildren("Resource"));
        Model model = ModelFactory.createModelForGraph(graph);
//        RDFDataMgr.write(System.out, model, Lang.TURTLE);
        try (FileOutputStream fos = new FileOutputStream("c:\\tmp\\graph_"+System.currentTimeMillis()+".ttl")) {
            RDFDataMgr.write(fos, model, Lang.TURTLE);
        }

    }

    private void addTriples(Graph graph, Collection<FedoraResource> fedoraResources) throws FedoraException{
       if (fedoraResources.isEmpty()){
           return;
       }

       for (FedoraResource resource: fedoraResources){
           Utils.triplesStream(resource.getProperties()).forEach(graph::add);
           if (resource instanceof FedoraObject){
               addTriples(graph, ((FedoraObject)resource).getChildren("Resource"));
           }
       }
    }

}
