package ch.unil.fcrepo4.spring.data.core.mapping;

import org.springframework.util.Assert;

import static ch.unil.fcrepo4.spring.data.core.Constants.PATH_SEPARATOR;

/**
 * Default implementation of {@linkplain PathCreator} which simply concatenates {@code namespace} and {@code path} to
 * construct the full path for the Fedora object.
 *
 * @author gushakov
 * @see ch.unil.fcrepo4.spring.data.core.Constants#PATH_SEPARATOR
 */
public class SimplePathCreator implements PathCreator {
    @Override
    public String createPath(final String namespace, final String path) {
        Assert.notNull(namespace);
        Assert.notNull(path);
        String fullPath = PATH_SEPARATOR + namespace;

        if (!path.startsWith(PATH_SEPARATOR)) {
            fullPath += PATH_SEPARATOR;
        }
        fullPath += path;
        return fullPath;
    }
}
