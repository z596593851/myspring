package org.hxm.myspring.annotation;

import org.hxm.myspring.asm.MyAnnotationAttributes;
import org.hxm.myspring.asm.MyMergedAnnotation;

import java.lang.annotation.Annotation;
import java.util.NoSuchElementException;

public abstract class MyAbstractMergedAnnotation<A extends Annotation> implements MyMergedAnnotation<A> {

    private volatile A synthesizedAnnotation;

    @Override
    public MyAnnotationAttributes asAnnotationAttributes(){
        return asMap(mergedAnnotation -> new MyAnnotationAttributes(mergedAnnotation.getType()));
    }

    @Override
    public boolean isDirectlyPresent() {
        return isPresent() && getDistance() == 0;
    }

    @Override
    public A synthesize() {
        if (!isPresent()) {
            throw new NoSuchElementException("Unable to synthesize missing annotation");
        }
        A synthesized = this.synthesizedAnnotation;
        if (synthesized == null) {
            synthesized = createSynthesized();
            this.synthesizedAnnotation = synthesized;
        }
        return synthesized;
    }

    protected abstract A createSynthesized();
}
