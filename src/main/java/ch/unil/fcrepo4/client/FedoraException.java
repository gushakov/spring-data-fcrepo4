package ch.unil.fcrepo4.client;

/**
 * Root exception thrown if there were any problem executing REST calls to Fedora repository.
 * <p/>
 * Based on {@code org.fcrepo.client.FedoraException} from deprecated {@code fcrepo4-client} project.
 * @author gushakov
 */
public class FedoraException extends Exception {
    public FedoraException() {
    }

    public FedoraException(String message) {
        super(message);
    }

    public FedoraException(String message, Throwable cause) {
        super(message, cause);
    }

    public FedoraException(Throwable cause) {
        super(cause);
    }
}
