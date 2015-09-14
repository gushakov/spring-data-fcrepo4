package ch.unil.fcrepo4.spring.data.core.mapping.annotation;

import ch.unil.fcrepo4.spring.data.core.Constants;
import org.springframework.data.annotation.Persistent;

import java.lang.annotation.*;

/**
 * @author gushakov
 */
@Inherited
@Target({ElementType.FIELD, ElementType.ANNOTATION_TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Persistent
public @interface Property {
    String localName() default Constants.DEFAULT_ANNOTATION_STRING_VALUE_TOKEN;
    String uriNs() default Constants.DEFAULT_ANNOTATION_STRING_VALUE_TOKEN;
}
