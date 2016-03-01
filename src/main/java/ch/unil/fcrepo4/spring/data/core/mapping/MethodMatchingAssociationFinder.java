package ch.unil.fcrepo4.spring.data.core.mapping;

import org.springframework.data.mapping.Association;
import org.springframework.data.mapping.PersistentProperty;
import org.springframework.data.mapping.SimpleAssociationHandler;

import java.lang.reflect.Method;
import java.util.Optional;

/**
 * @author gushakov
 */
public class MethodMatchingAssociationFinder implements SimpleAssociationHandler {

    private Association<? extends PersistentProperty<?>> matching;

    private Method getterOrSetter;

    public MethodMatchingAssociationFinder(Method getterOrSetter) {
        this.getterOrSetter = getterOrSetter;
    }

    @Override
    public void doWithAssociation(Association<? extends PersistentProperty<?>> association) {
        if (matching == null && ((association.getInverse().getGetter() != null
                && association.getInverse().getGetter().getName().equals(getterOrSetter.getName()))
                || (association.getInverse().getSetter() != null
                && association.getInverse().getSetter().getName().equals(getterOrSetter.getName())))) {
            matching = association;
        }
    }

    public Optional<Association<? extends PersistentProperty<?>>> getMatching() {
        return Optional.ofNullable(matching);
    }
}
