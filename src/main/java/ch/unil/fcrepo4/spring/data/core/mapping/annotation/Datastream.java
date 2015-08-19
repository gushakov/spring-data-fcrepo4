package ch.unil.fcrepo4.spring.data.core.mapping.annotation;

import ch.unil.fcrepo4.spring.data.core.Constants;
import org.springframework.data.annotation.Persistent;

import java.lang.annotation.*;

/**
 * @author gushakov
 */
@Inherited
@Target({ElementType.FIELD, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Persistent
public @interface Datastream {
    String path() default Constants.DEFAULT_ANNOTATION_STRING_VALUE_TOKEN;
    String mimetype() default Constants.MIME_TYPE_TEXT_XML;
    boolean lazyLoad() default false;
}
