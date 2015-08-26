package ch.unil.fcrepo4.spring.data.core.query;

import com.hp.hpl.jena.datatypes.xsd.XSDDatatype;
import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.NodeFactory;
import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.sparql.core.Var;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;

/**
 * @author gushakov
 */
public class BgpFragmentBuilder implements BgpFragment {
    private static final DateTimeFormatter THREE_DIGITS_MILLIS_ISO_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");

    private Node subject;

    private Node predicate;

    private Node object;

    public BgpFragmentBuilder(PrefixMap prefixMap, String varName, String predicateUri, Object value) {
        this.subject = Var.alloc(varName);
        this.predicate = predicateUri.startsWith("?")
                ? Var.alloc(predicateUri.substring(1))
                : NodeFactory.createURI(prefixMap.fullUri(predicateUri));
        if (value instanceof Integer) {
            Integer val = (Integer) value;
            this.object = NodeFactory.createLiteral(val.toString(), XSDDatatype.XSDinteger);
        } else if (value instanceof String) {
            String val = (String) value;
            this.object = val.startsWith("?")
                    ? Var.alloc(val.substring(1))
                    : NodeFactory.createLiteral(val, XSDDatatype.XSDstring);
        } else if (value instanceof Date) {
            Date date = (Date) value;
            ZonedDateTime dateTime = ZonedDateTime.ofInstant(date.toInstant(), ZoneId.of("UTC"));
            this.object = NodeFactory.createLiteral(dateTime.format(THREE_DIGITS_MILLIS_ISO_FORMATTER),
                    XSDDatatype.XSDdateTime);
        } else if (value instanceof ZonedDateTime) {
            ZonedDateTime dateTime = (ZonedDateTime) value;
            this.object = NodeFactory.createLiteral(dateTime.format(THREE_DIGITS_MILLIS_ISO_FORMATTER),
                    XSDDatatype.XSDdateTime);
        } else {
            throw new RuntimeException("Unknown type for the triple value: " + object.getClass());
        }
    }

    @Override
    public Triple getTriple() {
        return new Triple(subject, predicate, object);
    }
}
