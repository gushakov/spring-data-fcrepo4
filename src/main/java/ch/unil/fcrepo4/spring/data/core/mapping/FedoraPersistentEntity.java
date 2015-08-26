package ch.unil.fcrepo4.spring.data.core.mapping;

import org.springframework.data.mapping.PersistentEntity;
import org.springframework.data.mapping.SimplePropertyHandler;

import java.lang.reflect.Method;

/**
 * @author gushakov
 */
public interface FedoraPersistentEntity<T> extends PersistentEntity<T, FedoraPersistentProperty> {

    FedoraPersistentProperty findPropertyForGetterOrSetter(Method getterOrSetter);

    void doWithSimplePersistentProperties(SimplePropertyHandler simplePropertyHandler);
}
