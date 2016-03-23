package ch.unil.fcrepo4.spring.data.core.mapping;

import ch.unil.fcrepo4.spring.data.core.Constants;
import ch.unil.fcrepo4.spring.data.core.mapping.annotation.Relation;
import org.springframework.data.mapping.PersistentEntity;
import org.springframework.data.mapping.model.SimpleTypeHolder;

import java.beans.PropertyDescriptor;
import java.lang.reflect.Field;

/**
 * @author gushakov
 */
public class RelationPersistentProperty extends GenericFedoraPersistentProperty
        implements FedoraRelationPersistentProperty {
    private Relation relAnnot;

    public RelationPersistentProperty(Field field, PropertyDescriptor propertyDescriptor, PersistentEntity<?, FedoraPersistentProperty> owner, SimpleTypeHolder simpleTypeHolder) {
        super(field, propertyDescriptor, owner, simpleTypeHolder);
        this.relAnnot = findAnnotation(Relation.class);
    }

    @Override
    public boolean isAssociation() {
        return true;
    }


    @Override
    public String getUriNs() {
        // if no namespace was explicitly set on the property,
        // return the namespace of the property's owner entity
        if (relAnnot.uriNs().equals(Constants.DEFAULT_ANNOTATION_STRING_VALUE_TOKEN)){
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
            return relAnnot.uriNs();
        }
    }

    @Override
    public String getLocalName() {
        if (relAnnot.localName().equals(Constants.DEFAULT_ANNOTATION_STRING_VALUE_TOKEN)){
            return field.getName();
        }
        else {
            return relAnnot.localName();
        }
    }

    @Override
    public String getUri() {
        return getUriNs() + getLocalName();
    }
}
