package org.hxm.myspring.asm;


import org.objectweb.asm.Opcodes;

import java.lang.annotation.Annotation;
import java.util.*;

public class MySimpleAnnotationMetadata implements MyAnnotationMetadata{
    private String className;

    private int access;

    private String enclosingClassName;

    private String superClassName;

    private boolean independentInnerClass;

    private String[] interfaceNames;

    private Set<String> memberClassNames;

    private Class<?> introspectedClass;

    private boolean nestedAnnotationsAsMap;

    /**
     * 一个类上标注的所有注解,如compoment,scope
     */
    private List<MyTypeMappedAnnotation<Annotation>> annotations;

    private MyTypeMappedAnnotations annotationss;
    /**
     * 一个类里所有方法上的注解,如bean
     */
    private List<MyMethodMetadata> annotatedMethods;

    public MySimpleAnnotationMetadata(Class<?> introspectedClass, boolean nestedAnnotationsAsMap) {
        this.introspectedClass = introspectedClass;
        this.nestedAnnotationsAsMap = nestedAnnotationsAsMap;
    }


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

    @Override
    public String getClassName(){
        return this.className;
    }

    public List<MyTypeMappedAnnotation<Annotation>> getAnnotations(){
        return this.annotations;
    }

    @Override
    public MyTypeMappedAnnotations getAnnotationss(){
        return this.annotationss;
    }

    @Override
    public Map<String, Object> getAnnotationAttributes(String annotationName,boolean classValuesAsString){
        Object requiredType=annotationName;
        for(MyTypeMappedAnnotation<Annotation> myTypeMappedAnnotation:annotations){
            Class<? extends Annotation> actualType = myTypeMappedAnnotation.getAnnotationType();
            if(actualType==requiredType || actualType.getName().equals(requiredType)){
                Map<String, Object> map=(Map<String, Object>)myTypeMappedAnnotation.getAttributes();
                return map;
            }
        }
        return null;
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


    /**
     * 判断类是否有某个注解
     * @param requiredType 注解名or注解类
     * @return
     */
    @Override
    public boolean hasAnnotation(String requiredType) {
        for(MyTypeMappedAnnotation<?> annotation:this.annotations){
            Class<?> type = annotation.getAnnotationType();
            if (type == (Object)requiredType || type.getName().equals(requiredType)) {
                return true;
            }
        }
        return false;

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
        return (this.enclosingClassName == null || this.independentInnerClass);
    }
    @Override
    public String getEnclosingClassName() {
        return this.enclosingClassName;
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
        String[] a=new String[memberClassNames.size()];
        return this.memberClassNames.toArray(a).clone();
    }

}
