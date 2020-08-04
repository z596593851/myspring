package org.hxm.myspring;


import org.hxm.myspring.annotation.MyValue;
import org.hxm.myspring.function.MyObjectFactory;
import org.hxm.myspring.postprocessor.MyAutowiredProcessor;
import org.hxm.myspring.postprocessor.MyBeanFactoryPostProcessor;
import org.hxm.myspring.postprocessor.MyBeanPostProcessor;
import org.hxm.myspring.postprocessor.MyConfigurationClassPostProcessor;
import org.hxm.myspring.utils.MyClassUtil;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.core.ResolvableType;
import org.springframework.util.ClassUtils;
import org.springframework.util.ObjectUtils;
import org.springframework.util.ReflectionUtils;
import org.springframework.util.StringUtils;

import java.lang.annotation.Annotation;
import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class MyBeanFactory {

    private ClassLoader beanClassLoader= MyClassUtil.getDefaultClassLoader();

    private static final Map<Class<?>, Object> DEFAULT_TYPE_VALUES;

    private Map<String,MyBeanDefinition> beanDefinitionMap = new HashMap<>();

    private volatile List<String> beanDefinitionNames = new ArrayList<>();

    private Map<String, Object> singletonObjects = new ConcurrentHashMap<>();

    private List<MyBeanPostProcessor> beanPostProcessors=new ArrayList<>();

    private List<MyBeanFactoryPostProcessor> beanFactoryPostProcessors=new ArrayList<>();

    private ConcurrentMap<Class<?>, Method[]> factoryMethodCandidateCache = new ConcurrentHashMap<>();

    private Map<Class<?>, String[]> allBeanNamesByType = new ConcurrentHashMap<>();

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
        beanPostProcessors.add(new MyAutowiredProcessor(this));
        beanFactoryPostProcessors.add(new MyConfigurationClassPostProcessor());
    }

    public Object getBean(String beanName) throws Exception{
        MyBeanDefinition mbd = getBeanDefinition(beanName);
        if(mbd==null){
            throw new Exception(String.format("没有名为%s的beanDefinition",beanName));
        }
        try {
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
            //普通bean的实例化
            bean=createBeanInstance(beanName,mbd);
            Class<?> beanType=bean.getClass();
            mbd.resolvedTargetType = beanType;
            try {
                //属性注入
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

    public Object getSingleton(String beanName){
        return this.singletonObjects.get(beanName);
    }

    public void addSingleton(String beanName, Object singletonObject) {
        this.singletonObjects.put(beanName,singletonObject);
    }

    public Object createBeanInstance(String beanName, MyBeanDefinition mbd,Object... args) throws Exception {
        if (mbd.getFactoryMethodName() != null) {
            return instantiateUsingFactoryMethod(beanName,mbd,args);
        }
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

    public Object instantiateUsingFactoryMethod(String beanName, MyBeanDefinition mbd, Object[] explicitArgs){
        Object factoryBean=null;
        Object result=null;
        String factoryBeanName = mbd.getFactoryBeanName();

        try {
            factoryBean=getBean(factoryBeanName);
            Method factoryMethodToUse = (Method) mbd.resolvedConstructorOrFactoryMethod;
            List<Method> candidates = null;
            if (mbd.isFactoryMethodUnique) {
                if (factoryMethodToUse == null) {
                    factoryMethodToUse = mbd.getResolvedFactoryMethod();
                }
                if (factoryMethodToUse != null) {
                    candidates = Collections.singletonList(factoryMethodToUse);
                }
            }
            if (candidates.size() == 1){
                Method uniqueCandidate = candidates.get(0);
                if (uniqueCandidate.getParameterCount() == 0) {
                    mbd.factoryMethodToIntrospect = uniqueCandidate;
                    mbd.resolvedConstructorOrFactoryMethod = uniqueCandidate;
                    mbd.constructorArgumentsResolved = true;
                    mbd.resolvedConstructorArguments = new Object[0];
                    MyClassUtil.makeAccessible(uniqueCandidate);
                    result=uniqueCandidate.invoke(factoryBean,new Object[0]);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    public Object resolveDependency(String beanName, Field field, Set<String> autowiredBeanNames) throws Throwable {
        Annotation[] fieldAnnotations=field.getAnnotations();
        Annotation ann=fieldAnnotations[0];
        if(ann.annotationType()== MyValue.class) {
            Method[] methods = ann.annotationType().getDeclaredMethods();
            Method me = methods[0];
            Object value = me.invoke(ann, null);
            if (value != null) {
                return value;
            }
        }
        return doResolveDependency(beanName,field,autowiredBeanNames);
//        return getBean(beanName);
    }

    public Object doResolveDependency(String beanName, Field field,Set<String> autowiredBeanNames){
        Map<String, Object> candidates = null;
        try {
            Class<?> type = field.getType();
            //拿到type对应的beanDefination的beanName
            String[] candidateNames=getBeanNamesForType(type,true);
            candidates = new LinkedHashMap<>(candidateNames.length);
            for (String candidateName : candidateNames) {
                // <beanName,class>
                candidates.put(candidateName,getType(candidateName));
            }
            if(candidates.isEmpty()){
                throw new Exception("注解"+ObjectUtils.nullSafeToString(field.getAnnotations())+"没有对应的bean");
            }
            //beanName
            String autowiredBeanName=null;
            //beanClass
            Object instanceCandidate=null;
            if(candidates.size()>1){

            }else {
                Map.Entry<String, Object> entry = candidates.entrySet().iterator().next();
                autowiredBeanName = entry.getKey();
                instanceCandidate = entry.getValue();
            }
            if (autowiredBeanNames != null) {
                autowiredBeanNames.add(autowiredBeanName);
            }
            if(instanceCandidate instanceof Class){
                instanceCandidate=getBean(autowiredBeanName);
            }
            return instanceCandidate;
        } catch (Exception e) {
            e.printStackTrace();
        }

        return candidates;
    }

//    public Class<?> getType(String name){
//
//    }

    public void populateBean(String beanName, MyBeanDefinition mbd, Object bean) throws Throwable {
       if(bean==null){
           return;
       }
       //执行所有InstantiationAwareBeanPostProcessor的postProcessProperties方法
       for(MyBeanPostProcessor beanPostProcessor:getBeanPostProcessors()){
           beanPostProcessor.postProcessProperties(bean,beanName);
       }
    }

    public List<MyBeanPostProcessor> getBeanPostProcessors() {
        return this.beanPostProcessors;
    }

    public void registerBeanDefinition(String beanName, MyBeanDefinition beanDefinition){
        this.beanDefinitionMap.put(beanName,beanDefinition);
        this.beanDefinitionNames.add(beanName);
    }

    public MyBeanDefinition getBeanDefinition(String beanName){
        return this.beanDefinitionMap.get(beanName);
    }

    public void preInstantiateSingletons() throws Exception{
        List<String> beanNames = new ArrayList<>(this.beanDefinitionNames);
        for (String beanName : beanNames) {
            MyBeanDefinition mbd=this.beanDefinitionMap.get(beanName);
            if(mbd.isSingleton()){
                getBean(beanName);
            }
        }
    }

    public ClassLoader getBeanClassLoader() {
        return this.beanClassLoader;
    }

    public String[] getBeanNamesForType(Class<?> type,boolean allowEagerInit){
        if(!allowEagerInit){
            return doGetBeanNamesForType(type,allowEagerInit);
        }
        String[] resolvedBeanNames = this.allBeanNamesByType.get(type);
        if (resolvedBeanNames != null) {
            return resolvedBeanNames;
        }
        //找到和带匹配的type的class一致的beanName
        resolvedBeanNames = doGetBeanNamesForType(type,allowEagerInit);
        this.allBeanNamesByType.put(type, resolvedBeanNames);
        return resolvedBeanNames;
    }

    /**
     * 遍历所有beanDefinition，找到和type对应的bean的beanName
     * @param type 带匹配的class
     * @param allowEagerInit
     * @return
     */
    public String[] doGetBeanNamesForType(Class<?> type, boolean allowEagerInit) {
        List<String> result = new ArrayList<>();
        try {
            for(String beanName:beanDefinitionNames){
                MyBeanDefinition beanDefinition=getBeanDefinition(beanName);
                boolean isFactoryBean=isFactoryBean(beanName, beanDefinition);
                boolean matchFound = false;
                if(!isFactoryBean){
                    matchFound = isTypeMatch(beanName, type);
                }
                if (matchFound) {
                    result.add(beanName);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return StringUtils.toStringArray(result);
//        return result.isEmpty()?new String[]{}: (String[]) result.toArray();
    }
    protected boolean isTypeMatch(String name,Class<?> type){
        Object beanInstance = getSingleton(name);
        if(beanInstance!=null){
            return type.isAssignableFrom(beanInstance.getClass());
        }
        MyBeanDefinition mbd=getBeanDefinition(name);
        Class<?> predictedType=predictBeanType(name,mbd,type);
        if (predictedType == null) {
            return false;
        }
        //@Bean标注的方法返回的bean
        ResolvableType beanType = null;
        ResolvableType definedType=mbd.factoryMethodReturnType;
        if (definedType != null && definedType.resolve() == predictedType) {
            beanType = definedType;
        }
        if (beanType != null) {
            return beanType.isAssignableFrom(type);
        }
        //普通bean
        return type.isAssignableFrom(predictedType);
    }

    public boolean isFactoryBean(String beanName,MyBeanDefinition mbd){
        Boolean result = mbd.isFactoryBean;
        if (result == null) {
            Class<?> beanType = predictBeanType(beanName, mbd, FactoryBean.class);
            result = (beanType != null && FactoryBean.class.isAssignableFrom(beanType));
            mbd.isFactoryBean = result;
        }
        return result;
    }

    protected Class<?> predictBeanType(String beanName, MyBeanDefinition mbd,Class<?>... typesToMatch){
        Class<?> targetType = determineTargetType(beanName, mbd, typesToMatch);
        return targetType;

    }

    protected Class<?> determineTargetType(String beanName, MyBeanDefinition mbd, Class<?>... typesToMatch) {
        Class<?> targetType = mbd.getTargetType();
        if(targetType==null){
            targetType=(mbd.getFactoryMethodName()!=null?
                    getTypeForFactoryMethod(beanName,mbd,typesToMatch):
                    resolveBeanClass(beanName,mbd,typesToMatch));
            mbd.resolvedTargetType = targetType;
        }
        return targetType;
    }

    protected Class<?> resolveBeanClass(String beanName, MyBeanDefinition mbd, Class<?>... typesToMatch){
        try {
            if(mbd.hasBeanClass()){
                return mbd.getBeanClass();
            }else {
                ClassLoader beanClassLoader=getBeanClassLoader();
                return mbd.resolveBeanClass(beanClassLoader);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    protected Class<?> getTypeForFactoryMethod(String beanName, MyBeanDefinition mbd, Class<?>... typesToMatch){
        ResolvableType cachedReturnType = mbd.factoryMethodReturnType;
        if (cachedReturnType != null) {
            return cachedReturnType.resolve();
        }

        Class<?> commonType = null;
        Method uniqueCandidate = mbd.factoryMethodToIntrospect;
        if(uniqueCandidate==null){
            String factoryBeanName = mbd.getFactoryBeanName();
            Class<?> factoryClass=getType(factoryBeanName);
//                final List<Method> methods = new ArrayList<>(32);
            Method[] candidates = this.factoryMethodCandidateCache.computeIfAbsent(factoryClass,
                    clazz ->{
                        Method[] declaredMethods = clazz.getDeclaredMethods();
                        for(Method method:declaredMethods){
                            if(method.isBridge()||method.isSynthetic()){
                                continue;
                            }
                        }
                        return declaredMethods;
                    });
            for(Method candidate : candidates){
                if(mbd.isFactoryMethod(candidate)){
                    if (candidate.getTypeParameters().length > 0) {

                    }else {
                        uniqueCandidate= (commonType == null ? candidate : null);
                        commonType = ClassUtils.determineCommonAncestor(candidate.getReturnType(), commonType);
                        if (commonType == null) {
                            return null;
                        }
                    }
                }
            }
            mbd.factoryMethodToIntrospect = uniqueCandidate;
            if (commonType == null) {
                return null;
            }
        }
        cachedReturnType = (uniqueCandidate != null ?
                ResolvableType.forMethodReturnType(uniqueCandidate) : ResolvableType.forClass(commonType));
        mbd.factoryMethodReturnType = cachedReturnType;
        return cachedReturnType.resolve();
    }

    public List<String> getBeanDefinitionNames(){
        return this.beanDefinitionNames;
    }

    public List<MyBeanFactoryPostProcessor>getBeanFactoryPostProcessor(){
        return this.beanFactoryPostProcessors;
    }

//    public void instantiateUsingFactoryMethod(String beanName, MyBeanDefinition mbd){
//        Object factoryBean;
//        Class<?> factoryClass;
//        String factoryBeanName=mbd.getFactoryBeanName();
//        if(factoryBeanName!=null){
//            try {
//                factoryBean=getBean(factoryBeanName);
//                factoryClass=factoryBean.getClass();
//            } catch (Exception e) {
//                e.printStackTrace();
//            }
//        }else{
//            factoryBean=null;
//            factoryClass=mbd.getBeanClass();
//        }
//    }

    public Class<?> getType(String name){
        Object beanInstance=getSingleton(name);
        if(beanInstance!=null){
            return beanInstance.getClass();
        }
        //如果为空，说明不是单例，或者还未实例化，则从beanDefination中获取
        MyBeanDefinition mbd=getBeanDefinition(name);
        Class<?> beanClass = predictBeanType(name, mbd);
        return beanClass;
    }
}
