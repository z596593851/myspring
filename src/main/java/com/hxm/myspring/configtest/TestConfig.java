package com.hxm.myspring.configtest;

import org.hxm.myspring.annotation.MyBean;
import org.hxm.myspring.annotation.MyConfiguration;

@MyConfiguration
public class TestConfig {

    @MyBean
    public Bean1 bean1(){
        return new Bean1();
    }



}
