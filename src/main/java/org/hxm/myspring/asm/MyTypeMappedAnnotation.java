package org.hxm.myspring.asm;

public class MyTypeMappedAnnotation<A>  {

    private final ClassLoader classLoader;

    private final Object source;

    //一个注解对应的key-value，可以是类上的注解也可以是方法上的
    private final Object attributes;

    public Class<A> getAnnotationType() {
        return annotationType;
    }

    private Class<A> annotationType;

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
