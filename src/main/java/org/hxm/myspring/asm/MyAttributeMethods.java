package org.hxm.myspring.asm;

import org.hxm.myspring.utils.MyClassUtil;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;

public class MyAttributeMethods {

    static final MyAttributeMethods NONE = new MyAttributeMethods(null, new Method[0]);


    private static final Map<Class<? extends Annotation>, MyAttributeMethods> cache =
            new HashMap<>();
    private static final Comparator<Method> methodComparator = (m1, m2) -> {
        if (m1 != null && m2 != null) {
            return m1.getName().compareTo(m2.getName());
        }
        return m1 != null ? -1 : 1;
    };
    private final Class<? extends Annotation> annotationType;
    private final Method[] attributeMethods;
    private final boolean[] canThrowTypeNotPresentException;
    private final boolean hasDefaultValueMethod;

    private final boolean hasNestedAnnotation;

    private MyAttributeMethods(Class<? extends Annotation> annotationType, Method[] attributeMethods) {
        this.annotationType = annotationType;
        this.attributeMethods = attributeMethods;
        this.canThrowTypeNotPresentException = new boolean[attributeMethods.length];
        boolean foundDefaultValueMethod = false;
        boolean foundNestedAnnotation = false;
        for (int i = 0; i < attributeMethods.length; i++) {
            Method method = this.attributeMethods[i];
            Class<?> type = method.getReturnType();
            if (method.getDefaultValue() != null) {
                foundDefaultValueMethod = true;
            }
            if (type.isAnnotation() || (type.isArray() && type.getComponentType().isAnnotation())) {
                foundNestedAnnotation = true;
            }
            MyClassUtil.makeAccessible(method);
            this.canThrowTypeNotPresentException[i] = (type == Class.class || type == Class[].class || type.isEnum());
        }
        this.hasDefaultValueMethod = foundDefaultValueMethod;
        this.hasNestedAnnotation = foundNestedAnnotation;
    }

    static MyAttributeMethods forAnnotationType(Class<? extends Annotation> annotationType) {
        if (annotationType == null) {
            return NONE;
        }
        return cache.computeIfAbsent(annotationType, MyAttributeMethods::compute);
    }

    private static MyAttributeMethods compute(Class<? extends Annotation> annotationType) {
        Method[] methods = annotationType.getDeclaredMethods();
        int size = methods.length;
        for (int i = 0; i < methods.length; i++) {
            if (!isAttributeMethod(methods[i])) {
                methods[i] = null;
                size--;
            }
        }
        if (size == 0) {
            return NONE;
        }
        Arrays.sort(methods, methodComparator);
        Method[] attributeMethods = Arrays.copyOf(methods, size);
        return new MyAttributeMethods(annotationType, attributeMethods);
    }

    private static boolean isAttributeMethod(Method method) {
        return (method.getParameterCount() == 0 && method.getReturnType() != void.class);
    }

    public int size() {
        return this.attributeMethods.length;
    }

    public Method get(int index) {
        return this.attributeMethods[index];
    }

    int indexOf(String name) {
        for (int i = 0; i < this.attributeMethods.length; i++) {
            if (this.attributeMethods[i].getName().equals(name)) {
                return i;
            }
        }
        return -1;
    }


}
