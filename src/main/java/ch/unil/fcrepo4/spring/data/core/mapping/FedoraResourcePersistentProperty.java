package ch.unil.fcrepo4.spring.data.core.mapping;

/**
 * @author gushakov
 */
public interface FedoraResourcePersistentProperty extends FedoraPersistentProperty {

    boolean isReadOnly();

    String getUriNs();

    String getLocalName();

    String getUri();

}
