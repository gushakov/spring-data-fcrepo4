package ch.unil.fcrepo4.spring.data.core.mapping;

import ch.unil.fcrepo4.spring.data.core.mapping.annotation.FedoraObject;
import org.springframework.data.mapping.Association;
import org.springframework.data.mapping.PersistentProperty;
import org.springframework.data.mapping.SimpleAssociationHandler;
import org.springframework.data.mapping.SimplePropertyHandler;
import org.springframework.data.util.TypeInformation;

/**
 * @author gushakov
 */
public class FedoraObjectPersistentEntity<T> extends GenericFedoraPersistentEntity<T> implements FedoraPersistentEntity<T> {

    private FedoraObject foAnnot;

    public FedoraObjectPersistentEntity(TypeInformation<T> information) {
        super(information);
        this.foAnnot = findAnnotation(FedoraObject.class);
    }

    public String getNamespace() {
        return foAnnot.namespace();
    }

    public String getUriNs() {
        return foAnnot.uriNs();
    }

    public void doWithDatastreams(SimplePropertyHandler datastreamPropertyHandler){
        doWithAssociations((Association<? extends PersistentProperty<?>> association) -> {
             if (association.getInverse() instanceof DatastreamPersistentProperty){
                 datastreamPropertyHandler.doWithPersistentProperty(association.getInverse());
             }
        });
    }
}
