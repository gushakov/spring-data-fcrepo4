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
public class SimpleFedoraResourcePersistentProperty extends GenericFedoraPersistentProperty
    implements FedoraResourcePersistentProperty {
    private Property propAnnot;

    public SimpleFedoraResourcePersistentProperty(Field field, PropertyDescriptor propertyDescriptor, PersistentEntity<?, FedoraPersistentProperty> owner, SimpleTypeHolder simpleTypeHolder) {
        super(field, propertyDescriptor, owner, simpleTypeHolder);
        this.propAnnot = findAnnotation(Property.class);
    }

    @Override
    public boolean isReadOnly() {
        return false;
    }

    @Override
    public String getLocalName(){
        if (propAnnot.localName().equals(Constants.DEFAULT_ANNOTATION_STRING_VALUE_TOKEN)){
            return field.getName();
        }
        else {
            return propAnnot.localName();
        }
    }

    @Override
    public String getUriNs(){
        // if no namespace was explicitly set on the property,
        // return the namespace of the property's owner entity
        if (propAnnot.uriNs().equals(Constants.DEFAULT_ANNOTATION_STRING_VALUE_TOKEN)){
            if (owner instanceof FedoraObjectPersistentEntity){
                return ((FedoraObjectPersistentEntity<?>)owner).getUriNs();
            }
            else {
                // or if this is a property of a datastream entity
                // return the namespace of the datastream's owner entity
                return ((DatastreamPersistentEntity<?>)owner).getFedoraObjectEntity().getUriNs();
            }
        }
        else {
            return propAnnot.uriNs();
        }
    }

    @Override
    public String getPrefix() {
        // if no namespace was explicitly set on the property,
        // return the prefix of the property's owner entity
        if (propAnnot.prefix().equals(Constants.DEFAULT_ANNOTATION_STRING_VALUE_TOKEN)){
            if (owner instanceof FedoraObjectPersistentEntity){
                return ((FedoraObjectPersistentEntity<?>)owner).getPrefix();
            }
            else {
                // or if this is a property of a datastream entity
                // return the prefix of the datastream's owner entity
                return ((DatastreamPersistentEntity<?>)owner).getFedoraObjectEntity().getPrefix();
            }
        }
        else {
            return propAnnot.prefix();
        }
    }

    @Override
    public String getPrefixedName() {
        return getPrefix() + ":" + getName();
    }

    @Override
    public String getUri(){
        return getUriNs() + getLocalName();
    }
}
