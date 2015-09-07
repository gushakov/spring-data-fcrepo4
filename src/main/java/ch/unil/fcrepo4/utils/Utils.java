package ch.unil.fcrepo4.utils;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.Triple;

import java.util.Iterator;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/**
 * @author gushakov
 */
public class Utils {

    /*
    Based on http://stackoverflow.com/a/29010716
     */

    public static Stream<Triple> triplesStream(Iterator<Triple> triples) {
        final Iterable<Triple> iterable = () -> triples;
        return StreamSupport.stream(iterable.spliterator(), false);
    }

    public static Node getObjectLiteral(Iterator<Triple> props, String predicateUri) {
        boolean found = false;
        Node literal = null;
        while (props.hasNext() && !found) {
            Triple triple = props.next();
            if (triple.getPredicate().getURI().equals(predicateUri)) {
                if (!triple.getObject().isLiteral()) {
                    throw new RuntimeException("Property node " + triple.getObject() + " is not literal");
                }
                literal = triple.getObject();
                found = true;
            }
        }

        return literal;
    }

}
