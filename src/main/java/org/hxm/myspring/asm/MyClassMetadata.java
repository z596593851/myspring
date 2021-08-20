package org.hxm.myspring.asm;

import org.springframework.lang.Nullable;

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
