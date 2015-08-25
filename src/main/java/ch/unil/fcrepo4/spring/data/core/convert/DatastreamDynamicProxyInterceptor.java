package ch.unil.fcrepo4.spring.data.core.convert;

import ch.unil.fcrepo4.spring.data.core.mapping.DatastreamPersistentEntity;
import ch.unil.fcrepo4.spring.data.core.mapping.FedoraPersistentProperty;
import net.bytebuddy.implementation.bind.annotation.Origin;
import net.bytebuddy.implementation.bind.annotation.RuntimeType;
import net.bytebuddy.implementation.bind.annotation.SuperCall;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

    private Object dsBean;

    public DatastreamDynamicProxyInterceptor(String dsPath, DatastreamPersistentEntity<?> dsEntity, FedoraConverter fedoraConverter) {
        this.dsPath = dsPath;
        this.dsEntity = dsEntity;
        this.fedoraConverter = fedoraConverter;
    }

    @RuntimeType
    public Object interceptGetter(@SuperCall Callable<?> delegateCall, @Origin Method method) {
        Object result;
        logger.debug("Intercepted method call to getter: {}", method);
        try {

            FedoraPersistentProperty prop = dsEntity.findProperty(method);

            if (prop != null){

                // call to a getter of a persistent property, load datastream if needed

                if (dsBean == null) {
                    loadDatastream();
                }

                logger.debug("Calling method {} on the datastream bean", method.getName());
                result = method.invoke(dsBean);

            }
            else {
                logger.debug("Delegating call to the method {} to the super object", method.getName());
               result = delegateCall.call();
            }


            return result;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void loadDatastream() {
        logger.debug("Loading datastream from repository for path {}", dsPath);
        dsBean = fedoraConverter.read(dsEntity.getType(), fedoraConverter.fetchDatastream(dsPath));
    }

}
