package com.hxm.myspring.config;

import java.lang.annotation.*;

/**
 * @author xiaoming
 */
@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Inherited
@Documented
public @interface MyAop {
}
