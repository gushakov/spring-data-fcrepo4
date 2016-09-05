package ch.unil.fcrepo4.spring.data.core.convert.rdf;

import org.apache.jena.datatypes.RDFDatatype;
import org.apache.jena.datatypes.TypeMapper;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.sparql.expr.NodeValue;
import org.springframework.util.Assert;

/**
 * @author gushakov
 */
public class ExtendedXsdDatatypeConverter implements RdfDatatypeConverter {

    private TypeMapper delegateRdfMapper;

    public ExtendedXsdDatatypeConverter() {
        delegateRdfMapper = TypeMapper.getInstance();

        if (delegateRdfMapper.getTypeByClass(DateRdfDatatype.getInstance().getJavaClass()) == null) {
            delegateRdfMapper.registerDatatype(DateRdfDatatype.getInstance());
        }

        if (delegateRdfMapper.getTypeByClass(ZonedDateTimeRdfDatatype.getInstance().getJavaClass()) == null){
            delegateRdfMapper.registerDatatype(ZonedDateTimeRdfDatatype.getInstance());
        }

        if (delegateRdfMapper.getTypeByClass(UuidRdfDatatype.getInstance().getJavaClass()) == null){
            delegateRdfMapper.registerDatatype(UuidRdfDatatype.getInstance());
        }

    }

    @Override
    public <T> RDFDatatype convert(Class<T> javaType) {
        return delegateRdfMapper.getTypeByClass(javaType);
    }

    @Override
    public <T> Node encodeLiteralValue(T value) {
        RDFDatatype rdfDatatype = delegateRdfMapper.getTypeByValue(value);
        Assert.notNull(rdfDatatype, "No RDF datatype registered for value of type " + value.getClass());
        return NodeFactory.createLiteral(rdfDatatype.unparse(value), rdfDatatype);
    }

    @Override
    public <T> String serializeLiteralValue(T value) {
        return encodeLiteralValue(value).toString(true);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T parseLiteralValue(String lexicalForm, Class<T> javaType) {
        RDFDatatype rdfDatatype = delegateRdfMapper.getTypeByClass(javaType);
        Assert.notNull(rdfDatatype, "No RDF datatype registered for value of type " + javaType);
        Object value = rdfDatatype.parse(lexicalForm);
        T literalValue;
        if (javaType.isPrimitive()) {
            literalValue = (T) value;
        } else {
            literalValue = javaType.cast(value);
        }
        return literalValue;
    }

    @Override
    public <T> NodeValue encodeExpressionValue(T value) {
        RDFDatatype rdfDatatype = delegateRdfMapper.getTypeByValue(value);
        Assert.notNull(rdfDatatype, "No RDF datatype registered for value of type " + value.getClass());
        return NodeValue.makeNode(rdfDatatype.unparse(value), rdfDatatype);
    }

}
