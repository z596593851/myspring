package org.hxm.myspring.factory;

import org.hxm.myspring.asm.MyAnnotationMetadata;
import org.hxm.myspring.asm.MySimpleMetadataReader;

import java.lang.annotation.Annotation;

public class MyAnnotationTypeFilter extends MyAbstractTypeFilter {

    private Class<? extends Annotation> annotationType;

    public MyAnnotationTypeFilter(Class<? extends Annotation> annotationType){
        this.annotationType=annotationType;
    }

    @Override
    protected boolean matchSelf(MySimpleMetadataReader metadataReader) {
        MyAnnotationMetadata metadata = metadataReader.getAnnotationMetadata();
        return metadata.hasAnnotation(this.annotationType.getName());
    }
}
