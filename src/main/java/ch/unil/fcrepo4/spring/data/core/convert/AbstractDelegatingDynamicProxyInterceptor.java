package ch.unil.fcrepo4.spring.data.core.convert;

import net.bytebuddy.implementation.bind.annotation.AllArguments;
import net.bytebuddy.implementation.bind.annotation.BindingPriority;
import net.bytebuddy.implementation.bind.annotation.Origin;
import net.bytebuddy.implementation.bind.annotation.RuntimeType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * @author gushakov
 */
public abstract class AbstractDelegatingDynamicProxyInterceptor<T> {

    private static final Logger logger = LoggerFactory.getLogger(AbstractDelegatingDynamicProxyInterceptor.class);

    protected String path;

    protected Class<T> delegateType;

    protected FedoraConverter fedoraConverter;

    protected T delegate;

    public AbstractDelegatingDynamicProxyInterceptor(String path, Class<T> delegateType, FedoraConverter fedoraConverter) {
        this.path = path;
        this.delegateType = delegateType;
        this.fedoraConverter = fedoraConverter;
    }


    @RuntimeType
    @BindingPriority(2)
    public Object interceptGetter(@Origin Method method, @AllArguments String[] args) {
        logger.debug("Intercepted getter {}", method.getName());

        Object result;

        if (delegate == null) {
            loadProxy();
        }

        if (method.getName().equals(DelegatingDynamicProxy.DELEGATE_GETTER_NAME)) {
            logger.debug("Getting the delegate object");
            result = delegate;
        } else {

            try {
                result = method.invoke(delegate, args);
            } catch (IllegalAccessException | InvocationTargetException e) {
                throw new IllegalStateException("Cannot invoke method " + method.getName() + " on proxy delegate instance " + delegate);
            }
        }

        return result;
    }

    @RuntimeType
    @BindingPriority(1)
    public void interceptSetter(@Origin Method method, @AllArguments String[] args) {
        logger.debug("Intercepted setter {}", method.getName());
        if (delegate == null) {
            loadProxy();
        }
        try {

            method.invoke(delegate, args);
        } catch (IllegalAccessException | InvocationTargetException e) {
            throw new IllegalStateException("Cannot invoke method " + method.getName() + " on proxy delegate instance " + delegate);
        }
    }


    public abstract void loadProxy();

}
