package ch.unil.fcrepo4.spring.data.core.query.qom;

import org.apache.commons.lang3.tuple.MutablePair;
import org.apache.commons.lang3.tuple.Pair;

import java.util.EnumSet;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collector;

/**
 * @author gushakov
 */
public class AndCollector implements Collector<Constraint, MutablePair<Constraint, Constraint>, Constraint> {

    private Constraint base;

    public AndCollector() {
    }

    public AndCollector(Constraint base) {
        this.base = base;
    }

    @Override
    public Supplier<MutablePair<Constraint, Constraint>> supplier() {
        return () -> MutablePair.of(null, base);
    }

    @Override
    public BiConsumer<MutablePair<Constraint, Constraint>, Constraint> accumulator() {
        return (pair, constraint) -> {
            if (pair.getLeft() == null) {
                pair.setLeft(constraint);
            } else if (pair.getRight() == null) {
                pair.setRight(constraint);
            } else {
                pair.setLeft(new AndImpl(pair.getLeft(), pair.getRight()));
                pair.setRight(constraint);
            }
        };
    }

    @Override
    public BinaryOperator<MutablePair<Constraint, Constraint>> combiner() {
        return (pair1, pair2) -> {
            throw new UnsupportedOperationException();
        };
    }

    @Override
    public Function<MutablePair<Constraint, Constraint>, Constraint> finisher() {
        return pair -> pair.getRight() != null ? new AndImpl(pair.getLeft(), pair.getRight()) : pair.getLeft();
    }

    @Override
    public Set<Characteristics> characteristics() {
        return EnumSet.of(Characteristics.UNORDERED);
    }
}
