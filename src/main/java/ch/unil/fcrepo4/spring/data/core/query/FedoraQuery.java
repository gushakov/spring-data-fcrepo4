package ch.unil.fcrepo4.spring.data.core.query;

/**
 * @author gushakov
 */
public interface FedoraQuery {

    String getSerialized();

    boolean isPaged();

    long getOffset();

    long getLimit();
}
