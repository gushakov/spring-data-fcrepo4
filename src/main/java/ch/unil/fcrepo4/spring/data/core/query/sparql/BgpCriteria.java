package ch.unil.fcrepo4.spring.data.core.query.sparql;

import ch.unil.fcrepo4.spring.data.core.Constants;
import ch.unil.fcrepo4.spring.data.core.convert.rdf.RdfDatatypeConverter;
import ch.unil.fcrepo4.spring.data.core.mapping.DatastreamPersistentProperty;
import ch.unil.fcrepo4.spring.data.core.mapping.FedoraPersistentProperty;
import ch.unil.fcrepo4.spring.data.core.mapping.FedoraResourcePersistentProperty;
import com.hp.hpl.jena.graph.NodeFactory;
import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.sparql.core.BasicPattern;
import com.hp.hpl.jena.sparql.expr.E_GreaterThan;
import com.hp.hpl.jena.sparql.expr.Expr;
import com.hp.hpl.jena.sparql.expr.ExprVar;
import com.hp.hpl.jena.sparql.expr.NodeValue;
import org.fcrepo.kernel.api.RdfLexicon;
import org.springframework.data.mapping.PersistentProperty;
import org.springframework.data.mapping.context.PersistentPropertyPath;
import org.springframework.data.util.TypeInformation;

import java.util.*;

/**
 * @author gushakov
 */
public class BgpCriteria implements Criteria {

    private List<Triple> triples;

    private List<Expr> filters;

    private TypeInformation<?> domainTypeInfo;

    private RdfDatatypeConverter rdfDatatypeConverter;

    private Set<Class<?>> ocmClasses;

    public BgpCriteria(final PersistentPropertyPath<?> propertyPath, TypeInformation<?> domainTypeInfo, RdfDatatypeConverter rdfDatatypeConverter) {
        this.triples = new ArrayList<>();
        this.rdfDatatypeConverter = rdfDatatypeConverter;
        this.ocmClasses = new HashSet<>(Collections.singleton(domainTypeInfo.getType()));

        this.domainTypeInfo = domainTypeInfo;
        this.filters = new ArrayList<>();
        for (PersistentProperty prop : propertyPath) {

            if (prop instanceof FedoraResourcePersistentProperty) {
                FedoraResourcePersistentProperty property = (FedoraResourcePersistentProperty) prop;
                this.triples.add(new Triple(NodeFactory.createVariable(getSubjectName(property)),
                        NodeFactory.createURI(property.getUri()),
                        NodeFactory.createVariable(getObjectName(property))));
            } else if (prop instanceof DatastreamPersistentProperty) {
                DatastreamPersistentProperty property = (DatastreamPersistentProperty) prop;
                this.ocmClasses.add(property.getTypeInformation().getType());
                this.triples.add(new Triple(NodeFactory.createVariable(getSubjectName(property)),
                        RdfLexicon.CONTAINS.asNode(),
                        NodeFactory.createVariable(getSubjectName(property))));

            } else {
                throw new IllegalStateException("Cannot process property: " + prop);
            }

        }
    }

    public void substitutePropertyNodeValue(FedoraResourcePersistentProperty property, NodeValue nodeValue) {
        for (ListIterator<Triple> triplesIter = triples.listIterator(); triplesIter.hasNext(); ) {
            Triple triple = triplesIter.next();
            if (triple.getSubject().getName().equals(getSubjectName(property))
                    && triple.getPredicate().getURI().equals(property.getUri())) {
                triplesIter.set(new Triple(triple.getSubject(), triple.getPredicate(), nodeValue.asNode()));
            }
        }
    }

    private String getSubjectName(FedoraPersistentProperty property) {
        return typeInfoToName(property.getOwner().getTypeInformation());
    }

    private String getObjectName(FedoraPersistentProperty property){
        return getSubjectName(property) + "_" + property.getField().getName();
    }

    private String typeInfoToName(TypeInformation<?> typeInformation){
        return typeClassToName(typeInformation.getType());
    }

    private String typeClassToName(Class<?> type){
        return type.getName().replace('.', '_');
    }

    @Override
    public BasicPattern buildBgp() {
        ocmClasses.stream().forEach(ocmClass -> {
            triples.add(0, new Triple(NodeFactory.createVariable(typeClassToName(ocmClass)),
                    NodeFactory.createURI(Constants.OCM_URI_NAMESPACE + Constants.OCM_CLASS_PROPERTY),
                    rdfDatatypeConverter.encodeLiteralValue(ocmClass.getName())));
        });

        BasicPattern bgp = new BasicPattern();
        triples.stream().forEach(bgp::add);
        return bgp;
    }

    @Override
    public String getProjectionVariableName() {
        return typeInfoToName(domainTypeInfo);
    }

    @Override
    public void addGreaterThanFilter(FedoraResourcePersistentProperty property, NodeValue nodeValue) {
        filters.add(new E_GreaterThan(new ExprVar(getObjectName(property)), nodeValue));
    }

    @Override
    public List<Expr> getFilters() {
        return filters;
    }

    @Override
    public String toString() {
        return triples.toString();
    }
}
