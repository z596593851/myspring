package org.hxm.myspring.asm;

import org.hxm.myspring.annotation.MyAnnotationFilter;
import org.hxm.myspring.annotation.MyMergedAnnotationSelector;
import org.hxm.myspring.annotation.MyMergedAnnotationSelectors;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;
import java.lang.annotation.Annotation;
import java.util.Collection;
import java.util.Iterator;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.function.Consumer;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/**
 * A Collection of MergedAnnotation instances that represent direct annotations.
 */
public class MyMergedAnnotationsCollection implements MyMergedAnnotations{

    private final MyMergedAnnotation<?>[] annotations;

    private final MyAnnotationTypeMappings[] mappings;

    private MyMergedAnnotationsCollection(Collection<MyMergedAnnotation<?>> annotations) {
        this.annotations = annotations.toArray(new MyMergedAnnotation<?>[0]);
        this.mappings = new MyAnnotationTypeMappings[this.annotations.length];
        for (int i = 0; i < this.annotations.length; i++) {
            MyMergedAnnotation<?> annotation = this.annotations[i];
            Assert.notNull(annotation, "Annotation must not be null");
            Assert.isTrue(annotation.isDirectlyPresent(), "Annotation must be directly present");
            Assert.isTrue(annotation.getAggregateIndex() == 0, "Annotation must have aggregate index of zero");
            this.mappings[i] = new MyAnnotationTypeMappings(annotation.getType(), MyAnnotationFilter.PLAIN);
        }
    }

    @Override
    public boolean isPresent(String annotationType) {
        return isPresent(annotationType,false);
    }

