package org.hxm.myspring.asm;

import java.lang.annotation.Annotation;
import java.util.Map;

public interface MyAnnotatedTypeMetadata {


    default Map<String, Object> getAnnotationAttributes(String annotationName, boolean classValuesAsString) {
        MyTypeMappedAnnotation<Annotation> annotation = getAnnotationss().get(annotationName);
        if (!annotation.isPresent()) {
            return null;
        }
        return annotation.asAnnotationAttributes();
    }

    MyTypeMappedAnnotations getAnnotationss();
}
