package ch.unil.fcrepo4.client;

import org.apache.jena.graph.Triple;

import java.util.Date;
import java.util.Iterator;

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

    String getPropertyLiteralValueOrUri(String predicateUri) throws FedoraException;

    Date getCreatedDate() throws FedoraException;

    Iterator<Triple> getProperties() throws FedoraException;

    void updateProperties(String sparqlUpdate) throws FedoraException;
}
