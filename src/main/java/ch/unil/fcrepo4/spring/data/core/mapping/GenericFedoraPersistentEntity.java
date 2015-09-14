package ch.unil.fcrepo4.spring.data.core.mapping;

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

    public GenericFedoraPersistentEntity(TypeInformation<T> information) {
        super(information);
    }

    @Override
    public FedoraPersistentProperty findPropertyForGetterOrSetter(Method getterOrSetter) {
        final FedoraPersistentProperty found[] = new FedoraPersistentProperty[]{null};
        doWithProperties((FedoraPersistentProperty property) -> {
            if ((property.getGetter() != null && property.getGetter().getName().equals(getterOrSetter.getName()))
                    || (property.getSetter() != null && property.getSetter().getName().equals(getterOrSetter.getName()))) {
                found[0] = property;
            }
        });
        return found[0];
    }


    @Override
    public void doWithSimplePersistentProperties(SimplePropertyHandler simplePropertyHandler) {
        doWithProperties((PersistentProperty<?> property) -> {
            if (property instanceof SimpleFedoraResourcePersistentProperty) {
                simplePropertyHandler.doWithPersistentProperty(property);
            }
        });
    }

}
