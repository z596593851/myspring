package org.hxm.myspring.postprocessor;

import org.hxm.myspring.MyBeanDefinition;
import org.hxm.myspring.MyBeanFactory;
import org.hxm.myspring.annotation.MyBean;
import org.hxm.myspring.annotation.MyScope;
import org.hxm.myspring.asm.MyMethodMetadata;
import org.hxm.myspring.asm.MySimpleAnnotationMetadata;

import java.util.Map;
import java.util.Set;

public class MyConfigurationClassBeanDefinitionReader {

    private MyBeanFactory registry;

    public MyConfigurationClassBeanDefinitionReader(MyBeanFactory registry){
        this.registry=registry;
    }

    public void loadBeanDefinitions(Set<MyConfigurationClass> configClasses){
        for(MyConfigurationClass configClass:configClasses){
            for(MyBeanMethod beanMethod:configClass.getBeanMethods()){
                loadBeanDefinitionsForBeanMethod(beanMethod);
            }
        }
    }

    public void loadBeanDefinitionsForBeanMethod(MyBeanMethod beanMethod){
        MyConfigurationClass configClass=beanMethod.getConfigurationClass();
        MyMethodMetadata metadata=beanMethod.getMethodMetadata();
        String methodName=metadata.getMethodName();
        String name = (String) metadata.getAnnotationAttributes(MyBean.class).get("name");
        String beanName = name==null?methodName:name;
        MyConfigurationClassBeanDefinition beanDef=new MyConfigurationClassBeanDefinition(configClass,metadata);
        beanDef.setFactoryBeanName(configClass.getBeanName());
        beanDef.setUniqueFactoryMethodName(methodName);

        String scope=(String) metadata.getAnnotationAttributes(MyScope.class).get("name");
        if(scope!=null){
            beanDef.setScope(scope);
        }
        this.registry.registerBeanDefinition(beanName,beanDef);
    }

    private static class MyConfigurationClassBeanDefinition extends MyBeanDefinition {


        private MySimpleAnnotationMetadata annotationMetadata;

        private MyMethodMetadata factoryMethodMetadata;

        public MyConfigurationClassBeanDefinition(MyConfigurationClass configClass, MyMethodMetadata beanMethodMetadata){

            this.annotationMetadata=configClass.getMetadata();
            this.factoryMethodMetadata=beanMethodMetadata;
        }
    }

}
