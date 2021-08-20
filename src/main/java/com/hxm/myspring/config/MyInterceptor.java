package com.hxm.myspring.config;

import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.springframework.stereotype.Component;

public class MyInterceptor implements MethodInterceptor {

    @Override
    public Object invoke(MethodInvocation invocation) throws Throwable {
        System.out.println("执行前");
        try {
            return invocation.proceed();
        } finally {
            System.out.println("执行后");
        }
    }
}
