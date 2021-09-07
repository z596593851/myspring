package org.hxm.myspring.factory;

import org.hxm.myspring.postprocessor.MyBeanFactoryPostProcessor;
import org.hxm.myspring.utils.MyClassUtil;
import org.hxm.myspring.web.MyServletContextInitializer;
import org.hxm.myspring.web.MyServletContextInitializerBeans;
import org.hxm.myspring.web.MyTomcatWebServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.lang.Nullable;
import org.springframework.web.context.WebApplicationContext;
import sun.tools.native2ascii.Main;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import java.util.Collection;
import java.util.List;

public class MyApplicationContext {
    Logger logger= LoggerFactory.getLogger(MyApplicationContext.class);

    private MyScanner scanner;
    private volatile MyTomcatWebServer webServer;
    private MyLifecycleProcessor lifecycleProcessor;
    private MyBeanFactory beanFactory=new MyBeanFactory();
    private ServletContext servletContext;


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
        //扫描Import
        invokeBeanFactoryPostProcessors(beanFactory);

        // Initialize other special beans in specific context subclasses.
        onRefresh();

        //根据BeanDefinition创建实例和属性注入
        finishBeanFactoryInitialization(beanFactory);

        // Last step: publish corresponding event.
        finishRefresh();

    }

    private void finishRefresh() {
        // Initialize lifecycle processor for this context.
        initLifecycleProcessor();
        getLifecycleProcessor().onRefresh();
    }

    MyLifecycleProcessor getLifecycleProcessor(){
        return this.lifecycleProcessor;
    }

    private void initLifecycleProcessor() {
        MyLifecycleProcessor defaultProcessor = new MyLifecycleProcessor();
        defaultProcessor.setBeanFactory(beanFactory);
        this.lifecycleProcessor=defaultProcessor;
    }

    private void onRefresh() {
        createWebServer();
    }

    private void createWebServer() {
        MyTomcatWebServer webServer=this.webServer;
        if(webServer==null){
            MyTomcatServletWebServerFactory factory=new MyTomcatServletWebServerFactory();
            //todo 注册myServletRegistrationBean
            try {
                beanFactory.getBean("myServletRegistrationBean");
            } catch (Exception exception) {
                exception.printStackTrace();
            }
            this.webServer=factory.getWebServer(getSelfInitializer());
            //todo 注册bean
            beanFactory.registerSingleton("webServerStartStop",
                    new MyWebServerStartStopLifecycle(this,this.webServer));
        }
    }

    private MyServletContextInitializer getSelfInitializer() {
        return this::selfInitialize;
    }

    private void selfInitialize(ServletContext servletContext) throws ServletException {
        prepareWebApplicationContext(servletContext);
//        registerApplicationScope(servletContext);
        MyClassUtil.registerEnvironmentBeans(beanFactory, servletContext,null);
        for (MyServletContextInitializer beans : getServletContextInitializerBeans()) {
            beans.onStartup(servletContext);
        }
    }

    protected Collection<MyServletContextInitializer> getServletContextInitializerBeans() {
        return new MyServletContextInitializerBeans(beanFactory);
    }

    public void setServletContext(@Nullable ServletContext servletContext) {
        this.servletContext = servletContext;
    }

    protected void prepareWebApplicationContext(ServletContext servletContext) {
        Object rootContext = servletContext.getAttribute(WebApplicationContext.ROOT_WEB_APPLICATION_CONTEXT_ATTRIBUTE);
        if (rootContext != null) {
            if (rootContext == this) {
                throw new IllegalStateException(
                        "Cannot initialize context because there is already a root application context present - "
                                + "check whether you have multiple ServletContextInitializers!");
            }
            return;
        }
        servletContext.log("Initializing Spring embedded WebApplicationContext");
        try {
            servletContext.setAttribute(WebApplicationContext.ROOT_WEB_APPLICATION_CONTEXT_ATTRIBUTE, this);
            setServletContext(servletContext);
        }
        catch (RuntimeException | Error ex) {
            logger.error("Context initialization failed", ex);
            servletContext.setAttribute(WebApplicationContext.ROOT_WEB_APPLICATION_CONTEXT_ATTRIBUTE, ex);
            throw ex;
        }
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
