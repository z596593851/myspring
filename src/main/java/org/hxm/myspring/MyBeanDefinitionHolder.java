package org.hxm.myspring;

public class MyBeanDefinitionHolder {
    private MyBeanDefinition beanDefinition;
    private String beanName;

    public MyBeanDefinitionHolder(MyBeanDefinition beanDefinition, String beanName) {
        this.beanDefinition = beanDefinition;
        this.beanName = beanName;
    }

    public MyBeanDefinition getBeanDefinition() {
        return beanDefinition;
    }

    public String getBeanName() {
        return beanName;
    }
}
