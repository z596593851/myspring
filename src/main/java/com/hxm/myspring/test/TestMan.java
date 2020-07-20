package com.hxm.myspring.test;

import com.hxm.myspring.mybean.Man;
import org.hxm.myspring.MyApplicationContext;

public class TestMan {
    public static void main(String[] args) throws Exception{
        MyApplicationContext applicationContext=new MyApplicationContext("com.hxm.myspring.mybean");
        Man m=(Man) applicationContext.getBean("Man");
        m.say();
    }
}
