package ch.unil.fcrepo4.spring.data.core.mapping;

import ch.unil.fcrepo4.spring.data.core.mapping.annotation.DsContent;
import org.springframework.data.mapping.PersistentEntity;
import org.springframework.data.mapping.model.MappingException;
import org.springframework.data.mapping.model.SimpleTypeHolder;

import java.beans.PropertyDescriptor;
import java.io.InputStream;
import java.lang.reflect.Field;

/**
 * @author gushakov
 */
public class DatastreamContentPersistentProperty extends GenericFedoraPersistentProperty {

    private DsContent dsContentAnnot;

    public DatastreamContentPersistentProperty(Field field, PropertyDescriptor propertyDescriptor, PersistentEntity<?, FedoraPersistentProperty> owner, SimpleTypeHolder simpleTypeHolder) {
        super(field, propertyDescriptor, owner, simpleTypeHolder);
        this.dsContentAnnot = findAnnotation(DsContent.class);

        // this annotation must only be used with properties of type InputStream
        if (!InputStream.class.isAssignableFrom(field.getType())){
            throw new MappingException("Expected datastream field to be of type InputStream, but was " + field.getType().getSimpleName());
        }

    }

}
