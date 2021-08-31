package org.hxm.myspring.postprocessor;

import org.hxm.myspring.annotation.MyBean;
import org.hxm.myspring.annotation.MyScope;
import org.hxm.myspring.asm.MyAnnotationMetadata;
import org.hxm.myspring.asm.MyMethodMetadata;
import org.hxm.myspring.factory.MyBeanDefinition;
import org.hxm.myspring.factory.MyBeanFactory;
import org.hxm.myspring.utils.MyClassUtil;
import org.springframework.util.Assert;

import java.beans.Introspector;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class MyConfigurationClassBeanDefinitionReader {

    private MyBeanFactory registry;
    private final Map<String, Set<String>> metaAnnotationTypesCache = new ConcurrentHashMap<>();

    public MyConfigurationClassBeanDefinitionReader(MyBeanFactory registry){
        this.registry=registry;
    }

    public void loadBeanDefinitions(Set<MyConfigurationClass> configClasses){
        for(MyConfigurationClass configClass:configClasses){
            if(configClass.isImported()){
                registerBeanDefinitionForImportedConfigurationClass(configClass);
            }
            for(MyBeanMethod beanMethod:configClass.getBeanMethods()){
                loadBeanDefinitionsForBeanMethod(beanMethod);
            }
        }
    }

    private void registerBeanDefinitionForImportedConfigurationClass(MyConfigurationClass configClass) {
        MyAnnotationMetadata metadata = configClass.getMetadata();
        MyBeanDefinition configBeanDef=new MyBeanDefinition(metadata);
        configBeanDef.setScope("singleton");
        String configBeanName=buildDefaultBeanName(configBeanDef);
        this.registry.registerBeanDefinition(configBeanName,configBeanDef);
        configClass.setBeanName(configBeanName);
    }

    private String buildDefaultBeanName(MyBeanDefinition annotatedDef){
        String beanClassName = annotatedDef.getBeanClassName();
        Assert.state(beanClassName != null, "No bean class name set");
        String shortClassName = MyClassUtil.getShortName(beanClassName);
        return Introspector.decapitalize(shortClassName);
    }

    public void loadBeanDefinitionsForBeanMethod(MyBeanMethod beanMethod){
        MyConfigurationClass configClass=beanMethod.getConfigurationClass();
        MyMethodMetadata metadata=beanMethod.getMethodMetadata();
        String methodName=metadata.getMethodName();
        String[] names=(String[])metadata.getAnnotationAttributes(MyBean.class.getName()).get("name");
        String name = names[0] ;
        String beanName = name==null?methodName:name;
        MyConfigurationClassBeanDefinition beanDef=new MyConfigurationClassBeanDefinition(configClass,metadata);
        beanDef.setFactoryBeanName(configClass.getBeanName());
        beanDef.setUniqueFactoryMethodName(methodName);

        Map<String,Object> attributes=metadata.getAnnotationAttributes(MyScope.class.getName());
        if(attributes!=null){
            beanDef.setScope((String) attributes.get("value"));
        }
        this.registry.registerBeanDefinition(beanName,beanDef);
    }

    private static class MyConfigurationClassBeanDefinition extends MyBeanDefinition {


        private MyAnnotationMetadata annotationMetadata;

        private MyMethodMetadata factoryMethodMetadata;

        public MyConfigurationClassBeanDefinition(MyConfigurationClass configClass, MyMethodMetadata beanMethodMetadata){

            this.annotationMetadata=configClass.getMetadata();
            this.factoryMethodMetadata=beanMethodMetadata;
        }
    }

}
