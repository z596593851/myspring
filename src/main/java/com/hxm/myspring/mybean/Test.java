package com.hxm.myspring.mybean;

import com.hxm.myspring.configtest.Bean1;
import com.hxm.myspring.configtest.Bean2;
import org.hxm.myspring.MyApplicationContext;

public class Test {
    public static void main(String[] args) throws Exception {
        MyApplicationContext applicationContext=new MyApplicationContext("com.hxm.myspring.configtest");
        Bean2 bean2=(Bean2) applicationContext.getBean("bean2");
        bean2.say();
    }
}
