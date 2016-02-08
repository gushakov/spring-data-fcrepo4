package ch.unil.fcrepo4.spring.data.core.query.qom;

import javax.jcr.query.qom.DynamicOperand;
import javax.jcr.query.qom.QueryObjectModelConstants;
import javax.jcr.query.qom.StaticOperand;

/**
 * @author gushakov
 */
public class ComparisonImpl extends ConstraintImpl implements Comparison {
    private DynamicOperand dynamicOperand;

    private String operator;

    private StaticOperand staticOperand;

    public ComparisonImpl(DynamicOperand dynamicOperand, String operator, StaticOperand staticOperand) {
        this.dynamicOperand = dynamicOperand;
        this.operator = operator;
        this.staticOperand = staticOperand;
    }

    @Override
    public DynamicOperand getOperand1() {
        return dynamicOperand;
    }

    @Override
    public String getOperator() {
        return operator;
    }

    @Override
    public StaticOperand getOperand2() {
        return staticOperand;
    }

    @Override
    public String toString() {
        String text = dynamicOperand.toString() + " ";

        switch (operator) {
            case QueryObjectModelConstants.JCR_OPERATOR_EQUAL_TO:
                text += "=";
                break;
            default:
                throw new UnsupportedOperationException("Operator " + operator + " is not supported yet.");
        }

        text += " " + staticOperand;
        return text;

    }

    @Override
    public boolean isSimpleOrConjunctionsOnly() {
        return true;
    }
}
