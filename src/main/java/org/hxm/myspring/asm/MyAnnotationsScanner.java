package org.hxm.myspring.asm;

import org.hxm.myspring.annotation.MyAnnotationFilter;
import org.hxm.myspring.postprocessor.MyAnnotationsProcessor;
import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.util.HashMap;
import java.util.Map;

public class MyAnnotationsScanner {

    private static final Annotation[] NO_ANNOTATIONS = {};

    private static final Map<AnnotatedElement, Annotation[]> declaredAnnotationCache =
            new HashMap<>(256);

    private MyAnnotationsScanner() {}

    static Annotation[] getDeclaredAnnotations(AnnotatedElement source, boolean defensive){
        Annotation[] annotations=source.getDeclaredAnnotations();
        if (annotations.length != 0) {
            boolean allIgnored = true;
            for (int i = 0; i < annotations.length; i++) {
                Annotation annotation = annotations[i];
                if (isIgnorable(annotation.annotationType())) {
                    annotations[i] = null;
                } else {
                    allIgnored = false;
                }
            }
            annotations = (allIgnored ? NO_ANNOTATIONS : annotations);
        }
        if (!defensive || annotations.length == 0) {
            return annotations;
        }
        return annotations.clone();
    }

    private static boolean isIgnorable(Class<?> annotationType) {
        return MyAnnotationFilter.PLAIN.matches(annotationType);
    }

    /**
     *
     * @param context 期望寻找的注解
     * @param source 元素主体
     * @param processor 处理器
     */
    static <C, R> R scan(C context, AnnotatedElement source, MyAnnotationsProcessor<C, R> processor) {
        R result=process(context,source,processor);
        return processor.finish(result);
    }


    static <C, R> R process(C context, AnnotatedElement source, MyAnnotationsProcessor<C, R> processor){
        //TODO 应该带着策略
        return processElement(context, source, processor);
    }

    private static <R, C> R processElement(C context, AnnotatedElement source, MyAnnotationsProcessor<C,R> processor) {
        R result = processor.doWithAggregate(context, 0);
        return (result != null ? result : processor.doWithAnnotations(
                context, 0, source, getDeclaredAnnotations(source, false)));
    }

    private static <C, R> R processClass(C context, Class<?> source,MyAnnotationsProcessor<C, R> processor) {
        Annotation[] relevant = null;
        int remaining = Integer.MAX_VALUE;
        int aggregateIndex = 0;
        Class<?> root = source;
        while (source != null && source != Object.class && remaining > 0 &&
                !hasPlainJavaAnnotationsOnly(source)) {
            R result = processor.doWithAggregate(context, aggregateIndex);
            if (result != null) {
                return result;
            }
            //拿到类上的所有注解
            Annotation[] declaredAnnotations = getDeclaredAnnotations(source, true);
            if (relevant == null && declaredAnnotations.length > 0) {
                relevant = root.getAnnotations();
                remaining = relevant.length;
            }
            for (int i = 0; i < declaredAnnotations.length; i++) {
                if (declaredAnnotations[i] != null) {
                    boolean isRelevant = false;
                    for (int relevantIndex = 0; relevantIndex < relevant.length; relevantIndex++) {
                        if (relevant[relevantIndex] != null &&
                                declaredAnnotations[i].annotationType() == relevant[relevantIndex].annotationType()) {
                            isRelevant = true;
                            relevant[relevantIndex] = null;
                            remaining--;
                            break;
                        }
                    }
                    if (!isRelevant) {
                        declaredAnnotations[i] = null;
                    }
                }
            }
            result = processor.doWithAnnotations(context, aggregateIndex, source, declaredAnnotations);
            if (result != null) {
                return result;
            }
            source = source.getSuperclass();
            aggregateIndex++;
        }
        return null;
    }

    public static boolean hasPlainJavaAnnotationsOnly(Class<?> type) {
        return (type.getName().startsWith("java."));
    }
}
