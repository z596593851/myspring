package org.hxm.myspring.asm;


import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.Type;

import java.lang.annotation.Annotation;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Supplier;

import static org.objectweb.asm.Opcodes.ASM4;

public class MyAnnotationVisitor<A extends Annotation> extends AnnotationVisitor {

    private static int ASM_VERSION  = 1 << 24 | 8 << 16 | 0 << 8;

    private final ClassLoader classLoader;

    private final Object source;

    private final Class<A> annotationType;

    private final Consumer<MyTypeMappedAnnotation<A>> consumer;

    //一个注解的所有key-value
    private final Map<String, Object> attributes = new LinkedHashMap<>(4);


    public MyAnnotationVisitor(ClassLoader classLoader, Object source, Class<A> annotationType, Consumer<MyTypeMappedAnnotation<A>> consumer) {
        super(ASM4);
        this.classLoader = classLoader;
        this.source = source;
        this.annotationType = annotationType;
        this.consumer = consumer;
    }

    /**
     * 工厂方法，返回AnnotationVisitor的实例
     */
    static <A extends Annotation> AnnotationVisitor get(ClassLoader classLoader, Supplier<Object> sourceSupplier, String descriptor, boolean visible, Consumer<MyTypeMappedAnnotation<A>> consumer) {
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
        MyTypeMappedAnnotation<A> annotation = new MyTypeMappedAnnotation<>(this.classLoader, this.source, this.annotationType, this.attributes);
        this.consumer.accept(annotation);
    }
}
