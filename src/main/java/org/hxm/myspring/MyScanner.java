package org.hxm.myspring;

import org.hxm.myspring.asm.MyMetadataReader;
import org.hxm.myspring.utils.MyBeanNameGenerator;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.*;

public class MyScanner {

    private MyApplicationContext registry;

    private MyBeanNameGenerator beanNameGenerator=new MyBeanNameGenerator();

    public MyScanner(MyApplicationContext registry){
        this.registry=registry;
    }

    public void scan(String... basePackages) {
        for (String basePackage : basePackages) {
            Set<MyBeanDefinition> candidates=scanCandidateComponents(basePackage);
            for(MyBeanDefinition candidate:candidates){
                String beanName=beanNameGenerator.generateBeanName(candidate,this.registry);
                this.registry.registerBeanDefinition(beanName,candidate);

            }
        }
    }

    public Set<MyBeanDefinition> scanCandidateComponents(String basePackage) {
        Set<MyBeanDefinition> candidates = new LinkedHashSet<>();
        try {
            Set<Resource> result1 = new LinkedHashSet<>(16);
            ClassLoader cl = this.getClass().getClassLoader();
            basePackage=basePackage.replace(".","/")+"/";
            Enumeration<URL> resourceUrls = (cl != null ? cl.getResources(basePackage) : ClassLoader.getSystemResources(basePackage));
            while (resourceUrls.hasMoreElements()) {
                URL url = resourceUrls.nextElement();
                result1.add(new UrlResource(url));
            }
            Resource[] rootDirResources=result1.toArray(new Resource[0]);
            Set<Resource> result2 = new LinkedHashSet<>(16);
            Resource rootDirResource=rootDirResources[0];
            Set<File> result3 = new LinkedHashSet<>(8);
            File rootDir=rootDirResource.getFile().getAbsoluteFile();
            for (File content : listDirectory(rootDir)) {
                result3.add(content);
            }

            for (File file : result3) {
                result2.add(new FileSystemResource(file));
            }
            Resource[] resources=result2.toArray(new Resource[0]);
            for (Resource resource : resources) {
                if (resource.isReadable()) {
                    MyMetadataReader metadataReader=new MyMetadataReader(resource);
                    MyBeanDefinition mbd=new MyBeanDefinition(metadataReader);
                    mbd.setSource(resource);
                    candidates.add(mbd);
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
        return candidates;


    }

    public File[] listDirectory(File dir) {
        File[] files = dir.listFiles();
        if (files == null) {
            return new File[0];
        }
        Arrays.sort(files, Comparator.comparing(File::getName));
        return files;
    }
}
