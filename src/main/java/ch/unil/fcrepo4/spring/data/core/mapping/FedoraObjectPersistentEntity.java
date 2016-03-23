package ch.unil.fcrepo4.spring.data.core.mapping;

import ch.unil.fcrepo4.spring.data.core.Constants;
import ch.unil.fcrepo4.spring.data.core.mapping.annotation.FedoraObject;
import org.apache.commons.lang3.Validate;
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
        Validate.notBlank(foAnnot.namespace(), "A bean of type %s declared a blank namespace", getType().getName());
    }

    public String getNamespace() {
        return foAnnot.namespace().equals(Constants.DEFAULT_ANNOTATION_STRING_VALUE_TOKEN)
                ? getType().getSimpleName().toLowerCase() : foAnnot.namespace();
    }

    public boolean isDefaultNamespace(){
        return foAnnot.namespace().equals(Constants.DEFAULT_ANNOTATION_STRING_VALUE_TOKEN);
    }

    public String getUriNs() {
        return foAnnot.uriNs();
    }

}
