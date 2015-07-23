package ch.unil.fcrepo4.spring.data.core.mapping;

import ch.unil.fcrepo4.spring.data.core.Constants;
import ch.unil.fcrepo4.spring.data.core.mapping.annotation.Property;
import org.springframework.data.mapping.PersistentEntity;
import org.springframework.data.mapping.model.SimpleTypeHolder;

import java.beans.PropertyDescriptor;
import java.lang.reflect.Field;

/**
 * @author gushakov
 */
public class SimpleFedoraPersistentProperty extends GenericFedoraPersistentProperty {
    private Property propAnnot;

    public SimpleFedoraPersistentProperty(Field field, PropertyDescriptor propertyDescriptor, PersistentEntity<?, FedoraPersistentProperty> owner, SimpleTypeHolder simpleTypeHolder) {
        super(field, propertyDescriptor, owner, simpleTypeHolder);
        this.propAnnot = findAnnotation(Property.class);
    }

    public String getLocalName(){
        if (propAnnot.localName().equals(Constants.DEFAULT_ANNOTATION_STRING_VALUE_TOKEN)){
            return field.getName();
        }
        else {
            return propAnnot.localName();
        }
    }

    public String getUriNs(){
        if (propAnnot.uriNs().equals(Constants.DEFAULT_ANNOTATION_STRING_VALUE_TOKEN)){
            return ((FedoraObjectPersistentEntity<?>)owner).getUriNs();
        }
        else {
            return propAnnot.uriNs();
        }
    }

    public String getUri(){
        return getUriNs() + getLocalName();
    }
}
