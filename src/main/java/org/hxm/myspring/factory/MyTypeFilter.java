package org.hxm.myspring.factory;

import org.hxm.myspring.asm.MySimpleMetadataReader;

public interface MyTypeFilter {
    boolean match(MySimpleMetadataReader metadataReader);
}
