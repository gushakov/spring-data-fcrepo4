package ch.unil.fcrepo4.client;

import com.hp.hpl.jena.graph.Triple;
import org.fcrepo.client.FcrepoClient;

import java.io.InputStream;
import java.util.Iterator;
import java.util.List;

/**
 * Interface for communicating with Fedora repository.
 * <p/>
 * Modeled after {@code org.fcrepo.client.FedoraRepository}.
 * @author gushakov
 */
public interface FedoraClientRepository {

    String getRepositoryUrl();

    boolean exists(String path) throws FedoraException;

    FedoraDatastream getDatastream(String path) throws FedoraException;

    FedoraObject getObject(String path) throws FedoraException;

    FedoraObject createObject(String path) throws FedoraException;

    FedoraDatastream createDatastream(String path, FedoraContent fedoraContent) throws FedoraException;

    InputStream fetchDatastreamContent(String path) throws FedoraException;

    void updateDatastreamContent(String path, FedoraContent fedoraContent) throws FedoraException;

    List<Triple> getProperties(String path) throws FedoraException;

    void delete(String path) throws FedoraException;

    void forceDelete(String path) throws FedoraException;

    void updateProperties(String path, String sparqlUpdate) throws FedoraException;
}
