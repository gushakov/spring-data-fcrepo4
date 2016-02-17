package ch.unil.fcrepo4.spring.data.core.convert.rdf;

import com.hp.hpl.jena.datatypes.RDFDatatype;
import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.sparql.expr.NodeValue;

/**
 * @author gushakov
 */
public interface RdfDatatypeConverter {

    <T> RDFDatatype convert(Class<T> javaType);

    <T> Node encodeLiteralValue(T value);

    <T> String serializeLiteralValue(T value);

    <T> T parseLiteralValue(String lexicalForm, Class<T> javaType);

    <T> NodeValue encodeExpressionValue(T value);
}
