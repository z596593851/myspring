package org.hxm.myspring.annotation;

import org.hxm.myspring.asm.MyAnnotationMetadata;
import java.util.function.Predicate;

public interface MyImportSelector {

    String[] selectImports(MyAnnotationMetadata importingClassMetadata);

    default Predicate<String> getExclusionFilter() {
        return null;
    }
}
