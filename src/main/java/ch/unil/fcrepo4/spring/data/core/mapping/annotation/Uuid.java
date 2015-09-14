package ch.unil.fcrepo4.spring.data.core.mapping.annotation;

import org.springframework.data.annotation.Persistent;

import java.lang.annotation.*;

/**
 * @author gushakov
 */
@Inherited
@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Property
public @interface Uuid {

}
