package ch.unil.fcrepo4.spring.data.core.mapping;

import org.springframework.data.mapping.PersistentProperty;
import org.springframework.data.mapping.PersistentPropertyAccessor;
import org.springframework.data.mapping.SimplePropertyHandler;

/**
 * @author gushakov
 */
public interface SimplePropertyAndValueHandler extends SimplePropertyHandler {

    PersistentPropertyAccessor getPropertyAccessor();

    @Override
    default void doWithPersistentProperty(PersistentProperty<?> property) {
        if (getPropertyAccessor() == null) {
            throw new IllegalStateException("Property accessor is null");
        }
        doWithPersistentPropertyAndValue(property, getPropertyAccessor().getProperty(property));
    }

    void doWithPersistentPropertyAndValue(PersistentProperty<?> property, Object value);

}
