package org.hxm.myspring.asm;



import org.springframework.asm.AnnotationVisitor;
import org.springframework.asm.MethodVisitor;
import org.springframework.asm.SpringAsmInfo;
import org.springframework.asm.Type;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;



public class MyMethodVisitor extends MethodVisitor {

    private final ClassLoader classLoader;

    private final String declaringClassName;

    private final int access;

    private final String name;

    private final String descriptor;

    private final List<MyMergedAnnotation<?>> annotations = new ArrayList<>(4);

    private final Consumer<MySimpleMethodMetadata> consumer;

    private Source source;

    public MyMethodVisitor(ClassLoader classLoader, String declaringClassName,
                           int access, String name, String descriptor, Consumer<MySimpleMethodMetadata> consumer) {
        super(SpringAsmInfo.ASM_VERSION);
        this.classLoader = classLoader;
        this.declaringClassName = declaringClassName;
        this.access = access;
        this.name = name;
        this.descriptor = descriptor;
        this.consumer = consumer;
    }

    @Override
    public AnnotationVisitor visitAnnotation(String descriptor, boolean visible) {
        System.out.println("method's visitAnnotation:"+descriptor);
        return MyAnnotationVisitor.get(this.classLoader, this::getSource, descriptor, visible, this.annotations::add);
    }

    @Override
    public void visitEnd() {
        if (!this.annotations.isEmpty()) {
            String returnTypeName = Type.getReturnType(this.descriptor).getClassName();
            MyMergedAnnotations annotations = MyMergedAnnotationsCollection.of(this.annotations);
            MySimpleMethodMetadata metadata = new MySimpleMethodMetadata(this.name,
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
