package ch.unil.fcrepo4.client;

import com.hp.hpl.jena.graph.Triple;

import java.util.Iterator;
import java.util.List;

/**
 * Encapsulates information about a resource in Fedora repository. A resource can be an object (container) or a
 * datastream (binary content).
 * <p>
 * Modeled after {@code org.fcrepo.client.FedoraResource}.
 */
public interface FedoraResource {

    void delete() throws FedoraException;

    void forceDelete() throws FedoraException;

    String getName();

    String getPath();

    Iterator<Triple> getProperties() throws FedoraException;

    void updateProperties(String sparqlUpdate) throws FedoraException;
}
