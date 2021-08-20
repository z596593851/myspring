package org.hxm.myspring.postprocessor;

import org.springframework.lang.Nullable;

import java.lang.annotation.Annotation;

public interface MyAnnotationsProcessor<C, R> {

    default R doWithAggregate(C context, int aggregateIndex) {
        return null;
    }

    R doWithAnnotations(C context, int aggregateIndex, @Nullable Object source, Annotation[] annotations);

    default R finish(@Nullable R result) {
        return result;
    }
}
