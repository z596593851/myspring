package org.hxm.myspring.annotation;

import org.hxm.myspring.config.MyAutoConfigurationImportSelector;

import java.lang.annotation.*;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Inherited
@MyImport(MyAutoConfigurationImportSelector.class)
public @interface MyEnableAutoConfiguration {
}
