package ch.unil.fcrepo4.spring.data.core.query.sparql;

import ch.unil.fcrepo4.spring.data.core.query.FedoraQuery;
import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.Syntax;
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

    private boolean paged = false;

    private long offset = 0L;

    private long limit = Long.MAX_VALUE;

    public SparqlQuery(Criteria criteria) {
        this.query = buildQuery(criteria);
    }

    public SparqlQuery(Criteria criteria, long offset, long limit) {
        this.offset = offset;
        this.limit = limit;
        paged = true;
        this.query = buildQuery(criteria);
    }

    private Query buildQuery(Criteria criteria) {
        Op op = new OpBGP(criteria.buildBgp());
        for (Expr filter: criteria.getFilters()){
            op = OpFilter.filter(filter, op);
        }
        op = new OpProject(op, Collections.singletonList(Var.alloc(criteria.getProjectionVariableName())));
        Query sparqlQuery = OpAsQuery.asQuery(op);
        sparqlQuery.setQuerySelectType();
        if (isPaged()){
            sparqlQuery.setOffset(offset);
            sparqlQuery.setLimit(limit);
        }
        return sparqlQuery;
    }

    @Override
    public String getSerialized() {
        return query.serialize();
    }

    @Override
    public boolean isPaged() {
        return paged;
    }

    @Override
    public long getOffset() {
        return offset;
    }

    @Override
    public long getLimit() {
        return limit;
    }

    @Override
    public String toString() {
        return getSerialized();
    }
}
