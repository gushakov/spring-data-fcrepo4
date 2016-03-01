package ch.unil.fcrepo4.spring.data.core.convert;

import ch.unil.fcrepo4.spring.data.core.mapping.*;
import net.bytebuddy.implementation.bind.annotation.AllArguments;
import net.bytebuddy.implementation.bind.annotation.Origin;
import net.bytebuddy.implementation.bind.annotation.RuntimeType;
import org.fcrepo.client.FedoraDatastream;
import org.fcrepo.client.FedoraResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.mapping.Association;
import org.springframework.data.mapping.PersistentProperty;
import org.springframework.data.mapping.PersistentPropertyAccessor;

import java.lang.reflect.Method;
import java.util.Arrays;
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

    @RuntimeType
    public FedoraPersistentEntity<?> __getBeanEntity() {
        return entity;
    }

    @RuntimeType
    public PersistentPropertyAccessor __getPropertyAccessor() {
        return entity.getPropertyAccessor(bean);
    }

    @RuntimeType
    public void doWithProperties(SimplePropertyAndValueHandler propertyAndValueHandler, boolean withUpdatedPropertiesOnly) {
        PersistentPropertyAccessor propertyAccessor = entity.getPropertyAccessor(bean);
        entity.doWithProperties((PersistentProperty<?> property) -> {
            if (withUpdatedPropertiesOnly) {
                if (updatedProperties.contains(property)) {
                    propertyAndValueHandler.doWithPersistentPropertyAndValue(property, propertyAccessor.getProperty(property));
                }
            } else {
                propertyAndValueHandler.doWithPersistentPropertyAndValue(property, propertyAccessor.getProperty(property));
            }
        });
    }



    @RuntimeType
    public Object interceptGetter(@Origin Method getter) {

        logger.debug("Intercepted getter: {}", getter);

        return null;

    }

    @RuntimeType
    public void interceptSetter(@Origin Method setter, @AllArguments Object[] allArguments) {
        logger.debug("Intercepted setter: {}", setter);
    }

    /*
        @RuntimeType
    public Object intercept(@Origin Method getterOrSetter, @AllArguments Object[] allArguments) {

        logger.debug("Intercepted call {} with arguments {}", getterOrSetter, Arrays.toString(allArguments));

        // check if we are accessing an inverse of an association
        Optional<Association<? extends PersistentProperty<?>>> assoc = entity.findAssociationForGetterOrSetter(getterOrSetter);

        PersistentProperty<?> prop = null;

        if (assoc.isPresent()) {
            prop = assoc.get().getInverse();

            // check if this association is for a datastream
            if (prop instanceof DatastreamPersistentProperty) {
                DatastreamPersistentProperty dsProp = (DatastreamPersistentProperty) prop;

                String dsPath = fedoraConverter.getFedoraObjectPath(bean) + "/" + dsProp.getName();

                if (fedoraConverter.exists(dsPath)){
                    Object dsBean =  fedoraConverter.read(dsProp.getType(), fedoraConverter.fetchDatastream(dsPath));
                    entity.getPropertyAccessor(bean).setProperty(dsProp, dsBean);
                }

            }

            // TODO: implement the case for relations
        } else {
            // check if we are trying to access the binary content of a datastream

            Optional<PersistentProperty<?>> optional = entity.findPropertyForGetterOrSetter(getterOrSetter);

            if (optional.isPresent()) {
                prop = optional.get();
                if (prop instanceof BinaryPersistentProperty) {
                    fedoraConverter.readDatastreamContent(bean, (DatastreamPersistentEntity<?>) entity, (FedoraDatastream) fedoraResource);
                }
            }
        }

        if (prop != null && isSetter(getterOrSetter)) {
            updatedProperties.add(prop);
        }

        try {
            return getterOrSetter.invoke(bean, allArguments);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
*/

    private boolean isSetter(Method getterOrSetter) {
        return getterOrSetter.getName().matches("^set\\p{Lu}.*$")
                && getterOrSetter.getParameterCount() == 1
                && getterOrSetter.getReturnType().equals(void.class);
    }
}
