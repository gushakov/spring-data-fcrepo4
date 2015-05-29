package ch.unil.fcrepo4.spring.data.core.mapping;

import org.springframework.util.Assert;

/**
 * @author gushakov
 */
public class DefaultPathCreator implements PathCreator {
    @Override
    public String createPath(final String namespace, final String uuid) {
        Assert.notNull(namespace);
        Assert.notNull(uuid);
        return "/" + namespace + "/" + uuid.replaceAll("-", "/");
    }
}
