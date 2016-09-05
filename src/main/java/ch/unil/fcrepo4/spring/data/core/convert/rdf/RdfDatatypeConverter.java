package ch.unil.fcrepo4.spring.data.core.convert.rdf;


import org.apache.jena.datatypes.RDFDatatype;
import org.apache.jena.graph.Node;
import org.apache.jena.sparql.expr.NodeValue;

/**
 * @author gushakov
 */
public interface RdfDatatypeConverter {

    <T> RDFDatatype convert(Class<T> javaType);

    <T> Node encodeLiteralValue(T value);

    /**
     * Serializes given literal value to string using quoted form. For example: for {@code 123} will return {@code
     * "123"^^<http://www.w3.org/2001/XMLSchema#int>}.
     *
     * @param value value to serialized
     * @param <T>   any simple type
     * @return value serialized as a quoted string
     * @see Node#toString(boolean)
     */
    <T> String serializeLiteralValue(T value);

    <T> T parseLiteralValue(String lexicalForm, Class<T> javaType);

    <T> NodeValue encodeExpressionValue(T value);
}
