package org.hxm.myspring.postprocessor;

import org.hxm.myspring.asm.MyAnnotationMetadata;
import org.hxm.myspring.factory.MyBeanDefinition;
import org.hxm.myspring.factory.MyBeanDefinitionHolder;
import org.hxm.myspring.factory.MyBeanFactory;
import org.hxm.myspring.annotation.*;
import org.hxm.myspring.asm.MySimpleAnnotationMetadata;
import org.springframework.context.annotation.Bean;

import java.util.*;

/**
 * 解析@MyConfiguration
 */
public class MyConfigurationClassPostProcessor implements MyBeanFactoryPostProcessor {

    public static final String CONFIGURATION_CLASS_FULL = "full";

    public static final String CONFIGURATION_CLASS_LITE = "lite";

    public final String CONFIGURATION_CLASS_ATTRIBUTE = this.getClass().getName()+"configurationClass";

    private MyConfigurationClassBeanDefinitionReader reader;

    private static final Set<String> candidateIndicators = new HashSet<>(8);

    static {
        candidateIndicators.add(MyComponent.class.getName());
        candidateIndicators.add(MyComponentScan.class.getName());
        candidateIndicators.add(MyImport.class.getName());
        candidateIndicators.add(MyImportResource.class.getName());
    }

    @Override
    public void postProcessBeanDefinitionRegistry(MyBeanFactory registry) {
        processConfigBeanDefinitions(registry);
    }

    public void processConfigBeanDefinitions(MyBeanFactory registry) {
        MyConfigurationClassParser parser = new MyConfigurationClassParser();
        List<String> candidateNames = registry.getBeanDefinitionNames();
        //拿到所有被MyBeanDefinition标注的类
        List<MyBeanDefinitionHolder> configCandidates=new ArrayList<>();
        for (String beanName : candidateNames) {
            MyBeanDefinition beanDef=registry.getBeanDefinition(beanName);
            if (beanDef.getAttribute(this.CONFIGURATION_CLASS_ATTRIBUTE) == null && checkConfigurationClassCandidate(beanDef)) {
                configCandidates.add(new MyBeanDefinitionHolder(beanDef, beanName));
            }

        }
        parser.parse(configCandidates);
        Set<MyConfigurationClass> configClasses=parser.getConfigurationClasses();
        if(this.reader==null){
            this.reader=new MyConfigurationClassBeanDefinitionReader(registry);
        }
        //解析被@MyBean标注的方法
        this.reader.loadBeanDefinitions(configClasses);
    }

    public boolean checkConfigurationClassCandidate(MyBeanDefinition beanDef){
        String className=beanDef.getBeanClassName();
        if(className==null || beanDef.getFactoryMethodName()!=null){
            return false;
        }
        MyAnnotationMetadata metadata=beanDef.getMetadata();
        Map<String,Object> config = metadata.getAnnotationAttributes(MyConfiguration.class.getName(),false);
        if (config != null && !Boolean.FALSE.equals(config.get("proxyBeanMethods"))) {
            beanDef.setAttribute(CONFIGURATION_CLASS_ATTRIBUTE, CONFIGURATION_CLASS_FULL);
        }
        else if (config != null || isConfigurationCandidate(metadata)) {
            beanDef.setAttribute(CONFIGURATION_CLASS_ATTRIBUTE, CONFIGURATION_CLASS_LITE);
        }
        else {
            return false;
        }
        return true;
    }

    public boolean isConfigurationCandidate(MyAnnotationMetadata metadata){
        if(metadata.isInterface()){
            return false;
        }
        for(String indicator : candidateIndicators){
            if(metadata.hasAnnotation(indicator)){
                return true;
            }
        }
        return metadata.hasAnnotatedMethods(Bean.class.getName());
    }

}
