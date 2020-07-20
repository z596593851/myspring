package org.hxm.myspring;

import org.hxm.myspring.asm.MyAnnotationMetadata;
import org.hxm.myspring.asm.MyMetadataReader;
import org.springframework.core.io.Resource;

import java.lang.reflect.Executable;

public class MyBeanDefinition {

    private Object source;

    private String beanClassName;

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
        //todo bug
        return null;
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
}
