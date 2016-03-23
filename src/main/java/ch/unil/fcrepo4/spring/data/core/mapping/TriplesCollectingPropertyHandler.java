package ch.unil.fcrepo4.spring.data.core.mapping;

import ch.unil.fcrepo4.spring.data.core.convert.FedoraConverter;
import ch.unil.fcrepo4.spring.data.core.convert.rdf.RdfDatatypeConverter;
import com.hp.hpl.jena.graph.NodeFactory;
import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.sparql.syntax.ElementTriplesBlock;
import org.springframework.data.mapping.Association;
import org.springframework.data.mapping.PersistentProperty;
import org.springframework.data.mapping.PersistentPropertyAccessor;
import org.springframework.data.mapping.SimpleAssociationHandler;

/**
 * @author gushakov
 */
public class TriplesCollectingPropertyHandler implements SimplePropertyAndValueHandler, SimpleAssociationHandler {

    private PersistentPropertyAccessor propertyAccessor;

    private FedoraConverter fedoraConverter;

    private ElementTriplesBlock insertTriples;

    private ElementTriplesBlock deleteWhereTriples;

    public TriplesCollectingPropertyHandler(PersistentPropertyAccessor propertyAccessor, FedoraConverter fedoraConverter) {
        this.propertyAccessor = propertyAccessor;
        this.fedoraConverter = fedoraConverter;
        this.insertTriples = new ElementTriplesBlock();
        this.deleteWhereTriples = new ElementTriplesBlock();
    }

    @Override
    public PersistentPropertyAccessor getPropertyAccessor() {
        return propertyAccessor;
    }

    @Override
    public void doWithPersistentPropertyAndValue(PersistentProperty<?> property, Object value) {
        if (value != null) {
            if (property instanceof SimpleFedoraResourcePersistentProperty) {
                SimpleFedoraResourcePersistentProperty simpleProp = (SimpleFedoraResourcePersistentProperty) property;
                if (!simpleProp.isReadOnly()) {
                    Triple triple = new Triple(NodeFactory.createURI(""),
                            NodeFactory.createURI(simpleProp.getUri()),
                            fedoraConverter.getRdfDatatypeConverter().encodeLiteralValue(value));
                    insertTriples.addTriple(triple);
                    deleteWhereTriples.addTriple(new Triple(triple.getSubject(), triple.getPredicate(),
                            NodeFactory.createVariable(simpleProp.getName())));
                }
            }
        }
    }

    public ElementTriplesBlock getInsertTriples() {
        return insertTriples;
    }

    public ElementTriplesBlock getDeleteWhereTriples() {
        return deleteWhereTriples;
    }

    @Override
    public void doWithAssociation(Association<? extends PersistentProperty<?>> association) {
        if (association.getInverse() instanceof RelationPersistentProperty){
            RelationPersistentProperty relProp = (RelationPersistentProperty) association.getInverse();
            final Object relBean = propertyAccessor.getProperty(relProp);
            if (relBean!=null){
                Triple triple = new Triple(NodeFactory.createURI(""),
                        NodeFactory.createURI(relProp.getUri()),
                        NodeFactory.createURI(fedoraConverter.getFedoraObjectUrl(relBean)));
                insertTriples.addTriple(triple);
                deleteWhereTriples.addTriple(new Triple(triple.getSubject(), triple.getPredicate(),
                        NodeFactory.createVariable(relProp.getName())));
            }

        }
    }
}
