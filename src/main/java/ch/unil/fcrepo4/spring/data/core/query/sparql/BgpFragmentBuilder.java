package ch.unil.fcrepo4.spring.data.core.query.sparql;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.NodeFactory;
import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.sparql.core.Var;

/**
 * @author gushakov
 */
public class BgpFragmentBuilder extends AbstractSparqlSelectQueryBuilder implements BgpFragment {

    private Node subject;

    private Node predicate;

    private Node object;

    public BgpFragmentBuilder(SparqlQueryBuildContext context, String varName, String predicateUri, Object value) {
        super(context);
        this.subject = Var.alloc(varName);
        this.predicate = predicateUri.startsWith("?")
                ? Var.alloc(predicateUri.substring(1))
                : NodeFactory.createURI(context.getPrefixMap().fullUri(predicateUri));
        if (value instanceof String) {
            String val = (String) value;
            this.object = val.startsWith("?")
                    ? Var.alloc(val.substring(1))
                    : context.getRdfDatatypeConverter().encodeLiteralValue(value);
        } else {
            this.object = context.getRdfDatatypeConverter().encodeLiteralValue(value);
        }
    }

    @Override
    public Triple getTriple() {
        return new Triple(subject, predicate, object);
    }
}
