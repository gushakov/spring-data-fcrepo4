package ch.unil.fcrepo4.spring.data.core.convert;

import ch.unil.fcrepo4.client.FedoraDatastream;
import ch.unil.fcrepo4.client.FedoraException;
import ch.unil.fcrepo4.client.FedoraObject;
import ch.unil.fcrepo4.client.FedoraResource;
import ch.unil.fcrepo4.spring.data.core.mapping.*;
import net.bytebuddy.implementation.bind.annotation.Argument;
import net.bytebuddy.implementation.bind.annotation.Origin;
import net.bytebuddy.implementation.bind.annotation.RuntimeType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.mapping.Association;
import org.springframework.data.mapping.PersistentProperty;
import org.springframework.data.mapping.PersistentPropertyAccessor;

import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

/**
 * @author gushakov
 */
public class DynamicBeanProxyInterceptor {

    private static final Logger logger = LoggerFactory.getLogger(DynamicBeanProxyInterceptor.class);

    private Object bean;

    private FedoraPersistentEntity<?> entity;

    private FedoraResource fedoraResource;

    private FedoraConverter fedoraConverter;

    private Set<PersistentProperty<?>> updatedProperties;

    public DynamicBeanProxyInterceptor(Object bean, FedoraPersistentEntity<?> entity, FedoraResource fedoraResource, FedoraConverter fedoraConverter) {
        this.bean = bean;
        this.entity = entity;
        this.fedoraResource = fedoraResource;
        this.fedoraConverter = fedoraConverter;
        this.updatedProperties = new HashSet<>();
    }

    public Object __getBean() {
        return bean;
    }

    public FedoraPersistentEntity __getBeanEntity() {
        return entity;
    }

    public PersistentPropertyAccessor __getPropertyAccessor() {
        return entity.getPropertyAccessor(bean);
    }

    public boolean __isPropertyUpdated(PersistentProperty property) {
        return property != null && updatedProperties.contains(property);
    }

    @RuntimeType
    public Object interceptGetter(@Origin Method getter) {

        // find the property of the bean we are trying to access
        PersistentProperty<?> property = findProperty(getter);
        if (property == null) {
            throw new IllegalStateException("Persistent property is null for entity " + entity + " and method " + getter);
        }

        logger.debug("Intercepted getter: {} for property: {}", getter, property);


        // if there is a value for the property in the bean, just return it
        Object value = __getPropertyAccessor().getProperty(property);
        if (value != null) {
            return value;
        }

        // see if we are accessing a datastream
        if (property instanceof DatastreamPersistentProperty) {
            DatastreamPersistentProperty dsProp = (DatastreamPersistentProperty) property;
            return fedoraConverter.readDatastream(bean, entity, dsProp);
        }

        // check if we are trying to access the binary content of a datastream
        if (property instanceof BinaryPersistentProperty) {
            return fedoraConverter.readDatastreamContent(bean, (DatastreamPersistentEntity<?>) entity, (FedoraDatastream) fedoraResource);
        }

        // read Fedora object for relation properties
        if (property instanceof RelationPersistentProperty){
            RelationPersistentProperty relProp = (RelationPersistentProperty) property;
            try {
                final String relUri = fedoraResource.getPropertyLiteralValueOrUri(relProp.getUri());
                final FedoraObject relFedoraObject = fedoraConverter.getFedoraObject(fedoraConverter.getFedoraResourcePath(relUri));
                final Object relBean = fedoraConverter.read(relProp.getType(), relFedoraObject);
                __getPropertyAccessor().setProperty(relProp, relBean);
                return relBean;

            } catch (FedoraException e) {
                throw new IllegalStateException(e);
            }
        }

        throw new IllegalStateException("Cannot intercept getter for property of type " + property.getType());
    }

    @RuntimeType
    public void interceptSetter(@Origin Method setter, @Argument(0) Object argument) {

        // find the property of the bean we are trying to access
        PersistentProperty<?> property = findProperty(setter);
        if (property == null) {
            throw new IllegalStateException("Persistent property is null for entity " + entity + " and method " + setter);
        }

        logger.debug("Intercepted setter: {} for property: {} with argument: {}", setter, property, argument);

        // add property to the set of updated properties
        updatedProperties.add(property);

        __getPropertyAccessor().setProperty(property, argument);

    }

    private PersistentProperty<?> findProperty(Method getterOrSetter) {

        Optional<Association<? extends PersistentProperty<?>>> assoc = entity.findAssociationForGetterOrSetter(getterOrSetter);
        if (assoc.isPresent()) {
            return assoc.get().getInverse();
        }

        Optional<PersistentProperty<?>> prop = entity.findPropertyForGetterOrSetter(getterOrSetter);
        if (prop.isPresent()) {
            return prop.get();
        }

        return null;

    }
}
