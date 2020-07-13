package org.hxm.myspring;

import org.springframework.beans.PropertyValues;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import java.lang.annotation.Annotation;
import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Field;
import java.util.*;

public class MyAutowiredProcessor implements MyBeanPostProcessor{

    private final Set<Class<? extends Annotation>> autowiredAnnotationTypes = new LinkedHashSet<>(4);

    private MyBeanFactory beanFactory;

    public MyAutowiredProcessor() {
        //AutowiredAnnotationBeanPostProcessor只处理@Value和@Autowired注解
        this.autowiredAnnotationTypes.add(Autowired.class);
        this.autowiredAnnotationTypes.add(Value.class);
    }


    public void setBeanFactory(MyBeanFactory beanFactory){
        this.beanFactory=beanFactory;
    }

    public void postProcessMergedBeanDefinition(MyBeanDefinition beanDefinition,Class<?> beanType, String beanName){
    }

    public void postProcessProperties(Object bean, String beanName) throws Throwable {
        MyInjectionMetadata metadata = buildAutowiringMetadata(bean.getClass());
        metadata.inject(bean, beanName);
    }

    public MyInjectionMetadata buildAutowiringMetadata(Class<?> clazz) throws Exception{
        if (!isCandidateClass(clazz, this.autowiredAnnotationTypes)) {
            return MyInjectionMetadata.EMPTY;
        }
        List<MyInjectionMetadata.MyInjectedElement> elements = new ArrayList<>();
        Field[] result=clazz.getDeclaredFields();
        for(Field field:result){
            Annotation ann=findAutowiredAnnotation(field);
            if(ann!=null){
                elements.add(new MyAutowiredFieldElement(field, true));
            }
        }
        return MyInjectionMetadata.forElements(elements, clazz);
    }

    private class MyAutowiredFieldElement extends MyInjectionMetadata.MyInjectedElement {
        private final boolean required;
        private volatile boolean cached = false;
        private volatile Object cachedFieldValue;

        public MyAutowiredFieldElement(Field field, boolean required) {
            super(field, null);
            this.required = required;
        }

        protected void inject(Object bean,  String beanName) throws Throwable {
            Field field = (Field) this.member;
            Set<String> autowiredBeanNames = new LinkedHashSet<>(1);
            Object value = beanFactory.resolveDependency(beanName,field,autowiredBeanNames);
            if (value != null) {
                field.set(bean, value);
            }
        }
    }

    public Annotation findAutowiredAnnotation(AccessibleObject ao){
        for(Class<?extends Annotation> type:this.autowiredAnnotationTypes){
            Annotation ann=ao.getDeclaredAnnotations()[0];
            if(ann.annotationType()==type){
                return ann;
            }
        }
        return null;
    }

    private boolean isCandidateClass(Class<?> clazz, Collection<Class<? extends Annotation>> annotationTypes){
        for (Class<? extends Annotation> annotationType : annotationTypes) {
            String annotationName=annotationType.getName();
            if (annotationName.startsWith("java.")) {
                return true;
            }
            if (clazz.getName().startsWith("java.")) {
                return false;
            }
            return true;
        }
        return false;

    }

    public static void main(String[] args) throws Exception {
//        MyAutowiredProcessor processor=new MyAutowiredProcessor();
//        MyBeanFactory beanFactory=new MyBeanFactory();
//        Class<?> clazz = People.class;
//        Constructor<?> constructorToUse = clazz.getDeclaredConstructor();
//        People bean=(People) constructorToUse.newInstance();
//        processor.buildAutowiringMetadata(clazz,bean);
//        System.out.println(bean.name);


    }
}
