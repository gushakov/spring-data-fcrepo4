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
    public String createPath(final String namespace, final String path) {
        Assert.notNull(namespace);
        Assert.notNull(path);
        String fullPath = "/" + namespace;

        if (!path.startsWith("/")) {
            fullPath += "/";
        }
        fullPath += path;
        return fullPath;
    }
}
