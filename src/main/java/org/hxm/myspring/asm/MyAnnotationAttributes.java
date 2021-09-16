package org.hxm.myspring.asm;

import java.lang.annotation.Annotation;
import java.lang.reflect.Array;
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

    public static MyAnnotationAttributes fromMap(Map<String, Object> map) {
        if (map == null) {
            return null;
        }
        if (map instanceof MyAnnotationAttributes) {
            return (MyAnnotationAttributes) map;
        }
        return new MyAnnotationAttributes(map);
    }

    public String[] getStringArray(String attributeName) {
        return getRequiredAttribute(attributeName, String[].class);
    }

    private <T> T getRequiredAttribute(String attributeName, Class<T> expectedType) {
        Object value = get(attributeName);
        if (!expectedType.isInstance(value) && expectedType.isArray() &&
                expectedType.getComponentType().isInstance(value)) {
            Object array = Array.newInstance(expectedType.getComponentType(), 1);
            Array.set(array, 0, value);
            value = array;
        }
        if (!expectedType.isInstance(value) && expectedType.isArray() &&
                expectedType.getComponentType().isInstance(value)) {
            Object array = Array.newInstance(expectedType.getComponentType(), 1);
            Array.set(array, 0, value);
            value = array;
        }
        return (T) value;
    }
}
