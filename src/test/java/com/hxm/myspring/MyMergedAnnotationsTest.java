package com.hxm.myspring;

import org.hxm.myspring.annotation.MyValue;
import org.hxm.myspring.asm.MyAnnotationAttributes;
import org.hxm.myspring.asm.MyMergedAnnotation;
import org.hxm.myspring.asm.MyMergedAnnotations;
import org.hxm.myspring.stereotype.MyComponent;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import java.lang.reflect.Field;

public class MyMergedAnnotationsTest {


    /**
     * 检查某个类是否标注了某个注解，并获取注解中的某个值
     */
    @Test
    public void testIsPresent1() {
        MyMergedAnnotations annotations = MyMergedAnnotations.from(Person.class);
        boolean present = annotations.isPresent(MyComponent.class);
        Assertions.assertTrue(present);

        //get的时候就已经把注解的属性都解析好了
        MyMergedAnnotation<?> mergedAnnotation = annotations.get(MyComponent.class);

        MyAnnotationAttributes annotationAttributes = mergedAnnotation.asAnnotationAttributes();
        String value = (String)annotationAttributes.get("value");
        System.out.println(value);

    }

    /**
     * 检查某个成员是否标注了某个注解，并获取注解中的某个值
     */
    @Test
    public void testIsPresent2(){
        Field[] fields = Person.class.getDeclaredFields();
        for(Field field:fields){
            MyMergedAnnotations annotations = MyMergedAnnotations.from(field);
            MyMergedAnnotation<?> annotation = annotations.get(MyValue.class);
            if(annotation.isPresent()){
                System.out.println(field.getName());
                MyAnnotationAttributes annotationAttributes = annotation.asAnnotationAttributes();
                String value = (String)annotationAttributes.get("value");
                System.out.println(value);
            }
        }
    }
}
