package org.hxm.myspring.postprocessor;

import org.hxm.myspring.factory.MyBeanFactory;

public interface MyBeanFactoryAware {
    void setBeanFactory(MyBeanFactory beanFactory);
}
