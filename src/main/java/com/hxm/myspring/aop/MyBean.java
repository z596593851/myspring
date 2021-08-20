package com.hxm.myspring.aop;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class MyBean {

    @Autowired
    IMathCalculator mathCalculator;

    public void run(){
        mathCalculator.div(1,1);
    }
}
