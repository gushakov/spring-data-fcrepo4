package ch.unil.fcrepo4.client;

import org.fcrepo.client.FcrepoClient;

import java.io.InputStream;

/**
 * Leverages {@linkplain FcrepoClient} for accessing Fedora's REST API.
 * <p/>
 * Based on {@code org.fcrepo.client.FedoraRepository} from deprecated {@code fcrepo4-client} project.
 * @author gushakov
 */
public interface FedoraClientRepository {

    String getRepositoryUrl();

    boolean exists(String path) throws FedoraException;

    FedoraResource createObject(String path) throws FedoraException;

    FedoraResource createDatastream(String path, InputStream content, String contentType) throws FedoraException;
}
