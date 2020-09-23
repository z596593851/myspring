package com.hxm.myspring.configtest;

import org.hxm.myspring.annotation.MyAutowired;
import org.hxm.myspring.annotation.MyComponent;

@MyComponent
public class Bean2 {

    @MyAutowired
    private Bean1 bean1;

    public void say(){
        System.out.println("im bean2");
        bean1.say();
    }
}