    private boolean isPresent(Object requiredType, boolean directOnly) {
        for (MyMergedAnnotation<?> annotation : this.annotations) {
            Class<? extends Annotation> type = annotation.getType();
            if (type == requiredType || type.getName().equals(requiredType)) {
                return true;
            }
        }
        if (!directOnly) {
            for (MyAnnotationTypeMappings mappings : this.mappings) {
                for (int i = 1; i < mappings.size(); i++) {
                    MyAnnotationTypeMapping mapping = mappings.get(i);
                    if (isMappingForType(mapping, requiredType)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    private static boolean isMappingForType(MyAnnotationTypeMapping mapping, Object requiredType) {
        if (requiredType == null) {
            return true;
        }
        Class<? extends Annotation> actualType = mapping.getAnnotationType();
        return (actualType == requiredType || actualType.getName().equals(requiredType));
    }

    @Override
    public <A extends Annotation> MyMergedAnnotation<A> get(String annotationType) {
        MyMergedAnnotation<A> result = find(annotationType, null);
        return (result != null ? result : MyMergedAnnotation.missing());
    }

    private <A extends Annotation> MyMergedAnnotation<A> find(Object requiredType, MyMergedAnnotationSelector<A> selector) {
        if (selector == null) {
            selector = MyMergedAnnotationSelectors.nearest();
        }
        MyMergedAnnotation<A> result = null;
        for (int i = 0; i < this.annotations.length; i++) {
            MyMergedAnnotation<?> root = this.annotations[i];
            MyAnnotationTypeMappings mappings = this.mappings[i];
            for (int mappingIndex = 0; mappingIndex < mappings.size(); mappingIndex++) {
                MyAnnotationTypeMapping mapping = mappings.get(mappingIndex);
                if (!isMappingForType(mapping, requiredType)) {
                    continue;
                }
                MyMergedAnnotation<A> candidate = (mappingIndex == 0 ? (MyMergedAnnotation<A>) root :
                        MyTypeMappedAnnotation.createIfPossible(mapping, root));
                if (candidate != null) {
                    if (selector.isBestCandidate(candidate)) {
                        return candidate;
                    }
                    result = (result != null ? selector.select(result, candidate) : candidate);
                }
            }
        }
        return result;
    }

    static MyMergedAnnotations of(Collection<MyMergedAnnotation<?>> annotations) {
        Assert.notNull(annotations, "Annotations must not be null");
        if (annotations.isEmpty()) {
            return MyTypeMappedAnnotations.NONE;
        }
        return new MyMergedAnnotationsCollection(annotations);
    }

    @Override
    public Stream<MyMergedAnnotation<Annotation>> stream() {
        return StreamSupport.stream(spliterator(), false);
    }
    @Override
    public Spliterator<MyMergedAnnotation<Annotation>> spliterator() {
        return spliterator(null);
    }

    private <A extends Annotation> Spliterator<MyMergedAnnotation<A>> spliterator(@Nullable Object annotationType) {
        return new MyMergedAnnotationsCollection.MyAnnotationsSpliterator<>(annotationType);
    }

    @Override
    public Iterator<MyMergedAnnotation<Annotation>> iterator() {
        return Spliterators.iterator(spliterator());
    }

    private class MyAnnotationsSpliterator<A extends Annotation> implements Spliterator<MyMergedAnnotation<A>> {
        private Object requiredType;
        private final int[] mappingCursors;

        public MyAnnotationsSpliterator(Object requiredType) {
            this.mappingCursors = new int[annotations.length];
            this.requiredType = requiredType;
        }

        @Override
        public boolean tryAdvance(Consumer<? super MyMergedAnnotation<A>> action) {
            int lowestDistance = Integer.MAX_VALUE;
            int annotationResult = -1;
            for (int annotationIndex = 0; annotationIndex < annotations.length; annotationIndex++) {
                MyAnnotationTypeMapping mapping = getNextSuitableMapping(annotationIndex);
                if (mapping != null && mapping.getDistance() < lowestDistance) {
                    annotationResult = annotationIndex;
                    lowestDistance = mapping.getDistance();
                }
                if (lowestDistance == 0) {
                    break;
                }
            }
            if (annotationResult != -1) {
                MyMergedAnnotation<A> mergedAnnotation = createMergedAnnotationIfPossible(
                        annotationResult, this.mappingCursors[annotationResult]);
                this.mappingCursors[annotationResult]++;
                if (mergedAnnotation == null) {
                    return tryAdvance(action);
                }
                action.accept(mergedAnnotation);
                return true;
            }
            return false;
        }

        @Nullable
        private MyAnnotationTypeMapping getNextSuitableMapping(int annotationIndex) {
            MyAnnotationTypeMapping mapping;
            do {
                mapping = getMapping(annotationIndex, this.mappingCursors[annotationIndex]);
                if (mapping != null && isMappingForType(mapping, this.requiredType)) {
                    return mapping;
                }
                this.mappingCursors[annotationIndex]++;
            }
            while (mapping != null);
            return null;
        }

        @Nullable
        private MyAnnotationTypeMapping getMapping(int annotationIndex, int mappingIndex) {
            MyAnnotationTypeMappings mappings = MyMergedAnnotationsCollection.this.mappings[annotationIndex];
            return (mappingIndex < mappings.size() ? mappings.get(mappingIndex) : null);
        }

        @Nullable
        @SuppressWarnings("unchecked")
        private MyMergedAnnotation<A> createMergedAnnotationIfPossible(int annotationIndex, int mappingIndex) {
            MyMergedAnnotation<?> root = annotations[annotationIndex];
            if (mappingIndex == 0) {
                return (MyMergedAnnotation<A>) root;
            }
            return MyTypeMappedAnnotation.createIfPossible(
                    mappings[annotationIndex].get(mappingIndex), root);
        }

        @Override
        public Spliterator<MyMergedAnnotation<A>> trySplit() {
            return null;
        }

        @Override
        public long estimateSize() {
            int size = 0;
            for (int i = 0; i < annotations.length; i++) {
                MyAnnotationTypeMappings mappings = MyMergedAnnotationsCollection.this.mappings[i];
                int numberOfMappings = mappings.size();
                numberOfMappings -= Math.min(this.mappingCursors[i], mappings.size());
                size += numberOfMappings;
            }
            return size;
        }

        @Override
        public int characteristics() {
            return NONNULL | IMMUTABLE;
        }
    }
}
