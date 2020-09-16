package com.hxm.myspring.test;

import com.hxm.myspring.mybean.Women;
import com.hxm.myspring.mybean2.Cat;
import com.hxm.myspring.mybean2.Person;
import org.hxm.myspring.MyApplicationContext;

public class TestXunhuan {
    public static void main(String[] args) throws Exception{
        MyApplicationContext applicationContext=new MyApplicationContext("com.hxm.myspring.mybean2");
        Cat cat=(Cat) applicationContext.getBean("Cat");
        Person p=(Person) applicationContext.getBean("Person");
        cat.say();
        p.say();
    }
}
