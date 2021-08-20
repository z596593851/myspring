package org.hxm.myspring.web;

public class MyDispatcherServletRegistrationBean extends MyServletRegistrationBean<MyDispatcherServlet>{
    private final String path;
    public MyDispatcherServletRegistrationBean(MyDispatcherServlet servlet, String path){
        super(servlet);
        this.path=path;
        super.addUrlMappings(getPath());
    }

    public String getPath() {
        return this.path;
    }
}
