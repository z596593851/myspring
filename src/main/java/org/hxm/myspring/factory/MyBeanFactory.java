package org.hxm.myspring.factory;


import org.hxm.myspring.annotation.MyValue;
import org.hxm.myspring.function.MyObjectFactory;
import org.hxm.myspring.postprocessor.MyAutowiredProcessor;
import org.hxm.myspring.postprocessor.MyBeanFactoryPostProcessor;
import org.hxm.myspring.postprocessor.MyBeanPostProcessor;
import org.hxm.myspring.postprocessor.MyConfigurationClassPostProcessor;
import org.hxm.myspring.utils.MyClassUtil;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.core.ResolvableType;
import org.springframework.util.*;

import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class MyBeanFactory implements MyBeanDefinitionRegistry {

    private ClassLoader beanClassLoader= MyClassUtil.getDefaultClassLoader();

    private static final Map<Class<?>, Object> DEFAULT_TYPE_VALUES;

    private Map<String,MyBeanDefinition> beanDefinitionMap = new HashMap<>();

    private volatile List<String> beanDefinitionNames = new ArrayList<>();

    //手动调用registerSingleton注册的bean的name
    private volatile Set<String> manualSingletonNames = new LinkedHashSet<>(16);

    private Map<String, Object> singletonObjects = new ConcurrentHashMap<>();

    private List<MyBeanPostProcessor> beanPostProcessors=new ArrayList<>();

    private List<MyBeanFactoryPostProcessor> beanFactoryPostProcessors=new ArrayList<>();

    private ConcurrentMap<Class<?>, Method[]> factoryMethodCandidateCache = new ConcurrentHashMap<>();

    private Map<Class<?>, String[]> allBeanNamesByType = new ConcurrentHashMap<>();

    private boolean allowCircularReferences = true;

    //正在被创建的bean
    private final Set<String> singletonsCurrentlyInCreation =
            Collections.newSetFromMap(new ConcurrentHashMap<>(16));

    //三级缓存
    private final Map<String, Object> singletonFactories = new HashMap<>(16);

    //二级缓存
    private final Map<String, Object> earlySingletonObjects = new HashMap<>(16);

    /** Set of registered singletons, containing the bean names in registration order. */
    private final Set<String> registeredSingletons = new LinkedHashSet<>(256);

    static {
        Map<Class<?>, Object> values = new HashMap<>();
        values.put(boolean.class, false);
        values.put(byte.class, (byte) 0);
        values.put(short.class, (short) 0);
        values.put(int.class, 0);
        values.put(long.class, (long) 0);
        DEFAULT_TYPE_VALUES = Collections.unmodifiableMap(values);
    }

    public boolean containsBean(String name) {
        return containsSingleton(name) || containsBeanDefinition(name);
    }

    public boolean containsBeanDefinition(String beanName) {
        return this.beanDefinitionMap.containsKey(beanName);
    }

    public boolean containsSingleton(String beanName) {
        return this.singletonObjects.containsKey(beanName);
    }

    public void registerSingleton(String beanName, Object singletonObject){
        Object oldObject = this.singletonObjects.get(beanName);
        if (oldObject != null) {
            throw new IllegalStateException("Could not register object [" + singletonObject +
                    "] under bean name '" + beanName + "': there is already object [" + oldObject + "] bound");
        }
        addSingleton(beanName, singletonObject);
        if(!this.beanDefinitionMap.containsKey(beanName)){
            Set<String> updatedSingletons = new LinkedHashSet<>(this.manualSingletonNames);
            updatedSingletons.add(beanName);
            this.manualSingletonNames=updatedSingletons;
        }
    }


    public MyBeanFactory(){
        beanPostProcessors.add(new MyAutowiredProcessor(this));
        beanFactoryPostProcessors.add(new MyConfigurationClassPostProcessor());
    }

    public Object getBean(String beanName) throws Exception{
        Object sharedInstance = getSingleton(beanName);
        if(sharedInstance==null){
            MyBeanDefinition mbd = getBeanDefinition(beanName);
            if(mbd==null){
                throw new Exception(String.format("没有名为%s的beanDefinition",beanName));
            }
            sharedInstance=getSingleton(beanName,()->{
                try {
                    return createBean(beanName,mbd);
                } catch (Exception e) {
                    throw e;
                }
            });
        }
        return sharedInstance;
    }


    public Object createBean(String beanName,MyBeanDefinition mbd) throws Exception{
        Object bean = null;
        //普通bean的实例化
        bean=createBeanInstance(beanName,mbd);
        Class<?> beanType=bean.getClass();
        mbd.resolvedTargetType = beanType;
        //循环依赖 2-将当前正在创建的Bean保存到三级缓存中，并从二级缓存中移除（由于本来二级缓存中没有，故可以只认定为放入三级缓存）
        boolean earlySingletonExposure = (mbd.isSingleton() && this.allowCircularReferences &&
                isSingletonCurrentlyInCreation(beanName));
        if (earlySingletonExposure) {
            addSingletonFactory(beanName,bean);
        }
        try {
            //属性注入
            populateBean(beanName,mbd,bean);
        }catch (Throwable ex){
            ex.printStackTrace();
        }

        return bean;
    }

    public Object getSingleton(String beanName, MyObjectFactory<?> objectFactory){
        Object singletonObject = this.singletonObjects.get(beanName);
        if(singletonObject==null){
            //循环依赖 1-放入正在创建的bean的集合
            beforeSingletonCreation(beanName);
            boolean newSingleton = false;
            try {
                singletonObject=objectFactory.getObject();
                newSingleton = true;
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                //循环依赖 4-从"正在创建的bean中移除"
                afterSingletonCreation(beanName);
            }
            if(newSingleton){
                //循环依赖 5-将创建的这个Bean放入一级缓存，从二级缓存和三级缓存中移除
                addSingleton(beanName,singletonObject);
            }
        }
        return singletonObject;
    }

    public Object getSingleton(String beanName){
        Object singletonObject=this.singletonObjects.get(beanName);
        // 循环依赖 3-当一个对象被第二次获取时，处于正在被创建状态，进入if，
        // 放入二级缓存，从三级缓存移除，
        // 返回一个正在创建但还没依赖注入的对象
        if (singletonObject == null && isSingletonCurrentlyInCreation(beanName)) {
            singletonObject = this.earlySingletonObjects.get(beanName);
            if (singletonObject == null) {
                singletonObject = this.singletonFactories.get(beanName);
                if (singletonObject != null) {
                    this.earlySingletonObjects.put(beanName, singletonObject);
                    this.singletonFactories.remove(beanName);
                }
            }
        }
        return singletonObject;
    }

    public void addSingleton(String beanName, Object singletonObject) {
        this.singletonObjects.put(beanName, singletonObject);
        this.singletonFactories.remove(beanName);
        this.earlySingletonObjects.remove(beanName);
        this.registeredSingletons.add(beanName);
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
        //解析@MyValue
        if(ann.annotationType()== MyValue.class) {
            Method[] methods = ann.annotationType().getDeclaredMethods();
            Method me = methods[0];
            //@MyValue中设置的值
            Object value = me.invoke(ann, null);
            if (value != null) {
                return value;
            }
        }
        //解析@MyAutowired
        return doResolveDependency(beanName,field,autowiredBeanNames);
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

    /**
     * bean的属性注入,解析 @MyAutowired和@MyValue
     */
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

    @Override
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
        //找到和待匹配的type的class一致的beanName
        resolvedBeanNames = doGetBeanNamesForType(type,allowEagerInit);
        this.allBeanNamesByType.put(type, resolvedBeanNames);
        return resolvedBeanNames;
    }

    /**
     * 遍历所有beanDefinition，找到和type对应的bean的beanName,在注入@MyAutowired时用。
     * @param type 待匹配的class
     * @param allowEagerInit
     * @return
     */
    public String[] doGetBeanNamesForType(Class<?> type, boolean allowEagerInit) {
        List<String> result = new ArrayList<>();
        try {
            for(String beanName:this.beanDefinitionNames){
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
            for(String beanName:this.manualSingletonNames){
                if (isTypeMatch(beanName, type)) {
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
            //设置并查询BeanDefinition的resolvedTargetType
            Class<?> beanType = predictBeanType(beanName, mbd, FactoryBean.class);
            result = (beanType != null && FactoryBean.class.isAssignableFrom(beanType));
            mbd.isFactoryBean = result;
        }
        return result;
    }

    /**
     * 设置并查询BeanDefinition的resolvedTargetType
     */
    protected Class<?> predictBeanType(String beanName, MyBeanDefinition mbd,Class<?>... typesToMatch){
        Class<?> targetType = determineTargetType(beanName, mbd, typesToMatch);
        return targetType;

    }

    protected Class<?> determineTargetType(String beanName, MyBeanDefinition mbd, Class<?>... typesToMatch) {
        Class<?> targetType = mbd.getTargetType();
        if(targetType==null){
            //如果是普通bean，则调用beanDefinition的mbd.resolveBeanClass设置beanClass
            //如果是@Bean标注的方法产生的bean，则返回@Bean标注的方法的返回值对应的class
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
            //如果key不存在，就将key作为参数传入后面的函数中，函数的返回值作为value存入map
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
            //拿到factory类的所有方法，遍历找到当前@Bean标注的beanDefinition中存的factoryMethodName对应的那个方法
            for(Method candidate : candidates){
                if(mbd.isFactoryMethod(candidate)){
                    if (candidate.getTypeParameters().length > 0) {

                    }else {
                        //方法
                        uniqueCandidate= (commonType == null ? candidate : null);
                        //class
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

    /**
     * 是否是正在创建的bean
     * @param beanName
     * @return
     */
    public boolean isSingletonCurrentlyInCreation(String beanName) {
        return this.singletonsCurrentlyInCreation.contains(beanName);
    }

    protected void beforeSingletonCreation(String beanName) {
        this.singletonsCurrentlyInCreation.add(beanName);
    }
    protected void addSingletonFactory(String beanName, Object singletonBean) {
        Assert.notNull(singletonBean, "Singleton factory must not be null");
        synchronized (this.singletonObjects) {
            if (!this.singletonObjects.containsKey(beanName)) {
                //三级缓存
                this.singletonFactories.put(beanName, singletonBean);
                //二级缓存
                this.earlySingletonObjects.remove(beanName);
                this.registeredSingletons.add(beanName);
            }
        }
    }
    protected Object getEarlyBeanReference(String beanName,  MyBeanDefinition beanDefinition, Object bean) {
        return bean;
    }

    protected void afterSingletonCreation(String beanName) {
        this.singletonsCurrentlyInCreation.remove(beanName);
    }
}

