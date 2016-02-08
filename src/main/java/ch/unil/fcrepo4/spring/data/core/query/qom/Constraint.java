package ch.unil.fcrepo4.spring.data.core.query.qom;

/**
 * @author gushakov
 */
public interface Constraint extends javax.jcr.query.qom.Constraint {

    boolean isSimpleOrConjunctionsOnly();

}
