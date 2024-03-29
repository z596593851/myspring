package org.hxm.myspring.postprocessor;

import org.hxm.myspring.annotation.*;
import org.hxm.myspring.asm.MyAnnotationMetadata;
import org.hxm.myspring.factory.MyBeanDefinition;
import org.hxm.myspring.factory.MyBeanDefinitionHolder;
import org.hxm.myspring.factory.MyBeanDefinitionRegistry;
import org.hxm.myspring.factory.MyBeanFactory;
import org.hxm.myspring.stereotype.MyComponent;

import java.util.*;

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
    public void postProcessBeanDefinitionRegistry(MyBeanDefinitionRegistry registry) {
        processConfigBeanDefinitions((MyBeanFactory)registry);
    }

    public void processConfigBeanDefinitions(MyBeanFactory registry) {
        MyConfigurationClassParser parser = new MyConfigurationClassParser(registry);
        List<String> candidateNames = registry.getBeanDefinitionNames();
        //拿到所有被MyBeanDefinition标注的类
        List<MyBeanDefinitionHolder> configCandidates=new ArrayList<>();
        for (String beanName : candidateNames) {
            MyBeanDefinition beanDef=registry.getBeanDefinition(beanName);
            if (beanDef.getAttribute(this.CONFIGURATION_CLASS_ATTRIBUTE) == null && checkConfigurationClassCandidate(beanDef)) {
                configCandidates.add(new MyBeanDefinitionHolder(beanDef, beanName));
            }

        }
        Set<MyBeanDefinitionHolder> candidates = new LinkedHashSet<>(configCandidates);
        Set<MyConfigurationClass> alreadyParsed = new HashSet<>(configCandidates.size());
        do{
            parser.parse(candidates);
            Set<MyConfigurationClass> configClasses = new LinkedHashSet<>(parser.getConfigurationClasses());
            configClasses.removeAll(alreadyParsed);
            if(this.reader==null){
                this.reader=new MyConfigurationClassBeanDefinitionReader(registry);
            }
            //将解析到的所有configClasses注册到BeanDefinition
            this.reader.loadBeanDefinitions(configClasses);
            alreadyParsed.addAll(configClasses);
            candidates.clear();
            if (registry.getBeanDefinitionCount() > candidateNames.size()) {
                List<String> newCandidateNames = registry.getBeanDefinitionNames();
                Set<String> oldCandidateNames = new HashSet<>(candidateNames);
                Set<String> alreadyParsedClasses = new HashSet<>();
                for (MyConfigurationClass configurationClass : alreadyParsed) {
                    alreadyParsedClasses.add(configurationClass.getMetadata().getClassName());
                }
                for (String candidateName : newCandidateNames) {
                    if (!oldCandidateNames.contains(candidateName)) {
                        MyBeanDefinition bd = registry.getBeanDefinition(candidateName);
                        if (checkConfigurationClassCandidate(bd) &&
                                !alreadyParsedClasses.contains(bd.getBeanClassName())) {
                            candidates.add(new MyBeanDefinitionHolder(bd, candidateName));
                        }
                    }
                }
                candidateNames = newCandidateNames;
            }
        }while(!candidates.isEmpty());
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
        return metadata.hasAnnotatedMethods(MyBean.class.getName());
    }

}
