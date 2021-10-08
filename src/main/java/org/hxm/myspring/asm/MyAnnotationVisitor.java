package org.hxm.myspring.asm;


import org.hxm.myspring.annotation.MyAnnotationFilter;
import org.hxm.myspring.utils.MyClassUtil;
import org.springframework.asm.AnnotationVisitor;
import org.springframework.asm.SpringAsmInfo;
import org.springframework.asm.Type;
import org.springframework.util.ClassUtils;
import java.lang.annotation.Annotation;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
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
        super(SpringAsmInfo.ASM_VERSION);
        this.classLoader = classLoader;
        this.source = source;
        this.annotationType = annotationType;
        this.consumer = consumer;
    }

    /**
     * 工厂方法，返回AnnotationVisitor的实例
     */
    static <A extends Annotation> AnnotationVisitor get(ClassLoader classLoader, Supplier<Object> sourceSupplier, String descriptor, boolean visible, Consumer<MyMergedAnnotation<A>> consumer) {
        if (!visible) {
            return null;
        }

        String typeName = Type.getType(descriptor).getClassName();
        if(MyAnnotationFilter.PLAIN.matches(typeName)){
            return null;
        }
        Object source = (sourceSupplier != null ? sourceSupplier.get() : null);
        try {
            Class<A> annotationType = (Class<A>) MyClassUtil.forName(typeName, classLoader);
            return new MyAnnotationVisitor<>(classLoader, source, annotationType, consumer);
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
    public AnnotationVisitor visitAnnotation(String name, String descriptor) {
        return visitAnnotation(descriptor, annotation -> this.attributes.put(name, annotation));
    }

    @Override
    public AnnotationVisitor visitArray(String name) {
        return new ArrayVisitor(value -> this.attributes.put(name, value));
    }

    private <T extends Annotation> AnnotationVisitor visitAnnotation(
            String descriptor, Consumer<MyMergedAnnotation<T>> consumer) {

        String className = Type.getType(descriptor).getClassName();
        if (MyAnnotationFilter.PLAIN.matches(className)) {
            return null;
        }
        Class<T> type = (Class<T>) ClassUtils.resolveClassName(className, this.classLoader);
        return new MyAnnotationVisitor<>(this.classLoader, this.source, type, consumer);
    }

    @Override
    public void visitEnd() {
        MyAnnotationTypeMappings mappings=new MyAnnotationTypeMappings(this.annotationType, MyAnnotationFilter.PLAIN);
        MyTypeMappedAnnotation<A> annotation = new MyTypeMappedAnnotation<>(mappings.get(0),this.classLoader, this.source, this.attributes,MyTypeMappedAnnotation::extractFromMap,0);
        this.consumer.accept(annotation);
    }

    private class ArrayVisitor extends AnnotationVisitor {

        private final List<Object> elements = new ArrayList<>();

        private final Consumer<Object[]> consumer;

        ArrayVisitor(Consumer<Object[]> consumer) {
            super(SpringAsmInfo.ASM_VERSION);
            this.consumer = consumer;
        }

        @Override
        public void visit(String name, Object value) {
            if (value instanceof Type) {
                value = ((Type) value).getClassName();
            }
            this.elements.add(value);
        }


        @Override
        public AnnotationVisitor visitAnnotation(String name, String descriptor) {
            return MyAnnotationVisitor.this.visitAnnotation(descriptor, this.elements::add);
        }

        @Override
        public void visitEnd() {
            Class<?> componentType = getComponentType();
            Object[] array = (Object[]) Array.newInstance(componentType, this.elements.size());
            this.consumer.accept(this.elements.toArray(array));
        }

        private Class<?> getComponentType() {
            if (this.elements.isEmpty()) {
                return Object.class;
            }
            Object firstElement = this.elements.get(0);
            if (firstElement instanceof Enum) {
                return ((Enum<?>) firstElement).getDeclaringClass();
            }
            return firstElement.getClass();
        }
    }
}
