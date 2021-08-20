package com.hxm.myspring.config;

import org.springframework.aop.support.StaticMethodMatcherPointcut;
import org.springframework.beans.factory.annotation.Autowired;

import java.lang.reflect.Method;

public class MyPointCut extends StaticMethodMatcherPointcut {

    /**
     * 注解解析器
     */
    @Autowired
    MyAttributeSource attributeSource;

    @Override
    public boolean matches(Method method, Class<?> targetClass) {
        return attributeSource.getAttribute(method,targetClass);
    }
}
