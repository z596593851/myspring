package org.hxm.myspring.asm;

import org.hxm.myspring.annotation.MyAbstractMergedAnnotation;
import org.hxm.myspring.utils.MyClassUtil;
import java.lang.annotation.Annotation;
import java.lang.reflect.Array;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.function.Function;

/**
 * 一般形式的注解
 * @param <A> 注解的类型
 */
public class MyTypeMappedAnnotation<A extends Annotation> extends MyAbstractMergedAnnotation<A> {

    private MyAnnotationTypeMapping mapping;
    private final ClassLoader classLoader;
    private final Object source;
    private final Object rootAttributes;
    private MyValueExtractor valueExtractor;
    private int aggregateIndex;
    private boolean useMergedValues;

    public Class<A> getAnnotationType() {
        return (Class<A>) this.mapping.getAnnotationType();
    }

    @Override
    public int getDistance() {
        return this.mapping.getDistance();
    }

    public Object getAttributes() {
        return rootAttributes;
    }

    public MyTypeMappedAnnotation(MyAnnotationTypeMapping mapping, ClassLoader classLoader, Object source, Object rootAttributes, MyValueExtractor valueExtractor, int aggregateIndex) {
        this.mapping = mapping;
        this.classLoader = classLoader;
        this.source = source;
        this.rootAttributes = rootAttributes;
        this.valueExtractor = valueExtractor;
        this.aggregateIndex = aggregateIndex;
        this.useMergedValues = true;
    }

    static <A extends Annotation> MyTypeMappedAnnotation<A> createIfPossible(MyAnnotationTypeMapping mapping, MyMergedAnnotation<?> annotation) {

        if (annotation instanceof MyTypeMappedAnnotation) {
            MyTypeMappedAnnotation<?> typeMappedAnnotation = (MyTypeMappedAnnotation<?>) annotation;
            return new MyTypeMappedAnnotation<>(mapping,
                    null,
                    typeMappedAnnotation.source,
                    typeMappedAnnotation.rootAttributes,
                    typeMappedAnnotation.valueExtractor,
                    typeMappedAnnotation.aggregateIndex);
        }
        return createIfPossible(mapping, annotation.getSource(), annotation.synthesize(),
                annotation.getAggregateIndex());
    }

    static <A extends Annotation> MyTypeMappedAnnotation<A> createIfPossible(
            MyAnnotationTypeMapping mapping, Object source, Annotation annotation, int aggregateIndex) {

        return new MyTypeMappedAnnotation<>(mapping,null,source,annotation, MyClassUtil::invokeMethod,aggregateIndex);
    }

    @Override
    public boolean isPresent() {
        return true;
    }

    @Override
    public Class<A> getType() {
        return (Class<A>) this.mapping.getAnnotationType();
    }

    @Override
    public int getAggregateIndex() {
        return this.aggregateIndex;
    }

    @Override
    public Object getSource() {
        return this.source;
    }

    @Override
    public <T extends Map<String, Object>> T asMap(Function<MyMergedAnnotation<?>, T> factory) {
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
                                                                           Class<?> mapType, Function<MyMergedAnnotation<?>, T> factory) {
        if(value instanceof MyMergedAnnotation){
            MyTypeMappedAnnotation<?> annotation=(MyTypeMappedAnnotation<?>)value;
            return annotation.asMap(factory);
        }
        if(value instanceof MyMergedAnnotation[]){
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
            if (mappedIndex != -1) {
                mapping = mapping.getRoot();
                attributeIndex = mappedIndex;
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

    static Object extractFromMap(Method attribute, Object map) {
        return (map != null ? ((Map<String, ?>) map).get(attribute.getName()) : null);
    }

    @Override
    protected A createSynthesized() {
        return (A) this.rootAttributes;
    }
}
