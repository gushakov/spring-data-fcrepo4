package ch.unil.fcrepo4.spring.data.core.convert.rdf;

import com.hp.hpl.jena.datatypes.RDFDatatype;
import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.sparql.expr.NodeValue;

import java.time.format.DateTimeFormatter;

/**
 * @author gushakov
 */
public interface RdfDatatypeConverter {
    DateTimeFormatter THREE_DIGITS_MILLIS_ISO_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");

    <T> RDFDatatype convert(Class<T> javaType);

    <T> Node encodeLiteralValue(T value);

    <T> String serializeLiteralValue(T value);

    <T> NodeValue encodeExpressionValue(T value);

    <T> T parseLiteralValue(String lexicalForm, Class<T> javaType);
}
