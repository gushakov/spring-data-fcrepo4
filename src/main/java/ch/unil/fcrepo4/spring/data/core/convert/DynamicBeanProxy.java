package ch.unil.fcrepo4.spring.data.core.convert;

import ch.unil.fcrepo4.spring.data.core.mapping.FedoraPersistentEntity;
import ch.unil.fcrepo4.spring.data.core.mapping.SimplePropertyAndValueHandler;
import org.springframework.data.mapping.PersistentPropertyAccessor;

import java.lang.reflect.Method;

/**
 * @author gushakov
 */
public interface DynamicBeanProxy {
    Method GET_BEAN_ENTITY_METHOD = DynamicBeanProxy.class.getMethods()[0];

    Method GET_PROPERTY_ACCESSOR_METHOD = DynamicBeanProxy.class.getMethods()[1];

    Method DO_WITH_PROPERTIES_METHOD = DynamicBeanProxy.class.getMethods()[2];

    FedoraPersistentEntity<?> __getBeanEntity();

    PersistentPropertyAccessor __getPropertyAccessor();

    void doWithProperties(SimplePropertyAndValueHandler propertyAndValueHandler, boolean withUpdatedPropertiesOnly);
}
