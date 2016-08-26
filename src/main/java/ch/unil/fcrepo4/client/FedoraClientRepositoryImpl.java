package ch.unil.fcrepo4.client;

import ch.unil.fcrepo4.spring.data.core.convert.rdf.ExtendedXsdDatatypeConverter;
import ch.unil.fcrepo4.spring.data.core.convert.rdf.RdfDatatypeConverter;
import ch.unil.fcrepo4.utils.UriBuilder;
import ch.unil.fcrepo4.utils.Utils;
import com.hp.hpl.jena.graph.Graph;
import com.hp.hpl.jena.graph.impl.CollectionGraph;
import org.apache.http.HttpStatus;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFDataMgr;
import org.fcrepo.client.FcrepoClient;
import org.fcrepo.client.FcrepoOperationFailedException;
import org.fcrepo.client.FcrepoResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.Assert;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;

/**
 * Default implementation of {@linkplain FedoraClientRepository} using {@linkplain FcrepoClient} REST client.
 *
 * @author gushakov
 */
public class FedoraClientRepositoryImpl implements FedoraClientRepository {

    private static final Logger logger = LoggerFactory.getLogger(FedoraClientRepositoryImpl.class);

    private String fedoraUrl;

    private RdfDatatypeConverter rdfDatatypeConverter;

    private FcrepoClient fcrepoClient;

    public FedoraClientRepositoryImpl(String fedoraUrl) {
        this.fedoraUrl = fedoraUrl;
        this.rdfDatatypeConverter = new ExtendedXsdDatatypeConverter();
        this.fcrepoClient = FcrepoClient.client().build();
        logger.info("Initialized Fedora repository client with URL: {}", fedoraUrl);
    }

    @Override
    public String getRepositoryUrl() {
        return fedoraUrl;
    }

    @Override
    public RdfDatatypeConverter getRdfDatatypeConverter() {
        return rdfDatatypeConverter;
    }

    @Override
    public boolean exists(String path) throws FedoraException {
        Assert.hasText(path);
        final URI uri = new UriBuilder(fedoraUrl).appendPathSegment(path).build();
        try (FcrepoResponse response = fcrepoClient.get(uri).perform()) {
            return response.getStatusCode() == HttpStatus.SC_OK;
        } catch (FcrepoOperationFailedException | IOException e) {
            throw new FedoraException(e);
        }
    }

    @Override
    public FedoraDatastream getDatastream(String path) throws FedoraException {
        return new FedoraDatastreamImpl(this, path);
    }

    @Override
    public FedoraObject getObject(String path) throws FedoraException {
        return new FedoraObjectImpl(this, path);
    }

    @Override
    public FedoraObject createObject(String path) throws FedoraException {
        Assert.hasText(path);
        final URI uri = new UriBuilder(fedoraUrl).appendPathSegment(path).build();
        try (FcrepoResponse response = fcrepoClient.put(uri).perform()) {
            if (response.getStatusCode() != HttpStatus.SC_CREATED) {
                throw new FedoraException("Could not create object: " + uri + ", expected status code: " +
                        HttpStatus.SC_CREATED +
                        ", actual status code: " + response.getStatusCode());
            }
            return new FedoraObjectImpl(this, path);
        } catch (FcrepoOperationFailedException | IOException e) {
            throw new FedoraException(e);
        }
    }

    @Override
    public FedoraDatastream createDatastream(String path, FedoraContent fedoraContent) throws FedoraException {
        storeBinaryContent(path, fedoraContent, HttpStatus.SC_CREATED);
        return new FedoraDatastreamImpl(this, path);
    }

    @Override
    public InputStream fetchDatastreamContent(String path) throws FedoraException {
        final URI uri = new UriBuilder(fedoraUrl).appendPathSegment(path).build();
        FcrepoResponse response;
        try {
            response = fcrepoClient.get(uri).perform();
        } catch (FcrepoOperationFailedException e) {
            throw new FedoraException(e);
        }
        if (response.getStatusCode() != HttpStatus.SC_OK) {
            throw new FedoraException("Could not read content of datastream: " + uri + ", expected status code: " +
                    HttpStatus.SC_OK +
                    ", actual status code: " + response.getStatusCode());
        }
        return new FcrepoContentStream(response);
    }

