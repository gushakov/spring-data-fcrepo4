package ch.unil.fcrepo4.spring.data.core.query;

import com.hp.hpl.jena.datatypes.xsd.impl.XSDBaseNumericType;
import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.NodeFactory;
import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.sparql.core.Var;
import com.hp.hpl.jena.vocabulary.XSD;

/**
 * @author gushakov
 */
public class BgpFragmentBuilder implements BgpFragment {
    private Node subject;

    private Node predicate;

    private Node object;

    public BgpFragmentBuilder(PrefixMap prefixMap, String varName, String predicateUri, Object value) {
        this.subject = Var.alloc(varName);
        this.predicate = predicateUri.startsWith("?")
                ? Var.alloc(predicateUri.substring(1))
                : NodeFactory.createURI(prefixMap.fullUri(predicateUri));
        if (value instanceof Integer) {
            this.object = NodeFactory.createLiteral(Integer.toString((Integer) value), new XSDBaseNumericType(XSD.integer.getLocalName()));
        } else if (value instanceof String) {
            String val = (String) value;
            this.object = val.startsWith("?")
                    ? Var.alloc(val.substring(1))
                    : NodeFactory.createURI(prefixMap.fullUri(val));
        } else {
            throw new RuntimeException("Unknown type for the triple value: " + object.getClass());
        }
    }


    @Override
    public Triple getTriple() {
        return new Triple(subject, predicate, object);
    }
}