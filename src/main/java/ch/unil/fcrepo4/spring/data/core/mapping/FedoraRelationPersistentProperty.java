package ch.unil.fcrepo4.spring.data.core.mapping;

/**
 * @author gushakov
 */
public interface FedoraRelationPersistentProperty extends FedoraPersistentProperty {
    String getUriNs();

    String getLocalName();

    String getUri();
}
