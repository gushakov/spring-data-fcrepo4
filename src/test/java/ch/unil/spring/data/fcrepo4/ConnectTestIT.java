package ch.unil.spring.data.fcrepo4;

import com.hp.hpl.jena.graph.Graph;
import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.NodeFactory;
import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.util.iterator.ExtendedIterator;
import org.fcrepo.client.FedoraDatastream;
import org.fcrepo.client.FedoraObject;
import org.fcrepo.client.FedoraRepository;
import org.fcrepo.client.impl.FedoraDatastreamImpl;
import org.fcrepo.client.impl.FedoraRepositoryImpl;
import org.fcrepo.client.impl.ReadOnlyFedoraRepositoryImpl;
import org.fcrepo.kernel.RdfLexicon;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.env.Environment;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import static ch.unil.spring.data.fcrepo4.TestUtils.getRandomUuid;
import static ch.unil.spring.data.fcrepo4.TestUtils.getStringContent;
import static ch.unil.spring.data.fcrepo4.assertj.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThat;

// Based on org.fcrepo.client.impl.FedoraRepositoryImplIT in fcrepo4-client

/**
 * @author gushakov
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration
public class ConnectTestIT {

    @Configuration
    @PropertySource("classpath:fcrepo4.properties")
    static class TestConfig {

    }

    @Autowired
    private Environment env;

    @Test
    public void testReadObject() throws Exception {
        // read an existing object from Fedora
        String repoUrl = env.getProperty("fedora.repository.url");
        // read-only access
        FedoraRepository repository = new ReadOnlyFedoraRepositoryImpl(repoUrl);
        FedoraObject fo = repository.findOrCreateObject("/test");
        assertThat(fo)
                .isNotNull()
                .hasName("test")
                .hasPath("/test")
        ;
    }

    @Test
    public void testWriteDatastream() throws Exception {
        String repoUrl = env.getProperty("fedora.repository.url");
        FedoraRepository repository = new FedoraRepositoryImpl(repoUrl);
        assertThat(repository).isNotNull();
        String dsPath = "/test/" + getRandomUuid();
        repository.createDatastream(dsPath, getStringContent("Hello World"));

        FedoraDatastream datastream = repository.getDatastream(dsPath);
        assertThat(datastream).isNotNull();
        assertThat(datastream.getProperties()).isNotEmpty();
        Graph graph = ((FedoraDatastreamImpl) datastream).getGraph();
        assertThat(graph.size()).isGreaterThan(0);
        ExtendedIterator<Triple> triples = graph.find(Node.ANY, NodeFactory.createURI(RdfLexicon.HAS_MIME_TYPE.getURI()), Node.ANY);
        assertThat(triples.next().getObject().getLiteralValue()).isEqualTo("text/plain");
    }

}
