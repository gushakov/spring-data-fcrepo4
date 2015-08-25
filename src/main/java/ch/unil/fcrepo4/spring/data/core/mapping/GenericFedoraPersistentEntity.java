package ch.unil.fcrepo4.spring.data.core.mapping;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.mapping.PersistentProperty;
import org.springframework.data.mapping.SimplePropertyHandler;
import org.springframework.data.mapping.model.BasicPersistentEntity;
import org.springframework.data.util.TypeInformation;

import java.lang.reflect.Method;

/**
 * @author gushakov
 */
public class GenericFedoraPersistentEntity<T> extends BasicPersistentEntity<T, FedoraPersistentProperty>
        implements FedoraPersistentEntity<T> {
    private static final Logger logger = LoggerFactory.getLogger(GenericFedoraPersistentEntity.class);

    public GenericFedoraPersistentEntity(TypeInformation<T> information) {
        super(information);
    }

    @Override
    public FedoraPersistentProperty findGetterProperty(Method getter){
        final FedoraPersistentProperty found[] = new FedoraPersistentProperty[]{null};
        doWithProperties((FedoraPersistentProperty property) -> {
            if (property.getGetter() != null) {
                if (property.getGetter().getName().equals(getter.getName())){
                    found[0] = property;
                }
            }
            else {
                logger.warn("No getter for property {}", property);
            }
        });
        return found[0];
    }

    @Override
    public FedoraPersistentProperty findSetterProperty(Method setter) {
        final FedoraPersistentProperty found[] = new FedoraPersistentProperty[]{null};
        doWithProperties((FedoraPersistentProperty property) -> {
            if (property.getSetter() != null) {
                if (property.getSetter().getName().equals(setter.getName())){
                    found[0] = property;
                }
            }
            else {
                logger.warn("No setter for property {}", property);
            }
        });
        return found[0];
    }

    @Override
    public void doWithSimplePersistentProperties(SimplePropertyHandler simplePropertyHandler){
        doWithProperties((PersistentProperty<?> property) -> {
           if (property instanceof SimpleFedoraPersistentProperty){
               simplePropertyHandler.doWithPersistentProperty(property);
           }
        });
    }

}
