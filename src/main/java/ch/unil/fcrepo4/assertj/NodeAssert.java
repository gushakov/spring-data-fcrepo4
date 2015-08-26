package ch.unil.fcrepo4.assertj;

import com.hp.hpl.jena.graph.Node;
import org.assertj.core.api.AbstractAssert;
import org.assertj.core.util.Objects;

/**
 * @author gushakov
 */
public class NodeAssert extends AbstractAssert<NodeAssert, Node> {

    public NodeAssert(Node actual) {
        super(actual, NodeAssert.class);
    }

    public NodeAssert isBlank() {
        isNotNull();

        if (!actual.isBlank()) {
            failWithMessage("Expected node to be a blank node");
        }

        return this;
    }

    public NodeAssert isLiteral() {
        isNotNull();

        if (!actual.isLiteral()) {
            failWithMessage("Expected node to be literal");
        }

        return this;
    }

    public NodeAssert isNotLiteral() {
        isNotNull();

        if (actual.isLiteral()) {
            failWithMessage("Expected node not to be literal");
        }

        return this;
    }

    public NodeAssert isXml() {
        isLiteral();

        if (!actual.getLiteralIsXML()) {
            failWithMessage("Expected node to be an XML literal");
        }
        return this;
    }

    public NodeAssert isUri() {
        isNotNull();

        if (!actual.isURI()) {
            failWithMessage("Expected node to be an URI node");
        }
        return this;
    }

    public NodeAssert hasUri(String uri) {
        isUri();

        String actualUri = actual.getURI();

        if (!actualUri.equals(uri)) {
            failWithMessage("Expected node to have URI <%s> instead of <%s>", uri, actualUri);
        }

        return this;
    }

    public NodeAssert hasLiteralValue(Object value){
          isLiteral();

        Object actualValue = actual.getLiteralValue();

        if (!Objects.areEqual(actualValue, value)){
           failWithMessage("Expected literal value to be %s, but was %s", value, actualValue);
        }

        return this;
    }

}
