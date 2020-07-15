package org.hxm.myspring;

import org.hxm.myspring.asm.MyMetadataReader;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.*;

public class MyClassPathBeanDefinitionScanner {



    public static void main(String[] args) {
        MyClassPathBeanDefinitionScanner scan=new MyClassPathBeanDefinitionScanner();
        try {
            scan.scan("com.hxm.myspring.test");
        }catch (IOException e){
            e.printStackTrace();
        }

    }

    public void scan(String... basePackages) throws IOException {
        for (String basePackage : basePackages) {
            scanCandidateComponents(basePackage);
        }
    }

    public Set<MyBeanDefinition> scanCandidateComponents(String basePackage) throws IOException {
        Set<MyBeanDefinition> candidates = new LinkedHashSet<>();
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
                // TODO: 2020/6/27 将resource封装成reader，检查reader是否有注解，再将reader封装成BeanDefinition
//                System.out.println(resource.getFilename());
                MyMetadataReader metadataReader=new MyMetadataReader(resource);
                MyBeanDefinition mbd=new MyBeanDefinition(metadataReader);
                mbd.setSource(resource);
                candidates.add(mbd);
            }
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
