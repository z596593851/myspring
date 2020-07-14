package org.hxm.myspring.asm;


import org.hxm.myspring.annotation.MyMergedAnnotation;
import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.Type;

import java.lang.annotation.Annotation;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class MyAnnotationVisitor<A extends Annotation> extends AnnotationVisitor {

    private static int ASM_VERSION  = 1 << 24 | 8 << 16 | 0 << 8;

    private final ClassLoader classLoader;

    private final Object source;

    private final Class<A> annotationType;

    private final Consumer<MyMergedAnnotation<A>> consumer;

    private final Map<String, Object> attributes = new LinkedHashMap<>(4);


    public MyAnnotationVisitor(ClassLoader classLoader, Object source, Class<A> annotationType, Consumer<MyMergedAnnotation<A>> consumer) {
        super(ASM_VERSION);
        this.classLoader = classLoader;
        this.source = source;
        this.annotationType = annotationType;
        this.consumer = consumer;
    }

    static <A extends Annotation> AnnotationVisitor get(ClassLoader classLoader, Supplier<Object> sourceSupplier, String descriptor, boolean visible, Consumer<MyMergedAnnotation<A>> consumer) {
        if (!visible) {
            return null;
        }

        String typeName = Type.getType(descriptor).getClassName();
        Object source = (sourceSupplier != null ? sourceSupplier.get() : null);
        try {
            Class<A> annotationType = (Class<A>) Class.forName(typeName, false, classLoader);
            return new MyAnnotationVisitor<A>(classLoader, source, annotationType, consumer);
        }
        catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public void visit(String name, Object value) {
        if (value instanceof Type) {
            value = ((Type) value).getClassName();
        }
        this.attributes.put(name, value);
    }

    @Override
    public void visitEnd() {
        MyMergedAnnotation<A> annotation = new MyMergedAnnotation<>(this.classLoader, this.source, this.annotationType, this.attributes);
        this.consumer.accept(annotation);
    }
}
