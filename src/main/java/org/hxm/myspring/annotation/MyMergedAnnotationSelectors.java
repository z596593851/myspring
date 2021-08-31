package org.hxm.myspring.annotation;

import org.hxm.myspring.asm.MyMergedAnnotation;
import java.lang.annotation.Annotation;

public abstract class MyMergedAnnotationSelectors {
    private static final MyMergedAnnotationSelector<?> NEAREST = new MyMergedAnnotationSelectors.Nearest();

    private static final MyMergedAnnotationSelector<?> FIRST_DIRECTLY_DECLARED = new MyMergedAnnotationSelectors.FirstDirectlyDeclared();

    private MyMergedAnnotationSelectors() {

    }

    public static <A extends Annotation> MyMergedAnnotationSelector<A> nearest() {
        return (MyMergedAnnotationSelector<A>) NEAREST;
    }

    public static <A extends Annotation> MyMergedAnnotationSelector<A> firstDirectlyDeclared() {
        return (MyMergedAnnotationSelector<A>) FIRST_DIRECTLY_DECLARED;
    }

    /**
     * {@link MyMergedAnnotationSelector} 选择最近的注解
     */
    private static class Nearest implements MyMergedAnnotationSelector<Annotation> {

        @Override
        public boolean isBestCandidate(MyMergedAnnotation<Annotation> annotation) {
            return annotation.getDistance() == 0;
        }

        @Override
        public MyMergedAnnotation<Annotation> select(
                MyMergedAnnotation<Annotation> existing, MyMergedAnnotation<Annotation> candidate) {

            if (candidate.getDistance() < existing.getDistance()) {
                return candidate;
            }
            return existing;
        }

    }


    /**
     * {@link MyMergedAnnotationSelector} 选择第一个直接标注的注解
     */
    private static class FirstDirectlyDeclared implements MyMergedAnnotationSelector<Annotation> {

        @Override
        public boolean isBestCandidate(MyMergedAnnotation<Annotation> annotation) {
            return annotation.getDistance() == 0;
        }

        @Override
        public MyMergedAnnotation<Annotation> select(
                MyMergedAnnotation<Annotation> existing, MyMergedAnnotation<Annotation> candidate) {

            if (existing.getDistance() > 0 && candidate.getDistance() == 0) {
                return candidate;
            }
            return existing;
        }

    }
}
