package org.hxm.myspring.asm;

import java.lang.annotation.Annotation;
import java.util.Map;

/**
 * 对指定的类or方法，提供asm以及反射两种注解访问方式
 * <p>访问类：{@link MyAnnotationMetadata} 访问方法：{@link MyMethodMetadata}
 * <p>asm方式：simplexxx 反射方式：standarxxx
 */
public interface MyAnnotatedTypeMetadata {

    /**
     * 基于基本元素的直接注释返回注释详细信息
     * @return merged annotations
     */
    MyMergedAnnotations getAnnotations();

    /**
     * 判断基本元素是否包含指定注解
     * @param annotationName 指定注解的权限定类名
     * @return true/false
     */
    default boolean isAnnotated(String annotationName) {
        return getAnnotations().isPresent(annotationName);
    }

    /**
     * 获取指定类型注解的属性
     * @param annotationName
     * @return
     */
    default Map<String, Object> getAnnotationAttributes(String annotationName) {
        return getAnnotationAttributes(annotationName, false);
    }

    default Map<String, Object> getAnnotationAttributes(String annotationName, boolean classValuesAsString){
        MyMergedAnnotation<Annotation> annotation = getAnnotations().get(annotationName);
        if (!annotation.isPresent()) {
            return null;
        }
        return annotation.asAnnotationAttributes();
    }

}
