package com.hxm.myspring.test;

import org.hxm.myspring.annotation.MyAutowired;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Lookup;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.annotation.AliasFor;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Component
public class Dog {

    @Value("wang wang")
    String content;

    @MyAutowired
    People people;

    public void say(){
        System.out.println(content);
    }
}
