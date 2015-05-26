package ch.unil.fcrepo4.spring.data.core.mapping;

import org.springframework.data.mapping.PersistentEntity;

/**
 * @author gushakov
 */
public interface FedoraPersistentEntity<T> extends PersistentEntity<T, FedoraPersistentProperty> {
}
