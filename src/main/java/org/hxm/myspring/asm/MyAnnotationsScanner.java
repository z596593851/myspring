package org.hxm.myspring.asm;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Member;
import java.util.HashMap;
import java.util.Map;

public class MyAnnotationsScanner {

    private static final Map<AnnotatedElement, Annotation[]> declaredAnnotationCache =
            new HashMap<>(256);

    private MyAnnotationsScanner() {}

    static Annotation[] getDeclaredAnnotations(AnnotatedElement source, boolean defensive) {
        boolean cached = false;
        Annotation[] annotations = declaredAnnotationCache.get(source);
        if (annotations != null) {
            cached = true;
        }
        else {
            annotations = source.getDeclaredAnnotations();
            if (annotations.length != 0) {
                if (source instanceof Class || source instanceof Member) {
                    declaredAnnotationCache.put(source, annotations);
                    cached = true;
                }
            }
        }
        if (!defensive || annotations.length == 0 || !cached) {
            return annotations;
        }
        return annotations.clone();
    }
}
