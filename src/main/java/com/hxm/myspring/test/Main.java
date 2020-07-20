package com.hxm.myspring.test;

import org.hxm.myspring.annotation.MyScope;
import org.hxm.myspring.asm.MyAnnotationMetadata;
import org.hxm.myspring.asm.MyMetadataReader;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.Opcodes;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

public class Main {
    static Class<? extends Annotation> scopeAnnotationType = MyScope.class;

    public static void main(String[] args) throws Exception {
        AnnotationConfigApplicationContext ctx= new AnnotationConfigApplicationContext("com.hxm.myspring.test");
        Dog dog=(Dog) ctx.getBean("dog");
        dog.say();



//        Class<?> clazz = Dog.class;
//        Field[] result=clazz.getDeclaredFields();
//        Field field=result[0];
////        Class<?> declaringClass=field.getDeclaringClass();
//        Annotation[] fieldAnnotations=field.getAnnotations();
//        Annotation ann=fieldAnnotations[0];
//        Method[] methods = ann.annotationType().getDeclaredMethods();
//        Method me=methods[0];
//        Object value=me.invoke(ann,null);
//        System.out.println(value);

//

//        MyMetadataReader metadataReader=new MyMetadataReader(Me.class);
//        MyAnnotationMetadata metadata=metadataReader.getAnnotationMetadata();
//        System.out.println(metadata.getAnnotationAttributes(scopeAnnotationType).get("value"));


    }
}
