package ch.unil.fcrepo4.client;

/**
 * Modeled after {@code org.fcrepo.client.impl.FedoraObjectImpl}.
 *
 * @author gushakov
 */
public class FedoraObjectImpl extends FedoraResourceImpl implements FedoraObject {
    public FedoraObjectImpl(FedoraClientRepository fedoraClientRepository, String path) {
        super(fedoraClientRepository, path);
    }
}
