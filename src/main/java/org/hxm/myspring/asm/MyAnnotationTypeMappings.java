package org.hxm.myspring.asm;

import org.springframework.core.annotation.AnnotationFilter;
import java.lang.annotation.Annotation;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;

public class MyAnnotationTypeMappings {

    private final List<MyAnnotationTypeMapping> mappings;

    public MyAnnotationTypeMappings(Class<? extends Annotation> annotationType){
        this.mappings = new ArrayList<>();
        addAllMappings(annotationType);
//        this.mappings.forEach(MyAnnotationTypeMapping::afterAllMappingsSet);
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
                !AnnotationFilter.PLAIN.matches(source.getAnnotationType()) &&
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
