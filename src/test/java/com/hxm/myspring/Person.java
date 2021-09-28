package com.hxm.myspring;

import org.hxm.myspring.annotation.MyValue;
import org.hxm.myspring.stereotype.MyComponent;

@MyComponent("pp")
public class Person {

    @MyValue("haha")
    private String name;

    private int age;
}
