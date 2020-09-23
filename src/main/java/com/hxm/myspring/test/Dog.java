package com.hxm.myspring.test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

//@Configuration
public class Dog {

    @Value("wang wang")
    String content;

    @Autowired
    People people;

    @Bean
    public Me getMe(){
        return new Me();
    }

    public void say(){
        System.out.println(content);
    }
}
