package com.hxm.myspring.aop2;

import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;

public class Advice2 implements MethodInterceptor {
    @Override
    public Object invoke(MethodInvocation invocation) throws Throwable {
        System.out.println("拦截器2");
        Object proceed = invocation.proceed();
        return proceed;
    }
}
