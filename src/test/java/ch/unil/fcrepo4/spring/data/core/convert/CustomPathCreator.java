package ch.unil.fcrepo4.spring.data.core.convert;

import ch.unil.fcrepo4.spring.data.core.mapping.PathCreator;
import org.apache.commons.lang3.StringUtils;

/**
 * Custom implementation of {@linkplain PathCreator}, assumes that {@code namespace} is not blank and that {@code id}
 * value value is a long with at least 7 digits. Splits {@code id} value into three groups of digits and concatenates
 * them to {@code namespace} to create a path.
 *
 * @author gushakov
 */
public class CustomPathCreator<T> implements PathCreator<T, Long> {

    @Override
    public String createPath(String namespace, Class<T> beanType, Class<Long> pathPropType, String pathPropName, Long pathPropValue) {
        String value = pathPropValue.toString();
        String part1 = value.substring(0, 3);
        String part2 = value.substring(3, 6);
        String part3 = value.substring(6);
        return "/" + namespace + "/" + part1 + "/" + part2 + "/" + part3;
    }

    @Override
    public Long parsePath(String namespace, Class<T> beanType, Class<Long> pathPropType, String pathPropName, String path) {
        return Long.parseLong(StringUtils.removeStart(StringUtils.remove(path, "/"), namespace));
    }
}
