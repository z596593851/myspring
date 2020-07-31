package org.hxm.myspring.annotation;

import java.lang.annotation.*;

@Target({ElementType.METHOD, ElementType.ANNOTATION_TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface MyBean {

    String[] value() default {};

    String[] name() default {};
}
