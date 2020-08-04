package org.hxm.myspring.postprocessor;

import org.hxm.myspring.MyBeanDefinition;
import org.hxm.myspring.annotation.MyBean;
import org.hxm.myspring.asm.MyMethodMetadata;
import org.hxm.myspring.asm.MySimpleAnnotationMetadata;
import org.hxm.myspring.utils.MyBeanNameGenerator;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class MyConfigurationClassParser {

    Map<MyConfigurationClass, MyConfigurationClass> configurationClasses=new HashMap<>();

    public void parse(List<MyBeanDefinition> configCandidates){
        for(MyBeanDefinition beanDefinition:configCandidates){
            String beanName= MyBeanNameGenerator.generateBeanName(beanDefinition);
            processConfigurationClass(new MyConfigurationClass(beanDefinition.getMetadata(),beanName));
        }
    }

    public void processConfigurationClass(MyConfigurationClass configClass){
        MySimpleAnnotationMetadata origional=configClass.getMetadata();
        Set<MyMethodMetadata> beanMethods = origional.getAnnotatedMethods(MyBean.class.getName());
        for(MyMethodMetadata methodMetadata:beanMethods){
            configClass.addBeanMethod(new MyBeanMethod(methodMetadata,configClass));
        }
        this.configurationClasses.put(configClass,configClass);
    }

    public Set<MyConfigurationClass> getConfigurationClasses(){
        return this.configurationClasses.keySet();
    }
}
