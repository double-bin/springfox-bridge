package com.github.doublebin.springfox.bridge.core.builder.annotations;

import java.lang.annotation.*;

@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface BridgeGroup {
    String value() default "";
}
