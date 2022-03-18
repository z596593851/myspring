package com.hxm.myspring.aop2;


import org.springframework.aop.framework.ProxyFactory;

public class UserService {

    public void test(){
        System.out.println("test");
    }

    public static void main(String[] args) {
        UserService target=new UserService();
        ProxyFactory proxyFactory=new ProxyFactory();
        proxyFactory.setTarget(target);
        proxyFactory.addAdvice(new Advice1());
        proxyFactory.addAdvice(new Advice2());
        proxyFactory.addAdvice(new Advice3());

        UserService proxy = (UserService)proxyFactory.getProxy();
        proxy.test();
    }
}
