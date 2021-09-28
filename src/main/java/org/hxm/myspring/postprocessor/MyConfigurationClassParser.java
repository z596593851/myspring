package org.hxm.myspring.postprocessor;

import org.hxm.myspring.annotation.*;
import org.hxm.myspring.asm.*;
import org.hxm.myspring.factory.*;
import org.hxm.myspring.utils.MyAnnotationConfigUtils;
import org.hxm.myspring.utils.MyClassUtil;
import org.springframework.core.io.ClassPathResource;
import org.springframework.lang.Nullable;
import org.springframework.util.CollectionUtils;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.rmi.RemoteException;
import java.util.*;
import java.util.function.Predicate;

/**
 * @author xiaoming
 */
public class MyConfigurationClassParser {

    private final MyBeanDefinitionRegistry registry;
    private static final Predicate<String> DEFAULT_EXCLUSION_FILTER = className ->
            (className.startsWith("java.lang.annotation.") || className.startsWith("org.hxm.myspring.stereotype."));
    private final MySourceClass objectSourceClass = new MySourceClass(Object.class);
    Map<MyConfigurationClass, MyConfigurationClass> configurationClasses=new HashMap<>();
    private final MyDeferredImportSelectorHandler deferredImportSelectorHandler = new MyDeferredImportSelectorHandler();
    private final ImportStack importStack = new ImportStack();

    public MyConfigurationClassParser(MyBeanDefinitionRegistry registry){
        this.registry=registry;
    }

    public void parse(Set<MyBeanDefinitionHolder> configCandidates){
        for(MyBeanDefinitionHolder beanDefinitionHolder:configCandidates){
            //处理 @Component 和 @Configuration
            processConfigurationClass(new MyConfigurationClass(beanDefinitionHolder.getBeanDefinition().getMetadata(),beanDefinitionHolder.getBeanName()),
                    DEFAULT_EXCLUSION_FILTER);
        }
        //处理 deferredImportSelectorHandler 中的 ImportSelector ,其通过spi引入autoConfig类
        this.deferredImportSelectorHandler.process();
    }

