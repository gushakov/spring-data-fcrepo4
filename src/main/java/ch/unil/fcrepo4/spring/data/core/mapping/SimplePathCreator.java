package ch.unil.fcrepo4.spring.data.core.mapping;

/**
 * @author gushakov
 */
public class SimplePathCreator<T, ID> implements PathCreator<T, ID> {
    @Override
    public String createPath(String namespace, Class<T> beanType, Class<ID> pathPropType, String pathPropName, ID pathPropValue) {
        // prepend namespace only if not null or empty
        String path = (namespace != null && !namespace.matches("\\s*")) ? "/" + namespace : "";

        if ((pathPropValue instanceof String && !((String) pathPropValue).startsWith("/"))
                || !(pathPropValue instanceof String)) {
            path += "/";
        }

        path += pathPropValue;
        return path;
    }

    @Override
    public Object parsePath(String namespace, Class<T> beanType, Class<ID> pathPropType, String pathPropName, String path) {
        // remove namespace if not null or empty
        return (namespace != null && !namespace.matches("\\s*")) ? path.replaceFirst("/" + namespace, "") : path;
    }

}
