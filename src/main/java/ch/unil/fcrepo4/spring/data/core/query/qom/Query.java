package ch.unil.fcrepo4.spring.data.core.query.qom;

/**
 * @author gushakov
 */
public interface Query extends javax.jcr.query.Query {

    int getOffset();

    int getRowLimit();

}
