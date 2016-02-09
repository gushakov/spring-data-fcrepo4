package ch.unil.fcrepo4.spring.data.core.mapping;

import ch.unil.fcrepo4.spring.data.core.Constants;
import ch.unil.fcrepo4.spring.data.core.mapping.annotation.Datastream;
import org.springframework.data.mapping.PersistentEntity;
import org.springframework.data.mapping.model.MappingException;
import org.springframework.data.mapping.model.SimpleTypeHolder;

import java.beans.PropertyDescriptor;
import java.lang.reflect.Field;

/**
 * @author gushakov
 */
public class DatastreamPersistentProperty extends GenericFedoraPersistentProperty {

    private Datastream dsAnnot;

    @SuppressWarnings("unchecked")
    public DatastreamPersistentProperty(Field field, PropertyDescriptor propertyDescriptor, PersistentEntity<?, FedoraPersistentProperty> owner,
                                        SimpleTypeHolder simpleTypeHolder,
                                        DatastreamPersistentEntity dsEntity) {
        super(field, propertyDescriptor, owner, simpleTypeHolder);

        this.dsAnnot = findAnnotation(Datastream.class);

        checkName();

        // set the reference to the parent Fedora object entity
        dsEntity.setFedoraObjectEntity((FedoraObjectPersistentEntity<?>)owner);
    }

    @Override
    public boolean isAssociation() {
        return true;
    }


    private void checkName(){
        // name must not contain slashes
        if (!isDefaultDatastreamName() &&  dsAnnot.name().contains("/")){
            throw new MappingException("Invalid datastream name: " + dsAnnot);
        }
    }

    /**
     * Name of the datastream as specified on {@code @Datastream} type annotation.
     * @return datastream resource name
     */
    public String getDsName(){
        return dsAnnot.name();
    }

    public boolean isDefaultDatastreamName(){
        return dsAnnot.name().equals(Constants.DEFAULT_ANNOTATION_STRING_VALUE_TOKEN) || dsAnnot.name().matches("\\s*");
    }



}
