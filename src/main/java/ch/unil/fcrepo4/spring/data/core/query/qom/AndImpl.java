package ch.unil.fcrepo4.spring.data.core.query.qom;


/**
 * @author gushakov
 */
public class AndImpl extends ConstraintImpl implements And {
    private Constraint constraint1;
    private Constraint constraint2;

    public AndImpl(Constraint constraint1, Constraint constraint2) {
        this.constraint1 = constraint1;
        this.constraint2 = constraint2;
    }

    @Override
    public Constraint getConstraint1() {
        return constraint1;
    }

    @Override
    public Constraint getConstraint2() {
        return constraint2;
    }

    @Override
    public String toString() {
        return constraint1 + " AND " + constraint2;
    }

    @Override
    public boolean isSimpleOrConjunctionsOnly() {
        return constraint1.isSimpleOrConjunctionsOnly() && constraint2.isSimpleOrConjunctionsOnly();
    }

}
