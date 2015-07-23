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
            if (subject.isURI() && subject.getNameSpace().equals(namespace)) {
                subjects.add(subject);
            }
        }
        return new NodesIterableAssert(subjects);
    }

    public TriplesIteratorAssert contains(Triple anotherTriple) {
        isNotNull();
        boolean found = false;

        for (Triple triple : actual) {
            if (!triple.getSubject().isURI()
                    || !triple.getPredicate().isURI()
                    || !triple.getObject().isLiteral()) {
                continue;
            }
            if (triple.getSubject().getURI().equals(anotherTriple.getSubject().getURI())
                    && triple.getPredicate().getURI().equals(anotherTriple.getPredicate().getURI())
                    && triple.getObject().getLiteralLexicalForm().equals(anotherTriple.getObject().getLiteralLexicalForm())
                    ) {
                found = true;
                break;
            }
        }
        if (!found) {
            failWithMessage("Cannot find triple %s", anotherTriple);
        }
        return this;
    }

    public TriplesIteratorAssert contains(Node predicate, Node literalValue) {
        isNotNull();
        boolean found = false;

        for (Triple triple : actual) {
            if (!triple.getPredicate().isURI()
                    || !triple.getObject().isLiteral()) {
                continue;
            }
            if (triple.getPredicate().getURI().equals(predicate.getURI())
                    && triple.getObject().getLiteralLexicalForm().equals(literalValue.getLiteralLexicalForm())
                    ) {
                found = true;
                break;
            }
        }
        if (!found) {
            failWithMessage("Cannot find triple with predicate %s and literal value %s", predicate, literalValue);
        }
        return this;
    }
}
