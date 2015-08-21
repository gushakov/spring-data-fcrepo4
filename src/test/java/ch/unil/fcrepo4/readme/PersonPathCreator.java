package ch.unil.fcrepo4.readme;

import ch.unil.fcrepo4.spring.data.core.mapping.PathCreator;

/**
 * @author gushakov
 */
public class PersonPathCreator implements PathCreator {
    @Override
    public String createPath(String namespace, Class<?> entityType, Class<?> propType, String idPropName, Object id) {
        return "/" + namespace + "/" + entityType.getSimpleName().toLowerCase() +  "/" + idPropName + "/" + id;
    }
}
