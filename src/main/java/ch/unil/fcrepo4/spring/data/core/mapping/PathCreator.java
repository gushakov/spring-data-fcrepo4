package ch.unil.fcrepo4.spring.data.core.mapping;

/**
 * @author gushakov
 */
public interface PathCreator {
    String createPath(String namespace, Class<?> entityType, Class<?> propType, String idPropName, Object id);
}
