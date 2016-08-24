package ch.unil.fcrepo4.spring.data.core.mapping;

import ch.unil.fcrepo4.client.FcrepoConstants;
import org.springframework.data.mapping.PersistentEntity;
import org.springframework.data.mapping.model.SimpleTypeHolder;

import java.beans.PropertyDescriptor;
import java.lang.reflect.Field;

/**
 * @author gushakov
 */
public class CreatedPersistentProperty extends SimpleFedoraResourcePersistentProperty {

    public CreatedPersistentProperty(Field field, PropertyDescriptor propertyDescriptor, PersistentEntity<?, FedoraPersistentProperty> owner, SimpleTypeHolder simpleTypeHolder) {
        super(field, propertyDescriptor, owner, simpleTypeHolder);
    }

    @Override
    public boolean isReadOnly() {
        return true;
    }

    @Override
    public String getLocalName() {
        return FcrepoConstants.CREATED_DATE.getLocalName();
    }

    @Override
    public String getUriNs() {
        return FcrepoConstants.CREATED_DATE.getNameSpace();
    }

}
