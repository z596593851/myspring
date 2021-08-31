package org.hxm.myspring.asm;

import org.hxm.myspring.utils.MyClassUtil;

public class MyAssignableTypeFilter {
    private final Class<?> targetType;

    public MyAssignableTypeFilter(Class<?> targetType) {
        this.targetType = targetType;
    }

    public boolean match(MySimpleMetadataReader metadataReader){
        MyClassMetadata metadata=metadataReader.getClassMetadata();
        if (matchClassName(metadata.getClassName())) {
            return true;
        }
        String superClassName = metadata.getSuperClassName();
        if (superClassName != null) {
            // Optimization to avoid creating ClassReader for super class.
            Boolean superClassMatch = matchSuperClass(superClassName);
            if (superClassMatch != null) {
                if (superClassMatch.booleanValue()) {
                    return true;
                }
            }
        }
        return false;
    }

    protected boolean matchClassName(String className) {
        return this.targetType.getName().equals(className);
    }

    protected Boolean matchSuperClass(String superClassName) {
        return matchTargetType(superClassName);
    }

    protected Boolean matchTargetType(String typeName) {
        if (this.targetType.getName().equals(typeName)) {
            return true;
        }
        else if (Object.class.getName().equals(typeName)) {
            return false;
        }
        else if (typeName.startsWith("java")) {
            try {
                Class<?> clazz = MyClassUtil.forName(typeName, getClass().getClassLoader());
                return this.targetType.isAssignableFrom(clazz);
            }
            catch (Throwable ex) {
                // Class not regularly loadable - can't determine a match that way.
            }
        }
        return null;
    }
}
