package org.hxm.myspring;

import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.BeanDefinitionRegistryPostProcessor;

import java.util.Collection;
import java.util.List;

public class MyApplicationContext {
    private MyScanner scanner;

    private MyBeanFactory beanFactory=new MyBeanFactory();


    public MyApplicationContext(){
        this.scanner=new MyScanner(this);
    }

    public MyApplicationContext(String... basePackages){
        this();
        scan(basePackages);
    }

    public void scan(String... basePackages){
        this.scanner.scan(basePackages);
        refresh();
    }

    public void registerBeanDefinition(String beanName, MyBeanDefinition beanDefinition){
        this.beanFactory.registerBeanDefinition(beanName,beanDefinition);
    }

    public void refresh(){
        //省略一些方法
        invokeBeanFactoryPostProcessors(beanFactory);
        finishBeanFactoryInitialization(beanFactory);

    }

    protected void finishBeanFactoryInitialization(MyBeanFactory beanFactory){
        try {
            beanFactory.preInstantiateSingletons();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    protected void invokeBeanFactoryPostProcessors(MyBeanFactory beanFactory){
        beanFactory.getBeanNamesForType();
        List<MyBeanFactoryPostProcessor> currentRegistryProcessors=beanFactory.getBeanFactoryPostProcessor();
        invokeBeanDefinitionRegistryPostProcessors(currentRegistryProcessors,beanFactory);

    }

    private void invokeBeanDefinitionRegistryPostProcessors(
            Collection<? extends MyBeanFactoryPostProcessor> postProcessors, MyBeanFactory registry) {

        for (MyBeanFactoryPostProcessor postProcessor : postProcessors) {
            postProcessor.postProcessBeanDefinitionRegistry(registry);
        }
    }

    public Object getBean(String beanName) throws Exception{
       return this.beanFactory.getBean(beanName);
    }



}
