package ch.unil.fcrepo4.spring.data.core;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.fcrepo.client.FedoraException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.support.PersistenceExceptionTranslator;

/**
 * @author gushakov
 */
public class FedoraExceptionTranslator implements PersistenceExceptionTranslator {
    private static final Logger logger = LoggerFactory.getLogger(FedoraExceptionTranslator.class);
    @Override
    public DataAccessException translateExceptionIfPossible(RuntimeException ex) {
        if (ExceptionUtils.getRootCause(ex) instanceof FedoraException){
            UncategorizedFedoraException dae = new UncategorizedFedoraException(ex.getMessage(), ex);
            logger.debug("Translated exception from {} to {}", ex.getClass(), dae.getClass());
            return dae;
        }
        return null;
    }
}
