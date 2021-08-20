package org.hxm.myspring.test;

import org.hxm.myspring.factory.MyApplicationContext;
import org.hxm.myspring.test.model.People;

public class TestApplication {


    public static void main(String[] args) throws Exception {
        MyApplicationContext applicationContext=new MyApplicationContext("org.hxm.myspring.config");
    }
}
