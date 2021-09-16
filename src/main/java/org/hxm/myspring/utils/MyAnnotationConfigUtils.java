package org.hxm.myspring.utils;

import org.hxm.myspring.asm.MyAnnotatedTypeMetadata;
import org.hxm.myspring.asm.MyAnnotationAttributes;

public class MyAnnotationConfigUtils {
    public static MyAnnotationAttributes attributesFor(MyAnnotatedTypeMetadata metadata, Class<?> annotationClass) {
        return attributesFor(metadata, annotationClass.getName());
    }

    public static MyAnnotationAttributes attributesFor(MyAnnotatedTypeMetadata metadata, String annotationClassName) {
        return MyAnnotationAttributes.fromMap(metadata.getAnnotationAttributes(annotationClassName, false));
    }
}
