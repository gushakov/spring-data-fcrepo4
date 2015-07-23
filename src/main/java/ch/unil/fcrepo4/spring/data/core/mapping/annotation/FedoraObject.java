package ch.unil.fcrepo4.spring.data.core.mapping.annotation;

import ch.unil.fcrepo4.spring.data.core.Constants;
import org.springframework.data.annotation.Persistent;

import java.lang.annotation.*;

/**
 * @author gushakov
 */
@Inherited
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Persistent
public @interface FedoraObject {
    String namespace() default Constants.DEFAULT_NAMESPACE;
    String uriNs() default Constants.TEST_FEDORA_URI_NAMESPACE;
}
