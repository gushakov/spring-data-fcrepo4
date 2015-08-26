package ch.unil.fcrepo4.spring.data.core.convert;

import com.hp.hpl.jena.datatypes.RDFDatatype;
import com.hp.hpl.jena.datatypes.xsd.XSDDatatype;
import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.NodeFactory;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;

/**
 * @author gushakov
 */
public class XsdDatatypeConverter implements RdfDatatypeConverter {

    private static final DateTimeFormatter THREE_DIGITS_MILLIS_ISO_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");


    @Override
    public <T> RDFDatatype convert(Class<T> javaType) {
        if (javaType.equals(Integer.class)) {
            return XSDDatatype.XSDinteger;
        }

        if (javaType.equals(String.class)) {
            return XSDDatatype.XSDstring;
        }

        if (javaType.equals(Date.class)) {
            return XSDDatatype.XSDdateTime;
        }

        if (javaType.equals(ZonedDateTime.class)) {
            return XSDDatatype.XSDdateTime;
        }

        throw new RuntimeException("Cannot convert Java type " + javaType.getName() + " to an equivalent " + XSDDatatype.class.getName());
    }

    @Override
    public <T> Node encodeLiteralValue(T value) {

        // convert Date to ZonedDateTime, assume UTC timezone
        if (value.getClass().equals(Date.class)){
            ZonedDateTime dateTime = ZonedDateTime.ofInstant(((Date)value).toInstant(), ZoneId.of("UTC"));
            return NodeFactory.createLiteral(dateTime.format(THREE_DIGITS_MILLIS_ISO_FORMATTER),
                    convert(value.getClass()));
        }

        if (value.getClass().equals(ZonedDateTime.class)) {
            // format to ISO 8061 date-time string with three digit milliseconds value
            return NodeFactory.createLiteral(ZonedDateTime.class.cast(value).format(THREE_DIGITS_MILLIS_ISO_FORMATTER),
                    convert(value.getClass()));
        }

        return NodeFactory.createLiteral(value.toString(), convert(value.getClass()));

    }

    @Override
    public <T> String serializeLiteralValue(T value) {
        Node literal = encodeLiteralValue(value);
        return "\"" + literal.getLiteralLexicalForm() + "\"^^<" + literal.getLiteralDatatypeURI() + ">";
    }
}
