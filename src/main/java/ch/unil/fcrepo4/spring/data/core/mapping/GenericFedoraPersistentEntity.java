package ch.unil.fcrepo4.spring.data.core.mapping;

import org.springframework.data.mapping.model.BasicPersistentEntity;
import org.springframework.data.util.TypeInformation;

/**
 * @author gushakov
 */
public class GenericFedoraPersistentEntity<T> extends BasicPersistentEntity<T, FedoraPersistentProperty>
        implements FedoraPersistentEntity<T> {
    public GenericFedoraPersistentEntity(TypeInformation<T> information) {
        super(information);
    }

}
