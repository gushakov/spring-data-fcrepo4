package ch.unil.fcrepo4.spring.data.core.mapping;

/**
 * @author gushakov
 */
public interface PathCreator {
    String createPath(String namespace, String path);
}
