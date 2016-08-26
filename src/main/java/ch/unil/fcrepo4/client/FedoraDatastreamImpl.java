package ch.unil.fcrepo4.client;

import java.io.InputStream;

/**
 * Modeled after {@code org.fcrepo.client.impl.FedoraDatastreamImpl}.
 *
 * @author gushakov
 */
public class FedoraDatastreamImpl extends FedoraResourceImpl implements FedoraDatastream {
    public FedoraDatastreamImpl(FedoraClientRepository fedoraClientRepository, String path) {
        super(fedoraClientRepository, path);
    }

    @Override
    public InputStream getContent() throws FedoraException {
        return repository.fetchDatastreamContent(getPath());
    }

    @Override
    public void updateContent(FedoraContent fedoraContent) throws FedoraException {
        repository.updateDatastreamContent(path, fedoraContent);
    }
}
