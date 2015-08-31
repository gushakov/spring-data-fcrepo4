package ch.unil.fcrepo4.spring.data.core.mapping;

/**
 * @author gushakov
 */
public interface PathCreator<T, ID> {

    String createPath(String namespace, Class<T> beanType, Class<ID> pathPropType, String pathPropName, ID pathPropValue);

    ID parsePath(String namespace, Class<T> beanType, Class<ID> pathPropType, String pathPropName, String path);

}
