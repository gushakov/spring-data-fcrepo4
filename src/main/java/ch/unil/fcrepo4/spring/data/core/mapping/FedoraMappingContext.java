package ch.unil.fcrepo4.spring.data.core.mapping;

import ch.unil.fcrepo4.spring.data.core.mapping.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.mapping.context.AbstractMappingContext;
import org.springframework.data.mapping.model.SimpleTypeHolder;
import org.springframework.data.util.TypeInformation;

import java.beans.PropertyDescriptor;
import java.lang.reflect.Field;
import java.util.stream.Stream;

/**
 * @author gushakov
 */
public class FedoraMappingContext extends AbstractMappingContext<GenericFedoraPersistentEntity<?>, FedoraPersistentProperty> {
    private static final Logger logger = LoggerFactory.getLogger(FedoraMappingContext.class);

    @Override
    protected <T> GenericFedoraPersistentEntity<?> createPersistentEntity(TypeInformation<T> typeInformation) {
        final GenericFedoraPersistentEntity<?> entity;
        if (typeInformation.getType().getAnnotation(FedoraObject.class) != null) {
            logger.debug("Creating Fedora object persistent entity for type {}", typeInformation.getType().getSimpleName());
            entity = new FedoraObjectPersistentEntity<>(typeInformation);
        } else if (hasBinary(typeInformation)) {
            logger.debug("Creating datastream persistent entity for type {}", typeInformation.getType().getSimpleName());
            entity = new DatastreamPersistentEntity<>(typeInformation);
        } else {
            entity = new GenericFedoraPersistentEntity<>(typeInformation);
        }
        return entity;
    }

    @Override
    protected FedoraPersistentProperty createPersistentProperty(Field field, PropertyDescriptor descriptor, GenericFedoraPersistentEntity<?> owner, SimpleTypeHolder simpleTypeHolder) {
        final FedoraPersistentProperty prop;

        if (field != null && field.getAnnotation(Path.class) != null) {
            logger.debug("Found " + Path.class.getSimpleName() +
                    " annotated property on field <{}> of entity {}", field.getName(), owner.getType().getName());
            prop = new PathPersistentProperty(field, descriptor, owner, simpleTypeHolder);
        }  else if (field != null && field.getAnnotation(Created.class) != null) {
            logger.debug("Found " + Created.class.getSimpleName() +
                    " annotated property on field <{}> of entity {}", field.getName(), owner.getType().getName());
            prop = new CreatedPersistentProperty(field, descriptor, owner, simpleTypeHolder);
        } else if (field != null && field.getAnnotation(Binary.class) != null) {
            logger.debug("Found " + Binary.class.getSimpleName() +
                    " annotated property on field <{}> of entity {}", field.getName(), owner.getType().getName());
            prop = new BinaryPersistentProperty(field, descriptor, owner, simpleTypeHolder);
        } else if (field != null && field.getAnnotation(Property.class) != null
                && simpleTypeHolder.isSimpleType(field.getType())) {
            logger.debug("Found " + Property.class.getSimpleName() +
                    " annotated property on field <{}> of entity {}", field.getName(), owner.getType().getName());
            prop = new SimpleFedoraResourcePersistentProperty(field, descriptor, owner, simpleTypeHolder);
        } else if (field != null && field.getAnnotation(Datastream.class) != null) {
            logger.debug("Found association: Fedora object to Datastream, field <{}> of entity {}", field.getName(), owner.getType().getName());
            prop = new DatastreamPersistentProperty(field, descriptor, owner, simpleTypeHolder, (DatastreamPersistentEntity) getPersistentEntity(field.getType()));
        }
        else {
             // all other properties are transient
            prop = new TransientPersistentProperty(field, descriptor, owner, simpleTypeHolder);
        }

        return prop;
    }

    private boolean hasBinary(TypeInformation<?> typeInformation){
       return Stream.of(typeInformation.getType().getDeclaredFields())
                .filter(field -> field.getAnnotation(Binary.class) != null).findAny().isPresent();
    }

}
