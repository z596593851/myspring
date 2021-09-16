package org.hxm.myspring.annotation;

import java.lang.annotation.*;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Inherited
@MyConfiguration
@MyEnableAutoConfiguration
@MyComponentScan
public @interface MySpringBootApplication {
}
