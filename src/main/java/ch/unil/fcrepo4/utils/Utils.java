package ch.unil.fcrepo4.utils;

import com.hp.hpl.jena.datatypes.RDFDatatype;
import com.hp.hpl.jena.datatypes.xsd.impl.XSDBaseNumericType;
import com.hp.hpl.jena.datatypes.xsd.impl.XSDBaseStringType;
import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.NodeFactory;
import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.vocabulary.XSD;
import org.fcrepo.client.FedoraException;
import org.fcrepo.client.FedoraResource;
import org.fcrepo.kernel.RdfLexicon;
import org.springframework.util.Assert;

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

    public static Object getLiteralValue(Iterator<Triple> props, String predicateUri){
        boolean found = false;
        Object value = null;
        while (props.hasNext() && !found) {
            Triple triple = props.next();
            if (triple.getPredicate().getURI().equals(predicateUri)) {
                if (!triple.getObject().isLiteral()) {
                    throw new RuntimeException("Property node " + triple.getObject() + " is not literal");
                }
                value = triple.getObject().getLiteralValue();
                found = true;
            }
        }

        return value;

    }

    public static String encodeLiteralValue(Object value, Class<?> javaType) {
        Assert.notNull(value);
        RDFDatatype rdfDatatype;
        if (Integer.class.isAssignableFrom(javaType)
                || int.class.isAssignableFrom(javaType)) {
            rdfDatatype = new XSDBaseNumericType(XSD.integer.getLocalName());
        } else {
            rdfDatatype = new XSDBaseStringType(XSD.xstring.getLocalName());
        }

        Node literal = NodeFactory.createLiteral(value.toString(), rdfDatatype);
        return "\"" + literal.getLiteralLexicalForm() + "\"^^<" + literal.getLiteralDatatypeURI() + ">";
    }

}
