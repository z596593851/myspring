package org.hxm.myspring.asm;


import org.objectweb.asm.Opcodes;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

public class MySimpleAnnotationMetadata implements MyAnnotationMetadata{
    private final String className;

    private Set<String> annotationTypes;

    private final int access;

    private final String superClassName;

    private final String[] interfaceNames;

    private final String[] memberClassNames;

    private final MyMergedAnnotations annotations;

    private final MyMethodMetadata[] annotatedMethods;


    public MySimpleAnnotationMetadata(String className, int access,
                                      String superClassName, String[] interfaceNames,
                                      String[] memberClassNames, MyMergedAnnotations annotations, MyMethodMetadata[] annotatedMethods){
        this.className = className;
        this.access = access;
        this.superClassName = superClassName;
        this.interfaceNames = interfaceNames;
        this.memberClassNames = memberClassNames;
        this.annotations = annotations;
        this.annotatedMethods=annotatedMethods;
    }

    @Override
    public String getClassName(){
        return this.className;
    }

    @Override
    public MyMergedAnnotations getAnnotations(){
        return this.annotations;
    }

    @Override
    public Set<MyMethodMetadata> getAnnotatedMethods(String annotationName) {
        Set<MyMethodMetadata> annotatedMethods = null;
        for (MyMethodMetadata annotatedMethod : this.annotatedMethods) {
            if (annotatedMethod.isAnnotated(annotationName)) {
                if (annotatedMethods == null) {
                    annotatedMethods = new LinkedHashSet<>(4);
                }
                annotatedMethods.add(annotatedMethod);
            }
        }
        return annotatedMethods != null ? annotatedMethods : Collections.emptySet();
    }

    @Override
    public Set<String> getAnnotationTypes() {
        Set<String> annotationTypes = this.annotationTypes;
        if (annotationTypes == null) {
            annotationTypes = Collections.unmodifiableSet(
                    MyAnnotationMetadata.super.getAnnotationTypes());
            this.annotationTypes = annotationTypes;
        }
        return annotationTypes;
    }


    @Override
    public boolean isInterface() {
        return (this.access & Opcodes.ACC_INTERFACE) != 0;
    }
    @Override
    public boolean isAnnotation() {
        return (this.access & Opcodes.ACC_ANNOTATION) != 0;
    }
    @Override
    public boolean isAbstract() {
        return (this.access & Opcodes.ACC_ABSTRACT) != 0;
    }
    @Override
    public boolean isFinal() {
        return (this.access & Opcodes.ACC_FINAL) != 0;
    }

    @Override
    public boolean isIndependent() {
        return false;
    }

    @Override
    public String getEnclosingClassName() {
        return null;
    }

    @Override
    public String getSuperClassName() {
        return this.superClassName;
    }
    @Override
    public String[] getInterfaceNames() {
        return this.interfaceNames.clone();
    }
    @Override
    public String[] getMemberClassNames() {
        return this.memberClassNames.clone();
    }

}
