package ch.unil.fcrepo4.spring.data.core.mapping.annotation;

import ch.unil.fcrepo4.spring.data.core.mapping.DefaultPathCreator;
import ch.unil.fcrepo4.spring.data.core.mapping.PathCreator;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Persistent;

import java.lang.annotation.*;

/**
 * @author gushakov
 */
@Inherited
@Target({ElementType.FIELD, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Persistent
@Id
public @interface Uuid {
    Class<? extends PathCreator> pathCreator() default DefaultPathCreator.class;
}
