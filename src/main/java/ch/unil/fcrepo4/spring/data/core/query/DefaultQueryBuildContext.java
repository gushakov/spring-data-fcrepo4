package ch.unil.fcrepo4.spring.data.core.query;

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

    private PrefixMap prefixMap;

    private Aggregator countAggregator;

    private String resultVarName;

    private List<ElementTriplesBlock> fromBlocks;

    private ElementFilter whereFilter;

    public DefaultQueryBuildContext() {
        this.prefixMap = new PrefixMap();
        this.fromBlocks = new ArrayList<>();
    }

    public DefaultQueryBuildContext(PrefixMap prefixMap) {
        this.prefixMap = prefixMap;
        this.fromBlocks = new ArrayList<>();
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
