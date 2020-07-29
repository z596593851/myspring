package org.hxm.myspring.asm;

import java.lang.annotation.Annotation;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class MyAnnotationMetadata {
    private final String className;

    private final int access;

    private final String enclosingClassName;

    private final String superClassName;

    private final boolean independentInnerClass;

    private final String[] interfaceNames;

    private Set<String> memberClassNames;

    //一个类/方法上标注的所有注解,如compoment,scope,bean
    private List<MyTypeMappedAnnotation<Annotation>> annotations;

    public MyAnnotationMetadata(String className, int access, String enclosingClassName,
                                String superClassName, boolean independentInnerClass, String[] interfaceNames,
                                Set<String> memberClassNames, List<MyTypeMappedAnnotation<Annotation>> annotations){
        this.className = className;
        this.access = access;
        this.enclosingClassName = enclosingClassName;
        this.superClassName = superClassName;
        this.independentInnerClass = independentInnerClass;
        this.interfaceNames = interfaceNames;
        this.memberClassNames = memberClassNames;
        this.annotations = annotations;

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

    public boolean hasAnnotation(Object requiredType) {
        for(MyTypeMappedAnnotation<?> annotation:this.annotations){
            Class<?> type = annotation.getAnnotationType();
            if (type == requiredType || type.getName().equals(requiredType)) {
                return true;
            }
        }
        return false;

    }
}
