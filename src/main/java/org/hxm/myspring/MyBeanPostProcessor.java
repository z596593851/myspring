package org.hxm.myspring;

public interface MyBeanPostProcessor {
    public void postProcessProperties(Object bean, String beanName) throws Throwable;
}
