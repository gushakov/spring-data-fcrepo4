package ch.unil.fcrepo4.spring.data.core.mapping;

/**
 * @author gushakov
 */
public interface PathCreator {

    String createPath(String namespace, Class<?> beanType, Class<?> pathPropType, String pathPropName, Object pathPropValue);

    Object parsePath(String namespace, Class<?> beanType, Class<?> pathPropType, String pathPropName, String path);
}
