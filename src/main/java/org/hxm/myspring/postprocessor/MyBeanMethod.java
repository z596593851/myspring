package org.hxm.myspring.postprocessor;

import org.hxm.myspring.asm.MyMethodMetadata;

public class MyBeanMethod {

    private MyMethodMetadata methodMetadata;

    private MyConfigurationClass configurationClass;

    public MyBeanMethod(MyMethodMetadata methodMetadata, MyConfigurationClass configurationClass) {
        this.methodMetadata = methodMetadata;
        this.configurationClass = configurationClass;
    }

    public MyConfigurationClass getConfigurationClass(){
        return configurationClass;
    }

    public MyMethodMetadata getMethodMetadata() {
        return methodMetadata;
    }
}
