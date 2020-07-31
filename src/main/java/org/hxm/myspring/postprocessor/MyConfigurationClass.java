package org.hxm.myspring.postprocessor;

import org.hxm.myspring.asm.MySimpleAnnotationMetadata;

import java.util.ArrayList;
import java.util.List;

public class MyConfigurationClass {

    private String beanName;

    private MySimpleAnnotationMetadata metadata;

    private List<MyBeanMethod> beanMethods=new ArrayList<>();

    public MyConfigurationClass(MySimpleAnnotationMetadata metadata, String beanName){
        this.metadata=metadata;
        this.beanName=beanName;
    }

    public void addBeanMethod(MyBeanMethod method) {
        this.beanMethods.add(method);
    }

    public List<MyBeanMethod> getBeanMethods(){
        return this.beanMethods;
    }

    public MySimpleAnnotationMetadata getMetadata(){
        return this.metadata;
    }

    public String getBeanName(){
        return this.beanName;
    }

}
