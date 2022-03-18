package com.hxm.myspring.aop2;

import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;

public class Advice1 implements MethodInterceptor {
    @Override
    public Object invoke(MethodInvocation invocation) throws Throwable {
        System.out.println("拦截器1-前");
        Object proceed = invocation.proceed();
        System.out.println("拦截器1-后");
        return proceed;
    }
}
