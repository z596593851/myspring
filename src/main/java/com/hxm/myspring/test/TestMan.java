package com.hxm.myspring.test;

import com.hxm.myspring.mybean.Child;
import com.hxm.myspring.mybean.Man;
import com.hxm.myspring.mybean.Women;
import org.hxm.myspring.MyApplicationContext;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

public class TestMan {
    public static void main(String[] args) throws Exception{
        MyApplicationContext applicationContext=new MyApplicationContext("com.hxm.myspring.mybean");
//        Man m=(Man) applicationContext.getBean("Man");
//        m.say();
//        Child c=(Child) applicationContext.getBean("Child");
//        c.say();
        Women w=(Women)applicationContext.getBean("Women");
        w.say();
//        ApplicationContext applicationContext1=new ClassPathXmlApplicationContext();
//        applicationContext1.getBean()
    }

}
