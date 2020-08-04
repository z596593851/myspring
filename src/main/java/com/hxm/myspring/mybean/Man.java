package com.hxm.myspring.mybean;

import org.hxm.myspring.annotation.MyAutowired;
import org.hxm.myspring.annotation.MyComponent;
import org.hxm.myspring.annotation.MyValue;
import org.springframework.context.annotation.Configuration;

@MyComponent
public class Man {

    @MyValue("xiaoming")
    private String name;

    @MyValue("male")
    private String sex;

    @MyAutowired
    private Women women;

    public void say(){
        System.out.println(this.name+"===="+this.sex);
        women.say();
    }
}
