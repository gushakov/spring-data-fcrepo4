package ch.unil.fcrepo4.spring.data.core.mapping;

import org.springframework.data.mapping.Association;
import org.springframework.data.mapping.PersistentEntity;
import org.springframework.data.mapping.PersistentProperty;
import org.springframework.data.mapping.SimplePropertyHandler;

import java.lang.reflect.Method;
import java.util.Optional;

/**
 * @author gushakov
 */
public interface FedoraPersistentEntity<T> extends PersistentEntity<T, FedoraPersistentProperty> {

    Optional<PersistentProperty<?>> findPropertyForGetterOrSetter(Method getterOrSetter);

    Optional<Association<? extends PersistentProperty<?>>> findAssociationForGetterOrSetter(Method getterOrSetter);

}
