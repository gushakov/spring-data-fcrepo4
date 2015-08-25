package ch.unil.fcrepo4.spring.data.core.convert;

import ch.unil.fcrepo4.spring.data.core.mapping.DatastreamContentPersistentProperty;
import ch.unil.fcrepo4.spring.data.core.mapping.DatastreamPersistentEntity;
import ch.unil.fcrepo4.spring.data.core.mapping.FedoraPersistentProperty;
import net.bytebuddy.implementation.bind.annotation.*;
import org.fcrepo.client.FedoraContent;
import org.fcrepo.client.FedoraDatastream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.mapping.PersistentProperty;
import org.springframework.data.mapping.SimplePropertyHandler;

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

    private FedoraDatastream datastream;

    public DatastreamDynamicProxyInterceptor(String dsPath, DatastreamPersistentEntity<?> dsEntity, FedoraConverter fedoraConverter) {
        this.dsPath = dsPath;
        this.dsEntity = dsEntity;
        this.fedoraConverter = fedoraConverter;
    }

    @RuntimeType
    public Object interceptGetter(@This DatastreamDynamicProxy proxy,  @SuperCall Callable<?> delegateCall, @Origin Method method) {
        Object result;
        logger.debug("Intercepted method call to getter: " + method);
        try {

            FedoraPersistentProperty prop = dsEntity.findProperty(method);

            if (prop != null){
                // call to a getter of a persistent property, load datastream if needed
                if (datastream == null){
                    loadDatastream(proxy);
                }

                if (prop instanceof DatastreamContentPersistentProperty){
                    result = datastream.getContent();
                }
                else {
                    logger.debug("Delegating call to the super object");
                    result = delegateCall.call();
                }
            }
            else {
                logger.debug("Delegating call to the super object");
               result = delegateCall.call();
            }


            return result;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void loadDatastream(DatastreamDynamicProxy proxy) {
        logger.debug("Loading datastream from repository for path " + dsPath);
//        datastream = fedoraConverter.readDatastream(dsPath);
//        fedoraConverter.readFedoraResourceProperties(proxy, dsEntity, datastream);
    }


}
