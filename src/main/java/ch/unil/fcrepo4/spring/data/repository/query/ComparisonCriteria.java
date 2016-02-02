package ch.unil.fcrepo4.spring.data.repository.query;

import org.modeshape.jcr.query.model.Comparison;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author gushakov
 */
public class ComparisonCriteria {
    private List<Comparison> baseComparisons;
    private List<Comparison> additionalComparisons;

    public ComparisonCriteria(Comparison comparison){
        baseComparisons = Collections.singletonList(comparison);
    }

    public ComparisonCriteria(ComparisonCriteria baseCriteria, Comparison comparison) {
        this.baseComparisons = new ArrayList<>(baseCriteria.getBaseComparisons());
        this.baseComparisons.add(comparison);
    }

    public ComparisonCriteria(ComparisonCriteria baseCriteria, ComparisonCriteria additionalCriteria){
        this.baseComparisons = baseCriteria.getBaseComparisons();
        this.additionalComparisons = additionalCriteria.getBaseComparisons();
    }

    public List<Comparison> getBaseComparisons() {
        return baseComparisons;
    }

    public List<Comparison> getAdditionalComparisons() {
        return additionalComparisons;
    }
}
