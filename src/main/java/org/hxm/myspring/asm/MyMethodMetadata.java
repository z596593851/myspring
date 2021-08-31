package org.hxm.myspring.asm;

public interface MyMethodMetadata extends MyAnnotatedTypeMetadata{
    /**
     * 返回方法名
     */
    String getMethodName();

    /**
     * 返回声明这个方法的类的全限定类名
     */
    String getDeclaringClassName();

    /**
     * 返回这个方法的返回值的全限定类名
     */
    String getReturnTypeName();
}
