package ch.unil.fcrepo4.spring.data.core.query.qom;

/**
 * @author gushakov
 */
public class DescendantNodeImpl extends ConstraintImpl implements DescendantNode {

    private Selector selector;

    public DescendantNodeImpl(Selector selector) {
        this.selector = selector;
    }

    @Override
    public String getSelectorName() {
        return selector.getSelectorName();
    }

    @Override
    public String getAncestorPath() {
        return "/" + selector.getNamespace();
    }

    @Override
    public String toString() {
        return "ISDESCENDANTNODE(" + getSelectorName() + ", '" + getAncestorPath() + "')";
    }

    @Override
    public boolean isSimpleOrConjunctionsOnly() {
        return true;
    }
}
