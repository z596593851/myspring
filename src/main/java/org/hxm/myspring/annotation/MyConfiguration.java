package org.hxm.myspring.annotation;

import java.lang.annotation.*;

/**
 * @author xiaoming
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@MyComponent
public @interface MyConfiguration {
    String value() default "";
    boolean proxyBeanMethods() default true;
}
