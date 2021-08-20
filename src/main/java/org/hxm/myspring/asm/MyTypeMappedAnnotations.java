package org.hxm.myspring.asm;

import org.hxm.myspring.postprocessor.MyAnnotationsProcessor;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;

public class MyTypeMappedAnnotations {

    private final Object source;
    private final AnnotatedElement element;

    private MyTypeMappedAnnotations(AnnotatedElement element){
        this.source=element;
        this.element=element;
    }


    static MyTypeMappedAnnotations from(AnnotatedElement element){
        return new MyTypeMappedAnnotations(element);
    }

    <A extends Annotation> MyTypeMappedAnnotation<A> get(String annotationType){
        return scan(annotationType,new MyMergedAnnotationFinder<>(annotationType));
    }

    private <C, R> R scan(C criteria, MyAnnotationsProcessor<C, R> processor) {
        R result=process(criteria,processor);
        return processor.finish(result);
    }

    private <C, R> R process(C criteria, MyAnnotationsProcessor<C, R> processor) {
        if(this.element!=null){
            R result=processor.doWithAggregate(criteria,0);
            Annotation[] annotations=element.getDeclaredAnnotations();
            return (result != null ? result : processor.doWithAnnotations(criteria, 0, source, annotations));
        }
        return null;
    }

    private static boolean isMappingForType(MyAnnotationTypeMapping mapping, Object requiredType) {

        Class<? extends Annotation> actualType = mapping.getAnnotationType();
        return ((requiredType == null || actualType == requiredType || actualType.getName().equals(requiredType)));
    }

    private static boolean isMappingForType(Class<? extends Annotation> actualType, Object requiredType){
        return requiredType == null || actualType == requiredType || actualType.getName().equals(requiredType);
    }

    private class MyMergedAnnotationFinder<A extends Annotation> implements MyAnnotationsProcessor<Object,MyTypeMappedAnnotation<A>> {
        private final Object requiredType;
        private MyTypeMappedAnnotation<A> result;

        MyMergedAnnotationFinder(Object requiredType){
            this.requiredType=requiredType;
        }

        @Override
        public MyTypeMappedAnnotation<A> doWithAggregate(Object context, int aggregateIndex) {
            return this.result;
        }

        @Override
        public MyTypeMappedAnnotation<A> doWithAnnotations(Object type, int aggregateIndex, Object source, Annotation[] annotations) {
            for (Annotation annotation : annotations) {
                if (annotation != null) {
                    MyTypeMappedAnnotation<A> result = process(type, aggregateIndex, source, annotation);
                    if (result != null) {
                        return result;
                    }
                }
            }
            return null;
        }

        private MyTypeMappedAnnotation<A> process(Object type, int aggregateIndex, Object source, Annotation annotation) {
            MyAnnotationTypeMappings mappings =new MyAnnotationTypeMappings(annotation.annotationType());
            for (int i = 0; i < mappings.size(); i++) {
                MyAnnotationTypeMapping mapping = mappings.get(i);
                if (isMappingForType(mapping, this.requiredType)) {
                    MyTypeMappedAnnotation<A> candidate = MyTypeMappedAnnotation.createIfPossible(mapping, source, annotation, aggregateIndex);
                    if (candidate != null) {
                        updateLastResult(candidate);
                    }
                }
            }
            return null;
        }

        private void updateLastResult(MyTypeMappedAnnotation<A> candidate) {
            MyTypeMappedAnnotation<A> lastResult = this.result;
            this.result = candidate;
        }

        @Override
        public MyTypeMappedAnnotation<A> finish(MyTypeMappedAnnotation<A> result) {
            return (result != null ? result : this.result);
        }
    }
}
