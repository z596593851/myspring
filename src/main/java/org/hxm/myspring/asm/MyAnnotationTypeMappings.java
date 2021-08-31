package org.hxm.myspring.asm;

import org.hxm.myspring.annotation.MyAnnotationFilter;
import java.lang.annotation.Annotation;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;

/**
 * 一个注解里的子注解的{@link MyAnnotationTypeMapping}
 */
public class MyAnnotationTypeMappings {

    private final List<MyAnnotationTypeMapping> mappings;
    private final MyAnnotationFilter filter;

    public MyAnnotationTypeMappings(Class<? extends Annotation> annotationType, MyAnnotationFilter filter){
        this.mappings = new ArrayList<>();
        this.filter=filter;
        addAllMappings(annotationType);
    }

    private void addAllMappings(Class<? extends Annotation> annotationType) {
        Deque<MyAnnotationTypeMapping> queue = new ArrayDeque<>();
        queue.addLast(new MyAnnotationTypeMapping(null, annotationType, null));
        while (!queue.isEmpty()) {
            MyAnnotationTypeMapping mapping = queue.removeFirst();
            this.mappings.add(mapping);
            addMetaAnnotationsToQueue(queue, mapping);
        }
    }

    private void addMetaAnnotationsToQueue(Deque<MyAnnotationTypeMapping> queue, MyAnnotationTypeMapping source) {
        Annotation[] metaAnnotations = MyAnnotationsScanner.getDeclaredAnnotations(source.getAnnotationType(), false);
        for (Annotation metaAnnotation : metaAnnotations) {
            if(!isMappable(source,metaAnnotation)){
                continue;
            }
            queue.addLast(new MyAnnotationTypeMapping(source, metaAnnotation.annotationType(), metaAnnotation));
        }
    }

    private boolean isMappable(MyAnnotationTypeMapping source, Annotation metaAnnotation){
        return (metaAnnotation!=null &&
                !this.filter.matches(metaAnnotation) &&
                !MyAnnotationFilter.PLAIN.matches(source.getAnnotationType()) &&
                !isAlreadyMapped(source, metaAnnotation));
    }

    private boolean isAlreadyMapped(MyAnnotationTypeMapping source, Annotation metaAnnotation){
        Class<? extends Annotation> annotationType = metaAnnotation.annotationType();
        MyAnnotationTypeMapping mapping = source;
        while (mapping != null) {
            if (mapping.getAnnotationType() == annotationType) {
                return true;
            }
            mapping = mapping.getSource();
        }
        return false;
    }

    int size() {
        return this.mappings.size();
    }

    MyAnnotationTypeMapping get(int index) {
        return this.mappings.get(index);
    }

}
