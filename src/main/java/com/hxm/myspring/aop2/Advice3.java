package com.hxm.myspring.aop2;

import org.springframework.aop.MethodBeforeAdvice;

import java.lang.reflect.Method;

public class Advice3 implements MethodBeforeAdvice {
    @Override
    public void before(Method method, Object[] args, Object target) throws Throwable {
        System.out.println("advice3");
        //会自动执行invocation.proceed();
    }
}