    @Override
    public void updateDatastreamContent(String path, FedoraContent fedoraContent) throws FedoraException {
        storeBinaryContent(path, fedoraContent, HttpStatus.SC_NO_CONTENT);
    }

    @Override
    public Graph getGraph(String path) throws FedoraException {
        Assert.hasText(path);
        final UriBuilder uriBuilder = new UriBuilder(fedoraUrl)
                .appendPathSegment(path);
        if (!path.endsWith(FcrepoConstants.FCR_METADATA)) {
            uriBuilder.appendPathSegment(FcrepoConstants.FCR_METADATA);
        }
        return loadGraph(uriBuilder.build());
    }

    @Override
    public void delete(String path) throws FedoraException {
        Assert.hasText(path);
        final URI uri = new UriBuilder(fedoraUrl).appendPathSegment(path).build();
        try (FcrepoResponse response = fcrepoClient.delete(uri).perform()) {
            if (response.getStatusCode() != HttpStatus.SC_NO_CONTENT) {
                throw new FedoraException("Could not delete resource: " + uri + ", expected status code: " +
                        HttpStatus.SC_NO_CONTENT +
                        ", actual status code: " + response.getStatusCode());
            }
        } catch (FcrepoOperationFailedException | IOException e) {
            throw new FedoraException(e);
        }
    }

    @Override
    public void forceDelete(String path) throws FedoraException {
        delete(path);
        if (!path.endsWith(FcrepoConstants.FCR_TOMBSTONE)) {
            delete(Utils.normalize(path, FcrepoConstants.FCR_TOMBSTONE));
        }
    }

    @Override
    public void updateProperties(String path, String sparqlUpdate) throws FedoraException {
        Assert.hasText(path);
        Assert.hasText(sparqlUpdate);
        final UriBuilder uriBuilder = new UriBuilder(fedoraUrl).appendPathSegment(path);
        if (!path.endsWith(FcrepoConstants.FCR_METADATA)) {
            uriBuilder.appendPathSegment(FcrepoConstants.FCR_METADATA);
        }
        URI uri = uriBuilder.build();
        try (FcrepoResponse response = fcrepoClient
                .patch(uri)
                .body(new ByteArrayInputStream(sparqlUpdate.getBytes("UTF-8")))
                .perform()) {
            if (response.getStatusCode() != HttpStatus.SC_NO_CONTENT) {
                throw new FedoraException("Could not update properties of resource: " + uri + ", expected status code: " +
                        HttpStatus.SC_NO_CONTENT +
                        ", actual status code: " + response.getStatusCode());
            }
        } catch (FcrepoOperationFailedException | IOException e) {
            throw new FedoraException(e);
        }
    }

    private Graph loadGraph(URI uri) throws FedoraException {
        logger.debug("Loading graph for resource {}", uri);
        try (FcrepoResponse response = fcrepoClient.get(uri)
                .accept(FcrepoConstants.RDF_XML_MIMETYPE)
                .perform()) {
            if (response.getStatusCode() != HttpStatus.SC_OK) {
                throw new FedoraException("Could not load triples for resource: " + uri + ", expected status code: " +
                        HttpStatus.SC_OK +
                        ", actual status code: " + response.getStatusCode());
            }
            Graph graph = new CollectionGraph();
            RDFDataMgr.read(graph, response.getBody(), Lang.RDFXML);
            return graph;
        } catch (FcrepoOperationFailedException | IOException e) {
            throw new FedoraException(e);
        }
    }

    private void storeBinaryContent(String path, FedoraContent fedoraContent, int okStatusCode) throws FedoraException {
        Assert.hasText(path);
        Assert.notNull(fedoraContent);
        Assert.notNull(fedoraContent.getContent());
        Assert.hasText(fedoraContent.getContentType());
        URI uri = new UriBuilder(fedoraUrl).appendPathSegment(path).build();
        try (FcrepoResponse response = fcrepoClient
                .put(uri)
                .body(fedoraContent.getContent(), fedoraContent.getContentType())
                .perform()) {
            if (response.getStatusCode() != okStatusCode) {
                throw new FedoraException("Could not store binary content for datastream: " + uri +
                        ", expected status code: " +
                        okStatusCode +
                        ", actual status code: " + response.getStatusCode());
            }
        } catch (FcrepoOperationFailedException | IOException e) {
            throw new FedoraException(e);
        }
    }
}
