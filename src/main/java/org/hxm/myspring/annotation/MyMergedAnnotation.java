package org.hxm.myspring.annotation;

public class MyMergedAnnotation<A> {

    private final ClassLoader classLoader;

    private final Object source;

    private final Class<A> annotationType;

    private final Object attributes;

    public MyMergedAnnotation(ClassLoader classLoader, Object source, Class<A> annotationType, Object attributes) {
        this.classLoader = classLoader;
        this.source = source;
        this.annotationType = annotationType;
        this.attributes = attributes;
    }
}
