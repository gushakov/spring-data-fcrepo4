package ch.unil.fcrepo4.client;

import com.hp.hpl.jena.graph.Triple;

import java.util.Iterator;
import java.util.List;

/**
 * Encapsulates properties of a Fedora object: resource or datastream.
 * <p>
 * Based on {@code org.fcrepo.client.FedoraResource} from the deprecated {@code fcrepo4-client} project.
 *
 * @author gushakov
 * @see <a href="https://github.com/fcrepo4-labs/fcrepo4-client">Fedora 4 Java Client</a>
 */
public interface FedoraResource {

    String getName();

    String getPath();

    Iterator<Triple> getProperties();

}
