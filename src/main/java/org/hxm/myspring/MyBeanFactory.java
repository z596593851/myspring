package org.hxm.myspring;


import org.hxm.myspring.function.MyObjectFactory;
import org.springframework.beans.factory.annotation.Value;

import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class MyBeanFactory {

    private static final Map<Class<?>, Object> DEFAULT_TYPE_VALUES;

    private Map<String,MyBeanDefinition> beanDefinitionMap = new HashMap<>();

    private Map<String, Object> singletonObjects = new ConcurrentHashMap<>();

    private List<MyBeanPostProcessor> beanPostProcessors=new ArrayList<>();

    static {
        Map<Class<?>, Object> values = new HashMap<>();
        values.put(boolean.class, false);
        values.put(byte.class, (byte) 0);
        values.put(short.class, (short) 0);
        values.put(int.class, 0);
        values.put(long.class, (long) 0);
        DEFAULT_TYPE_VALUES = Collections.unmodifiableMap(values);
    }

    public MyBeanFactory(){
        beanPostProcessors.add(new MyAutowiredProcessor());
    }

    public Object getBean(String beanName) throws Exception{
//        final RootBeanDefinition mbd = getMergedLocalBeanDefinition(beanName);
        try {
            MyBeanDefinition mbd=new MyBeanDefinition();
            Object bean=getSingleton(beanName,()->{
                try {
                    return createBean(beanName,mbd);
                } catch (Exception e) {
                    throw e;
                }
            });
            return bean;
        } catch (Exception e) {
            throw e;
        }
    }

    public Object createBean(String beanName,MyBeanDefinition mbd) throws Exception{
        Object bean = this.singletonObjects.get(beanName);
        if(bean==null){
            bean=createBeanInstance(beanName,mbd);
            Class<?> beanType=bean.getClass();
            mbd.resolvedTargetType = beanType;
            try {
                populateBean(beanName,mbd,bean);
            }catch (Throwable ex){
                ex.printStackTrace();
            }
        }

        return bean;
    }

    public Object getSingleton(String beanName, MyObjectFactory<?> objectFactory) throws Exception{
        Object singletonObject = this.singletonObjects.get(beanName);
        if(singletonObject==null){
            singletonObject=objectFactory.getObject();
            addSingleton(beanName,singletonObject);
        }
        return singletonObject;
    }

    public void addSingleton(String beanName, Object singletonObject) {
        this.singletonObjects.put(beanName,singletonObject);
    }

    public Object createBeanInstance(String beanName, MyBeanDefinition mbd,Object... args) throws Exception {
        Constructor<?> constructorToUse= (Constructor<?>) mbd.resolvedConstructorOrFactoryMethod;
        if (constructorToUse == null) {
            Class<?> clazz = mbd.getBeanClass();
            constructorToUse = clazz.getDeclaredConstructor();
            mbd.resolvedConstructorOrFactoryMethod = constructorToUse;
        }
        Class<?>[] parameterTypes = constructorToUse.getParameterTypes();
        Object[] argsWithDefaultValues = new Object[args.length];
        for (int i = 0 ; i < args.length; i++) {
            if (args[i] == null) {
                Class<?> parameterType = parameterTypes[i];
                argsWithDefaultValues[i] = (parameterType.isPrimitive() ? DEFAULT_TYPE_VALUES.get(parameterType) : null);
            }
            else {
                argsWithDefaultValues[i] = args[i];
            }
        }
        return constructorToUse.newInstance(argsWithDefaultValues);
    }

    public Object resolveDependency(String beanName, Field field, Set<String> autowiredBeanNames) throws Throwable {

        Annotation[] fieldAnnotations=field.getAnnotations();
        Annotation ann=fieldAnnotations[0];
        if(ann.annotationType()!= Value.class){
            return null;
        }
        Method[] methods = ann.annotationType().getDeclaredMethods();
        Method me=methods[0];
        Object value=me.invoke(ann,null);
        if(value!=null){
            return value;
        }
        return getBean(beanName);

    }

    public void populateBean(String beanName, MyBeanDefinition mbd, Object bean) throws Throwable {
       if(bean==null){
           return;
       }
       for(MyBeanPostProcessor beanPostProcessor:getBeanPostProcessors()){
           beanPostProcessor.postProcessProperties(bean,beanName);
       }
    }

    public List<MyBeanPostProcessor> getBeanPostProcessors() {
        return this.beanPostProcessors;
    }

    public void registerBeanDefinition(String beanName, MyBeanDefinition beanDefinition){
        this.beanDefinitionMap.put(beanName,beanDefinition);
    }
}
