package ch.unil.fcrepo4.spring.data.core.mapping;

import org.springframework.util.Assert;

/**
 * Default implementation of {@linkplain PathCreator} which simply concatenates {@code namespace} and {@code path} to
 * construct the full path for the Fedora object.
 *
 * @author gushakov
 */
public class SimplePathCreator implements PathCreator {
    @Override
    public String createPath(String namespace, Class<?> entityType, Class<?> propType, String idPropName, Object id) {
        Assert.notNull(namespace);
        Assert.notNull(id);
        String fullPath = "/" + namespace;

        if (id instanceof String && !((String) id).startsWith("/")) {
            fullPath += "/";
        }

        fullPath += id;
        return fullPath;
    }
}
