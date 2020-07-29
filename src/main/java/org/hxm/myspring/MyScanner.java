package org.hxm.myspring;

import org.hxm.myspring.annotation.MyComponent;
import org.hxm.myspring.annotation.MyScopeMetadata;
import org.hxm.myspring.asm.MyMetadataReader;
import org.hxm.myspring.utils.MyAnnotationTypeFilter;
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

    private MyApplicationContext registry;

    private MyBeanNameGenerator beanNameGenerator=new MyBeanNameGenerator();

    private MyMetadataResolver myMetadataResolver = new MyMetadataResolver();

    private List<MyAnnotationTypeFilter> includeFilters = new LinkedList<>();

    public MyScanner(MyApplicationContext registry){
        this.registry=registry;
        registerDefaultFilters();
    }

    public void scan(String... basePackages) {
        for (String basePackage : basePackages) {
            Set<MyBeanDefinition> candidates=scanCandidateComponents(basePackage) ;
            for(MyBeanDefinition candidate:candidates){
                String beanName=beanNameGenerator.generateBeanName(candidate);
                MyScopeMetadata scopeMetadata=this.myMetadataResolver.resolveScopeMetadata(candidate);
                candidate.setScope(scopeMetadata.getScopeName());
                this.registry.registerBeanDefinition(beanName,candidate);
            }
        }
    }

    public Set<MyBeanDefinition> scanCandidateComponents(String basePackage) {
        Set<MyBeanDefinition> candidates = new LinkedHashSet<>();
        try {
            ClassLoader cl = MyClassUtil.getDefaultClassLoader();
            basePackage=basePackage.replace(".","/")+"/";
            //拿到指定路径下所有的URL(包括嵌套的文件夹内的类也可以拿到)
            Enumeration<URL> resourceUrls = (cl != null ? cl.getResources(basePackage) : ClassLoader.getSystemResources(basePackage));
            Resource rootDirResource= new UrlResource(resourceUrls.nextElement());
            File rootDir=rootDirResource.getFile().getAbsoluteFile();
            List<Resource> resources= Arrays.stream(listDirectory(rootDir)).map(FileSystemResource::new).collect(Collectors.toList());
            for (Resource resource : resources) {
                if (resource.isReadable()) {
                    MyMetadataReader metadataReader=new MyMetadataReader(resource);
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
    }

    public File[] listDirectory(File dir) {
        File[] files = dir.listFiles();
        if (files == null) {
            return new File[0];
        }
        Arrays.sort(files, Comparator.comparing(File::getName));
        return files;
    }

    protected boolean isCandidateComponent(MyMetadataReader metadataReader){
        for(MyAnnotationTypeFilter filter:this.includeFilters){
            if(filter.match(metadataReader)){
                return true;
            }
        }
        return false;
    }
}
