package org.hxm.myspring.asm;

import org.hxm.myspring.utils.MyClassUtil;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.*;

/**
 * 单独的注解，及其元信息
 */
public class MyAnnotationTypeMapping {
    private final MyAnnotationTypeMapping source;
    private final MyAnnotationTypeMapping root;
    private final Class<? extends Annotation> annotationType;
    private final Annotation annotation;
    private final MyAttributeMethods attributes;
    private final int[] aliasMappings;
    private final int[] conventionMappings;
    private final int distance;
    private final int[] annotationValueMappings;
    private final MyAnnotationTypeMapping[] annotationValueSource;

    public MyAnnotationTypeMapping(MyAnnotationTypeMapping source, Class<? extends Annotation> annotationType, Annotation annotation) {
        this.source = source;
        this.root = (source != null ? source.getRoot() : this);
        this.distance = (source == null ? 0 : source.getDistance() + 1);
        this.annotationType = annotationType;
        this.annotation = annotation;
        this.attributes=MyAttributeMethods.forAnnotationType(annotationType);
        this.aliasMappings = filledIntArray(this.attributes.size());
        this.annotationValueMappings = filledIntArray(this.attributes.size());
        //addConventionMappings()赋值
        this.conventionMappings = filledIntArray(this.attributes.size());
        //addConventionAnnotationValues()赋值
        this.annotationValueSource = new MyAnnotationTypeMapping[this.attributes.size()];
        addConventionMappings();
        addConventionAnnotationValues();
    }

    private void addConventionMappings() {
        if (this.distance == 0) {
            return;
        }
        MyAttributeMethods rootAttributes = this.root.getAttributes();
        int[] mappings = this.conventionMappings;
        for (int i = 0; i < mappings.length; i++) {
            String name = this.attributes.get(i).getName();
            int mapped = rootAttributes.indexOf(name);
            if (!MyMergedAnnotation.VALUE.equals(name) && mapped != -1) {
                mappings[i] = mapped;
            }
        }
    }


    private void addConventionAnnotationValues() {
        for (int i = 0; i < this.attributes.size(); i++) {
            Method attribute = this.attributes.get(i);
            boolean isValueAttribute = MyMergedAnnotation.VALUE.equals(attribute.getName());
            MyAnnotationTypeMapping mapping = this;
            while (mapping != null && mapping.distance > 0) {
                int mapped = mapping.getAttributes().indexOf(attribute.getName());
                if (mapped != -1 && isBetterConventionAnnotationValue(i, isValueAttribute, mapping)) {
                    this.annotationValueMappings[i] = mapped;
                    this.annotationValueSource[i] = mapping;
                }
                mapping = mapping.source;
            }
        }
    }

    private boolean isBetterConventionAnnotationValue(int index, boolean isValueAttribute, MyAnnotationTypeMapping mapping) {
        if (this.annotationValueMappings[index] == -1) {
            return true;
        }
        int existingDistance = this.annotationValueSource[index].distance;
        return !isValueAttribute && existingDistance > mapping.distance;
    }

    private Map<Method, List<Method>> resolveAliasedForTargets() {
        Map<Method, List<Method>> aliasedBy = new HashMap<>();
        return Collections.unmodifiableMap(aliasedBy);
    }

    private static int[] filledIntArray(int size) {
        int[] array = new int[size];
        Arrays.fill(array, -1);
        return array;
    }

    Class<? extends Annotation> getAnnotationType() {
        return this.annotationType;
    }

    public MyAnnotationTypeMapping getSource() {
        return source;
    }

    public MyAttributeMethods getAttributes() {
        return this.attributes;
    }
    public MyAnnotationTypeMapping getRoot(){
        return this.root;
    }

    public int getAliasMapping(int attributeIndex) {
        return this.aliasMappings[attributeIndex];
    }
    public int getConventionMapping(int attributeIndex) {
        return this.conventionMappings[attributeIndex];
    }
    public int getDistance() {
        return this.distance;
    }
    public Object getMappedAnnotationValue(int attributeIndex, boolean metaAnnotationsOnly) {
        MyAnnotationTypeMapping source = this.annotationValueSource[attributeIndex];
        if (source == this && metaAnnotationsOnly) {
            return null;
        }
        return MyClassUtil.invokeMethod(source.attributes.get(attributeIndex), source.annotation);
    }

    public Annotation getAnnotation() {
        return this.annotation;
    }


}
