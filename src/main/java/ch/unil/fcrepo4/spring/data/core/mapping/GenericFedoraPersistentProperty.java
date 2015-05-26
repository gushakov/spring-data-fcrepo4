package ch.unil.fcrepo4.spring.data.core.mapping;

import org.springframework.data.mapping.Association;
import org.springframework.data.mapping.PersistentEntity;
import org.springframework.data.mapping.model.AnnotationBasedPersistentProperty;
import org.springframework.data.mapping.model.SimpleTypeHolder;

import java.beans.PropertyDescriptor;
import java.lang.reflect.Field;

/**
 * Generic property of a Fedora object.
 *
 * @author gushakov
 */
public class GenericFedoraPersistentProperty extends AnnotationBasedPersistentProperty<FedoraPersistentProperty>
        implements FedoraPersistentProperty {

    public GenericFedoraPersistentProperty(Field field, PropertyDescriptor propertyDescriptor, PersistentEntity<?, FedoraPersistentProperty> owner, SimpleTypeHolder simpleTypeHolder) {
        super(field, propertyDescriptor, owner, simpleTypeHolder);
    }

    @Override
    protected Association<FedoraPersistentProperty> createAssociation() {
        return null;
    }
}
