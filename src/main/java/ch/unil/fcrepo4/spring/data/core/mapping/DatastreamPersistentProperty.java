package ch.unil.fcrepo4.spring.data.core.mapping;

import ch.unil.fcrepo4.spring.data.core.Constants;
import ch.unil.fcrepo4.spring.data.core.mapping.annotation.Datastream;
import org.springframework.data.mapping.PersistentEntity;
import org.springframework.data.mapping.model.MappingException;
import org.springframework.data.mapping.model.SimpleTypeHolder;

import java.beans.PropertyDescriptor;
import java.io.InputStream;
import java.lang.reflect.Field;

/**
 * @author gushakov
 */
public class DatastreamPersistentProperty extends GenericFedoraPersistentProperty {

    private Datastream dsAnnot;

    public DatastreamPersistentProperty(Field field, PropertyDescriptor propertyDescriptor, PersistentEntity<?, FedoraPersistentProperty> owner, SimpleTypeHolder simpleTypeHolder) {
        super(field, propertyDescriptor, owner, simpleTypeHolder);
        this.dsAnnot = findAnnotation(Datastream.class);

        if (!InputStream.class.isAssignableFrom(field.getType())){
            throw new MappingException("Expected datastream field to be of type InputStream, but was " + field.getType().getSimpleName());
        }

    }

    public String getPath(){
        if (dsAnnot.path().equals(Constants.DEFAULT_ANNOTATION_STRING_VALUE_TOKEN)){
            return getField().getName().toLowerCase();
        }
        else {
            return dsAnnot.path();
        }
    }

    public String getMimetype(){
      return dsAnnot.mimetype();
    }

    public boolean getLazyLoad() {
        return dsAnnot.lazyLoad();
    }

}
