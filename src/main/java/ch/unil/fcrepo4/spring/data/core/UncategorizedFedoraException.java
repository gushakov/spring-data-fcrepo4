package ch.unil.fcrepo4.spring.data.core;

import org.springframework.dao.UncategorizedDataAccessException;

/**
 * @author gushakov
 */
public class UncategorizedFedoraException extends UncategorizedDataAccessException {
    /**
     * Constructor for UncategorizedDataAccessException.
     *
     * @param msg   the detail message
     * @param cause the exception thrown by underlying data access API
     */
    public UncategorizedFedoraException(String msg, Throwable cause) {
        super(msg, cause);
    }
}
