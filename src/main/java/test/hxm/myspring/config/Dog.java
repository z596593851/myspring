package test.hxm.myspring.config;

import org.hxm.myspring.annotation.MyAutowired;
import org.hxm.myspring.stereotype.MyComponent;

@MyComponent
public class Dog {

    @MyAutowired
    Person person;


}
