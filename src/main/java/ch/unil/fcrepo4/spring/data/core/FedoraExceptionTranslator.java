package ch.unil.fcrepo4.spring.data.core;

import org.fcrepo.client.FedoraException;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.support.PersistenceExceptionTranslator;

/**
 * @author gushakov
 */
public class FedoraExceptionTranslator implements PersistenceExceptionTranslator {
    @Override
    public DataAccessException translateExceptionIfPossible(RuntimeException ex) {
        if (ex.getCause() instanceof FedoraException){

            System.out.println("||||||||||||||||||||||||||||||||||||");
            System.out.println("||  TRANSLATED FEDORA EXCEPTION   ||");
            System.out.println("||||||||||||||||||||||||||||||||||||");

            // TODO: specify exception type
            return new UncategorizedFedoraException(ex.getMessage(), ex);
        }
        return null;
    }
}
