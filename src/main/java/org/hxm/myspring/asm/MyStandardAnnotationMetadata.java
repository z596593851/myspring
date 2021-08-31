package org.hxm.myspring.asm;

import org.hxm.myspring.annotation.MyAnnotationFilter;
import org.hxm.myspring.utils.MyAnnotatedElementUtils;
import org.hxm.myspring.utils.MyClassUtil;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

public class MyStandardAnnotationMetadata extends MyStandardClassMetadata implements MyAnnotationMetadata{

    private final MyMergedAnnotations mergedAnnotations;
    private Set<String> annotationTypes;
    private final boolean nestedAnnotationsAsMap;

    public MyStandardAnnotationMetadata(Class<?> introspectedClass, boolean nestedAnnotationsAsMap) {
        super(introspectedClass);
        this.mergedAnnotations=MyMergedAnnotations.from(introspectedClass, MyAnnotationFilter.PLAIN);
        this.nestedAnnotationsAsMap = nestedAnnotationsAsMap;
    }

    @Override
    public MyMergedAnnotations getAnnotations() {
        return this.mergedAnnotations;
    }

    @Override
    public Map<String, Object> getAnnotationAttributes(String annotationName, boolean classValuesAsString) {
        if(this.nestedAnnotationsAsMap){
            return MyAnnotationMetadata.super.getAnnotationAttributes(annotationName,classValuesAsString);
        }
        return MyAnnotatedElementUtils.getMergedAnnotationAttributes(getIntrospectedClass(),annotationName, classValuesAsString, false);
    }

    @Override
    public Set<MyMethodMetadata> getAnnotatedMethods(String annotationName) {
        Set<MyMethodMetadata> annotatedMethods = null;
        if (MyClassUtil.isCandidateClass(getIntrospectedClass(), annotationName)) {
            try {
                Method[] methods = MyClassUtil.getDeclaredMethods(getIntrospectedClass());
                for (Method method : methods) {
                    if (isAnnotatedMethod(method, annotationName)) {
                        if (annotatedMethods == null) {
                            annotatedMethods = new LinkedHashSet<>(4);
                        }
                        annotatedMethods.add(new MyStandardMethodMetadata(method, this.nestedAnnotationsAsMap));
                    }
                }
            }
            catch (Throwable ex) {
                throw new IllegalStateException("Failed to introspect annotated methods on " + getIntrospectedClass(), ex);
            }
        }
        return annotatedMethods != null ? annotatedMethods : Collections.emptySet();
    }

    private boolean isAnnotatedMethod(Method method, String annotationName) {
        return !method.isBridge() && method.getAnnotations().length > 0 &&
                MyAnnotatedElementUtils.isAnnotated(method,annotationName);
    }

    @Override
    public Set<String> getAnnotationTypes() {
        Set<String> annotationTypes = this.annotationTypes;
        if (annotationTypes == null) {
            annotationTypes = Collections.unmodifiableSet(MyAnnotationMetadata.super.getAnnotationTypes());
            this.annotationTypes = annotationTypes;
        }
        return annotationTypes;
    }

    static MyAnnotationMetadata from(Class<?> introspectedClass) {
        return new MyStandardAnnotationMetadata(introspectedClass, true);
    }

}
