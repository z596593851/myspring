package org.hxm.myspring.asm;

import org.springframework.asm.*;
import org.springframework.util.StringUtils;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public class MySimpleAnnotationMetadataReadingVisitor extends ClassVisitor {

    private final ClassLoader classLoader;

    private String className = "";

    private int access;

    private String superClassName;

    private String[] interfaceNames = new String[0];

    private final Set<String> memberClassNames = new LinkedHashSet<>(4);

    /**
     * 一个类上标注的所有注解
     */
    private final List<MyMergedAnnotation<?>> annotations = new ArrayList<>();

    /**
     * 一个类里所有方法上的注解
     */
    private final List<MySimpleMethodMetadata> annotatedMethods = new ArrayList<>();

    private Source source;

    private MySimpleAnnotationMetadata metadata;

    public MySimpleAnnotationMetadataReadingVisitor(ClassLoader classLoader){
        super(SpringAsmInfo.ASM_VERSION);
        this.classLoader = classLoader;
    }

    private Source getSource() {
        Source source = this.source;
        if (source == null) {
            source = new Source(this.className);
            this.source = source;
        }
        return source;
    }


    @Override
    public void visit(int version, int access, String name, String signature, String supername, String[] interfaces) {
        this.className = toClassName(name);
        this.access = access;
        if (supername != null && !isInterface(access)) {
            this.superClassName = toClassName(supername);
        }
        this.interfaceNames = new String[interfaces.length];
        for (int i = 0; i < interfaces.length; i++) {
            this.interfaceNames[i] = toClassName(interfaces[i]);
        }
    }

    @Override
    public void visitSource(String s, String s1) {
        super.visitSource(s, s1);
    }


    @Override
    public AnnotationVisitor visitAnnotation(String descriptor, boolean visible) {
        return MyAnnotationVisitor.get(this.classLoader, this::getSource, descriptor, visible, this.annotations::add);
    }

    @Override
    public MethodVisitor visitMethod(int access, String name, String descriptor, String signature, String[] exceptions) {
        if (isBridge(access)) {
            return null;
        }
        return new MyMethodVisitor(this.classLoader, this.className, access, name, descriptor, this.annotatedMethods::add);
    }

    @Override
    public void visitEnd() {
        String[] memberClassNames = StringUtils.toStringArray(this.memberClassNames);
        MyMethodMetadata[] annotatedMethods = this.annotatedMethods.toArray(new MyMethodMetadata[0]);
        MyMergedAnnotations annotations = MyMergedAnnotationsCollection.of(this.annotations);
        this.metadata = new MySimpleAnnotationMetadata(this.className, this.access,
                this.superClassName, this.interfaceNames, memberClassNames, annotations, annotatedMethods);
    }

    private boolean isBridge(int access) {
        return (access & Opcodes.ACC_BRIDGE) != 0;
    }

    public String toClassName(String name){
        return name.replace("/",".");
    }

    private boolean isInterface(int access) {
        return (access & Opcodes.ACC_INTERFACE) != 0;
    }

    public MySimpleAnnotationMetadata getMetadata(){
        return metadata;
    }

    private static final class Source {

        private final String className;

        Source(String className) {
            this.className = className;
        }

        @Override
        public int hashCode() {
            return this.className.hashCode();
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (obj == null || getClass() != obj.getClass()) {
                return false;
            }
            return this.className.equals(((MySimpleAnnotationMetadataReadingVisitor.Source) obj).className);
        }

        @Override
        public String toString() {
            return this.className;
        }

    }



}
