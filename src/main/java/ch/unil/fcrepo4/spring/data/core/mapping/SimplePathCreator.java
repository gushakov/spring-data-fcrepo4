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
//        String path = (namespace != null && !namespace.matches("\\s*")) ? namespace : "";

        path += pathPropValue;
        return path;
    }

    @Override
    public ID parsePath(String namespace, Class<T> beanType, Class<ID> pathPropType, String pathPropName, String path) {
        // remove namespace if not null or empty
        String idPath = (namespace != null && !namespace.matches("\\s*")) ? path.replaceFirst("/" + namespace + "/", "") : path;
        return deserializeId(idPath, pathPropType);
    }

    @SuppressWarnings("unchecked")
    protected ID deserializeId(String idPath, Class<ID> pathPropType) {
        Object pathPropValue;
        if (pathPropType.equals(Integer.class)) {
            pathPropValue = pathPropType.cast(Integer.parseInt(idPath));
        } else if (pathPropType.equals(int.class)) {
            pathPropValue = Integer.parseInt(idPath);
        } else if (pathPropType.equals(Long.class)) {
            pathPropValue = pathPropType.cast(Long.parseLong(idPath));
        } else if (pathPropType.equals(long.class)) {
            pathPropValue = Long.parseLong(idPath);
        } else if (pathPropType.equals(String.class)) {
            pathPropValue = pathPropType.cast(idPath);
        } else {
            throw new RuntimeException("Unknown path property type " + pathPropType);
        }
        return (ID) pathPropValue;
    }

}
