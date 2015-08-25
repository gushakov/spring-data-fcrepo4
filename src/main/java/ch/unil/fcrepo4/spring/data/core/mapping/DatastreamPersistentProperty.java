package ch.unil.fcrepo4.spring.data.core.mapping;

import org.springframework.data.mapping.PersistentEntity;
import org.springframework.data.mapping.model.SimpleTypeHolder;

import java.beans.PropertyDescriptor;
import java.lang.reflect.Field;

/**
 * @author gushakov
 */
public class DatastreamPersistentProperty extends GenericFedoraPersistentProperty {


    @SuppressWarnings("unchecked")
    public DatastreamPersistentProperty(Field field, PropertyDescriptor propertyDescriptor, PersistentEntity<?, FedoraPersistentProperty> owner, SimpleTypeHolder simpleTypeHolder,
                                        DatastreamPersistentEntity dsEntity) {
        super(field, propertyDescriptor, owner, simpleTypeHolder);

        // set the reference to the parent Fedora object entity
        dsEntity.setFedoraObjectEntity((FedoraObjectPersistentEntity<?>)owner);
    }

    @Override
    public boolean isAssociation() {
        return true;
    }




}
