package org.hxm.myspring.asm;

import org.hxm.myspring.annotation.MyAnnotationFilter;
import org.hxm.myspring.utils.MyAnnotatedElementUtils;
import java.lang.reflect.Method;
import java.util.Map;

public class MyStandardMethodMetadata implements MyMethodMetadata{
    private final Method introspectedMethod;

    private final boolean nestedAnnotationsAsMap;

    private final MyMergedAnnotations mergedAnnotations;

    public MyStandardMethodMetadata(Method introspectedMethod, boolean nestedAnnotationsAsMap){
        this.introspectedMethod = introspectedMethod;
        this.nestedAnnotationsAsMap = nestedAnnotationsAsMap;
        this.mergedAnnotations = MyTypeMappedAnnotations.from(introspectedMethod, MyAnnotationFilter.PLAIN);
    }

    @Override
    public MyMergedAnnotations getAnnotations() {
        return this.mergedAnnotations;
    }

    @Override
    public Map<String, Object> getAnnotationAttributes(String annotationName, boolean classValuesAsString) {
        if (this.nestedAnnotationsAsMap) {
            return MyMethodMetadata.super.getAnnotationAttributes(annotationName, classValuesAsString);
        }
        return MyAnnotatedElementUtils.getMergedAnnotationAttributes(this.introspectedMethod,
                annotationName, classValuesAsString, false);
    }

    @Override
    public String getMethodName() {
        return this.introspectedMethod.getName();
    }

    @Override
    public String getDeclaringClassName() {
        return this.introspectedMethod.getDeclaringClass().getName();
    }

    @Override
    public String getReturnTypeName() {
        return this.introspectedMethod.getReturnType().getName();
    }
}
