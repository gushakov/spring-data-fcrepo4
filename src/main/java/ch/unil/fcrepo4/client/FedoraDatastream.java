package ch.unil.fcrepo4.client;

import java.io.InputStream;

/**
 * Fedora resource representing some binary content.
 * <p>
 * Modeled after {@code org.fcrepo.client.FedoraDatastream}.
 *
 * @author gushakov
 */
public interface FedoraDatastream extends FedoraResource {

    InputStream getContent() throws FedoraException;

    void updateContent(FedoraContent fedoraContent) throws FedoraException;
}
