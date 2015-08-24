package ch.unil.fcrepo4.spring.data.core.mapping;

import org.springframework.data.mapping.PersistentProperty;
import org.springframework.data.mapping.PropertyHandler;
import org.springframework.data.mapping.SimplePropertyHandler;
import org.springframework.data.mapping.model.BasicPersistentEntity;
import org.springframework.data.util.TypeInformation;

import java.lang.reflect.Method;

/**
 * @author gushakov
 */
public class GenericFedoraPersistentEntity<T> extends BasicPersistentEntity<T, FedoraPersistentProperty>
        implements FedoraPersistentEntity<T> {
    public GenericFedoraPersistentEntity(TypeInformation<T> information) {
        super(information);
    }

    public FedoraPersistentProperty findProperty(Method getter){
        final FedoraPersistentProperty found[] = new FedoraPersistentProperty[]{null};
        doWithProperties((FedoraPersistentProperty property) -> {
            if (property.getGetter().getName().equals(getter.getName())){
                found[0] = property;
            }
        });
        return found[0];
    }

}
