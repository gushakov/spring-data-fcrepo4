package ch.unil.fcrepo4.spring.data.core.mapping;

import org.springframework.data.mapping.PersistentEntity;
import org.springframework.data.mapping.model.SimpleTypeHolder;

import java.beans.PropertyDescriptor;
import java.lang.reflect.Field;

/**
 * @author gushakov
 */
public class TransientFedoraPersistentProperty extends GenericFedoraPersistentProperty {
    public TransientFedoraPersistentProperty(Field field, PropertyDescriptor propertyDescriptor, PersistentEntity<?, FedoraPersistentProperty> owner, SimpleTypeHolder simpleTypeHolder) {
        super(field, propertyDescriptor, owner, simpleTypeHolder);
    }

    @Override
    public boolean isTransient() {
        return true;
    }
}
