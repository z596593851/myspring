package org.hxm.myspring.asm;

import org.hxm.myspring.annotation.MyAnnotationFilter;
import org.hxm.myspring.postprocessor.MyAnnotationsProcessor;
import org.springframework.core.annotation.*;
import org.springframework.lang.Nullable;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Member;
import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public class MyTypeMappedAnnotations implements MyMergedAnnotations {

    private final Object source;
    private final AnnotatedElement element;
    private final MyAnnotationFilter annotationFilter;
    private volatile List<MyAggregate> aggregates;
    static final MyTypeMappedAnnotations NONE=new MyTypeMappedAnnotations(null,MyAnnotationFilter.ALL);


    private MyTypeMappedAnnotations(AnnotatedElement element, MyAnnotationFilter annotationFilter){
        this.source=element;
        this.element=element;
        this.annotationFilter=annotationFilter;
    }

    @Override
    public boolean isPresent(String annotationType) {
        if (this.annotationFilter.matches(annotationType)) {
            return false;
        }
        return Boolean.TRUE.equals(scan(annotationType,new IsPresent(this.annotationFilter,false)));
    }

    @Override
    public Stream<MyMergedAnnotation<Annotation>> stream() {
        if (this.annotationFilter == MyAnnotationFilter.ALL) {
            return Stream.empty();
        }
        return StreamSupport.stream(spliterator(), false);
    }

    @Override
    public Iterator<MyMergedAnnotation<Annotation>> iterator() {
        if (this.annotationFilter == MyAnnotationFilter.ALL) {
            return Collections.emptyIterator();
        }
        return Spliterators.iterator(spliterator());
    }

    @Override
    public Spliterator<MyMergedAnnotation<Annotation>> spliterator() {
        if (this.annotationFilter == AnnotationFilter.ALL) {
            return Spliterators.emptySpliterator();
        }
        return spliterator(null);
    }
    private <A extends Annotation> Spliterator<MyMergedAnnotation<A>> spliterator(@Nullable Object annotationType) {
        return new MyAggregatesSpliterator<>(annotationType, getAggregates());
    }

    private List<MyAggregate> getAggregates() {
        List<MyAggregate> aggregates = this.aggregates;
        if (aggregates == null) {
            aggregates = scan(this, new MyAggregatesCollector());
            if (aggregates == null || aggregates.isEmpty()) {
                aggregates = Collections.emptyList();
            }
            this.aggregates = aggregates;
        }
        return aggregates;
    }

    @Override
    public <A extends Annotation> MyMergedAnnotation<A> get(String annotationType){
        if(this.annotationFilter.matches(annotationType)){
            return MyMergedAnnotation.missing();
        }
        MyMergedAnnotation<A> result=scan(annotationType, new MyMergedAnnotationFinder<>(annotationType));
        return (result !=null? result: MyMergedAnnotation.missing());
    }

    /**
     * mergedAnnotations 经过processor的处理后，返回处理结果
     * @param criteria 期望参与对比的element的权限定类名
     * @param processor 处理器
     * @return 处理结果
     */
    private <C, R> R scan(C criteria, MyAnnotationsProcessor<C, R> processor) {
        if(this.element!=null){
            return MyAnnotationsScanner.scan(criteria,this.element,processor);
        }
        return null;
    }


    static MyTypeMappedAnnotations from(AnnotatedElement element, MyAnnotationFilter annotationFilter){
        if(element instanceof Class && ((Class<?>) element).getName().startsWith("java.")){
            return NONE;
        }else if(element instanceof Member && ((Member) element).getDeclaringClass().getName().startsWith("java.")){
            return NONE;
        }
        return new MyTypeMappedAnnotations(element, annotationFilter);
    }

    private static boolean isMappingForType(MyAnnotationTypeMapping mapping, MyAnnotationFilter annotationFilter, Object requiredType) {

        Class<? extends Annotation> actualType = mapping.getAnnotationType();
        return (!annotationFilter.matches(actualType) &&
                (requiredType == null || actualType == requiredType || actualType.getName().equals(requiredType)));
    }


    /**
     * 注解处理器-判断注解 requiredType 是否出现在 annotations 中
     */
    private static final class IsPresent implements MyAnnotationsProcessor<Object, Boolean> {
        private final MyAnnotationFilter annotationFilter;
        private final boolean directOnly;
        IsPresent(MyAnnotationFilter annotationFilter,boolean directOnly){
            this.annotationFilter=annotationFilter;
            this.directOnly=directOnly;
        }

        @Override
        public Boolean doWithAnnotations(Object requiredType, int aggregateIndex, Object source, Annotation[] annotations) {
            for (Annotation annotation : annotations) {
                if (annotation != null) {
                    Class<? extends Annotation> type = annotation.annotationType();
                    if (type != null && !this.annotationFilter.matches(type)) {
                        if (type == requiredType || type.getName().equals(requiredType)) {
                            return Boolean.TRUE;
                        }

                        if (!this.directOnly) {
                            MyAnnotationTypeMappings mappings = new MyAnnotationTypeMappings(type,MyAnnotationFilter.PLAIN);
                            for (int i = 0; i < mappings.size(); i++) {
                                MyAnnotationTypeMapping mapping = mappings.get(i);
                                if (isMappingForType(mapping, this.annotationFilter, requiredType)) {
                                    return Boolean.TRUE;
                                }
                            }
                        }
                    }
                }
            }
            return null;
        }
    }

    /**
     * 注解处理器-找到期望的注解
     */
    private class MyMergedAnnotationFinder<A extends Annotation> implements MyAnnotationsProcessor<Object,MyTypeMappedAnnotation<A>> {
        /**
         * 期望找到的注解
         */
        private final Object requiredType;
        private MyTypeMappedAnnotation<A> result;

        MyMergedAnnotationFinder(Object requiredType){
            this.requiredType=requiredType;
        }

        @Override
        public MyTypeMappedAnnotation<A> doWithAggregate(Object context, int aggregateIndex) {
            return this.result;
        }

        /**
         * 从给定的注解中找到期望的注解
         * @param source 被标注的实体
         * @param annotations 给定的注解
         * @return 查找结果
         */
        @Override
        public MyTypeMappedAnnotation<A> doWithAnnotations(Object type, int aggregateIndex, Object source, Annotation[] annotations) {
            for (Annotation annotation : annotations) {
                if (annotation != null) {
                    MyTypeMappedAnnotation<A> result = process(type, aggregateIndex, source, annotation);
                    if (result != null) {
                        return result;
                    }
                }
            }
            return null;
        }

        private MyTypeMappedAnnotation<A> process(Object type, int aggregateIndex, Object source, Annotation annotation) {
            MyAnnotationTypeMappings mappings =new MyAnnotationTypeMappings(annotation.annotationType(),annotationFilter);
            for (int i = 0; i < mappings.size(); i++) {
                MyAnnotationTypeMapping mapping = mappings.get(i);
                if (isMappingForType(mapping,annotationFilter, this.requiredType)) {
                    MyTypeMappedAnnotation<A> candidate = MyTypeMappedAnnotation.createIfPossible(mapping, source, annotation, aggregateIndex);
                    if (candidate != null) {
                        updateLastResult(candidate);
                    }
                }
            }
            return null;
        }

        private void updateLastResult(MyTypeMappedAnnotation<A> candidate) {
            MyTypeMappedAnnotation<A> lastResult = this.result;
            this.result = candidate;
        }

        @Override
        public MyTypeMappedAnnotation<A> finish(MyTypeMappedAnnotation<A> result) {
            return (result != null ? result : this.result);
        }
    }

    private static class MyAggregate {

        private final int aggregateIndex;

        private final Object source;

        private final List<Annotation> annotations;

        private final MyAnnotationTypeMappings[] mappings;

        MyAggregate(int aggregateIndex, Object source, List<Annotation> annotations) {
            this.aggregateIndex = aggregateIndex;
            this.source = source;
            this.annotations = annotations;
            this.mappings = new MyAnnotationTypeMappings[annotations.size()];
            for (int i = 0; i < annotations.size(); i++) {
                this.mappings[i] = new MyAnnotationTypeMappings(annotations.get(i).annotationType(),MyAnnotationFilter.PLAIN);
            }
        }

        int size() {
            return this.annotations.size();
        }

        MyAnnotationTypeMapping getMapping(int annotationIndex, int mappingIndex) {
            MyAnnotationTypeMappings mappings = getMappings(annotationIndex);
            return (mappingIndex < mappings.size() ? mappings.get(mappingIndex) : null);
        }

        MyAnnotationTypeMappings getMappings(int annotationIndex) {
            return this.mappings[annotationIndex];
        }

        <A extends Annotation> MyMergedAnnotation<A> createMergedAnnotationIfPossible(
                int annotationIndex, int mappingIndex) {

            return MyTypeMappedAnnotation.createIfPossible(
                    this.mappings[annotationIndex].get(mappingIndex), this.source,
                    this.annotations.get(annotationIndex), this.aggregateIndex);
        }
    }

    /**
     * 注解处理器-将给定的注解s封装成 MyAggregate 供遍历使用
     */
    private class MyAggregatesCollector implements MyAnnotationsProcessor<Object, List<MyAggregate>> {

        private final List<MyAggregate> aggregates = new ArrayList<>();

        @Override
        public List<MyAggregate> doWithAnnotations(Object criteria, int aggregateIndex, Object source, Annotation[] annotations) {
            this.aggregates.add(createAggregate(aggregateIndex, source, annotations));
            return null;
        }

        private MyAggregate createAggregate(int aggregateIndex, Object source, Annotation[] annotations) {
            List<Annotation> aggregateAnnotations = getAggregateAnnotations(annotations);
            return new MyAggregate(aggregateIndex, source, aggregateAnnotations);
        }

        private List<Annotation> getAggregateAnnotations(Annotation[] annotations) {
            List<Annotation> result = new ArrayList<>(annotations.length);
            addAggregateAnnotations(result, annotations);
            return result;
        }

        private void addAggregateAnnotations(List<Annotation> aggregateAnnotations, Annotation[] annotations) {
            for (Annotation annotation : annotations) {
                if (annotation != null && !annotationFilter.matches(annotation)) {
                    aggregateAnnotations.add(annotation);
                }
            }
        }

        @Override
        public List<MyAggregate> finish(List<MyAggregate> processResult) {
            return this.aggregates;
        }
    }

    /**
     * {@link Spliterator} used to consume merged annotations from the
     * aggregates in distance fist order.
     */
    private class MyAggregatesSpliterator<A extends Annotation> implements Spliterator<MyMergedAnnotation<A>> {

        private final Object requiredType;

        private final List<MyAggregate> aggregates;

        private int aggregateCursor;

        private int[] mappingCursors;

        MyAggregatesSpliterator(Object requiredType, List<MyAggregate> aggregates) {
            this.requiredType = requiredType;
            this.aggregates = aggregates;
            this.aggregateCursor = 0;
        }

        @Override
        public boolean tryAdvance(Consumer<? super MyMergedAnnotation<A>> action) {
            while (this.aggregateCursor < this.aggregates.size()) {
                MyAggregate aggregate = this.aggregates.get(this.aggregateCursor);
                if (tryAdvance(aggregate, action)) {
                    return true;
                }
                this.aggregateCursor++;
                this.mappingCursors = null;
            }
            return false;
        }

        private boolean tryAdvance(MyAggregate aggregate, Consumer<? super MyMergedAnnotation<A>> action) {
            if (this.mappingCursors == null) {
                this.mappingCursors = new int[aggregate.size()];
            }
            int lowestDistance = Integer.MAX_VALUE;
            int annotationResult = -1;
            for (int annotationIndex = 0; annotationIndex < aggregate.size(); annotationIndex++) {
                MyAnnotationTypeMapping mapping = getNextSuitableMapping(aggregate, annotationIndex);
                if (mapping != null && mapping.getDistance() < lowestDistance) {
                    annotationResult = annotationIndex;
                    lowestDistance = mapping.getDistance();
                }
                if (lowestDistance == 0) {
                    break;
                }
            }
            if (annotationResult != -1) {
                MyMergedAnnotation<A> mergedAnnotation = aggregate.createMergedAnnotationIfPossible(annotationResult, this.mappingCursors[annotationResult]);
                this.mappingCursors[annotationResult]++;
                if (mergedAnnotation == null) {
                    return tryAdvance(aggregate, action);
                }
                action.accept(mergedAnnotation);
                return true;
            }
            return false;
        }

        private MyAnnotationTypeMapping getNextSuitableMapping(MyAggregate aggregate, int annotationIndex) {
            int[] cursors = this.mappingCursors;
            if (cursors != null) {
                MyAnnotationTypeMapping mapping;
                do {
                    mapping = aggregate.getMapping(annotationIndex, cursors[annotationIndex]);
                    if (mapping != null && isMappingForType(mapping, annotationFilter, this.requiredType)) {
                        return mapping;
                    }
                    cursors[annotationIndex]++;
                }
                while (mapping != null);
            }
            return null;
        }

        @Override
        public Spliterator<MyMergedAnnotation<A>> trySplit() {
            return null;
        }

        @Override
        public long estimateSize() {
            int size = 0;
            for (int aggregateIndex = this.aggregateCursor;
                 aggregateIndex < this.aggregates.size(); aggregateIndex++) {
                MyAggregate aggregate = this.aggregates.get(aggregateIndex);
                for (int annotationIndex = 0; annotationIndex < aggregate.size(); annotationIndex++) {
                    MyAnnotationTypeMappings mappings = aggregate.getMappings(annotationIndex);
                    int numberOfMappings = mappings.size();
                    if (aggregateIndex == this.aggregateCursor && this.mappingCursors != null) {
                        numberOfMappings -= Math.min(this.mappingCursors[annotationIndex], mappings.size());
                    }
                    size += numberOfMappings;
                }
            }
            return size;
        }

        @Override
        public int characteristics() {
            return NONNULL | IMMUTABLE;
        }
    }

}
