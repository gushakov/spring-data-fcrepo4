package ch.unil.fcrepo4.spring.data.core.query.sparql;

import ch.unil.fcrepo4.spring.data.core.convert.RdfDatatypeConverter;
import ch.unil.fcrepo4.spring.data.core.convert.XsdDatatypeConverter;
import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.sparql.core.Var;
import com.hp.hpl.jena.sparql.expr.aggregate.Aggregator;
import com.hp.hpl.jena.sparql.syntax.ElementFilter;
import com.hp.hpl.jena.sparql.syntax.ElementGroup;
import com.hp.hpl.jena.sparql.syntax.ElementTriplesBlock;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * @author gushakov
 */
public class SelectQueryBuildContextHolder implements SparqlQueryBuildContext, SelectQueryBuilder {

    private RdfDatatypeConverter rdfDatatypeConverter;

    private PrefixMap prefixMap;

    private Aggregator countAggregator;

    private String resultVarName;

    private List<ElementTriplesBlock> fromBlocks;

    private ElementFilter whereFilter;

    public SelectQueryBuildContextHolder() {
        init();
    }

    public SelectQueryBuildContextHolder(PrefixMap prefixMap) {
        this.prefixMap = prefixMap;
        init();
    }

    public SelectQueryBuildContextHolder(RdfDatatypeConverter rdfDatatypeConverter) {
        this.rdfDatatypeConverter = rdfDatatypeConverter;
        init();
    }

    public SelectQueryBuildContextHolder(PrefixMap prefixMap, RdfDatatypeConverter rdfDatatypeConverter) {
        this.rdfDatatypeConverter = rdfDatatypeConverter;
        this.prefixMap = prefixMap;
        init();
    }

    private void init() {
        if (prefixMap == null) {
            prefixMap = new PrefixMap();
        }

        if (fromBlocks == null) {
            fromBlocks = new ArrayList<>();
        }

        if (rdfDatatypeConverter == null) {
            rdfDatatypeConverter = new XsdDatatypeConverter();
        }
    }

    @Override
    public void setDatatypeConverter(RdfDatatypeConverter converter) {
        this.rdfDatatypeConverter = converter;
    }

    @Override
    public RdfDatatypeConverter getDatatypeConverter() {
        return rdfDatatypeConverter;
    }

    @Override
    public PrefixMap getPrefixMap() {
        return prefixMap;
    }

    @Override
    public Aggregator getCountAggregator() {
        return countAggregator;
    }

    @Override
    public void setCountAggregator(Aggregator aggregator) {
        this.countAggregator = aggregator;
    }

    @Override
    public String getResultVarName() {
        return resultVarName;
    }

    @Override
    public void setResultVarName(String resultVarName) {
        this.resultVarName = resultVarName;
    }

    @Override
    public List<ElementTriplesBlock> getFromBlocks() {
        return fromBlocks;
    }

    @Override
    public void addFromBlock(ElementTriplesBlock fromBlock) {
        fromBlocks.add(fromBlock);
    }

    @Override
    public ElementFilter getWhereFilter() {
        return whereFilter;
    }

    @Override
    public void setWhereFilter(ElementFilter whereFilter) {
        this.whereFilter = whereFilter;
    }

    @Override
    public Query build() {
        return null;

    }
}
