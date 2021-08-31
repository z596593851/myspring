package org.hxm.myspring.asm;

public class MySimpleMethodMetadata implements MyMethodMetadata {
    private final String methodName;

    private final int access;

    private final String declaringClassName;

    private final String returnTypeName;

    private MyMergedAnnotations annotations;

    public MySimpleMethodMetadata(String methodName, int access, String declaringClassName,
                            String returnTypeName, MyMergedAnnotations annotations) {

        this.methodName = methodName;
        this.access = access;
        this.declaringClassName = declaringClassName;
        this.returnTypeName = returnTypeName;
        this.annotations = annotations;
    }

    @Override
    public String getMethodName(){
        return this.methodName;
    }

    @Override
    public String getDeclaringClassName() {
        return this.declaringClassName;
    }

    @Override
    public String getReturnTypeName() {
        return this.returnTypeName;
    }

    @Override
    public MyMergedAnnotations getAnnotations() {
        return this.annotations;
    }

}
