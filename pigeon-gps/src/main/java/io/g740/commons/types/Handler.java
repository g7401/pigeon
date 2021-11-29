package io.g740.commons.types;

import org.springframework.core.annotation.AliasFor;
import org.springframework.stereotype.Component;

import java.lang.annotation.*;

/**
 * @author bbottong
 */
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Component
public @interface Handler {
    @AliasFor(
            annotation = Component.class
    )
    String value() default "";
}