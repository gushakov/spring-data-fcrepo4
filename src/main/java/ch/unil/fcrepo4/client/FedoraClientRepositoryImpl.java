package ch.unil.fcrepo4.client;

import ch.unil.fcrepo4.utils.Utils;
import com.hp.hpl.jena.graph.Graph;
import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.graph.impl.CollectionGraph;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpStatus;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFDataMgr;
import org.fcrepo.client.FcrepoClient;
import org.fcrepo.client.FcrepoOperationFailedException;
import org.fcrepo.client.FcrepoResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.Assert;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;

/**
 * <p>
 * Based on Based on {@code org.fcrepo.client.impl.FedoraRepositoryImpl} from deprecated {@code fcrepo4-client}
 * project.
 *
 * @author gushakov
 */
public class FedoraClientRepositoryImpl implements FedoraClientRepository {

    private static final Logger logger = LoggerFactory.getLogger(FedoraClientRepositoryImpl.class);

    private String fedoraUrl;

    private FcrepoClient fcrepoClient;

    public FedoraClientRepositoryImpl(String fedoraUrl) {
        this.fedoraUrl = fedoraUrl;
        // default security credentials, do not throw an exception in case of error
        this.fcrepoClient = new FcrepoClient(null, null, null, false);
        logger.info("Initialized Fedora repository client with URL: {}", fedoraUrl);
    }

    @Override
    public String getRepositoryUrl() {
        return fedoraUrl;
    }

    @Override
    public boolean exists(String path) throws FedoraException {
        Assert.hasText(path);
        try {
             URI uri = new URI(Utils.concatenate(fedoraUrl, path));
             try (FcrepoResponse response = fcrepoClient.get(uri, null, null)) {
                 return response.getStatusCode() == HttpStatus.SC_OK;
             } catch (FcrepoOperationFailedException | IOException e) {
                 throw new FedoraException(e);
             }

         } catch (URISyntaxException e) {
             throw new FedoraException(e);
         }
    }

    @Override
    public FedoraResource createObject(String path) throws FedoraException {
        Assert.hasText(path);
        try {
            URI uri = new URI(Utils.concatenate(fedoraUrl, path));
            // PUT with empty body to the resource path
            try (FcrepoResponse response = fcrepoClient.put(uri, null, null)) {
                if (response.getStatusCode() != HttpStatus.SC_CREATED) {
                    throw new IllegalStateException("Could not create object: " + uri + ", status code: " + response.getStatusCode());
                }
                return new FedoraResourceImpl(StringUtils.stripEnd(path, "/"), loadTriples(uri));
            } catch (FcrepoOperationFailedException | IOException e) {
                throw new FedoraException(e);
            }
        } catch (URISyntaxException e) {
            throw new FedoraException(e);
        }
    }

    @Override
    public FedoraResource createDatastream(String path, InputStream content, String contentType) throws FedoraException {
        Assert.hasText(path);
        Assert.notNull(content);
        try {
            URI uri = new URI(Utils.concatenate(fedoraUrl, path));
            // POST to the path with the contents in the body
            try (FcrepoResponse response = fcrepoClient.post(uri, content, contentType)) {
                if (response.getStatusCode() != HttpStatus.SC_CREATED) {
                    throw new IllegalStateException("Could not create datastream: " + uri + ", status code: " + response.getStatusCode());
                }
                return new FedoraResourceImpl(StringUtils.stripEnd(path, "/"), loadTriples(uri));
            } catch (FcrepoOperationFailedException | IOException e) {
                throw new FedoraException(e);
            }
        } catch (URISyntaxException e) {
            throw new FedoraException(e);
        }
    }

    private List<Triple> loadTriples(URI uri) throws FcrepoOperationFailedException, IOException, FedoraException {
        try (FcrepoResponse response = fcrepoClient.get(uri, null, null)) {
            if (response.getStatusCode() != HttpStatus.SC_OK) {
                throw new FedoraException("Could not load object: " + uri + ", status code: " + response.getStatusCode());
            }
            Graph graph = new CollectionGraph();
            RDFDataMgr.read(graph, response.getBody(), Lang.TURTLE);
            return graph.find(Node.ANY, Node.ANY, Node.ANY).toList();
        }
    }
}
