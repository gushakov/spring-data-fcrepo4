package ch.unil.fcrepo4.spring.data.core.query.sparql;

import ch.unil.fcrepo4.spring.data.core.convert.RdfDatatypeConverter;
import com.hp.hpl.jena.sparql.expr.aggregate.AggregatorFactory;

/**
 * @author gushakov
 */
public class SparqlSelectQueryBuilder extends AbstractSparqlSelectQueryBuilder implements SelectBlock {

    public SparqlSelectQueryBuilder() {
        super(new SelectQueryBuildContextHolder());
    }

    public SparqlSelectQueryBuilder(SparqlQueryBuildContext context) {
        super(context);
    }

    public SparqlSelectQueryBuilder(PrefixMap prefixMap) {
        super(new SelectQueryBuildContextHolder(prefixMap));
    }

    public SparqlSelectQueryBuilder(PrefixMap prefixMap, RdfDatatypeConverter rdfDatatypeConverter) {
        super(new SelectQueryBuildContextHolder(prefixMap, rdfDatatypeConverter));
    }

    @Override
    public SelectBlock select(String varName) {
        context.setResultVarName(varName);
        return this;
    }

    @Override
    public SelectBlock count(boolean distinct) {
        context.setCountAggregator(AggregatorFactory.createCount(distinct));
        return this;
    }

    @Override
    public FromBlock from(String varName, String predicateUri, Object value) {
        return new FromBlockBuilder(context, new BgpFragmentBuilder(context, varName, predicateUri, value));
    }

}
