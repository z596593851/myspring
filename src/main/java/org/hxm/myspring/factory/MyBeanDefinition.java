package org.hxm.myspring.factory;

import org.hxm.myspring.asm.MyAnnotationMetadata;
import org.hxm.myspring.asm.MySimpleAnnotationMetadata;
import org.hxm.myspring.asm.MySimpleMetadataReader;
import org.hxm.myspring.asm.MyStandardAnnotationMetadata;
import org.hxm.myspring.utils.MyClassUtil;
import org.springframework.core.ResolvableType;
import org.springframework.core.io.Resource;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;

import java.lang.reflect.Executable;
import java.lang.reflect.Method;
import java.util.LinkedHashMap;
import java.util.Map;

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

    volatile Method factoryMethodToIntrospect;

    private String factoryMethodName;

    private Resource resource;

    private int autowireMode = AUTOWIRE_NO;

    private MyAnnotationMetadata metadata;

    Executable resolvedConstructorOrFactoryMethod;

    volatile Class<?> resolvedTargetType;

    volatile ResolvableType factoryMethodReturnType;

    boolean constructorArgumentsResolved = false;

    Object[] resolvedConstructorArguments;

    private String scope = "";

    boolean isFactoryMethodUnique = false;

    private Map<String, Object> attributes = new LinkedHashMap<>();

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

    public MyBeanDefinition(Class<?> beanClass){
        setBeanClass(beanClass);
        this.metadata=new MyStandardAnnotationMetadata(beanClass,true);
    }

    public void setBeanClass(Class<?> beanClass){
        this.beanClass=beanClass;
    }

    public String getBeanClassName() {
        Object beanClassObject = this.beanClass;
        if (beanClassObject instanceof Class) {
            return ((Class<?>) beanClassObject).getName();
        }
        else {
            return (String) beanClassObject;
        }
    }

    public void setBeanClassName(String beanClassName) {
        this.beanClass = beanClassName;
    }

    public Resource getResource() {
        return resource;
    }

    public void setResource(Resource resource) {
        this.resource = resource;
    }

    public MyAnnotationMetadata getMetadata() {
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
        Class<?> resolvedClass = MyClassUtil.forName(className, classLoader);
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

    public Class<?> getTargetType(){
        return this.resolvedTargetType;
    }

    public Method getResolvedFactoryMethod() {
        return this.factoryMethodToIntrospect;
    }

    public boolean isFactoryMethod(Method candidate) {
        return candidate.getName().equals(getFactoryMethodName());
    }


    public void setAttribute(String name, @Nullable Object value) {
        Assert.notNull(name, "Name must not be null");
        if (value != null) {
            this.attributes.put(name, value);
        }
        else {
            removeAttribute(name);
        }
    }

    public Object getAttribute(String name) {
        Assert.notNull(name, "Name must not be null");
        return this.attributes.get(name);
    }

    public Object removeAttribute(String name) {
        Assert.notNull(name, "Name must not be null");
        return this.attributes.remove(name);
    }
}
