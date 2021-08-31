package org.hxm.myspring.utils;

import org.hxm.myspring.annotation.MyAnnotationFilter;
import org.hxm.myspring.asm.MyAnnotationAttributes;
import org.hxm.myspring.asm.MyMergedAnnotation;
import org.hxm.myspring.asm.MyMergedAnnotations;
import org.springframework.core.annotation.*;

import java.lang.reflect.AnnotatedElement;

public abstract class MyAnnotatedElementUtils {

    public static MyAnnotationAttributes getMergedAnnotationAttributes(AnnotatedElement element,
                                                                       String annotationName, boolean classValuesAsString, boolean nestedAnnotationsAsMap) {

        MyMergedAnnotation<?> mergedAnnotation = getAnnotations(element).get(annotationName);
        return getAnnotationAttributes(mergedAnnotation, classValuesAsString, nestedAnnotationsAsMap);
    }

    private static MyMergedAnnotations getAnnotations(AnnotatedElement element) {
        return MyMergedAnnotations.from(element,MyAnnotationFilter.PLAIN);
    }

    private static MyAnnotationAttributes getAnnotationAttributes(MyMergedAnnotation<?> annotation,
                                                                boolean classValuesAsString, boolean nestedAnnotationsAsMap) {

        if (!annotation.isPresent()) {
            return null;
        }
        return annotation.asAnnotationAttributes();
    }

    public static boolean isAnnotated(AnnotatedElement element, String annotationName) {
        return getAnnotations(element).isPresent(annotationName);
    }
}
