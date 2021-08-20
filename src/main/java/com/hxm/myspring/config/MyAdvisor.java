package com.hxm.myspring.config;

import org.springframework.aop.Pointcut;
import org.springframework.aop.support.AbstractBeanFactoryPointcutAdvisor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

public class MyAdvisor extends AbstractBeanFactoryPointcutAdvisor {

    @Autowired
    private MyPointCut pointCut;

    @Override
    public Pointcut getPointcut() {
        return pointCut;
    }
}
