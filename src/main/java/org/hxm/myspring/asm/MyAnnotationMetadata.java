package org.hxm.myspring.asm;

import org.hxm.myspring.annotation.MyTypeMappedAnnotation;
import org.springframework.core.annotation.MergedAnnotations;
import org.springframework.core.type.MethodMetadata;
import org.springframework.lang.Nullable;

import java.util.List;
import java.util.Set;

public class MyAnnotationMetadata {
    private final String className;

    private final int access;

    @Nullable
    private final String enclosingClassName;

    @Nullable
    private final String superClassName;

    private final boolean independentInnerClass;

    private final String[] interfaceNames;

    private Set<String> memberClassNames;

    private List<MyTypeMappedAnnotation<?>> annotations;

    public MyAnnotationMetadata(String className, int access, @Nullable String enclosingClassName,
                                @Nullable String superClassName, boolean independentInnerClass, String[] interfaceNames,
                                Set<String> memberClassNames, List<MyTypeMappedAnnotation<?>> annotations){
        this.className = className;
        this.access = access;
        this.enclosingClassName = enclosingClassName;
        this.superClassName = superClassName;
        this.independentInnerClass = independentInnerClass;
        this.interfaceNames = interfaceNames;
        this.memberClassNames = memberClassNames;
        this.annotations = annotations;

    }

    public String getClassName(){
        return this.className;
    }
}
