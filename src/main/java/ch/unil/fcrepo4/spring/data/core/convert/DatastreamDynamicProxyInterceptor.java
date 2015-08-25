package ch.unil.fcrepo4.spring.data.core.convert;

import ch.unil.fcrepo4.spring.data.core.mapping.DatastreamPersistentEntity;
import ch.unil.fcrepo4.spring.data.core.mapping.FedoraPersistentProperty;
import net.bytebuddy.implementation.bind.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.concurrent.Callable;

/**
 * @author gushakov
 */
public class DatastreamDynamicProxyInterceptor {

    private static final Logger logger = LoggerFactory.getLogger(DatastreamDynamicProxyInterceptor.class);

    private String dsPath;

    private DatastreamPersistentEntity<?> dsEntity;

    private FedoraConverter fedoraConverter;

    private Object targetDsBean;

    public DatastreamDynamicProxyInterceptor(String dsPath, DatastreamPersistentEntity<?> dsEntity, FedoraConverter fedoraConverter) {
        this.dsPath = dsPath;
        this.dsEntity = dsEntity;
        this.fedoraConverter = fedoraConverter;
    }

    @RuntimeType
    @BindingPriority(1)
    public Object interceptCallForTargetDatastreamBean(@Origin Method method){
        logger.debug("Getting target datastream bean");
        if (targetDsBean == null) {
            loadTargetDatastreamBean();
        }
        return targetDsBean;
    }


    @RuntimeType
    @BindingPriority(2)
    public Object interceptGetter(@SuperCall Callable<?> delegateCall, @Origin Method method) {
        logger.debug("Intercepted method call to getter: {}", method);

        FedoraPersistentProperty prop = dsEntity.findGetterProperty(method);
        if (prop != null) {

            if (targetDsBean == null) {
                loadTargetDatastreamBean();
            }

            logger.debug("Calling method {} on the datastream bean", method.getName());
            try {
                return method.invoke(targetDsBean);
            } catch (IllegalAccessException | InvocationTargetException e) {
                throw new RuntimeException(e);
            }

        } else {
            logger.debug("Delegating call to the method {} to the super object", method.getName());
            try {
                return delegateCall.call();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }

    @RuntimeType
    @BindingPriority(3)
    public void interceptSetter(@SuperCall Callable<?> delegateCall, @Origin Method method, @AllArguments Object[] args) {
        logger.debug("Intercepted method call to setter: {}", method);

        try {
            FedoraPersistentProperty prop = dsEntity.findSetterProperty(method);
            if (prop != null) {

                // trying to access a persistent property, load and convert datastream object

                if (targetDsBean == null) {
                    loadTargetDatastreamBean();
                }

                logger.debug("Calling method {} on the datastream bean", method.getName());
                method.invoke(targetDsBean, args);

            } else {
                logger.debug("Delegating call to the method {} to the super object", method.getName());
                delegateCall.call();
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void loadTargetDatastreamBean() {
        logger.debug("Loading datastream from repository for path {}", dsPath);
        targetDsBean = fedoraConverter.read(dsEntity.getType(), fedoraConverter.fetchDatastream(dsPath));
    }

}
