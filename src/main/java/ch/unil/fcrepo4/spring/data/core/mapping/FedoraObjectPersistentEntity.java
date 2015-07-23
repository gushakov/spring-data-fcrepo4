package ch.unil.fcrepo4.spring.data.core.mapping;

import ch.unil.fcrepo4.spring.data.core.mapping.annotation.FedoraObject;
import org.springframework.data.util.TypeInformation;

/**
 * @author gushakov
 */
public class FedoraObjectPersistentEntity<T> extends GenericFedoraPersistenceEntity<T> implements FedoraPersistentEntity<T> {

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
}
