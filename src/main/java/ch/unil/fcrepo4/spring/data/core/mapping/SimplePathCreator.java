package ch.unil.fcrepo4.spring.data.core.mapping;

import ch.unil.fcrepo4.spring.data.core.Constants;

/**
 * Default implementation of {@linkplain PathCreator}, simply concatenates {@code namespace} (if not empty or null) and
 * {@code idPath}.
 *
 * @author gushakov
 */
public class SimplePathCreator implements PathCreator {
    @Override
    public String createPath(String namespace, Class<?> entityType, Class<?> propType, String idPropName, Object idPath) {
        // prepend namespace only if not null or empty
        String fullPath = ((namespace != null && namespace.matches("\\s*")) ? "/" + namespace : "");

        if (idPath instanceof String && !((String) idPath).startsWith("/")) {
            fullPath += "/";
        }

        fullPath += idPath;
        return fullPath;
    }
}
