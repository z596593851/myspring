package com.hxm.myspring.mybean;

import org.hxm.myspring.annotation.MyValue;

public class Man {

    @MyValue("xiaoming")
    String name;

    @MyValue("male")
    String sex;

    public void say(){
        System.out.println(this.name+"===="+this.sex);
    }
}
