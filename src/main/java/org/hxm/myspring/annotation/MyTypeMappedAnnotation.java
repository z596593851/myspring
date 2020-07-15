package org.hxm.myspring.annotation;

public class MyTypeMappedAnnotation<A>  {

    private final ClassLoader classLoader;

    private final Object source;

    private final Class<A> annotationType;

    private final Object attributes;

    public MyTypeMappedAnnotation(ClassLoader classLoader, Object source, Class<A> annotationType, Object attributes) {
        this.classLoader = classLoader;
        this.source = source;
        this.annotationType = annotationType;
        this.attributes = attributes;
    }
}
