package org.hxm.myspring.asm;

import org.springframework.util.Assert;

import java.lang.annotation.Annotation;
import java.util.LinkedHashMap;

public class MyAnnotationAttributes extends LinkedHashMap<String, Object> {
    private final Class<? extends Annotation> annotationType;
    final String displayName;

    public MyAnnotationAttributes(Class<? extends Annotation> annotationType) {

        this.annotationType = annotationType;
        this.displayName = annotationType.getName();
    }
}
