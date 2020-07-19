package org.hxm.myspring.asm;

import org.hxm.myspring.annotation.MyTypeMappedAnnotation;
import org.springframework.core.annotation.MergedAnnotations;
import org.springframework.core.type.MethodMetadata;
import org.springframework.lang.Nullable;

import java.lang.annotation.Annotation;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class MyAnnotationMetadata {
    private final String className;

    private final int access;

    @Nullable
    private final String enclosingClassName;

    @Nullable
    private final String superClassName;

    private final boolean independentInnerClass;

    private final String[] interfaceNames;

    private Set<String> memberClassNames;

    //一个类里的所有注解
    private List<MyTypeMappedAnnotation<Annotation>> annotations;

    public MyAnnotationMetadata(String className, int access, @Nullable String enclosingClassName,
                                @Nullable String superClassName, boolean independentInnerClass, String[] interfaceNames,
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
}
