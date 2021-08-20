package org.hxm.myspring.asm;

import java.util.Set;

public interface MyAnnotationMetadata extends MyClassMetadata,MyAnnotatedTypeMetadata {

    Set<MyMethodMetadata> getAnnotatedMethods(String annotationName);

    /**
     * 判断类是否包含标注了某个注解的方法
     * @param annotationName
     * @return
     */
    default boolean hasAnnotatedMethods(String annotationName){
        return !getAnnotatedMethods(annotationName).isEmpty();
    }


    boolean hasAnnotation(String annotationName);

}
