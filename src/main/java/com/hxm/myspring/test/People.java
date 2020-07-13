package com.hxm.myspring.test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

//@Component
public class People {


    @Value("xiao ming")
    public String name;

    public void say(){
        System.out.println(name);
    }

}
