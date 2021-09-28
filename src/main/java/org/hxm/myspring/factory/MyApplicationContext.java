package org.hxm.myspring.factory;

import org.hxm.myspring.postprocessor.MyAutowiredProcessor;
import org.hxm.myspring.postprocessor.MyBeanFactoryPostProcessor;
import org.hxm.myspring.postprocessor.MyBeanPostProcessor;
import org.hxm.myspring.utils.MyClassUtil;
import org.hxm.myspring.web.MyServletContextInitializer;
import org.hxm.myspring.web.MyServletContextInitializerBeans;
import org.hxm.myspring.web.MyTomcatWebServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.lang.Nullable;
import org.springframework.web.context.WebApplicationContext;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import java.util.Collection;
import java.util.List;

public class MyApplicationContext implements MyBeanDefinitionRegistry {
    Logger logger= LoggerFactory.getLogger(MyApplicationContext.class);
    public static final String AUTOWIRED_ANNOTATION_PROCESSOR_BEAN_NAME =
            "org.hxm.myspring.postprocessor.internalAutowiredAnnotationProcessor";

    private final MyScanner scanner;
    private volatile MyTomcatWebServer webServer;
    private MyLifecycleProcessor lifecycleProcessor;
    private final MyBeanFactory beanFactory=new MyBeanFactory();
    private ServletContext servletContext;


    public MyApplicationContext(){
        registerAnnotationConfigProcessors();
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

    @Override
    public void registerBeanDefinition(String beanName, MyBeanDefinition beanDefinition){
        this.beanFactory.registerBeanDefinition(beanName,beanDefinition);
    }

    public void refresh(){
        try {
            /*
                执行所有BeanDefinitionRegistryPostProcessor的postProcessBeanDefinitionRegistry
                BeanDefinitionRegistryPostProcessor是BeanFactoryPostProcessor的子接口，先于后者执行
                其中MyConfigurationClassPostProcessor用来解析@Configuration、@Component 、@ComponentScan 、@Import
                并注册到Beandefination中
             */
            invokeBeanFactoryPostProcessors(beanFactory);

            /*
                注册BeanPostProcessor,
                其中一种AutowiredAnnotationBeanPostProcessor用来解析@Autowired、@Value 、@Inject完成自动注入
             */
            registerBeanPostProcessors(beanFactory);

            // 子类扩展刷新，如创建tomcat容器
            onRefresh();

            //根据BeanDefinition创建实例和属性注入
            finishBeanFactoryInitialization(beanFactory);

            finishRefresh();
        } catch (Exception exception) {
            exception.printStackTrace();
        }

    }

    private void registerBeanPostProcessors(MyBeanFactory beanFactory) {
        String[] postProcessorNames = beanFactory.getBeanNamesForType(MyBeanPostProcessor.class, false);
        for(String ppName:postProcessorNames){
            MyBeanPostProcessor beanPostProcessor=(MyBeanPostProcessor)beanFactory.getBean(ppName);
            beanFactory.addBeanPostProcessor(beanPostProcessor);
        }
    }

    private void registerAnnotationConfigProcessors(){
        MyBeanDefinition def=new MyBeanDefinition(MyAutowiredProcessor.class);
        registerBeanDefinition(AUTOWIRED_ANNOTATION_PROCESSOR_BEAN_NAME,def);
    }

    private void finishRefresh() {
        //从IOC容器中找出所有的 Lifecycle 类型的Bean，遍历回调 start 方法
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

    private void onRefresh() throws Exception {
        createWebServer();
    }

    private void createWebServer() throws Exception {
        MyTomcatWebServer webServer=this.webServer;
        if(webServer==null){
            MyTomcatServletWebServerFactory factory=new MyTomcatServletWebServerFactory();
            //加载通过spi注册的MyDispatcherServletRegistrationConfiguration
            beanFactory.getBean("myServletRegistrationBean");
            this.webServer=factory.getWebServer(getSelfInitializer());
            //向容器中注入Lifecycle类型的bean，在 finishRefresh 时调用
            beanFactory.registerSingleton("webServerStartStop",
                    new MyWebServerStartStopLifecycle(this,this.webServer));
        }
    }

    /**
     * 返回一个 MyServletContextInitializer.onStartup 的函数引用
     * selfInitialize相当于 MyServletContextInitializer 的匿名实现类
     */
    private MyServletContextInitializer getSelfInitializer() {
        return this::selfInitialize;
    }

    private void selfInitialize(ServletContext servletContext) throws ServletException {
        prepareWebApplicationContext(servletContext);
        MyClassUtil.registerEnvironmentBeans(beanFactory, servletContext);
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
        beanFactory.preInstantiateSingletons();
    }

    protected void invokeBeanFactoryPostProcessors(MyBeanFactory beanFactory){
        List<MyBeanFactoryPostProcessor> currentRegistryProcessors=beanFactory.getBeanFactoryPostProcessor();
        /*
            执行BeanDefinitionRegistryPostProcessor
            其中MyConfigurationClassPostProcessor用来解析@Configuration、@Component 、@ComponentScan 、@Import
            并注册到Beandefination中
         */
        invokeBeanDefinitionRegistryPostProcessors(currentRegistryProcessors,beanFactory);
//        beanFactory.getBeanNamesForType(Main.class,false);


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

    public MyBeanFactory getBeanFactory() {
        return beanFactory;
    }
}
