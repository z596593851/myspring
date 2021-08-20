package org.hxm.myspring.postprocessor;

import org.hxm.myspring.asm.MyAnnotationMetadata;
import org.hxm.myspring.asm.MySimpleAnnotationMetadata;

import java.util.ArrayList;
import java.util.List;

public class MyConfigurationClass {

    private String beanName;

    private MyAnnotationMetadata metadata;

    private List<MyBeanMethod> beanMethods=new ArrayList<>();

    public MyConfigurationClass(MyAnnotationMetadata metadata, String beanName){
        this.metadata=metadata;
        this.beanName=beanName;
    }

    public void addBeanMethod(MyBeanMethod method) {
        this.beanMethods.add(method);
    }

    public List<MyBeanMethod> getBeanMethods(){
        return this.beanMethods;
    }

    public MyAnnotationMetadata getMetadata(){
        return this.metadata;
    }

    public String getBeanName(){
        return this.beanName;
    }

}
