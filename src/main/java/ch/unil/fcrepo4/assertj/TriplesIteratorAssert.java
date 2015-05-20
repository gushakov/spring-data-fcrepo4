package ch.unil.fcrepo4.assertj;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.Triple;
import org.assertj.core.api.IterableAssert;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * @author gushakov
 */
public class TriplesIteratorAssert extends IterableAssert<Triple> {


    protected TriplesIteratorAssert(Iterable<? extends Triple> actual) {
        super(actual);
    }

    protected TriplesIteratorAssert(Iterator<? extends Triple> actual) {
        super(actual);
    }

    public NodesIterableAssert extractingSubjectsWithNamespace(String namespace) {
        isNotNull();
        List<Node> subjects = new ArrayList<>();
        for (Triple triple : actual) {
            Node subject = triple.getSubject();
            if (subject.isURI() && subject.getNameSpace().equals(namespace)){
                subjects.add(subject);
            }
        }
        return new NodesIterableAssert(subjects);
    }
}
