package org.hxm.myspring.postprocessor;

import org.hxm.myspring.factory.MyApplicationContext;
import org.hxm.myspring.factory.MyBeanDefinitionRegistry;
import org.hxm.myspring.factory.MyBeanFactory;

public interface MyBeanFactoryPostProcessor {

    public void postProcessBeanDefinitionRegistry(MyBeanDefinitionRegistry registry);
}
