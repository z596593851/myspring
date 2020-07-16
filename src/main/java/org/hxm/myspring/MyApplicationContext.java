package org.hxm.myspring;

public class MyApplicationContext {
    private MyScanner scanner;

    private MyBeanFactory beanFactory=new MyBeanFactory();


    public MyApplicationContext(){
        this.scanner=new MyScanner(this);
    }

    public MyApplicationContext(String... basePackages){
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
        finishBeanFactoryInitialization(beanFactory);

    }

    protected void finishBeanFactoryInitialization(MyBeanFactory beanFactory){

    }

}
