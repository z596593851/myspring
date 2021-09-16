package org.hxm.myspring.factory;

import org.hxm.myspring.asm.MyClassMetadata;
import org.hxm.myspring.asm.MySimpleMetadataReader;

public abstract class MyAbstractTypeFilter implements MyTypeFilter{

    @Override
    public boolean match(MySimpleMetadataReader metadataReader) {
        if(matchSelf(metadataReader)){
            return true;
        }
        MyClassMetadata metadata = metadataReader.getClassMetadata();
        if(matchClassName(metadata.getClassName())){
            return true;
        }
        return false;
    }

    protected boolean matchSelf(MySimpleMetadataReader metadataReader) {
        return false;
    }

    protected boolean matchClassName(String className) {
        return false;
    }
}
