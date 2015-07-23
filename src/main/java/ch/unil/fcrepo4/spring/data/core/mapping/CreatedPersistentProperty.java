package ch.unil.fcrepo4.spring.data.core.mapping;

import ch.unil.fcrepo4.spring.data.core.mapping.annotation.Created;
import org.springframework.data.mapping.PersistentEntity;
import org.springframework.data.mapping.model.SimpleTypeHolder;

import java.beans.PropertyDescriptor;
import java.lang.reflect.Field;
import java.time.ZonedDateTime;
import java.util.Date;

/**
 * @author gushakov
 */
public class CreatedPersistentProperty extends GenericFedoraPersistentProperty {

    public CreatedPersistentProperty(Field field, PropertyDescriptor propertyDescriptor, PersistentEntity<?, FedoraPersistentProperty> owner, SimpleTypeHolder simpleTypeHolder) {
        super(field, propertyDescriptor, owner, simpleTypeHolder);
    }

    public boolean isDate(){
      return Date.class.isAssignableFrom(getField().getType());
    }

    public boolean isZonedDateTime(){
      return ZonedDateTime.class.isAssignableFrom(getField().getType());
    }
}
