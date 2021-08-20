package org.hxm.myspring.asm;

import java.lang.reflect.Method;

@FunctionalInterface
public interface MyValueExtractor {
    Object extract(Method attribute, Object object);
}
