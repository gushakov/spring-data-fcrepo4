package ch.unil.fcrepo4.spring.data.core.convert;

import ch.unil.fcrepo4.spring.data.core.mapping.PathCreator;

/**
 * @author gushakov
 */
public class ConcatenatingPathCreator implements PathCreator {
    @Override
    public String createPath(String namespace, String uuid) {
        return namespace + "/" +uuid;
    }
}
