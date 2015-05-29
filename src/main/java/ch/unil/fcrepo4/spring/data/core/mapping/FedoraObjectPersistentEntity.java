package ch.unil.fcrepo4.spring.data.core.mapping;

import ch.unil.fcrepo4.spring.data.core.Constants;
import ch.unil.fcrepo4.spring.data.core.mapping.annotation.FedoraObject;
import org.springframework.data.util.TypeInformation;

/**
 * @author gushakov
 */
public class FedoraObjectPersistentEntity<T> extends GenericFedoraPersistenceEntity<T> implements FedoraPersistentEntity<T> {

    private FedoraObject foAnnot;

    private String namespace;

    public FedoraObjectPersistentEntity(TypeInformation<T> information) {
        super(information);
        FedoraObject annot = findAnnotation(FedoraObject.class);
        this.foAnnot = annot;
        if (annot.namespace().equals(Constants.DEFAULT_ANNOTATION_STRING_VALUE_TOKEN)){
            // default namespace
            this.namespace = Constants.DEFAULT_NAMESPACE;
        }
        else {
            this.namespace = annot.namespace();
        }
    }

    public String getNamespace() {
        return namespace;
    }
}
