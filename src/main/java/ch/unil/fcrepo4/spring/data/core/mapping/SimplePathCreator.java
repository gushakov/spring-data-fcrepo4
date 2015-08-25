package ch.unil.fcrepo4.spring.data.core.mapping;

/**
 * @author gushakov
 */
public class SimplePathCreator implements PathCreator {
    @Override
    public String createPath(String namespace, Class<?> beanType, Class<?> pathPropType, String pathPropName, Object pathPropValue) {
        // prepend namespace only if not null or empty
        String path = (namespace != null && !namespace.matches("\\s*")) ? "/" + namespace : "";

        if (pathPropValue instanceof String && !((String) pathPropValue).startsWith("/")) {
            path += "/";
        }

        path += pathPropValue;
        return path;
    }

    @Override
    public Object parsePath(String namespace, Class<?> beanType, Class<?> pathPropType, String pathPropName, String path) {
        // remove namespace if not null or empty
        return (namespace != null && !namespace.matches("\\s*")) ? path.replaceFirst("/" + namespace, "") : path;
    }

}
