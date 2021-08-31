package org.hxm.myspring.asm;

import org.springframework.asm.ClassReader;
import org.springframework.core.io.Resource;


import java.io.IOException;

public class MySimpleMetadataReader {
    private static final int PARSING_OPTIONS = ClassReader.SKIP_DEBUG
            | ClassReader.SKIP_CODE | ClassReader.SKIP_FRAMES;

    private  Resource resource;

    private final MyAnnotationMetadata annotationMetadata;


    public MySimpleMetadataReader(Resource resource) throws IOException {
        MySimpleAnnotationMetadataReadingVisitor visitor=new MySimpleAnnotationMetadataReadingVisitor(this.getClass().getClassLoader());
        ClassReader classReader=new ClassReader(resource.getInputStream());
        classReader.accept(visitor, PARSING_OPTIONS);
        this.resource = resource;
        this.annotationMetadata = visitor.getMetadata();
    }

    /**
     * 测试用构造函数
     * @param clazz
     */
    public MySimpleMetadataReader(Class<?> clazz){
        ClassReader classReader=null;
        try {
            classReader=new ClassReader(clazz.getName());
        } catch (Exception e) {
            e.printStackTrace();
        }
        MySimpleAnnotationMetadataReadingVisitor visitor=new MySimpleAnnotationMetadataReadingVisitor(this.getClass().getClassLoader());
        classReader.accept(visitor, PARSING_OPTIONS);
        this.annotationMetadata = visitor.getMetadata();
    }

    public MyAnnotationMetadata getAnnotationMetadata(){
        return annotationMetadata;
    }

    public MyClassMetadata getClassMetadata() {
        return this.annotationMetadata;
    }

    public Resource getResource() {
        return resource;
    }
}
