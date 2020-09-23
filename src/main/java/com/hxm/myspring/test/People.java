package com.hxm.myspring.test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

@Configuration
public class People {


    @Value("xiao ming")
    public String name;

    public void say(){
        System.out.println(name);
    }

}
