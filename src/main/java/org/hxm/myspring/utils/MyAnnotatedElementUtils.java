package org.hxm.myspring.utils;

import org.hxm.myspring.annotation.MyAnnotationFilter;
import org.hxm.myspring.asm.MyAnnotationAttributes;
import org.hxm.myspring.asm.MyMergedAnnotation;
import org.hxm.myspring.asm.MyMergedAnnotations;
import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;

public abstract class MyAnnotatedElementUtils {

    public static MyAnnotationAttributes getMergedAnnotationAttributes(AnnotatedElement element,Class<? extends Annotation> annotationType){
        MyMergedAnnotation<?> mergedAnnotation = getAnnotations(element).get(annotationType);
        return getAnnotationAttributes(mergedAnnotation, false, false);
    }

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

    public static AnnotatedElement forAnnotations(Annotation... annotations) {
        return new MyAnnotatedElementForAnnotations(annotations);
    }

    private static class MyAnnotatedElementForAnnotations implements AnnotatedElement {

        private final Annotation[] annotations;

        MyAnnotatedElementForAnnotations(Annotation... annotations) {
            this.annotations = annotations;
        }

        @Override
        public <T extends Annotation> T getAnnotation(Class<T> annotationClass) {
            for (Annotation annotation : this.annotations) {
                if (annotation.annotationType() == annotationClass) {
                    return (T) annotation;
                }
            }
            return null;
        }

        @Override
        public Annotation[] getAnnotations() {
            return this.annotations.clone();
        }

        @Override
        public Annotation[] getDeclaredAnnotations() {
            return this.annotations.clone();
        }

    }
}
