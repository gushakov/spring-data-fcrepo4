package ch.unil.fcrepo4.spring.data.core.mapping;

import ch.unil.fcrepo4.spring.data.core.mapping.annotation.Uuid;
import org.springframework.data.mapping.PersistentEntity;
import org.springframework.data.mapping.model.SimpleTypeHolder;

import java.beans.PropertyDescriptor;
import java.lang.reflect.Field;
import java.util.UUID;

/**
 * @author gushakov
 */
public class UuidPersistentProperty extends GenericFedoraPersistentProperty {

    private Uuid uuidAnnot;

    boolean isUUID = false;

    public UuidPersistentProperty(Field field, PropertyDescriptor propertyDescriptor, PersistentEntity<?, FedoraPersistentProperty> owner, SimpleTypeHolder simpleTypeHolder) {
        super(field, propertyDescriptor, owner, simpleTypeHolder);
        this.uuidAnnot = findAnnotation(Uuid.class);
        if (UUID.class.isAssignableFrom(field.getType())){
            this.isUUID = true;
        }
    }

    public boolean isUUID() {
        return isUUID;
    }
}
