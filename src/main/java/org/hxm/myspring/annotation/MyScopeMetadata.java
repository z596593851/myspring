package org.hxm.myspring.annotation;


public class MyScopeMetadata {

    private String scopeName="singleton";

    public void setScopeName(String scopeName) {
        this.scopeName = scopeName;
    }

    public String getScopeName() {
        return this.scopeName;
    }
}
