package com.github.doublebin.springfox.bridge.core.builder.annotations;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * mark class for building controller class with annotation: io.swagger.annotations.Api
 */
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface BridgeApi {

    String value() default "";

    String description() default "";

    @Deprecated
    boolean coverAll() default true;

    String[] tags() default "";

    String produces() default "";

    String consumes() default "";

    String protocols() default "";

    boolean hidden() default false;

}
