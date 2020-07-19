package org.hxm.myspring.utils;

import org.hxm.myspring.MyBeanDefinition;
import org.hxm.myspring.annotation.MyScope;
import org.hxm.myspring.annotation.MyScopeMetadata;
import org.hxm.myspring.asm.MyAnnotationMetadata;
import org.springframework.context.annotation.Scope;

import java.lang.annotation.Annotation;
import java.util.Map;

public class MyMetadataResolver {

    protected Class<? extends Annotation> scopeAnnotationType = MyScope.class;

    public MyScopeMetadata resolveScopeMetadata(MyBeanDefinition definition){
        MyScopeMetadata metadata=new MyScopeMetadata();
        Map<String,Object> attributes=definition.getMetadata().getAnnotationAttributes(scopeAnnotationType);
        if(attributes!=null){
            //todo 好像不对
            metadata.setScopeName((String)attributes.get("value"));
        }
        return metadata;
    }
}
