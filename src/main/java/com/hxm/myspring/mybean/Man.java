package com.hxm.myspring.mybean;

import org.hxm.myspring.annotation.MyValue;
import org.springframework.context.annotation.Configuration;


public class Man {

    //todo 私有或者受保护的类型好像没法注入
    @MyValue("xiaoming")
    public String name;

    @MyValue("male")
    public String sex;

    public void say(){
        System.out.println(this.name+"===="+this.sex);
    }
}
