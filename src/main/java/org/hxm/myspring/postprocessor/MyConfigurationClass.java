package org.hxm.myspring.postprocessor;

import org.hxm.myspring.asm.MyAnnotationMetadata;
import org.hxm.myspring.asm.MySimpleMetadataReader;
import org.hxm.myspring.utils.MyClassUtil;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public class MyConfigurationClass {

    private String beanName;

    private MyAnnotationMetadata metadata;

    private List<MyBeanMethod> beanMethods=new ArrayList<>();

    private final Set<MyConfigurationClass> importedBy = new LinkedHashSet<>(1);

    public MyConfigurationClass(Class<?> clazz, MyConfigurationClass importedBy) {
        this.metadata = MyAnnotationMetadata.introspect(clazz);
        this.importedBy.add(importedBy);
    }

    public MyConfigurationClass(MyAnnotationMetadata metadata, String beanName){
        this.metadata=metadata;
        this.beanName=beanName;
    }

    public MyConfigurationClass(MySimpleMetadataReader metadataReader, MyConfigurationClass importedBy) {
        this.metadata = metadataReader.getAnnotationMetadata();
        this.importedBy.add(importedBy);
    }

    public MyConfigurationClass(MySimpleMetadataReader metadataReader, String beanName) {
        this.metadata = metadataReader.getAnnotationMetadata();
        this.beanName = beanName;
    }

    public void mergeImportedBy(MyConfigurationClass otherConfigClass) {
        this.importedBy.addAll(otherConfigClass.importedBy);
    }

    public void addBeanMethod(MyBeanMethod method) {
        this.beanMethods.add(method);
    }

    public List<MyBeanMethod> getBeanMethods(){
        return this.beanMethods;
    }

    public MyAnnotationMetadata getMetadata(){
        return this.metadata;
    }

    public String getBeanName(){
        return this.beanName;
    }

    public String getSimpleName() {
        return MyClassUtil.getShortName(getMetadata().getClassName());
    }

    public boolean isImported() {
        return !this.importedBy.isEmpty();
    }

    public void setBeanName(String beanName) {
        this.beanName = beanName;
    }


}
