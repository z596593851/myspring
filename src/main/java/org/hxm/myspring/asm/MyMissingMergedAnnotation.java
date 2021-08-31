package org.hxm.myspring.asm;

import org.hxm.myspring.annotation.MyAbstractMergedAnnotation;

import java.lang.annotation.Annotation;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.function.Function;

public class MyMissingMergedAnnotation<A extends Annotation> extends MyAbstractMergedAnnotation<A> {
    private static final MyMissingMergedAnnotation<?> INSTANCE = new MyMissingMergedAnnotation<>();

    private MyMissingMergedAnnotation(){

    }

    @Override
    public Class<A> getType() {
        throw new NoSuchElementException("Unable to get type for missing annotation");
    }

    @Override
    public int getAggregateIndex() {
        return -1;
    }

    @Override
    public Object getSource() {
        return null;
    }

    @Override
    public int getDistance() {
        return -1;
    }

    @Override
    public boolean isPresent() {
        return false;
    }

    @Override
    public <T extends Map<String, Object>> T asMap(Function<MyMergedAnnotation<?>, T> factory) {
        return factory.apply(this);
    }

    static <A extends Annotation> MyMergedAnnotation<A> getInstance() {
        return (MyMergedAnnotation<A>) INSTANCE;
    }

    @Override
    protected A createSynthesized() {
        throw new NoSuchElementException("Unable to synthesize missing annotation");
    }
}
