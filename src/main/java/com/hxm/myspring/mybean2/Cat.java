package com.hxm.myspring.mybean2;

import org.hxm.myspring.annotation.MyAutowired;
import org.hxm.myspring.annotation.MyComponent;

@MyComponent
public class Cat {

    @MyAutowired
    Person person;

    public void catSay(){
        System.out.println("miao miao");
    }

    public void say(){
        person.personSay();
    }
}
