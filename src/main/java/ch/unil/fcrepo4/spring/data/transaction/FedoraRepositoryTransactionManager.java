package ch.unil.fcrepo4.spring.data.transaction;

import org.fcrepo.client.FedoraException;
import org.fcrepo.client.FedoraRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionException;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.SimpleTransactionStatus;

/**
 * @author gushakov
 */
public class FedoraRepositoryTransactionManager implements PlatformTransactionManager{
    private static final Logger logger = LoggerFactory.getLogger(FedoraRepositoryTransactionManager.class);

    private FedoraRepository repository;

    public FedoraRepositoryTransactionManager(FedoraRepository repository) {
        this.repository = repository;
    }

    @Override
    public TransactionStatus getTransaction(TransactionDefinition definition) throws TransactionException {
        logger.debug("Starting new transaction");
        try {
            repository.startTransaction();
        } catch (FedoraException e) {
            throw new RuntimeException(e);
        }
        return new SimpleTransactionStatus(true);
    }

    @Override
    public void commit(TransactionStatus status) throws TransactionException {
        logger.debug("Committing current transaction");
        try {
            repository.commitTransaction();
        } catch (FedoraException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void rollback(TransactionStatus status) throws TransactionException {
        logger.debug("Rolling back current transaction");
        try {
            repository.rollbackTransaction();
        } catch (FedoraException e) {
            throw new RuntimeException(e);
        }
    }
}
