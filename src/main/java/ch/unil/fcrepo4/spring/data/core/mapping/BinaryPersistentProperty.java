package ch.unil.fcrepo4.spring.data.core.mapping;

import ch.unil.fcrepo4.spring.data.core.mapping.annotation.Binary;
import org.springframework.data.mapping.PersistentEntity;
import org.springframework.data.mapping.model.MappingException;
import org.springframework.data.mapping.model.SimpleTypeHolder;

import java.beans.PropertyDescriptor;
import java.io.InputStream;
import java.lang.reflect.Field;

/**
 * @author gushakov
 */
public class BinaryPersistentProperty extends GenericFedoraPersistentProperty {

    private Binary binaryAnnot;

    public String getMimetype(){
        return binaryAnnot.mimetype();
    }

    public BinaryPersistentProperty(Field field, PropertyDescriptor propertyDescriptor, PersistentEntity<?, FedoraPersistentProperty> owner, SimpleTypeHolder simpleTypeHolder) {
        super(field, propertyDescriptor, owner, simpleTypeHolder);
        this.binaryAnnot = findAnnotation(Binary.class);

        // this annotation must only be used with properties of type InputStream
        if (!InputStream.class.isAssignableFrom(field.getType())){
            throw new MappingException("Expected datastream field to be of type InputStream, but was " + field.getType().getSimpleName());
        }

    }

}
