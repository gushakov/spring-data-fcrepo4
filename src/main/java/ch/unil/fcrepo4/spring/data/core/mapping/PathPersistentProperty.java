package ch.unil.fcrepo4.spring.data.core.mapping;

import ch.unil.fcrepo4.spring.data.core.mapping.annotation.Path;
import org.springframework.data.mapping.PersistentEntity;
import org.springframework.data.mapping.model.SimpleTypeHolder;

import java.beans.PropertyDescriptor;
import java.lang.reflect.Field;

/**
 * @author gushakov
 */
public class PathPersistentProperty extends GenericFedoraPersistentProperty {

    private Path pathAnnot;

    private PathCreator pathCreator;

    public PathPersistentProperty(Field field, PropertyDescriptor propertyDescriptor, PersistentEntity<?, FedoraPersistentProperty> owner, SimpleTypeHolder simpleTypeHolder) {
        super(field, propertyDescriptor, owner, simpleTypeHolder);
        Path annot = findAnnotation(Path.class);
        this.pathAnnot = annot;
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
