package org.hxm.myspring.annotation;

import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Documented
public @interface MyComponentScan {

    String[] basePackages() default {};
}
