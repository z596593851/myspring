package org.hxm.myspring;

import java.lang.reflect.Executable;

public class MyBeanDefinition {

    Executable resolvedConstructorOrFactoryMethod;

    volatile Class<?> resolvedTargetType;

    public Class<?> getBeanClass(){
        return null;
    }
}
