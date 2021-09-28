package org.hxm.myspring.postprocessor;

public interface MyBeanPostProcessor {
    void postProcessProperties(Object bean, String beanName) throws Throwable;
}
