package org.hxm.myspring.utils;

import org.hxm.myspring.asm.MyAnnotationMetadata;
import org.hxm.myspring.asm.MyMetadataReader;

import java.lang.annotation.Annotation;

public class MyAnnotationTypeFilter {

    private Class<? extends Annotation> annotationType;

    public MyAnnotationTypeFilter(Class<? extends Annotation> annotationType){
        this.annotationType=annotationType;
    }

    public boolean match(MyMetadataReader metadataReader) {
        MyAnnotationMetadata metadata = metadataReader.getAnnotationMetadata();
        return metadata.hasAnnotation(this.annotationType.getName());
    }
}
