package org.hxm.myspring.asm;

import java.util.LinkedHashSet;
import java.util.Set;
import java.util.stream.Collectors;

public interface MyAnnotationMetadata extends MyClassMetadata,MyAnnotatedTypeMetadata {

    /**
     * 获取标注在基本类上的所有注解的全限定类名
     * @return 类名set
     */
    default Set<String> getAnnotationTypes(){
        return getAnnotations().stream()
                .filter(MyMergedAnnotation::isDirectlyPresent)
                .map(annotation -> annotation.getType().getName())
                .collect(Collectors.toCollection(LinkedHashSet::new));
    }

    /**
     * 返回使用给定注释类型进行注释（或元注释）的所有方法的方法元数据。
     * <p>对于任何返回的方法，{@link MyMethodMetadata#isAnnotated}将为给定注释类型返回true。
     * @param annotationName 给定注解的全限定类名
     * @return 匹配的方法元数据
     */
    Set<MyMethodMetadata> getAnnotatedMethods(String annotationName);

    /**
     * 判断类是否包含标注了某个注解的方法
     * @param annotationName 指定注解的全限定类名
     * @return true/false
     */
    default boolean hasAnnotatedMethods(String annotationName){
        return !getAnnotatedMethods(annotationName).isEmpty();
    }

    /**
     * 检查基本类上是否标注了指定注解
     * @param annotationName 指定注解的权限定类名
     * @return true/false
     */
    default boolean hasAnnotation(String annotationName){
        return getAnnotations().isDirectlyPresent(annotationName);
    }

    /**
     * 给指定class创建AnnotationMetadata实例
     * @param type 指定类
     * @return AnnotationMetadata
     */
    static MyAnnotationMetadata introspect(Class<?> type) {
        return MyStandardAnnotationMetadata.from(type);
    }

}
