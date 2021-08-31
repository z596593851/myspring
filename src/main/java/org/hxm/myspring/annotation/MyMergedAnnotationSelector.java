package org.hxm.myspring.annotation;

import org.hxm.myspring.asm.MyMergedAnnotation;

import java.lang.annotation.Annotation;

@FunctionalInterface
public interface MyMergedAnnotationSelector<A extends Annotation> {
    /**
     * 确定现有注释是否已知为最佳候选注释，并且可以跳过任何后续选择
     * @param annotation 待确认注解
     * @return true/false
     */
    default boolean isBestCandidate(MyMergedAnnotation<A> annotation) {
        return false;
    }

    /**
     * 选择现有或候选注解中更合适的注解
     * @param existing 更早阶段返回的已存在的注解
     * @param candidate 可能更适合的候选注解
     * @return 更合适的注解
     */
    MyMergedAnnotation<A> select(MyMergedAnnotation<A> existing, MyMergedAnnotation<A> candidate);

}