    public void processConfigurationClass(MyConfigurationClass configClass, Predicate<String> filter){
        try {
            MyConfigurationClass existingClass = this.configurationClasses.get(configClass);
            if (existingClass != null) {
                if (configClass.isImported()) {
                    if (existingClass.isImported()) {
                        existingClass.mergeImportedBy(configClass);
                    }
                    // Otherwise ignore new imported config class; existing non-imported class overrides it.
                    return;
                } else {
                    // Explicit bean definition found, probably replacing an import.
                    // Let's remove the old one and go with the new one.
                    this.configurationClasses.remove(configClass);
                }
            }
            MySourceClass sourceClass=asSourceClass(configClass,DEFAULT_EXCLUSION_FILTER);
            do {
                sourceClass = doProcessConfigurationClass(configClass, sourceClass, filter);
            }
            while (sourceClass != null);
            this.configurationClasses.put(configClass,configClass);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private MySourceClass doProcessConfigurationClass(MyConfigurationClass configClass, MySourceClass sourceClass,  Predicate<String> filter) throws IOException {
        //是否标记了@ComponentScan
        MyAnnotationAttributes componentScan = MyAnnotationConfigUtils.attributesFor(sourceClass.getMetadata(), MyComponentScan.class);
        if(componentScan!=null){
            Set<String> basePackages = new LinkedHashSet<>();
            String[] basePackagesArray=componentScan.getStringArray("basePackages");
            for(String pkg : basePackagesArray){
                basePackages.add(pkg);
            }
            if (basePackages.isEmpty()) {
                basePackages.add(MyClassUtil.getPackageName(sourceClass.getMetadata().getClassName()));
            }
            MyScanner scanner=new MyScanner(this.registry);
            //将当前类设置为filter 防止循环扫描
            scanner.addExcludeFilter(new MyAbstractTypeFilter() {
                @Override
                protected boolean matchClassName(String className) {
                    return sourceClass.getMetadata().getClassName().equals(className);
                }
            });
            //扫描application所在根目录下的 @Compoment 和 @Configuration
            Set<MyBeanDefinitionHolder> scannedBeanDefinitions = scanner.scan(StringUtils.toStringArray(basePackages));
            for(MyBeanDefinitionHolder holder:scannedBeanDefinitions){
                //递归调用
                processConfigurationClass(new MyConfigurationClass(new MySimpleMetadataReader(new ClassPathResource(MyClassUtil.convertClassNameToResourcePath(holder.getBeanDefinition().getBeanClassName())+".class",
                        MyClassUtil.getDefaultClassLoader())),holder.getBeanName()),DEFAULT_EXCLUSION_FILTER);
            }

        }
        //处理 @Import 导入的类，可以是 ImportSelector 也可以是普通类
        processImports(configClass, sourceClass,getImports(sourceClass),DEFAULT_EXCLUSION_FILTER,true);
        //处理 @Configuration 中的 @Bean
        Set<MyMethodMetadata> beanMethods=sourceClass.getMetadata().getAnnotatedMethods(MyBean.class.getName());
        for(MyMethodMetadata methodMetadata:beanMethods){
            configClass.addBeanMethod(new MyBeanMethod(methodMetadata,configClass));
        }
        return null;
    }

    private void processImports(MyConfigurationClass configClass, MySourceClass currentSourceClass,
                                Collection<MySourceClass> importCandidates, Predicate<String> exclusionFilter, boolean checkForCircularImports){
        //没有标记@Importx
        if (importCandidates.isEmpty()) {
            return;
        }
        this.importStack.push(configClass);
        try {
            for (MySourceClass candidate : importCandidates) {
                //如果 @Import 导入的是一个 MyImportSelector，那么实例化它，然后加入到
                // deferredImportSelectorHandler 中等待后续的处理
                if(candidate.isAssignable(MyImportSelector.class)){
                    Class<?> candidateClass = candidate.loadClass();
                    MyImportSelector selector=MyClassUtil.instantiateClass(candidateClass,MyImportSelector.class, (MyBeanFactory) registry);
                    if(selector instanceof MyDeferredImportSelector){
                        this.deferredImportSelectorHandler.handle(configClass,(MyDeferredImportSelector)selector);
                    }
                }else{
                    //否则直接将 @Import 导入的类交给 processConfigurationClass 解析
                    this.importStack.registerImport(currentSourceClass.getMetadata(), candidate.getMetadata().getClassName());
                    processConfigurationClass(candidate.asConfigClass(configClass), exclusionFilter);

                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }finally {
            this.importStack.pop();
        }
    }

    private Set<MySourceClass> getImports(MySourceClass sourceClass) throws IOException {
        Set<MySourceClass> imports = new LinkedHashSet<>();
        Set<MySourceClass> visited = new LinkedHashSet<>();
        collectImports(sourceClass, imports, visited);
        return imports;
    }

    private void collectImports(MySourceClass sourceClass, Set<MySourceClass> imports, Set<MySourceClass> visited)
            throws IOException {

        if (visited.add(sourceClass)) {
            for (MySourceClass annotation : sourceClass.getAnnotations()) {
                String annName = annotation.getMetadata().getClassName();
                if (!annName.equals(MyImport.class.getName())) {
                    collectImports(annotation, imports, visited);
                }
            }
            imports.addAll(sourceClass.getAnnotationAttributes(MyImport.class.getName(), "value"));
        }
    }

    public Set<MyConfigurationClass> getConfigurationClasses(){
        return this.configurationClasses.keySet();
    }

    private class MySourceClass{
        private final Object source;
        private final MyAnnotationMetadata metadata;

        public MySourceClass(Object source) {
            this.source = source;
            if (source instanceof Class) {
                this.metadata = MyAnnotationMetadata.introspect((Class<?>) source);
            }
            else {
                this.metadata = ((MySimpleMetadataReader) source).getAnnotationMetadata();
            }
        }

        public MyConfigurationClass asConfigClass(MyConfigurationClass importedBy) {
            if (this.source instanceof Class) {
                return new MyConfigurationClass((Class<?>) this.source, importedBy);
            }
            return new MyConfigurationClass((MySimpleMetadataReader) this.source, importedBy);
        }

        public final MyAnnotationMetadata getMetadata() {
            return this.metadata;
        }

        public Set<MySourceClass> getAnnotations() {
            Set<MySourceClass> result = new LinkedHashSet<>();
            if(this.source instanceof Class){
                Class<?> sourceClass = (Class<?>) this.source;
                for (Annotation ann : sourceClass.getDeclaredAnnotations()) {
                    Class<?> annType = ann.annotationType();
                    if (!annType.getName().startsWith("java")) {
                        result.add(asSourceClass(annType, DEFAULT_EXCLUSION_FILTER));
                    }
                }
            }else {
                for (String className : this.metadata.getAnnotationTypes()) {
                    if (!className.startsWith("java")) {
                        try {
                            result.add(getRelated(className));
                        }
                        catch (Throwable ex) {
                            // An annotation not present on the classpath is being ignored
                            // by the JVM's class loading -> ignore here as well.
                        }
                    }
                }
            }

            return result;
        }

        public Collection<MySourceClass> getAnnotationAttributes(String annType, String attribute) throws IOException {
            Map<String, Object> annotationAttributes = this.metadata.getAnnotationAttributes(annType, true);
            if (annotationAttributes == null || !annotationAttributes.containsKey(attribute)) {
                return Collections.emptySet();
            }
            Set<MySourceClass> result = new LinkedHashSet<>();
            Class<?>[] classes= (Class<?>[])annotationAttributes.get(attribute);
            for (Class<?> clazz:classes){
                String className=clazz.getName();
                result.add(getRelated(className));
            }
            return result;
        }

        private MySourceClass getRelated(String className) throws IOException  {
            if(this.source instanceof Class){
                try {
                    Class<?> clazz = MyClassUtil.forName(className, ((Class<?>) this.source).getClassLoader());
                    return asSourceClass(clazz, DEFAULT_EXCLUSION_FILTER);
                } catch (Exception e) {
                    e.printStackTrace();
                }

            }
            return asSourceClass(className, DEFAULT_EXCLUSION_FILTER);
        }

        public boolean isAssignable(Class<?> clazz) throws IOException {
            if(this.source instanceof Class){
                return clazz.isAssignableFrom((Class<?>) this.source);
            }
            return new MyAssignableTypeFilter(clazz).match((MySimpleMetadataReader) this.source);

        }

        public Class<?> loadClass(){
            return (Class<?>) this.source;
        }

        @Override
        public boolean equals(@Nullable Object other) {
            return (this == other || (other instanceof MySourceClass &&
                    this.metadata.getClassName().equals(((MySourceClass) other).metadata.getClassName())));
        }

        @Override
        public int hashCode() {
            return this.metadata.getClassName().hashCode();
        }

        @Override
        public String toString() {
            return this.metadata.getClassName();
        }
    }

    public MySourceClass asSourceClass(Class<?> classType, Predicate<String> filter){
        if (classType == null || filter.test(classType.getName())) {
            return this.objectSourceClass;
        }
        return new MySourceClass(classType);
    }

    private MySourceClass asSourceClass(MyConfigurationClass configurationClass, Predicate<String> filter) throws IOException {
        MyAnnotationMetadata metadata = configurationClass.getMetadata();
        if (metadata instanceof MyStandardAnnotationMetadata) {
            return asSourceClass(((MyStandardAnnotationMetadata) metadata).getIntrospectedClass(), filter);
        }
        return asSourceClass(metadata.getClassName(), filter);
    }

    MySourceClass asSourceClass(String className, Predicate<String> filter) throws IOException {
        if (className == null || filter.test(className)) {
            return this.objectSourceClass;
        }
        if (className.startsWith("java")) {
            // Never use ASM for core java types
            try {
                return new MySourceClass(MyClassUtil.forName(className, MyClassUtil.getDefaultClassLoader()));
            }
            catch (Exception ex) {
                throw new RemoteException("Failed to load class [" + className + "]", ex);
            }
        }
        return new MySourceClass(new MySimpleMetadataReader(new ClassPathResource(
                MyClassUtil.convertClassNameToResourcePath(className)+".class",
                MyClassUtil.getDefaultClassLoader())));
    }

    private static class MyDeferredImportSelectorHolder {

        private final MyConfigurationClass configurationClass;

        private final MyDeferredImportSelector importSelector;

        public MyDeferredImportSelectorHolder(MyConfigurationClass configClass, MyDeferredImportSelector selector) {
            this.configurationClass = configClass;
            this.importSelector = selector;
        }

        public MyConfigurationClass getConfigurationClass() {
            return this.configurationClass;
        }

        public MyDeferredImportSelector getImportSelector() {
            return this.importSelector;
        }
    }

    private class MyDeferredImportSelectorHandler {

        private List<MyDeferredImportSelectorHolder> deferredImportSelectors = new ArrayList<>();

        public void handle(MyConfigurationClass configClass, MyDeferredImportSelector importSelector) {
            MyDeferredImportSelectorHolder holder = new MyDeferredImportSelectorHolder(configClass, importSelector);
            if (this.deferredImportSelectors == null) {
                MyDeferredImportSelectorGroupingHandler handler = new MyDeferredImportSelectorGroupingHandler();
                handler.register(holder);
                handler.processGroupImports();
            }
            else {
                this.deferredImportSelectors.add(holder);
            }
        }

        public void process() {
            List<MyDeferredImportSelectorHolder> deferredImports = this.deferredImportSelectors;
            this.deferredImportSelectors = null;
            try {
                if (deferredImports != null) {
                    MyDeferredImportSelectorGroupingHandler handler = new MyDeferredImportSelectorGroupingHandler();
                    deferredImports.forEach(handler::register);
                    handler.processGroupImports();
                }
            }
            finally {
                this.deferredImportSelectors = new ArrayList<>();
            }
        }
    }

    private class MyDeferredImportSelectorGroupingHandler {

        private final Map<Object, MyDeferredImportSelectorGrouping> groupings = new LinkedHashMap<>();

        private final Map<MyAnnotationMetadata, MyConfigurationClass> configurationClasses = new HashMap<>();

        public void register(MyDeferredImportSelectorHolder deferredImport) {
            Class<? extends MyDeferredImportSelector.Group> group = deferredImport.getImportSelector().getImportGroup();
            MyDeferredImportSelectorGrouping grouping = this.groupings.computeIfAbsent(
                    (group != null ? group : deferredImport),
                    key -> new MyDeferredImportSelectorGrouping(createGroup(group)));
            grouping.add(deferredImport);
            this.configurationClasses.put(deferredImport.getConfigurationClass().getMetadata(),
                    deferredImport.getConfigurationClass());
        }

        public void processGroupImports() {
            for (MyDeferredImportSelectorGrouping grouping : this.groupings.values()) {
                Predicate<String> exclusionFilter = grouping.getCandidateFilter();
                grouping.getImports().forEach(entry -> {
                    MyConfigurationClass configurationClass = this.configurationClasses.get(entry.getMetadata());
                    try {
                        processImports(configurationClass, asSourceClass(configurationClass, exclusionFilter),
                                Collections.singleton(asSourceClass(entry.getImportClassName(), exclusionFilter)),
                                exclusionFilter, false);
                    }catch (Throwable ex) {
                        ex.printStackTrace();
                    }
                });
            }
        }

        private MyDeferredImportSelector.Group createGroup(Class<? extends MyDeferredImportSelector.Group> type) {
//            Class<? extends MyDeferredImportSelector.Group> effectiveType = (type != null ? type : MyDefaultDeferredImportSelectorGroup.class);
            return MyClassUtil.instantiateClass(type, MyDeferredImportSelector.Group.class, (MyBeanFactory) MyConfigurationClassParser.this.registry);
        }

    }

//    private static class MyDefaultDeferredImportSelectorGroup implements MyDeferredImportSelector.Group {
//
//        private final List<Entry> imports = new ArrayList<>();
//
//        @Override
//        public void process(MyAnnotationMetadata metadata, MyDeferredImportSelector selector) {
//            for (String importClassName : selector.selectImports(metadata)) {
//                this.imports.add(new Entry(metadata, importClassName));
//            }
//        }
//
//        @Override
//        public Iterable<Entry> selectImports() {
//            return this.imports;
//        }
//    }

    private static class MyDeferredImportSelectorGrouping {

        private final MyDeferredImportSelector.Group group;

        private final List<MyDeferredImportSelectorHolder> deferredImports = new ArrayList<>();

        MyDeferredImportSelectorGrouping(MyDeferredImportSelector.Group group) {
            this.group = group;
        }

        public void add(MyDeferredImportSelectorHolder deferredImport) {
            this.deferredImports.add(deferredImport);
        }

        /**
         * Return the imports defined by the group.
         * @return each import with its associated configuration class
         */
        public Iterable<MyDeferredImportSelector.Group.Entry> getImports() {
            for (MyDeferredImportSelectorHolder deferredImport : this.deferredImports) {
                this.group.process(deferredImport.getConfigurationClass().getMetadata(),
                        deferredImport.getImportSelector());
            }
            return this.group.selectImports();
        }

        public Predicate<String> getCandidateFilter() {
            Predicate<String> mergedFilter = DEFAULT_EXCLUSION_FILTER;
            for (MyDeferredImportSelectorHolder deferredImport : this.deferredImports) {
                Predicate<String> selectorFilter = deferredImport.getImportSelector().getExclusionFilter();
                if (selectorFilter != null) {
                    mergedFilter = mergedFilter.or(selectorFilter);
                }
            }
            return mergedFilter;
        }
    }

    private static class ImportStack extends ArrayDeque<MyConfigurationClass>{

        private final MultiValueMap<String, MyAnnotationMetadata> imports = new LinkedMultiValueMap<>();

        public void registerImport(MyAnnotationMetadata importingClass, String importedClass) {
            this.imports.add(importedClass, importingClass);
        }

        public MyAnnotationMetadata getImportingClassFor(String importedClass) {
            return CollectionUtils.lastElement(this.imports.get(importedClass));
        }

        public void removeImportingClass(String importingClass) {
            for (List<MyAnnotationMetadata> list : this.imports.values()) {
                for (Iterator<MyAnnotationMetadata> iterator = list.iterator(); iterator.hasNext();) {
                    if (iterator.next().getClassName().equals(importingClass)) {
                        iterator.remove();
                        break;
                    }
                }
            }
        }

        /**
         * Given a stack containing (in order)
         * <ul>
         * <li>com.acme.Foo</li>
         * <li>com.acme.Bar</li>
         * <li>com.acme.Baz</li>
         * </ul>
         * return "[Foo->Bar->Baz]".
         */
        @Override
        public String toString() {
            StringJoiner joiner = new StringJoiner("->", "[", "]");
            for (MyConfigurationClass configurationClass : this) {
                joiner.add(configurationClass.getSimpleName());
            }
            return joiner.toString();
        }
    }
}
