package org.hxm.myspring.function;


@FunctionalInterface
public interface MyObjectFactory<T> {
    T getObject() throws Exception;
}
