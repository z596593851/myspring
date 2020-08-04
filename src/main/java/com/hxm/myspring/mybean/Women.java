package com.hxm.myspring.mybean;

import org.hxm.myspring.annotation.MyComponent;
import org.hxm.myspring.annotation.MyValue;

@MyComponent
public class Women {

    @MyValue("wwwomen")
    public String name;

    public void say(){
        System.out.println("im "+name);
    }
}
