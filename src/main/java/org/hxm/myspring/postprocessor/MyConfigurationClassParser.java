package org.hxm.myspring.postprocessor;

import org.hxm.myspring.asm.MyAnnotationMetadata;
import org.hxm.myspring.factory.MyBeanDefinitionHolder;
import org.hxm.myspring.annotation.MyBean;
import org.hxm.myspring.asm.MyMethodMetadata;
import org.hxm.myspring.asm.MySimpleAnnotationMetadata;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author xiaoming
 */
public class MyConfigurationClassParser {

    Map<MyConfigurationClass, MyConfigurationClass> configurationClasses=new HashMap<>();

    public void parse(List<MyBeanDefinitionHolder> configCandidates){
        for(MyBeanDefinitionHolder beanDefinitionHolder:configCandidates){
            //将标注了@MyConfiguration的类封装成MyConfigurationClass
            processConfigurationClass(new MyConfigurationClass(beanDefinitionHolder.getBeanDefinition().getMetadata(),beanDefinitionHolder.getBeanName()));
        }
        //todo 处理通过spi引入的config类

    }

    public void processConfigurationClass(MyConfigurationClass configClass){
        MyAnnotationMetadata origional=configClass.getMetadata();
        //todo 处理import

        Set<MyMethodMetadata> beanMethods = origional.getAnnotatedMethods(MyBean.class.getName());
        //提取MyConfigurationClass中所有标注了@MyBean的方法
        for(MyMethodMetadata methodMetadata:beanMethods){
            configClass.addBeanMethod(new MyBeanMethod(methodMetadata,configClass));
        }
        this.configurationClasses.put(configClass,configClass);
    }

    public Set<MyConfigurationClass> getConfigurationClasses(){
        return this.configurationClasses.keySet();
    }
}
