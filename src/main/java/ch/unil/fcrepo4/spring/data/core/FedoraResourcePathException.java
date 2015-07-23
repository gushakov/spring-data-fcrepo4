package ch.unil.fcrepo4.spring.data.core;

import org.springframework.dao.DataAccessException;

/**
 * @author gushakov
 */
public class FedoraResourcePathException extends DataAccessException {
    public FedoraResourcePathException(String msg) {
        super(msg);
    }

    public FedoraResourcePathException(String msg, Throwable cause) {
        super(msg, cause);
    }
}
