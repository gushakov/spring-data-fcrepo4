package ch.unil.fcrepo4.spring.data.core.query.sparql;

import ch.unil.fcrepo4.spring.data.core.mapping.DatastreamPersistentProperty;
import ch.unil.fcrepo4.spring.data.core.mapping.FedoraResourcePersistentProperty;
import com.hp.hpl.jena.graph.NodeFactory;
import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.sparql.core.BasicPattern;
import com.hp.hpl.jena.sparql.expr.NodeValue;
import org.fcrepo.kernel.api.RdfLexicon;
import org.springframework.data.mapping.PersistentProperty;
import org.springframework.data.mapping.context.PersistentPropertyPath;
import org.springframework.data.util.TypeInformation;

import java.util.ListIterator;

/**
 * @author gushakov
 */
public class BgpCriteria<T extends PersistentProperty<T>> implements Criteria {

    private BasicPattern bgp;

    public BgpCriteria(final PersistentPropertyPath<T> propertyPath) {
        this.bgp = new BasicPattern();
//        for (PersistentProperty prop : (Iterable<PersistentProperty<?>>) propertyPath) {
        for (PersistentProperty prop : propertyPath) {

            if (prop instanceof FedoraResourcePersistentProperty) {
                FedoraResourcePersistentProperty property = (FedoraResourcePersistentProperty) prop;
                this.bgp.add(new Triple(NodeFactory.createVariable(getVariableName(property.getOwner().getTypeInformation())),
                        NodeFactory.createURI(property.getUri()),
                        NodeFactory.createVariable(property.getLocalName())));
            } else if (prop instanceof DatastreamPersistentProperty) {
                DatastreamPersistentProperty property = (DatastreamPersistentProperty) prop;
                this.bgp.add(new Triple(NodeFactory.createVariable(getVariableName(property.getOwner().getTypeInformation())),
                        RdfLexicon.CONTAINS.asNode(),
                        NodeFactory.createVariable(getVariableName(property.getTypeInformation()))));

            } else {
                throw new IllegalStateException("Cannot process property: " + prop);
            }

        }
    }

    public void substitutePropertyNodeValue(FedoraResourcePersistentProperty property, NodeValue nodeValue) {
        for (ListIterator<Triple> triplesIter = bgp.getList().listIterator(); triplesIter.hasNext(); ) {
            Triple triple = triplesIter.next();
            if (triple.getSubject().getName().equals(getVariableName(property.getOwner().getTypeInformation()))
                    && triple.getPredicate().getURI().equals(property.getUri())) {
                triplesIter.set(new Triple(triple.getSubject(), triple.getPredicate(), nodeValue.asNode()));
            }
        }
    }

    private String getVariableName(TypeInformation<?> typeInformation) {
        return typeInformation.getType().getName().replace('.', '_');
    }

    @Override
    public String toString() {
        return bgp.toString();
    }
}
