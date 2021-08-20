package org.hxm.myspring.asm;

import org.hxm.myspring.utils.MyClassUtil;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.*;

public class MyAnnotationTypeMapping {
    private final MyAnnotationTypeMapping source;
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
        this.distance = (source == null ? 0 : source.getDistance() + 1);
        this.annotationType = annotationType;
        this.annotation = annotation;
        this.attributes=MyAttributeMethods.forAnnotationType(annotationType);
        this.aliasMappings = filledIntArray(this.attributes.size());
        this.annotationValueMappings = filledIntArray(this.attributes.size());
        this.conventionMappings = filledIntArray(this.attributes.size());
        this.annotationValueSource = new MyAnnotationTypeMapping[this.attributes.size()];
        addConventionAnnotationValues();
    }


    private void addConventionAnnotationValues() {
        for (int i = 0; i < this.attributes.size(); i++) {
            Method attribute = this.attributes.get(i);
            boolean isValueAttribute = "value".equals(attribute.getName());
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

    private boolean isBetterConventionAnnotationValue(int index, boolean isValueAttribute,
                                                      MyAnnotationTypeMapping mapping) {

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
