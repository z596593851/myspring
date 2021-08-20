package com.hxm.myspring.config;

import org.springframework.stereotype.Service;

@Service
public class MyAopDemo {

    @MyAop
    public void test(){
        System.out.println("im aop demo");
    }
}
