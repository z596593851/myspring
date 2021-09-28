package org.hxm.myspring.asm;

import java.lang.annotation.Annotation;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.function.Function;

/**
 * 一个单独的注解，及其子注解
 * @param <A> 注解的类型
 */
public interface MyMergedAnnotation<A extends Annotation>{
    String VALUE = "value";

    /**
     * 返回注解的排序，直接注解返回1，之后的注解返回2、3...缺省注解返回-1
     * @return 注解的顺序
     */
    int getDistance();

    /**
     * 当前注解是否存在于source上。默认返回true，缺省注解返回false
     * @return true/false
     */
    boolean isPresent();

    /**
     * 返回注解的类型
     * @return class
     */
    Class<A> getType();

    /**
     * 返回注解的下表
     * @return 下表，如果缺省返回-1
     */
    int getAggregateIndex();

    /**
     * 检查注解是否为直接标注注解
     * @return true/false
     */
    boolean isDirectlyPresent();

    Object getSource();

    A synthesize() throws NoSuchElementException;

    /**
     * 以MyAnnotationAttributes形式返回注解的k-v
     * @return k-v
     */
    MyAnnotationAttributes asAnnotationAttributes();

    /**
     * 以factory所指定的形式返回注解的k-v
     * @param factory map factory
     * @return k-v map
     */
    <T extends Map<String, Object>> T asMap(Function<MyMergedAnnotation<?>, T> factory);

    /**
     * 创建一个缺省的注解
     * @return 缺省的注解
     */
    static <A extends Annotation> MyMergedAnnotation<A> missing() {
        return MyMissingMergedAnnotation.getInstance();
    }
}
