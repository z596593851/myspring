package org.hxm.myspring.config;

import org.hxm.myspring.annotation.MyDeferredImportSelector;
import org.hxm.myspring.annotation.MyEnableAutoConfiguration;
import org.hxm.myspring.asm.MyAnnotationAttributes;
import org.hxm.myspring.asm.MyAnnotationMetadata;
import org.hxm.myspring.spi.SpringSPILoader;
import org.hxm.myspring.utils.MyClassUtil;
import org.springframework.util.Assert;
import org.springframework.util.ClassUtils;
import org.springframework.util.StringUtils;

import java.util.*;

public class MyAutoConfigurationImportSelector implements MyDeferredImportSelector {


    @Override
    public String[] selectImports(MyAnnotationMetadata annotationMetadata) {
        MyAutoConfigurationEntry autoConfigurationEntry = getAutoConfigurationEntry(annotationMetadata);
        return StringUtils.toStringArray(autoConfigurationEntry.getConfigurations());
    }

    protected MyAutoConfigurationEntry getAutoConfigurationEntry(MyAnnotationMetadata annotationMetadata) {
        MyAnnotationAttributes attributes = getAttributes(annotationMetadata);
        List<String> configurations = getCandidateConfigurations(annotationMetadata, attributes);
        configurations = removeDuplicates(configurations);
        return new MyAutoConfigurationEntry(configurations);
    }

    protected MyAnnotationAttributes getAttributes(MyAnnotationMetadata metadata) {
        String name = MyEnableAutoConfiguration.class.getName();
        MyAnnotationAttributes attributes = MyAnnotationAttributes.fromMap(metadata.getAnnotationAttributes(name, true));
        Assert.notNull(attributes, () -> "No auto-configuration attributes found. Is " + metadata.getClassName()
                + " annotated with " + ClassUtils.getShortName(name) + "?");
        return attributes;
    }

    protected List<String> getCandidateConfigurations(MyAnnotationMetadata metadata, MyAnnotationAttributes attributes) {
        List<String> configurations = SpringSPILoader.loadFactoryNames(getSpringFactoriesLoaderFactoryClass(), MyClassUtil.getDefaultClassLoader());
        Assert.notEmpty(configurations, "No auto configuration classes found in META-INF/spring.factories. If you "
                + "are using a custom packaging, make sure that file is correct.");
        return configurations;
    }

    protected Class<?> getSpringFactoriesLoaderFactoryClass() {
        return MyEnableAutoConfiguration.class;
    }

    protected final <T> List<T> removeDuplicates(List<T> list) {
        return new ArrayList<>(new LinkedHashSet<>(list));
    }

    private static class MyAutoConfigurationGroup implements Group{

        private final List<MyAutoConfigurationEntry> autoConfigurationEntries = new ArrayList<>();
        private final Map<String, MyAnnotationMetadata> entries = new LinkedHashMap<>();

        @Override
        public void process(MyAnnotationMetadata annotationMetadata, MyDeferredImportSelector deferredImportSelector) {
            MyAutoConfigurationEntry autoConfigurationEntry = ((MyAutoConfigurationImportSelector) deferredImportSelector)
                    .getAutoConfigurationEntry(annotationMetadata);
            this.autoConfigurationEntries.add(autoConfigurationEntry);
            for (String importClassName : autoConfigurationEntry.getConfigurations()) {
                this.entries.putIfAbsent(importClassName, annotationMetadata);
            }
        }

        @Override
        public Iterable<Entry> selectImports() {
            return null;
        }
    }

    protected static class MyAutoConfigurationEntry {

        private final List<String> configurations;

        private final Set<String> exclusions;

        private MyAutoConfigurationEntry() {
            this.configurations = Collections.emptyList();
            this.exclusions = Collections.emptySet();
        }

        MyAutoConfigurationEntry(Collection<String> configurations) {
            this.configurations = new ArrayList<>(configurations);
            this.exclusions = new HashSet<>();
        }

        public List<String> getConfigurations() {
            return this.configurations;
        }

        public Set<String> getExclusions() {
            return this.exclusions;
        }

    }

}
