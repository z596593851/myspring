package com.hxm.myspring.config;

import org.springframework.aop.support.AopUtils;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.core.annotation.AnnotationAttributes;
import org.springframework.transaction.annotation.Transactional;

import java.lang.reflect.Method;

public class MyAttributeSource {
    public boolean getAttribute(Method method, Class<?> targetClass){
        Method specificMethod = AopUtils.getMostSpecificMethod(method, targetClass);
        AnnotationAttributes attributes = AnnotatedElementUtils.findMergedAnnotationAttributes(
                specificMethod, MyAop.class, false, false);
        if(attributes!=null){
            return true;
        }
        return false;
    }
}
