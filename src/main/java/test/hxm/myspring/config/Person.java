package test.hxm.myspring.config;

import org.hxm.myspring.annotation.MyValue;
import org.hxm.myspring.stereotype.MyComponent;

@MyComponent
public class Person {
    @MyValue("xiaoming")
    private String name;

    public String getName(){
        return name;
    }
}
