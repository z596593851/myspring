package org.hxm.myspring.utils;

import org.hxm.myspring.factory.MyBeanDefinition;
import org.hxm.myspring.annotation.MyScope;
import org.hxm.myspring.annotation.MyScopeMetadata;

import java.lang.annotation.Annotation;
import java.util.Map;

public class MyMetadataResolver {

    protected Class<? extends Annotation> scopeAnnotationType = MyScope.class;

    public MyScopeMetadata resolveScopeMetadata(MyBeanDefinition definition){
        MyScopeMetadata metadata=new MyScopeMetadata();
        Map<String,Object> attributes=definition.getMetadata().getAnnotationAttributes(scopeAnnotationType.getName(),false);
        if(attributes!=null){
            metadata.setScopeName((String)attributes.get("value"));
        }
        return metadata;
    }
}
