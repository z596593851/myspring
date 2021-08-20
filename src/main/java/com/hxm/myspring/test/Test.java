package com.hxm.myspring.test;

import com.hxm.myspring.aop.IMathCalculator;
import com.hxm.myspring.aop.MathCalculator;
import com.hxm.myspring.aop.MyBean;
import com.hxm.myspring.config.AopConfig;
import com.hxm.myspring.config.TxDemo2;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

public class Test {
    public static void main(String[] args) {
        AnnotationConfigApplicationContext applicationContext=new AnnotationConfigApplicationContext(AopConfig.class);
        TxDemo2 bean=applicationContext.getBean(TxDemo2.class);
        bean.test2();
    }

}
