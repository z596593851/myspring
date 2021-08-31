package org.hxm.myspring.annotation;

import java.lang.annotation.Annotation;

public interface MyAnnotationFilter {

    MyAnnotationFilter PLAIN = packages("java.lang", "org.springframework.lang");
    MyAnnotationFilter JAVA = packages("java", "javax");
    MyAnnotationFilter ALL = new MyAnnotationFilter() {
        @Override
        public boolean matches(Annotation annotation) {
            return true;
        }
        @Override
        public boolean matches(Class<?> type) {
            return true;
        }
        @Override
        public boolean matches(String typeName) {
            return true;
        }
        @Override
        public String toString() {
            return "All annotations filtered";
        }
    };

    @Deprecated
    MyAnnotationFilter NONE = new MyAnnotationFilter() {
        @Override
        public boolean matches(Annotation annotation) {
            return false;
        }
        @Override
        public boolean matches(Class<?> type) {
            return false;
        }
        @Override
        public boolean matches(String typeName) {
            return false;
        }
        @Override
        public String toString() {
            return "No annotation filtering";
        }
    };


    default boolean matches(Annotation annotation) {
        return matches(annotation.annotationType());
    }

    default boolean matches(Class<?> type) {
        return matches(type.getName());
    }

    boolean matches(String typeName);

    static MyAnnotationFilter packages(String... packages) {
        return new MyPackagesAnnotationFilter(packages);
    }
}
