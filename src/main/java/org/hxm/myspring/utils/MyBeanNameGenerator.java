package org.hxm.myspring.utils;

import org.hxm.myspring.factory.MyBeanDefinition;

public class MyBeanNameGenerator {
    public static String generateBeanName(MyBeanDefinition beanDefinition){
        String className=beanDefinition.getBeanClassName();
        String shortName=MyBeanNameGenerator.getShortName(className);
        return MyBeanNameGenerator.decapitalize(shortName);
    }

    public static String getShortName(String beanClassName){
        int lastDotIndex = beanClassName.lastIndexOf(".");
        int nameEndIndex = beanClassName.length();
        return beanClassName.substring(lastDotIndex + 1, nameEndIndex);

    }
    public static String decapitalize(String name) {
        if (name == null || name.length() == 0) {
            return name;
        }
        if (name.length() > 1 && Character.isUpperCase(name.charAt(1)) &&
                Character.isUpperCase(name.charAt(0))){
            return name;
        }
        char chars[] = name.toCharArray();
        chars[0] = Character.toLowerCase(chars[0]);
        return new String(chars);
    }
}
