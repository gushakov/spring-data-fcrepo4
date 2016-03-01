package ch.unil.fcrepo4.spring.data.core.mapping;

import ch.unil.fcrepo4.spring.data.core.convert.rdf.RdfDatatypeConverter;
import com.hp.hpl.jena.graph.NodeFactory;
import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.sparql.syntax.ElementTriplesBlock;
import org.springframework.data.mapping.PersistentProperty;
import org.springframework.data.mapping.PersistentPropertyAccessor;

/**
 * @author gushakov
 */
public class TriplesCollectingPropertyHandler implements SimplePropertyAndValueHandler {

    private PersistentPropertyAccessor propertyAccessor;

    private RdfDatatypeConverter rdfDatatypeConverter;

    private ElementTriplesBlock insertTriples;

    private ElementTriplesBlock deleteWhereTriples;

    public TriplesCollectingPropertyHandler(PersistentPropertyAccessor propertyAccessor, RdfDatatypeConverter rdfDatatypeConverter) {
        this.propertyAccessor = propertyAccessor;
        this.rdfDatatypeConverter = rdfDatatypeConverter;
        this.insertTriples = new ElementTriplesBlock();
        this.deleteWhereTriples = new ElementTriplesBlock();
    }

    @Override
    public PersistentPropertyAccessor getPropertyAccessor() {
        return propertyAccessor;
    }

    @Override
    public void doWithPersistentPropertyAndValue(PersistentProperty<?> property, Object value) {
        if (property instanceof SimpleFedoraResourcePersistentProperty) {
            SimpleFedoraResourcePersistentProperty simpleProp = (SimpleFedoraResourcePersistentProperty) property;
            if (!simpleProp.isReadOnly()) {
                Triple triple = new Triple(NodeFactory.createURI(""),
                        NodeFactory.createURI(simpleProp.getUri()),
                        rdfDatatypeConverter.encodeLiteralValue(value));
                insertTriples.addTriple(triple);
                deleteWhereTriples.addTriple(new Triple(triple.getSubject(), triple.getPredicate(),
                        NodeFactory.createVariable(simpleProp.getName())));
            }
        }
    }

    public ElementTriplesBlock getInsertTriples() {
        return insertTriples;
    }

    public ElementTriplesBlock getDeleteWhereTriples() {
        return deleteWhereTriples;
    }
}
