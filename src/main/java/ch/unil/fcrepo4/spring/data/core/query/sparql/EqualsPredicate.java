package ch.unil.fcrepo4.spring.data.core.query.sparql;

import com.hp.hpl.jena.sparql.expr.E_Equals;
import com.hp.hpl.jena.sparql.expr.Expr;

/**
 * @author gushakov
 */
public class EqualsPredicate extends Predicate {

    public EqualsPredicate(Variable variable, Value value) {
        super(variable, value);
    }

    @Override
    protected Expr makeExpression() {
        return new E_Equals(getVariable().getExpression(), getValue().getExpression());
    }

}
