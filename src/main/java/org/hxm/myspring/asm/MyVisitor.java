package org.hxm.myspring.asm;


import org.hxm.myspring.annotation.MyTypeMappedAnnotation;
import org.objectweb.asm.*;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public class MyVisitor extends ClassVisitor {

    private final ClassLoader classLoader;

    private static int ASM_VERSION  = 1 << 24 | 8 << 16 | 0 << 8;

    private int ACC_INTERFACE = 0x0200;

    private String className = "";

    private int access;

    private String superClassName;

    private String[] interfaceNames = new String[0];

    private String enclosingClassName;

    private boolean independentInnerClass;

    private Set<String> memberClassNames = new LinkedHashSet<>(4);

    private List<MyTypeMappedAnnotation<Annotation>> annotations = new ArrayList<>();

    private Source source;

    private MyAnnotationMetadata metadata;

    public MyVisitor(ClassLoader classLoader){
        super(ASM_VERSION);
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
            return this.className.equals(((MyVisitor.Source) obj).className);
        }

        @Override
        public String toString() {
            return this.className;
        }

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
    public void visitEnd() {
        this.metadata = new MyAnnotationMetadata(this.className, this.access,
                this.enclosingClassName, this.superClassName, this.independentInnerClass,
                this.interfaceNames, memberClassNames, annotations);
    }

    public String toClassName(String name){
        return name.replace(".","/");
    }

    private boolean isInterface(int access) {
        return (access & org.springframework.asm.Opcodes.ACC_INTERFACE) != 0;
    }

    public MyAnnotationMetadata getMetadata(){
        return metadata;
    }





}
