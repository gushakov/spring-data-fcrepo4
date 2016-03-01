package ch.unil.fcrepo4.spring.data.core.mapping;

import org.springframework.data.mapping.PersistentProperty;
import org.springframework.data.mapping.SimplePropertyHandler;

import java.lang.reflect.Method;
import java.util.Optional;

/**
 * @author gushakov
 */
public class MethodMatchingPropertyFinder implements SimplePropertyHandler {
    private PersistentProperty<?> matching;

    private Method getterOrSetter;

    public MethodMatchingPropertyFinder(Method getterOrSetter) {
        this.getterOrSetter = getterOrSetter;
    }

    @Override
    public void doWithPersistentProperty(PersistentProperty<?> property) {
        if ((property.getGetter() != null && property.getGetter().getName().equals(getterOrSetter.getName()))
                || (property.getSetter() != null && property.getSetter().getName().equals(getterOrSetter.getName()))) {
            matching = property;
        }
    }

    public Optional<PersistentProperty<?>> getMatching(){
        return Optional.ofNullable(matching);
    }
}
