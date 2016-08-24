package ch.unil.fcrepo4.spring.data.core.query.sparql;

import ch.unil.fcrepo4.client.FcrepoConstants;
import ch.unil.fcrepo4.spring.data.core.Constants;
import ch.unil.fcrepo4.spring.data.core.convert.rdf.RdfDatatypeConverter;
import ch.unil.fcrepo4.spring.data.core.mapping.DatastreamPersistentProperty;
import ch.unil.fcrepo4.spring.data.core.mapping.FedoraPersistentProperty;
import ch.unil.fcrepo4.spring.data.core.mapping.FedoraResourcePersistentProperty;
import ch.unil.fcrepo4.spring.data.core.mapping.RelationPersistentProperty;
import com.hp.hpl.jena.graph.NodeFactory;
import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.sparql.core.BasicPattern;
import com.hp.hpl.jena.sparql.expr.E_GreaterThan;
import com.hp.hpl.jena.sparql.expr.Expr;
import com.hp.hpl.jena.sparql.expr.ExprVar;
import com.hp.hpl.jena.sparql.expr.NodeValue;
import org.springframework.data.mapping.PersistentProperty;
import org.springframework.data.mapping.context.PersistentPropertyPath;
import org.springframework.data.util.TypeInformation;

import java.util.*;

/**
 * @author gushakov
 */
public class BgpCriteria implements Criteria {

    private Set<Triple> triples;

    private List<Expr> filters;

    private TypeInformation<?> domainTypeInfo;

    private RdfDatatypeConverter rdfDatatypeConverter;

    private Set<Class<?>> ocmClasses;

    public BgpCriteria(final PersistentPropertyPath<?> propertyPath, TypeInformation<?> domainTypeInfo, RdfDatatypeConverter rdfDatatypeConverter) {
        this.triples = new LinkedHashSet<>();

        this.rdfDatatypeConverter = rdfDatatypeConverter;
        this.ocmClasses = new HashSet<>(Collections.singleton(domainTypeInfo.getType()));

        this.domainTypeInfo = domainTypeInfo;
        this.filters = new ArrayList<>();
        for (PersistentProperty prop : propertyPath) {

            if (prop instanceof FedoraResourcePersistentProperty) {
                FedoraResourcePersistentProperty property = (FedoraResourcePersistentProperty) prop;
                this.triples.add(new Triple(NodeFactory.createVariable(getPropertyOwnerVarName(property)),
                        NodeFactory.createURI(property.getUri()),
                        NodeFactory.createVariable(getPropertyVarName(property))));
            } else if (prop instanceof DatastreamPersistentProperty) {
                DatastreamPersistentProperty property = (DatastreamPersistentProperty) prop;
                this.ocmClasses.add(property.getTypeInformation().getType());
                this.triples.add(new Triple(NodeFactory.createVariable(getPropertyOwnerVarName(property)),
                        FcrepoConstants.CONTAINS.asNode(),
                        NodeFactory.createVariable(getPropertyTypeVarName(property))));

            } else if (prop instanceof RelationPersistentProperty) {
                RelationPersistentProperty property = (RelationPersistentProperty) prop;
                this.ocmClasses.add(property.getTypeInformation().getType());
                triples.add(new Triple(NodeFactory.createVariable(getPropertyOwnerVarName(property)),
                        NodeFactory.createURI(property.getUri()),
                        NodeFactory.createVariable(typeClassToName(property.getType()))));
            }
            else {
                throw new IllegalStateException("Cannot process property: " + prop);
            }

        }
    }

    @Override
    public void substitutePropertyNodeValue(FedoraResourcePersistentProperty property, NodeValue nodeValue) {
        Set<Triple> done = new HashSet<>();
        for (Triple triple : triples) {
            if (triple.getSubject().getName().startsWith(getPropertyOwnerVarName(property))
                    && triple.getPredicate().getURI().equals(property.getUri())) {
                done.add(new Triple(triple.getSubject(), triple.getPredicate(), nodeValue.asNode()));
            } else {
                done.add(triple);
            }
        }
        triples.clear();
        triples.addAll(done);
    }

    private String getPropertyOwnerVarName(FedoraPersistentProperty property) {
        return typeInfoToName(property.getOwner().getTypeInformation());
    }

    private String getPropertyVarName(FedoraPersistentProperty property){
        return getPropertyOwnerVarName(property) + "_" + property.getField().getName();
    }

    private String getPropertyTypeVarName(FedoraPersistentProperty property){
        return typeInfoToName(property.getTypeInformation());
    }

    private String typeInfoToName(TypeInformation<?> typeInformation){
        return typeClassToName(typeInformation.getType());
    }

    private String typeClassToName(Class<?> type){
        return type.getName().replace('.', '_');
    }

    @Override
    public BasicPattern buildBgp() {
        ocmClasses.stream().forEach(ocmClass -> triples.add(new Triple(NodeFactory.createVariable(typeClassToName(ocmClass)),
                NodeFactory.createURI(Constants.OCM_URI_NAMESPACE + Constants.OCM_CLASS_PROPERTY),
                rdfDatatypeConverter.encodeLiteralValue(ocmClass.getName()))));

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
        filters.add(new E_GreaterThan(new ExprVar(getPropertyVarName(property)), nodeValue));
    }

    @Override
    public List<Expr> getFilters() {
        return filters;
    }

    @Override
    public Criteria and(Criteria otherCriteria) {
        BasicPattern bgp = otherCriteria.buildBgp();
        bgp.getList().stream().forEach(triples::add);
        return this;
    }

    @Override
    public String toString() {
        return triples.toString();
    }
}
