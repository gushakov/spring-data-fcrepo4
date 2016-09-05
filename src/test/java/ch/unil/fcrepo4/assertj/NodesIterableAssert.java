package ch.unil.fcrepo4.assertj;


import org.apache.jena.graph.Node;
import org.assertj.core.api.IterableAssert;

/**
 * @author gushakov
 */
public class NodesIterableAssert extends IterableAssert<Node> {
    public NodesIterableAssert(Iterable<? extends Node> actual) {
        super(actual);
    }
}
