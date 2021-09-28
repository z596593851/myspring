package org.hxm.myspring.factory;

import java.beans.PropertyDescriptor;
import java.lang.reflect.Field;
import java.lang.reflect.Member;
import java.util.Collection;
import java.util.Collections;
import java.util.Set;

public class MyInjectionMetadata {
    public static final MyInjectionMetadata EMPTY = new MyInjectionMetadata(Collections.emptyList());
    private final Collection<MyInjectedElement> injectedElements;

    public MyInjectionMetadata(Collection<MyInjectedElement> elements) {
        this.injectedElements = elements;
    }

    public abstract static class MyInjectedElement {

        protected final Member member;

        protected final boolean isField;

        protected final PropertyDescriptor pd;

        protected MyInjectedElement(Member member, PropertyDescriptor pd) {
            this.member = member;
            this.isField = (member instanceof Field);
            this.pd = pd;
        }
        protected void inject(Object bean,  String beanName) throws Throwable{}
    }

    public void inject(Object bean, String beanName) throws Throwable{
        if (!this.injectedElements.isEmpty()) {
            for (MyInjectedElement element : this.injectedElements) {
                element.inject(bean, beanName);
            }
        }
    }

    public static MyInjectionMetadata forElements(Collection<MyInjectedElement> elements) {
        return (elements.isEmpty() ? MyInjectionMetadata.EMPTY : new MyInjectionMetadata(elements));
    }
}
