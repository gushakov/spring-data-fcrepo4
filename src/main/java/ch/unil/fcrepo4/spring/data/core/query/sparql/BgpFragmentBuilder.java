package ch.unil.fcrepo4.spring.data.core.query.sparql;

import ch.unil.fcrepo4.spring.data.core.convert.RdfDatatypeConverter;
import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.NodeFactory;
import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.sparql.core.Var;

/**
 * @author gushakov
 */
public class BgpFragmentBuilder implements BgpFragment {

    private Node subject;

    private Node predicate;

    private Node object;

    public BgpFragmentBuilder(PrefixMap prefixMap, String varName, String predicateUri, Object value, RdfDatatypeConverter rdfDatatypeConverter) {
        this.subject = Var.alloc(varName);
        this.predicate = predicateUri.startsWith("?")
                ? Var.alloc(predicateUri.substring(1))
                : NodeFactory.createURI(prefixMap.fullUri(predicateUri));
        if (value instanceof String) {
            String val = (String) value;
            this.object = val.startsWith("?")
                    ? Var.alloc(val.substring(1))
                    : rdfDatatypeConverter.encodeLiteralValue(value);
        } else {
            this.object = rdfDatatypeConverter.encodeLiteralValue(value);
        }
    }

    @Override
    public Triple getTriple() {
        return new Triple(subject, predicate, object);
    }
}
