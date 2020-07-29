package org.hxm.myspring.asm;

public class MyTypeMappedAnnotation<A>  {

    private final ClassLoader classLoader;

    private final Object source;

    //一个注解对应的key-value
    private final Object attributes;

    public Class<A> getAnnotationType() {
        return annotationType;
    }

    private final Class<A> annotationType;

    public Object getAttributes() {
        return attributes;
    }

    public MyTypeMappedAnnotation(ClassLoader classLoader, Object source, Class<A> annotationType, Object attributes) {
        this.classLoader = classLoader;
        this.source = source;
        this.annotationType = annotationType;
        this.attributes = attributes;
    }
}
