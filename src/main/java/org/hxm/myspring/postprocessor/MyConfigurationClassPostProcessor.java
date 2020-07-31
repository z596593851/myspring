package org.hxm.myspring.postprocessor;

import org.hxm.myspring.MyBeanDefinition;
import org.hxm.myspring.MyBeanFactory;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class MyConfigurationClassPostProcessor implements MyBeanFactoryPostProcessor {
    private MyConfigurationClassBeanDefinitionReader reader;
    @Override
    public void postProcessBeanDefinitionRegistry(MyBeanFactory registry) {
        processConfigBeanDefinitions(registry);
    }

    public void processConfigBeanDefinitions(MyBeanFactory registry) {
        MyConfigurationClassParser parser = new MyConfigurationClassParser();
        List<String> candidateNames = registry.getBeanDefinitionNames();
        //拿到所有MyBeanDefinition
        List<MyBeanDefinition> configCandidates = candidateNames.stream().map(registry::getBeanDefinition).collect(Collectors.toList());
        parser.parse(configCandidates);
        Set<MyConfigurationClass> configClasses=parser.getConfigurationClasses();
        if(this.reader==null){
            this.reader=new MyConfigurationClassBeanDefinitionReader(registry);
        }
        this.reader.loadBeanDefinitions(configClasses);
    }

}
