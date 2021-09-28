package org.hxm.myspring.factory;

import org.hxm.myspring.annotation.MyConfiguration;
import org.hxm.myspring.annotation.MyScopeMetadata;
import org.hxm.myspring.asm.MySimpleMetadataReader;
import org.hxm.myspring.stereotype.MyComponent;
import org.hxm.myspring.utils.MyBeanNameGenerator;
import org.hxm.myspring.utils.MyClassUtil;
import org.hxm.myspring.utils.MyMetadataResolver;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.core.type.filter.TypeFilter;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.*;
import java.util.stream.Collectors;

public class MyScanner {

    private MyBeanDefinitionRegistry registry;

    private MyMetadataResolver myMetadataResolver = new MyMetadataResolver();

    private List<MyAnnotationTypeFilter> includeFilters = new LinkedList<>();
    private final List<MyTypeFilter> excludeFilters = new LinkedList<>();

    public MyScanner(MyBeanDefinitionRegistry registry){
        this.registry=registry;
        registerDefaultFilters();
    }

    public Set<MyBeanDefinitionHolder> scan(String... basePackages) {
        Set<MyBeanDefinitionHolder> beanDefinitions = new LinkedHashSet<>();
        for (String basePackage : basePackages) {
            Set<MyBeanDefinition> candidates=scanCandidateComponents(basePackage) ;
            for(MyBeanDefinition candidate:candidates){
                String beanName= MyBeanNameGenerator.generateBeanName(candidate);
                MyScopeMetadata scopeMetadata=this.myMetadataResolver.resolveScopeMetadata(candidate);
                candidate.setScope(scopeMetadata.getScopeName());
                beanDefinitions.add(new MyBeanDefinitionHolder(candidate,beanName));
                this.registry.registerBeanDefinition(beanName,candidate);
            }
        }
        return beanDefinitions;
    }

    public Set<MyBeanDefinition> scanCandidateComponents(String basePackage) {
        Set<MyBeanDefinition> candidates = new LinkedHashSet<>();
        try {
            ClassLoader cl = MyClassUtil.getDefaultClassLoader();
            basePackage=basePackage.replace(".","/")+"/";
            Enumeration<URL> resourceUrls = (cl != null ? cl.getResources(basePackage) : ClassLoader.getSystemResources(basePackage));
            Resource rootDirResource= new UrlResource(resourceUrls.nextElement());
            File rootDir=rootDirResource.getFile().getAbsoluteFile();
            List<File> fileList=new ArrayList<>();
            listDirectory(rootDir,fileList);
            List<Resource> resources= fileList.stream().map(FileSystemResource::new).collect(Collectors.toList());
            for (Resource resource : resources) {
                if (resource.isReadable()) {
                    MySimpleMetadataReader metadataReader=new MySimpleMetadataReader(resource);
                    //检查是否有@MyComponent注解
                    if(isCandidateComponent(metadataReader)){
                        MyBeanDefinition mbd=new MyBeanDefinition(metadataReader);
                        mbd.setSource(resource);
                        candidates.add(mbd);
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return candidates;
    }

    protected void registerDefaultFilters() {
        this.includeFilters.add(new MyAnnotationTypeFilter(MyComponent.class));
        this.includeFilters.add(new MyAnnotationTypeFilter(MyConfiguration.class));
    }

    public void listDirectory(File dir, List<File> list) {
        if(!dir.isDirectory()){
            list.add(dir);
        }else{
            File[] files = dir.listFiles();
            for(File file:files) {
                listDirectory(file,list);
            }
        }
    }

    protected boolean isCandidateComponent(MySimpleMetadataReader metadataReader){
        for (MyTypeFilter tf : this.excludeFilters) {
            if (tf.match(metadataReader)) {
                return false;
            }
        }
        for(MyTypeFilter filter:this.includeFilters){
            if(filter.match(metadataReader)){
                return true;
            }
        }
        return false;
    }

    public void addExcludeFilter(MyTypeFilter excludeFilter) {
        this.excludeFilters.add(0, excludeFilter);
    }
}
