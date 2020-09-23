package org.hxm.myspring.asm;


import org.objectweb.asm.Opcodes;

import java.lang.annotation.Annotation;
import java.util.*;

public class MySimpleAnnotationMetadata {
    private final String className;

    private final int access;

    private final String enclosingClassName;

    private final String superClassName;

    private final boolean independentInnerClass;

    private final String[] interfaceNames;

    private Set<String> memberClassNames;

    /**
     * 一个类上标注的所有注解,如compoment,scope
     */
    private List<MyTypeMappedAnnotation<Annotation>> annotations;

    /**
     * 一个类里所有方法上的注解,如bean
     */
    private List<MyMethodMetadata> annotatedMethods;

    public MySimpleAnnotationMetadata(String className, int access, String enclosingClassName,
                                      String superClassName, boolean independentInnerClass, String[] interfaceNames,
                                      Set<String> memberClassNames, List<MyTypeMappedAnnotation<Annotation>> annotations,List<MyMethodMetadata> annotatedMethods){
        this.className = className;
        this.access = access;
        this.enclosingClassName = enclosingClassName;
        this.superClassName = superClassName;
        this.independentInnerClass = independentInnerClass;
        this.interfaceNames = interfaceNames;
        this.memberClassNames = memberClassNames;
        this.annotations = annotations;
        this.annotatedMethods=annotatedMethods;

    }

    public String getClassName(){
        return this.className;
    }

    public List<MyTypeMappedAnnotation<Annotation>> getAnnotations(){
        return this.annotations;
    }

    public Map<String, Object> getAnnotationAttributes(Class<?> annotationClass){
        Object requiredType=annotationClass.getName();
        for(MyTypeMappedAnnotation<Annotation> myTypeMappedAnnotation:annotations){
            Class<? extends Annotation> actualType = myTypeMappedAnnotation.getAnnotationType();
            if(actualType==requiredType || actualType.getName().equals(requiredType)){
                Map<String, Object> map=(Map<String, Object>)myTypeMappedAnnotation.getAttributes();
                return map;
            }
        }
        return null;
    }

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

    /**
     * 判断类是否有某个注解
     * @param requiredType 注解名or注解类
     * @return
     */
    public boolean hasAnnotation(Object requiredType) {
        for(MyTypeMappedAnnotation<?> annotation:this.annotations){
            Class<?> type = annotation.getAnnotationType();
            if (type == requiredType || type.getName().equals(requiredType)) {
                return true;
            }
        }
        return false;

    }

    /**
     * 判断类是否包含标注了某个注解的方法
     * @param annotationName
     * @return
     */
    public boolean hasAnnotatedMethods(String annotationName){
        return !getAnnotatedMethods(annotationName).isEmpty();
    }

    public boolean isInterface() {
        return (this.access & Opcodes.ACC_INTERFACE) != 0;
    }
    public boolean isAnnotation() {
        return (this.access & Opcodes.ACC_ANNOTATION) != 0;
    }

    public boolean isAbstract() {
        return (this.access & Opcodes.ACC_ABSTRACT) != 0;
    }

    public boolean isFinal() {
        return (this.access & Opcodes.ACC_FINAL) != 0;
    }

    public boolean isIndependent() {
        return (this.enclosingClassName == null || this.independentInnerClass);
    }

    public String getEnclosingClassName() {
        return this.enclosingClassName;
    }

    public String getSuperClassName() {
        return this.superClassName;
    }

    public String[] getInterfaceNames() {
        return this.interfaceNames.clone();
    }

    public String[] getMemberClassNames() {
        String[] a=new String[memberClassNames.size()];
        return this.memberClassNames.toArray(a).clone();
    }

}
