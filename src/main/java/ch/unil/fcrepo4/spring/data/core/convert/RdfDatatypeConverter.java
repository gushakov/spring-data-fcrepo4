package ch.unil.fcrepo4.spring.data.core.convert;

import com.hp.hpl.jena.datatypes.RDFDatatype;
import com.hp.hpl.jena.graph.Node;

/**
 * @author gushakov
 */
public interface RdfDatatypeConverter {
    <T> RDFDatatype convert(Class<T> javaType);

    <T> Node encodeLiteralValue(T value);

    <T> String serializeLiteralValue(T value);
}