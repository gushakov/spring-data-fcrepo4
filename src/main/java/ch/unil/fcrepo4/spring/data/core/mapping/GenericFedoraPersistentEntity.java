package ch.unil.fcrepo4.spring.data.core.mapping;

import org.springframework.data.mapping.Association;
import org.springframework.data.mapping.PersistentProperty;
import org.springframework.data.mapping.SimplePropertyHandler;
import org.springframework.data.mapping.model.BasicPersistentEntity;
import org.springframework.data.util.TypeInformation;

import java.lang.reflect.Method;
import java.util.Optional;

/**
 * @author gushakov
 */
public class GenericFedoraPersistentEntity<T> extends BasicPersistentEntity<T, FedoraPersistentProperty>
        implements FedoraPersistentEntity<T> {

    public GenericFedoraPersistentEntity(TypeInformation<T> information) {
        super(information);
    }

    @Override
    public Optional<PersistentProperty<?>> findPropertyForGetterOrSetter(Method getterOrSetter) {
        MethodMatchingPropertyFinder finder = new MethodMatchingPropertyFinder(getterOrSetter);
        doWithProperties(finder);
        return finder.getMatching();
    }

    @Override
    public Optional<Association<? extends PersistentProperty<?>>> findAssociationForGetterOrSetter(Method getterOrSetter) {
        MethodMatchingAssociationFinder finder = new MethodMatchingAssociationFinder(getterOrSetter);
        doWithAssociations(finder);
        return finder.getMatching();
    }

}
