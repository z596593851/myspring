package org.hxm.myspring;

import org.hxm.myspring.asm.MyAnnotationMetadata;
import org.hxm.myspring.asm.MyMetadataReader;
import org.springframework.core.io.Resource;
import org.springframework.lang.Nullable;
import org.springframework.util.ClassUtils;

import java.lang.reflect.Executable;

public class MyBeanDefinition {

    private Object source;

    private String beanClassName;

    private Object beanClass;

    private Resource resource;

    private MyAnnotationMetadata metadata;

    Executable resolvedConstructorOrFactoryMethod;

    volatile Class<?> resolvedTargetType;

    private String scope = "";

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

    public MyBeanDefinition(MyMetadataReader metadataReader){
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
        Class<?> resolvedClass = ClassUtils.forName(className.replace("/","."), classLoader);
        this.beanClass = resolvedClass;
        return resolvedClass;
    }
}
