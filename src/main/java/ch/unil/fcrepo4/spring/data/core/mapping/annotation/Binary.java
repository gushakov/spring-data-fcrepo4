package ch.unil.fcrepo4.spring.data.core.mapping.annotation;

import ch.unil.fcrepo4.spring.data.core.Constants;
import org.springframework.data.annotation.Persistent;

import java.lang.annotation.*;

/**
 * @author gushakov
 */
@Inherited
@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Persistent
public @interface Binary {
    String mimetype() default Constants.MIME_TYPE_TEXT_XML;
}
