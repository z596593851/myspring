package org.hxm.myspring.postprocessor;

import org.hxm.myspring.MyBeanFactory;

public interface MyBeanFactoryPostProcessor {

    public void postProcessBeanDefinitionRegistry(MyBeanFactory registry);
}
