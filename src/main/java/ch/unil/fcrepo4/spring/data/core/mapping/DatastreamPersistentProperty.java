package ch.unil.fcrepo4.spring.data.core.mapping;

import ch.unil.fcrepo4.spring.data.core.Constants;
import ch.unil.fcrepo4.spring.data.core.mapping.annotation.Datastream;
import org.springframework.data.mapping.PersistentEntity;
import org.springframework.data.mapping.model.SimpleTypeHolder;

import java.beans.PropertyDescriptor;
import java.lang.reflect.Field;

/**
 * @author gushakov
 */
public class DatastreamPersistentProperty extends GenericFedoraPersistentProperty {

    private Datastream dsAnnot;

    public DatastreamPersistentProperty(Field field, PropertyDescriptor propertyDescriptor, PersistentEntity<?, FedoraPersistentProperty> owner, SimpleTypeHolder simpleTypeHolder) {
        super(field, propertyDescriptor, owner, simpleTypeHolder);
        this.dsAnnot = findAnnotation(Datastream.class);
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

    public String getJaxbContextPath(){
        return dsAnnot.jaxbContextPath();
    }

    public boolean getLazyLoad() {
        return dsAnnot.lazyLoad();
    }

}
