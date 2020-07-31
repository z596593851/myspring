package org.hxm.myspring.asm;

import org.springframework.core.annotation.MergedAnnotation;

import java.lang.annotation.Annotation;
import java.util.List;
import java.util.Map;

public class MyMethodMetadata {
    private final String methodName;

    private final int access;

    private final String declaringClassName;

    private final String returnTypeName;

    private List<MyTypeMappedAnnotation<Annotation>> annotations;

    public MyMethodMetadata(String methodName, int access, String declaringClassName,
                                String returnTypeName, List<MyTypeMappedAnnotation<Annotation>> annotations) {

        this.methodName = methodName;
        this.access = access;
        this.declaringClassName = declaringClassName;
        this.returnTypeName = returnTypeName;
        this.annotations = annotations;
    }

    public boolean isAnnotated(Object requiredType){
        for (MyTypeMappedAnnotation<?> annotation : this.annotations) {
            Class<?> type = annotation.getAnnotationType();
            if (type == requiredType || type.getName().equals(requiredType)) {
                return true;
            }
        }
        return false;
    }

    public String getMethodName(){
        return this.methodName;
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
