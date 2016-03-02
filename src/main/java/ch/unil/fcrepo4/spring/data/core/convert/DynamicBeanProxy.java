package ch.unil.fcrepo4.spring.data.core.convert;

import ch.unil.fcrepo4.spring.data.core.mapping.FedoraPersistentEntity;
import org.springframework.data.mapping.PersistentProperty;
import org.springframework.data.mapping.PersistentPropertyAccessor;

/**
 * @author gushakov
 */
public interface DynamicBeanProxy {

    Object __getBean();

    FedoraPersistentEntity __getBeanEntity();

    PersistentPropertyAccessor __getPropertyAccessor();

    boolean __isPropertyUpdated(PersistentProperty property);
}
