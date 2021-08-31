package org.hxm.myspring.asm;

import org.springframework.lang.Nullable;

import java.lang.annotation.Annotation;
import java.util.LinkedHashMap;
import java.util.Map;

public class MyAnnotationAttributes extends LinkedHashMap<String, Object> {

    private static final String UNKNOWN = "unknown";

    private final Class<? extends Annotation> annotationType;
    final String displayName;

    public MyAnnotationAttributes(Class<? extends Annotation> annotationType) {

        this.annotationType = annotationType;
        this.displayName = annotationType.getName();
    }

    public MyAnnotationAttributes(Map<String, Object> map) {
        super(map);
        this.annotationType = null;
        this.displayName = UNKNOWN;
    }

    public static MyAnnotationAttributes fromMap(@Nullable Map<String, Object> map) {
        if (map == null) {
            return null;
        }
        if (map instanceof MyAnnotationAttributes) {
            return (MyAnnotationAttributes) map;
        }
        return new MyAnnotationAttributes(map);
    }
}
