package org.hxm.myspring;

import org.springframework.beans.PropertyValues;

import java.beans.PropertyDescriptor;
import java.lang.reflect.Field;
import java.lang.reflect.Member;
import java.util.Collection;
import java.util.Collections;
import java.util.Set;

public class MyInjectionMetadata {



    public static final MyInjectionMetadata EMPTY = new MyInjectionMetadata(Object.class, Collections.emptyList());

    private final Class<?> targetClass;
    private final Collection<MyInjectedElement> injectedElements;
    private volatile Set<MyInjectedElement> checkedElements;

    public MyInjectionMetadata(Class<?> targetClass, Collection<MyInjectedElement> elements) {
        this.targetClass = targetClass;
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

        Collection<MyInjectedElement> checkedElements = this.checkedElements;
        Collection<MyInjectedElement> elementsToIterate = (checkedElements != null ? checkedElements : this.injectedElements);
        if (!elementsToIterate.isEmpty()) {
            for (MyInjectedElement element : elementsToIterate) {
                element.inject(bean, beanName);
            }
        }
    }



    public static MyInjectionMetadata forElements(Collection<MyInjectedElement> elements, Class<?> clazz) {
        return (elements.isEmpty() ? MyInjectionMetadata.EMPTY : new MyInjectionMetadata(clazz, elements));
    }
}
