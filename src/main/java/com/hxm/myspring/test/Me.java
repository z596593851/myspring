package com.hxm.myspring.test;

import org.hxm.myspring.annotation.MyAutowired;
import org.hxm.myspring.annotation.MyScope;
import org.hxm.myspring.annotation.MyValue;

@MyScope("nosingetlon")
public class Me {

    @MyValue("xiaoming")
    String name;

    @MyAutowired
    Object o;
}
