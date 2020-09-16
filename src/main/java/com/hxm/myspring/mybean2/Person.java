package com.hxm.myspring.mybean2;

import com.hxm.myspring.mybean2.Cat;
import org.hxm.myspring.annotation.MyAutowired;
import org.hxm.myspring.annotation.MyComponent;

@MyComponent
public class Person {

    @MyAutowired
    Cat cat;

    public void personSay(){
        System.out.println("ha ha ha");
    }
    public void say(){
        cat.catSay();
    }
}
