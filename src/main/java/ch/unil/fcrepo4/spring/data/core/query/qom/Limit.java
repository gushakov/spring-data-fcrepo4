package ch.unil.fcrepo4.spring.data.core.query.qom;

/**
 * Copied from org.modeshape.jcr.api.query.qom.Limit
 * @author gushakov
 */
public interface Limit {
    int getOffset();

    int getRowLimit();

    boolean isUnlimited();

    boolean isOffset();
}
