package org.hxm.myspring.postprocessor;

import org.hxm.myspring.factory.MyBeanFactory;

public interface MyBeanFactoryPostProcessor {

    public void postProcessBeanDefinitionRegistry(MyBeanFactory registry);
}
