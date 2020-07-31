package org.hxm.myspring;

import org.hxm.myspring.asm.MySimpleAnnotationMetadata;
import org.hxm.myspring.asm.MySimpleMetadataReader;
import org.hxm.myspring.utils.MyClassUtil;
import org.springframework.core.io.Resource;

import java.lang.reflect.Executable;

public class MyBeanDefinition {

    public static final int AUTOWIRE_NO = 0;

    public static final int AUTOWIRE_BY_NAME = 1;

    public static final int AUTOWIRE_BY_TYPE = 2;

    public static final int AUTOWIRE_CONSTRUCTOR = 3;

    private Object source;

    private String beanClassName;

    volatile Boolean isFactoryBean;

    private Object beanClass;

    private String factoryBeanName;

    private String factoryMethodName;

    private Resource resource;

    private int autowireMode = AUTOWIRE_NO;

    private MySimpleAnnotationMetadata metadata;

    Executable resolvedConstructorOrFactoryMethod;

    volatile Class<?> resolvedTargetType;

    private String scope = "";

    boolean isFactoryMethodUnique = false;

    public String getScope() {
        return scope;
    }

    public void setScope(String scope) {
        this.scope = scope;
    }

    public boolean isSingleton() {
        return "singleton".equals(this.scope) || "".equals(this.scope);
    }

    public Class<?> getBeanClass(){
        return (Class<?>)beanClass;
    }

    public MyBeanDefinition(){}

    public MyBeanDefinition(MySimpleMetadataReader metadataReader){
        this.metadata=metadataReader.getAnnotationMetadata();
        setBeanClassName(this.metadata.getClassName());

    }

    public String getBeanClassName() {
        return beanClassName;
    }

    public void setBeanClassName(String beanClassName) {
        this.beanClassName = beanClassName;
    }

    public Resource getResource() {
        return resource;
    }

    public void setResource(Resource resource) {
        this.resource = resource;
    }

    public MySimpleAnnotationMetadata getMetadata() {
        return metadata;
    }

    public Object getSource() {
        return source;
    }

    public void setSource(Object source) {
        this.source = source;
    }

    public boolean hasBeanClass() {
        return (this.beanClass instanceof Class);
    }

    public Class<?> resolveBeanClass(ClassLoader classLoader) throws Exception{
        String className = getBeanClassName();
        if (className == null) {
            return null;
        }
        Class<?> resolvedClass = MyClassUtil.forName(className.replace("/","."), classLoader);
        this.beanClass = resolvedClass;
        return resolvedClass;
    }

    public void setFactoryBeanName(String factoryBeanName){
        this.factoryBeanName=factoryBeanName;
    }

    public void setFactoryMethodName(String factoryMethodName) {
        this.factoryMethodName = factoryMethodName;
    }

    public void setUniqueFactoryMethodName(String name){
        setFactoryMethodName(name);
        this.isFactoryMethodUnique=true;
    }

    public void setAutowireMode(int autowireMode){
        this.autowireMode=autowireMode;
    }

    public String getFactoryMethodName(){
        return this.factoryMethodName;
    }

    public String getFactoryBeanName(){
        return this.factoryBeanName;
    }
}
