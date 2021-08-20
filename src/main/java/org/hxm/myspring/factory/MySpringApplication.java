package org.hxm.myspring.factory;

import org.hxm.myspring.utils.MyBeanNameGenerator;

public class MySpringApplication {
    private Class<?> primarySources;

    public MySpringApplication(Class<?> primarySources) {
        this.primarySources = primarySources;
    }

    public static void run(Class<?> primarySources){
        new MySpringApplication(primarySources).run();
    }

    public void run(){
        MyApplicationContext context=new MyApplicationContext();
        prepareContext(context);
        refreshContext(context);
    }

    private void refreshContext(MyApplicationContext context) {
        context.refresh();
    }

    private void prepareContext(MyApplicationContext context) {
        MyBeanDefinition mbd=new MyBeanDefinition(primarySources);
        String beanName= MyBeanNameGenerator.generateBeanName(mbd);
        context.registerBeanDefinition(beanName,mbd);
    }
}
