package ch.unil.fcrepo4.spring.data.core.mapping;

import ch.unil.fcrepo4.spring.data.core.mapping.annotation.Binary;
import org.springframework.data.util.TypeInformation;

/**
 * @author gushakov
 */
public class DatastreamPersistentEntity<T> extends GenericFedoraPersistentEntity<T> implements FedoraPersistentEntity<T> {

    private FedoraObjectPersistentEntity<?> foEntity;

    public DatastreamPersistentEntity(TypeInformation<T> information) {
        super(information);
    }

    public FedoraObjectPersistentEntity<?> getFedoraObjectEntity() {
        return foEntity;
    }

    public void setFedoraObjectEntity(FedoraObjectPersistentEntity<?> foEntity) {
        this.foEntity = foEntity;
    }

    public BinaryPersistentProperty getContentProperty() {
        return (BinaryPersistentProperty) getPersistentProperty(Binary.class);
    }

}
