package ch.unil.fcrepo4.spring.data.core.query.sparql;

import com.hp.hpl.jena.sparql.expr.Expr;

/**
 * @author gushakov
 */
public interface FromBlock extends SelectQueryBuilder {

    FromBlock and(String varName, String predicateUri, Object value);

    WhereBlock where(Expr filterExpr);

}
