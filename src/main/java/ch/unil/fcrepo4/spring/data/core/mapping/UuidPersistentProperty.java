package ch.unil.fcrepo4.spring.data.core.mapping;

import org.fcrepo.kernel.RdfLexicon;
import org.springframework.data.mapping.PersistentEntity;
import org.springframework.data.mapping.model.SimpleTypeHolder;

import java.beans.PropertyDescriptor;
import java.lang.reflect.Field;

/**
 * @author gushakov
 */
public class UuidPersistentProperty extends SimpleFedoraResourcePersistentProperty {

    public UuidPersistentProperty(Field field, PropertyDescriptor propertyDescriptor, PersistentEntity<?, FedoraPersistentProperty> owner, SimpleTypeHolder simpleTypeHolder) {
        super(field, propertyDescriptor, owner, simpleTypeHolder);
    }

    @Override
    public boolean isReadOnly() {
        return true;
    }

    @Override
    public String getLocalName() {
        return RdfLexicon.HAS_PRIMARY_IDENTIFIER.getLocalName();
    }

    @Override
    public String getUriNs() {
        return RdfLexicon.HAS_PRIMARY_IDENTIFIER.getNameSpace();
    }
}
