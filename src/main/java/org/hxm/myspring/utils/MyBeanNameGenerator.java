package org.hxm.myspring.utils;

import org.hxm.myspring.MyApplicationContext;
import org.hxm.myspring.MyBeanDefinition;

public class MyBeanNameGenerator {
    public String generateBeanName(MyBeanDefinition beanDefinition){
        String className=beanDefinition.getBeanClassName();
        int lastDotIndex = className.lastIndexOf(".");
        int nameEndIndex = className.length();
        String shortName = className.substring(lastDotIndex + 1, nameEndIndex);
        return shortName;
    }
}
