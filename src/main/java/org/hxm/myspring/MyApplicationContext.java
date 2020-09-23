package org.hxm.myspring;

import org.hxm.myspring.postprocessor.MyBeanFactoryPostProcessor;
import sun.tools.native2ascii.Main;

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
        //扫描标注了@MyComponent的类(@MyConfiguration里也有@MyComponent)为BeanDefinition
        this.scanner.scan(basePackages);
        refresh();
    }

    public void registerBeanDefinition(String beanName, MyBeanDefinition beanDefinition){
        this.beanFactory.registerBeanDefinition(beanName,beanDefinition);
    }

    public void refresh(){
        //扫描标注了@MyBean的类(方法)为BeanDefinition
        invokeBeanFactoryPostProcessors(beanFactory);

        //根据BeanDefinition创建实例和属性注入
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
        List<MyBeanFactoryPostProcessor> currentRegistryProcessors=beanFactory.getBeanFactoryPostProcessor();
        //解析@MyConfiguration和@MyBean
        invokeBeanDefinitionRegistryPostProcessors(currentRegistryProcessors,beanFactory);
        beanFactory.getBeanNamesForType(Main.class,false);


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
