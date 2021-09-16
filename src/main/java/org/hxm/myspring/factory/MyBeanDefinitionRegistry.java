package org.hxm.myspring.factory;

public interface MyBeanDefinitionRegistry {

    void registerBeanDefinition(String beanName, MyBeanDefinition beanDefinition);
}
