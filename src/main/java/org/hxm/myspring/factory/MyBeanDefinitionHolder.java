package org.hxm.myspring.factory;

import org.springframework.lang.Nullable;

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

    @Override
    public boolean equals(@Nullable Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof MyBeanDefinitionHolder)) {
            return false;
        }
        MyBeanDefinitionHolder otherHolder = (MyBeanDefinitionHolder) other;
        return this.beanDefinition.equals(otherHolder.beanDefinition) &&
                this.beanName.equals(otherHolder.beanName);
    }

    @Override
    public int hashCode() {
        int hashCode = this.beanDefinition.hashCode();
        hashCode = 29 * hashCode + this.beanName.hashCode();
        return hashCode;
    }
}
