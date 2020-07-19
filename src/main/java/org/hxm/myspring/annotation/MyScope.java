package org.hxm.myspring.annotation;

import java.lang.annotation.*;

@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface MyScope {

    String value() default "";

    String scopeName() default "";
}
