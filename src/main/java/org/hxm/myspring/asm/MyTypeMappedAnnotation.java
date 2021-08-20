package org.hxm.myspring.asm;

import org.hxm.myspring.utils.MyClassUtil;

import java.lang.annotation.Annotation;
import java.lang.reflect.Array;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.function.Function;

public class MyTypeMappedAnnotation<A extends Annotation>  {

    private MyAnnotationTypeMapping mapping;
    private final ClassLoader classLoader;
    private final Object source;
    //一个注解对应的key-value，可以是类上的注解也可以是方法上的
    private final Object rootAttributes;
    private MyValueExtractor valueExtractor;
    private int aggregateIndex;
    private Class<A> annotationType;
    private boolean useMergedValues;

    public Class<A> getAnnotationType() {
        return annotationType;
    }

    public Object getAttributes() {
        return rootAttributes;
    }

    public MyTypeMappedAnnotation(ClassLoader classLoader, Object source, Class<A> annotationType, Object attributes) {
        this.classLoader = classLoader;
        this.source = source;
        this.annotationType = annotationType;
        this.rootAttributes = attributes;
        this.useMergedValues = true;
    }

    public MyTypeMappedAnnotation( MyAnnotationTypeMapping mapping, ClassLoader classLoader, Object source, Object rootAttributes, MyValueExtractor valueExtractor, int aggregateIndex) {
        this.mapping = mapping;
        this.classLoader = classLoader;
        this.source = source;
        this.rootAttributes = rootAttributes;
        this.valueExtractor = valueExtractor;
        this.aggregateIndex = aggregateIndex;
    }


    static <A extends Annotation> MyTypeMappedAnnotation<A> createIfPossible(
            MyAnnotationTypeMapping mapping, Object source, Annotation annotation, int aggregateIndex) {

        return new MyTypeMappedAnnotation<>(mapping,null,source,annotation, MyClassUtil::invokeMethod,aggregateIndex);
    }

    public boolean isPresent() {
        return true;
    }

    public Class<A> getType() {
        return (Class<A>) this.mapping.getAnnotationType();
    }

    public MyAnnotationAttributes asAnnotationAttributes(){
        return asMap(mergedAnnotation -> new MyAnnotationAttributes(mergedAnnotation.getType()));
    }

    public <T extends Map<String, Object>> T asMap(Function<MyTypeMappedAnnotation<?>, T> factory) {
        T map = factory.apply(this);
        MyAttributeMethods attributes = this.mapping.getAttributes();
        for (int i = 0; i < attributes.size(); i++) {
            Method attribute = attributes.get(i);
            Object value = getValue(i, Object.class);
            if (value != null) {
                map.put(attribute.getName(),
                        adaptValueForMapOptions(attribute, value, map.getClass(), factory));
            }
        }
        return map;
    }

    private <T extends Map<String, Object>> Object adaptValueForMapOptions(Method attribute, Object value,
                                                                           Class<?> mapType, Function<MyTypeMappedAnnotation<?>, T> factory) {
        if(value instanceof MyTypeMappedAnnotation){
            MyTypeMappedAnnotation<?> annotation=(MyTypeMappedAnnotation<?>)value;
            return annotation.asMap(factory);
        }
        if(value instanceof MyTypeMappedAnnotation[]){
            MyTypeMappedAnnotation<?>[] annotations=(MyTypeMappedAnnotation<?>[])value;
            Object result = Array.newInstance(mapType, annotations.length);
            for (int i = 0; i < annotations.length; i++) {
                Array.set(result, i, annotations[i].asMap(factory));
            }
            return result;
        }
        return value;
    }

    private <T> T getValue(int attributeIndex, Class<T> type) {
        Method attribute = this.mapping.getAttributes().get(attributeIndex);
        Object value = getValue(attributeIndex, true, false);
        if (value == null) {
            value = attribute.getDefaultValue();
        }
        return (T)value;
    }

    private Object getValue(int attributeIndex, boolean useConventionMapping, boolean forMirrorResolution) {
        MyAnnotationTypeMapping mapping = this.mapping;
        if (this.useMergedValues) {
            int mappedIndex = this.mapping.getAliasMapping(attributeIndex);
            if (mappedIndex == -1 && useConventionMapping) {
                mappedIndex = this.mapping.getConventionMapping(attributeIndex);
            }
        }
        if (attributeIndex == -1) {
            return null;
        }
        if (mapping.getDistance() == 0) {
            Method attribute = mapping.getAttributes().get(attributeIndex);
            Object result = this.valueExtractor.extract(attribute, this.rootAttributes);
            return (result != null ? result : attribute.getDefaultValue());
        }
        return getValueFromMetaAnnotation(attributeIndex, forMirrorResolution);
    }

    private Object getValueFromMetaAnnotation(int attributeIndex, boolean forMirrorResolution) {
        Object value = null;
        if (this.useMergedValues || forMirrorResolution) {
            value = this.mapping.getMappedAnnotationValue(attributeIndex, forMirrorResolution);
        }
        if (value == null) {
            Method attribute = this.mapping.getAttributes().get(attributeIndex);
            value = MyClassUtil.invokeMethod(attribute, this.mapping.getAnnotation());
        }
        return value;
    }
}
