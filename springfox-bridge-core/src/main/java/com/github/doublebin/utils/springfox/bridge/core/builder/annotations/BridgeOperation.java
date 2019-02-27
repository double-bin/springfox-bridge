package com.github.doublebin.utils.springfox.bridge.core.builder.annotations;
import io.swagger.annotations.Extension;
import io.swagger.annotations.ExtensionProperty;
import io.swagger.annotations.ResponseHeader;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface BridgeOperation {

    String value();

    String notes() default "";

    /**
     *
     * @return
     */
    String[] tags() default "";

    Class<?> response() default Void.class;

    String responseContainer() default "";

    String responseReference() default "";

    String httpMethod() default "";

    String produces() default "";

    String consumes() default "";

    String protocols() default "";

    /*Authorization[] authorizations() default @Authorization(value = "");*/

    boolean hidden() default false;

    ResponseHeader[] responseHeaders() default @ResponseHeader(name = "", response = Void.class);

    int code() default 200;

    Extension[] extensions() default @Extension(properties = @ExtensionProperty(name = "", value = ""));
}
