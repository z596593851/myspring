package com.hxm.myspring.test;

import com.hxm.myspring.config.AopConfig;
import com.hxm.myspring.config.MyAopDemo;
import com.hxm.myspring.config.TxDemo;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

public class TxTest {
    public static void main(String[] args) {
//        AnnotationConfigApplicationContext applicationContext=new AnnotationConfigApplicationContext(AopConfig.class);
//        TxDemo txDemo=applicationContext.getBean(TxDemo.class);
//        txDemo.test1();
//        txDemo.test2();

        AnnotationConfigApplicationContext applicationContext=new AnnotationConfigApplicationContext(AopConfig.class);
        MyAopDemo demo=applicationContext.getBean(MyAopDemo.class);
        demo.test();
    }


}
