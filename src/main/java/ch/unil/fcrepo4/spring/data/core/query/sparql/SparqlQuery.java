package ch.unil.fcrepo4.spring.data.core.query.sparql;

import ch.unil.fcrepo4.spring.data.core.query.FedoraQuery;
import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.sparql.algebra.Op;
import com.hp.hpl.jena.sparql.algebra.OpAsQuery;
import com.hp.hpl.jena.sparql.algebra.op.OpBGP;
import com.hp.hpl.jena.sparql.algebra.op.OpFilter;
import com.hp.hpl.jena.sparql.algebra.op.OpProject;
import com.hp.hpl.jena.sparql.core.Var;
import com.hp.hpl.jena.sparql.expr.Expr;

import java.util.Collections;

// code based on examples from https://jena.apache.org/documentation/query/manipulating_sparql_using_arq.html

/**
 * @author gushakov
 */
public class SparqlQuery implements FedoraQuery {

    private Query query;

    public SparqlQuery(Criteria criteria) {
        Op op = new OpBGP(criteria.getBgp());
        for (Expr filter: criteria.getFilters()){
            op = OpFilter.filter(filter, op);
        }
        op = new OpProject(op, Collections.singletonList(Var.alloc(criteria.getProjectionVariableName())));
        this.query = OpAsQuery.asQuery(op);
        this.query.setQuerySelectType();
    }

    @Override
    public String getSerialized() {
        return query.serialize();
    }

    @Override
    public boolean isPaged() {
        return false;
    }

    @Override
    public int getOffset() {
        return 0;
    }

    @Override
    public int getLimit() {
        return 0;
    }

    @Override
    public String toString() {
        return getSerialized();
    }
}
