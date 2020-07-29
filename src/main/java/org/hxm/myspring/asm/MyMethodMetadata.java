package org.hxm.myspring.asm;

import java.lang.annotation.Annotation;
import java.util.List;

public class MyMethodMetadata {
    private final String methodName;

    private final int access;

    private final String declaringClassName;

    private final String returnTypeName;

    private List<MyTypeMappedAnnotation<Annotation>> annotations;

    public MyMethodMetadata(String methodName, int access, String declaringClassName,
                                String returnTypeName, List<MyTypeMappedAnnotation<Annotation>> annotations) {

        this.methodName = methodName;
        this.access = access;
        this.declaringClassName = declaringClassName;
        this.returnTypeName = returnTypeName;
        this.annotations = annotations;
    }
}
