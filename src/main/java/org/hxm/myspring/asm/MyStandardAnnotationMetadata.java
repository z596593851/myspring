package org.hxm.myspring.asm;

import java.lang.annotation.Annotation;
import java.util.Map;
import java.util.Set;

public class MyStandardAnnotationMetadata extends MyStandardClassMetadata implements MyAnnotationMetadata{

    private final MyTypeMappedAnnotations mergedAnnotations;
    private Set<String> annotationTypes;
    private final boolean nestedAnnotationsAsMap;

    public MyStandardAnnotationMetadata(Class<?> introspectedClass, boolean nestedAnnotationsAsMap) {
        super(introspectedClass);
        this.mergedAnnotations=MyTypeMappedAnnotations.from(introspectedClass);
        this.nestedAnnotationsAsMap = nestedAnnotationsAsMap;
    }

    @Override
    public Map<String, Object> getAnnotationAttributes(String annotationName, boolean classValuesAsString) {
        if(this.nestedAnnotationsAsMap){
            return MyAnnotationMetadata.super.getAnnotationAttributes(annotationName,false);
        }
        return null;
    }

    @Override
    public Set<MyMethodMetadata> getAnnotatedMethods(String annotationName) {
        return null;
    }

    @Override
    public boolean hasAnnotation(String annotationName) {
        return false;
    }

    @Override
    public MyTypeMappedAnnotations getAnnotationss() {
        return this.mergedAnnotations;
    }

}
