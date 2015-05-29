package ch.unil.fcrepo4.spring.data.core.mapping;

import ch.unil.fcrepo4.spring.data.core.mapping.annotation.Uuid;
import org.springframework.data.mapping.PersistentEntity;
import org.springframework.data.mapping.model.SimpleTypeHolder;

import java.beans.PropertyDescriptor;
import java.lang.reflect.Field;

/**
 * @author gushakov
 */
public class UuidPersistentProperty extends GenericFedoraPersistentProperty {

    private Uuid uuidAnnot;

    private PathCreator pathCreator;

    public UuidPersistentProperty(Field field, PropertyDescriptor propertyDescriptor, PersistentEntity<?, FedoraPersistentProperty> owner, SimpleTypeHolder simpleTypeHolder) {
        super(field, propertyDescriptor, owner, simpleTypeHolder);
        Uuid annot = findAnnotation(Uuid.class);
        this.uuidAnnot = annot;
        try {
           this.pathCreator = annot.pathCreator().newInstance();
        } catch (InstantiationException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }

    }

    public PathCreator getPathCreator() {
        return pathCreator;
    }
}
