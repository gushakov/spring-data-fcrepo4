package ch.unil.fcrepo4.client;

import ch.unil.fcrepo4.spring.data.core.convert.rdf.RdfDatatypeConverter;
import org.apache.jena.graph.Graph;

import java.io.InputStream;

/**
 * Interface for communicating with Fedora repository.
 * <p/>
 * Modeled after {@code org.fcrepo.client.FedoraRepository}.
 * @author gushakov
 */
public interface FedoraClientRepository {

    String getRepositoryUrl();

    RdfDatatypeConverter getRdfDatatypeConverter();

    boolean exists(String path) throws FedoraException;

    FedoraDatastream getDatastream(String path) throws FedoraException;

    FedoraObject getObject(String path) throws FedoraException;

    FedoraObject createObject(String path) throws FedoraException;

    FedoraDatastream createDatastream(String path, FedoraContent fedoraContent) throws FedoraException;

    InputStream fetchDatastreamContent(String path) throws FedoraException;

    void updateDatastreamContent(String path, FedoraContent fedoraContent) throws FedoraException;

    Graph getGraph(String path) throws FedoraException;

    void delete(String path) throws FedoraException;

    void forceDelete(String path) throws FedoraException;

    void updateProperties(String path, String sparqlUpdate) throws FedoraException;
}
