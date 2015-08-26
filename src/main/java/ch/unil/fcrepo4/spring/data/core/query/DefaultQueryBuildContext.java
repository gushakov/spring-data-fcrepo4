package ch.unil.fcrepo4.spring.data.core.query;

import ch.unil.fcrepo4.spring.data.core.convert.RdfDatatypeConverter;
import ch.unil.fcrepo4.spring.data.core.convert.XsdDatatypeConverter;
import com.hp.hpl.jena.sparql.expr.aggregate.Aggregator;
import com.hp.hpl.jena.sparql.syntax.ElementFilter;
import com.hp.hpl.jena.sparql.syntax.ElementTriplesBlock;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * @author gushakov
 */
public class DefaultQueryBuildContext implements QueryBuildContext {

    private RdfDatatypeConverter rdfDatatypeConverter;

    private PrefixMap prefixMap;

    private Aggregator countAggregator;

    private String resultVarName;

    private List<ElementTriplesBlock> fromBlocks;

    private ElementFilter whereFilter;

    public DefaultQueryBuildContext() {
        init();
    }

    public DefaultQueryBuildContext(PrefixMap prefixMap) {
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
    public Optional<Aggregator> getCountAggregator() {
        return Optional.ofNullable(countAggregator);
    }

    @Override
    public void setCountAggregator(Aggregator aggregator) {
        this.countAggregator = aggregator;
    }

    @Override
    public Optional<String> getResultVarName() {
        return Optional.ofNullable(resultVarName);
    }

    @Override
    public void setResultVarName(String resultVarName) {
        this.resultVarName = resultVarName;
    }

    @Override
    public Optional<List<ElementTriplesBlock>> getFromBlocks() {
        return Optional.ofNullable(fromBlocks);
    }

    @Override
    public void addFromBlock(ElementTriplesBlock fromBlock) {
        fromBlocks.add(fromBlock);
    }

    @Override
    public Optional<ElementFilter> getWhereFilter() {
        return Optional.ofNullable(whereFilter);
    }

    @Override
    public void setWhereFilter(ElementFilter whereFilter) {
        this.whereFilter = whereFilter;
    }
}
