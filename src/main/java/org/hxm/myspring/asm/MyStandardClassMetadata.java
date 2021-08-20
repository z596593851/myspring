package org.hxm.myspring.asm;

import org.springframework.core.type.ClassMetadata;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

import java.lang.reflect.Modifier;
import java.util.LinkedHashSet;

public class MyStandardClassMetadata implements MyClassMetadata {

    private final Class<?> introspectedClass;

    public MyStandardClassMetadata(Class<?> introspectedClass) {
        Assert.notNull(introspectedClass, "Class must not be null");
        this.introspectedClass = introspectedClass;
    }

    /**
     * Return the underlying Class.
     */
    public final Class<?> getIntrospectedClass() {
        return this.introspectedClass;
    }


    @Override
    public String getClassName() {
        return this.introspectedClass.getName();
    }

    @Override
    public boolean isInterface() {
        return this.introspectedClass.isInterface();
    }

    @Override
    public boolean isAnnotation() {
        return this.introspectedClass.isAnnotation();
    }

    @Override
    public boolean isAbstract() {
        return Modifier.isAbstract(this.introspectedClass.getModifiers());
    }

    @Override
    public boolean isFinal() {
        return Modifier.isFinal(this.introspectedClass.getModifiers());
    }

    @Override
    public boolean isIndependent() {
        return (!hasEnclosingClass() ||
                (this.introspectedClass.getDeclaringClass() != null &&
                        Modifier.isStatic(this.introspectedClass.getModifiers())));
    }

    @Override
    @Nullable
    public String getEnclosingClassName() {
        Class<?> enclosingClass = this.introspectedClass.getEnclosingClass();
        return (enclosingClass != null ? enclosingClass.getName() : null);
    }

    @Override
    @Nullable
    public String getSuperClassName() {
        Class<?> superClass = this.introspectedClass.getSuperclass();
        return (superClass != null ? superClass.getName() : null);
    }

    @Override
    public String[] getInterfaceNames() {
        Class<?>[] ifcs = this.introspectedClass.getInterfaces();
        String[] ifcNames = new String[ifcs.length];
        for (int i = 0; i < ifcs.length; i++) {
            ifcNames[i] = ifcs[i].getName();
        }
        return ifcNames;
    }

    @Override
    public String[] getMemberClassNames() {
        LinkedHashSet<String> memberClassNames = new LinkedHashSet<>(4);
        for (Class<?> nestedClass : this.introspectedClass.getDeclaredClasses()) {
            memberClassNames.add(nestedClass.getName());
        }
        return StringUtils.toStringArray(memberClassNames);
    }
}
