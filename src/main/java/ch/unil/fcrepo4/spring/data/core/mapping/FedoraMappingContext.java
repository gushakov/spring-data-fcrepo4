package ch.unil.fcrepo4.spring.data.core.mapping;

import ch.unil.fcrepo4.spring.data.core.mapping.annotation.FedoraObject;
import ch.unil.fcrepo4.spring.data.core.mapping.annotation.Path;
import ch.unil.fcrepo4.spring.data.core.mapping.annotation.Uuid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.mapping.context.AbstractMappingContext;
import org.springframework.data.mapping.model.SimpleTypeHolder;
import org.springframework.data.util.TypeInformation;

import java.beans.PropertyDescriptor;
import java.lang.reflect.Field;

/**
 * @author gushakov
 */
public class FedoraMappingContext extends AbstractMappingContext<GenericFedoraPersistenceEntity<?>, FedoraPersistentProperty> {
    private static final Logger logger = LoggerFactory.getLogger(FedoraMappingContext.class);

    @Override
    protected <T> GenericFedoraPersistenceEntity<?> createPersistentEntity(TypeInformation<T> typeInformation) {
        final GenericFedoraPersistenceEntity<?> entity;
        if (typeInformation.getRawTypeInformation().getType().getAnnotation(FedoraObject.class) != null){
            logger.debug("Creating Fedora object persistent entity from type {}", typeInformation.getRawTypeInformation().getType().getSimpleName());
            entity = new FedoraObjectPersistentEntity<>(typeInformation);
        }
        else {
            entity = new GenericFedoraPersistenceEntity<>(typeInformation);
        }
        return entity;
    }

    @Override
    protected FedoraPersistentProperty createPersistentProperty(Field field, PropertyDescriptor descriptor, GenericFedoraPersistenceEntity<?> owner, SimpleTypeHolder simpleTypeHolder) {
        final FedoraPersistentProperty prop;

        if (field != null && field.getAnnotation(Path.class) != null){
            logger.debug("Found Path annotated property on field <{}> of entity {}", field.getName(), owner.getType().getName());
            prop = new PathPersistentProperty(field, descriptor, owner, simpleTypeHolder);
        }
        else if (field != null && field.getAnnotation(Uuid.class) != null) {
            logger.debug("Found Uuid annotated property on field <{}> of entity {}", field.getName(), owner.getType().getName());
            prop = new UuidPersistentProperty(field, descriptor, owner, simpleTypeHolder);
        }
        else {
            prop = new GenericFedoraPersistentProperty(field, descriptor, owner, simpleTypeHolder);
        }

        return prop;
    }
}
