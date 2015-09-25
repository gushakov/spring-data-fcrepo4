package ch.unil.fcrepo4.client;

import org.fcrepo.client.FedoraException;
import org.fcrepo.client.FedoraRepository;

/**
 * @author gushakov
 */
public interface TransactionalFedoraRepository extends FedoraRepository {

    String startTransaction() throws FedoraException;

    void commitTransaction() throws FedoraException;

    void rollbackTransaction() throws FedoraException;

}
