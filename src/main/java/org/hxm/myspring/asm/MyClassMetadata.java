package org.hxm.myspring.asm;

import org.springframework.lang.Nullable;

/**
 * 定义特定类的抽象元数据的接口，其形式不要求加载该类
 */
public interface MyClassMetadata {
    String getClassName();

    boolean isInterface();

    boolean isAnnotation();

    boolean isAbstract();

    default boolean isConcrete() {
        return !(isInterface() || isAbstract());
    }

    boolean isFinal();

    boolean isIndependent();

    default boolean hasEnclosingClass() {
        return (getEnclosingClassName() != null);
    }

    @Nullable
    String getEnclosingClassName();

    default boolean hasSuperClass() {
        return (getSuperClassName() != null);
    }

    @Nullable
    String getSuperClassName();

    String[] getInterfaceNames();

    String[] getMemberClassNames();
}
