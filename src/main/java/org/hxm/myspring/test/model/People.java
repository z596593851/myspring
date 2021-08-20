package org.hxm.myspring.test.model;

import org.hxm.myspring.annotation.MyComponent;
import org.hxm.myspring.annotation.MyValue;

@MyComponent
public class People {
    @MyValue("张三")
    private String name;
    @MyValue("10")
    private String age;


    public String say(){
        return "姓名:"+name+",年龄:"+age;
    }
}
