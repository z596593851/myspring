package org.hxm.myspring.asm;

import org.hxm.myspring.annotation.MyAnnotationFilter;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.util.stream.Stream;

/**
 * merged annotations的集合
 *  <p>For example, a {@code @PostMapping} annotation might be defined as follows:
 *
 *  <pre class="code">
 *  &#064;Retention(RetentionPolicy.RUNTIME)
 *  &#064;RequestMapping(method = RequestMethod.POST)
 *  public &#064;interface PostMapping {
 *
 *      &#064;AliasFor(attribute = "path")
 *      String[] value() default {};
 *
 *      &#064;AliasFor(attribute = "value")
 *      String[] path() default {};
 *  }
 * </pre>
 *
 * <p>If a method is annotated with {@code @PostMapping("/home")} it will contain
 * merged annotations for both {@code @PostMapping} and the meta-annotation
 *
 */
public interface MyMergedAnnotations extends Iterable<MyMergedAnnotation<Annotation>> {

    /**
     * 检查一个注解是否存在
     * @param annotationType 待确认注解的全限定类名
     * @return true/false
     */
    boolean isPresent(String annotationType);

    /**
     * 检查指定注解是否存在
     * @param annotationType 待检测的注解的权限定类名
     * @return true/false
     */
    boolean isDirectlyPresent(String annotationType);

    /**
     * 获取与指定类型最匹配的注解（annotation or meta-annotation）
     * 如果没有则返回 {@link MyMergedAnnotation#missing()}
     * @param annotationType 指定注解的权限定类名
     * @return {@link MyMergedAnnotation} 实例
     */
    <A extends Annotation> MyMergedAnnotation<A> get(String annotationType);

    Stream<MyMergedAnnotation<Annotation>> stream();

    /**
     * 从指定的 element 创建一个包含所有注解(annotation and meta-annotation)的 {@link MyMergedAnnotation}
     * @param element 指定element
     * @param annotationFilter 注解过滤器
     * @return {@link MyTypeMappedAnnotations}
     */
    static MyTypeMappedAnnotations from(AnnotatedElement element, MyAnnotationFilter annotationFilter){
        return MyTypeMappedAnnotations.from(element,annotationFilter);
    }
}
