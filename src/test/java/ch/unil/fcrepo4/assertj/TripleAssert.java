package ch.unil.fcrepo4.assertj;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.Triple;
import org.assertj.core.api.AbstractAssert;

/**
 * @author gushakov
 */
public class TripleAssert extends AbstractAssert<TripleAssert, Triple> {
    protected TripleAssert(Triple actual) {
        super(actual, TripleAssert.class);
    }

    public TripleAssert hasSubject(String uri) {
        isNotNull();

        Node subject = actual.getSubject();

        if (subject == null) {
            failWithMessage("Expected triple to have a subject node, but it was null",
                    uri);
        }
        else  {
            String actualUri = actual.getSubject().getURI();
            if (!subject.isURI() || !subject.getURI().equals(uri)) {
                failWithMessage("Expected triple to have a subject node with the URI <%s>, but the URI was <%s>",
                        uri, actualUri);
            }
        }

        return this;
    }
}
