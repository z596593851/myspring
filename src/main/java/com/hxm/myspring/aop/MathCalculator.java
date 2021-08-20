package com.hxm.myspring.aop;

import org.springframework.stereotype.Component;

@Component
public class MathCalculator implements IMathCalculator {

     @Override
     public int div(int i, int j){
        System.out.println("执行除法");
        return i/j;
    }
}
