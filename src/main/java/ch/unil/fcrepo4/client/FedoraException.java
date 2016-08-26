package ch.unil.fcrepo4.client;

/**
 * Root exception thrown if there were any problems communicating with the Fedora repository.
 * <p>
 * Modeled after {@code org.fcrepo.client.FedoraException}.
 *
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
