package org.hxm.myspring.asm;


import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.MethodVisitor;
import org.springframework.asm.Type;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import static org.objectweb.asm.Opcodes.ASM4;

public class MyMethodVisitor extends MethodVisitor {

    private final ClassLoader classLoader;

    private final String declaringClassName;

    private final int access;

    private final String name;

    private final String descriptor;

    private final List<MyTypeMappedAnnotation<Annotation>> annotations = new ArrayList<>(4);

    private final Consumer<MyMethodMetadata> consumer;

    private Source source;

    public MyMethodVisitor(ClassLoader classLoader, String declaringClassName,
                           int access, String name, String descriptor, Consumer<MyMethodMetadata> consumer) {
        super(ASM4);
        this.classLoader = classLoader;
        this.declaringClassName = declaringClassName;
        this.access = access;
        this.name = name;
        this.descriptor = descriptor;
        this.consumer = consumer;
    }

    public AnnotationVisitor visitAnnotation(String descriptor, boolean visible) {
        return MyAnnotationVisitor.get(this.classLoader, this::getSource, descriptor, visible, this.annotations::add);
    }

    @Override
    public void visitEnd() {
        if (!this.annotations.isEmpty()) {
            String returnTypeName = Type.getReturnType(this.descriptor).getClassName();
            MyMethodMetadata metadata = new MyMethodMetadata(this.name,
                    this.access, this.declaringClassName, returnTypeName, annotations);
            this.consumer.accept(metadata);
        }
    }

    private Object getSource() {
        Source source = this.source;
        if (source == null) {
            source = new Source(this.declaringClassName, this.name, this.descriptor);
            this.source = source;
        }
        return source;
    }

    static final class Source {

        private final String declaringClassName;

        private final String name;

        private final String descriptor;

        private String toStringValue;

        Source(String declaringClassName, String name, String descriptor) {
            this.declaringClassName = declaringClassName;
            this.name = name;
            this.descriptor = descriptor;
        }

        @Override
        public int hashCode() {
            int result = 1;
            result = 31 * result + this.declaringClassName.hashCode();
            result = 31 * result + this.name.hashCode();
            result = 31 * result + this.descriptor.hashCode();
            return result;
        }

        @Override
        public boolean equals(Object other) {
            if (this == other) {
                return true;
            }
            if (other == null || getClass() != other.getClass()) {
                return false;
            }
            Source otherSource = (Source)other;
            return (this.declaringClassName.equals(otherSource.declaringClassName) &&
                    this.name.equals(otherSource.name) && this.descriptor.equals(otherSource.descriptor));
        }

        @Override
        public String toString() {
            String value = this.toStringValue;
            if (value == null) {
                StringBuilder builder = new StringBuilder();
                builder.append(this.declaringClassName);
                builder.append(".");
                builder.append(this.name);
                Type[] argumentTypes = Type.getArgumentTypes(this.descriptor);
                builder.append("(");
                for (Type type : argumentTypes) {
                    builder.append(type.getClassName());
                }
                builder.append(")");
                value = builder.toString();
                this.toStringValue = value;
            }
            return value;
        }
    }
}
