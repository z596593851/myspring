package org.hxm.myspring.web;

import org.apache.catalina.core.ApplicationServletRegistration;

import javax.servlet.*;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Set;

public class MyServletRegistrationBean<T extends Servlet> implements MyServletContextInitializer {
    private T servlet;
    private String name;
    private Set<String> urlMappings = new LinkedHashSet<>();

    public MyServletRegistrationBean(){
    }

    public MyServletRegistrationBean(T servlet,String... urlMappings){
        this.servlet=servlet;
        this.urlMappings.addAll(Arrays.asList(urlMappings));
    }

    @Override
    public void onStartup(ServletContext servletContext) throws ServletException {
        MultipartConfigElement multipartConfig=new MultipartConfigElement("",1048576,10485760,0);
        //关键
        String[] urlMapping=new String[]{"/"};
        ApplicationServletRegistration registration = (ApplicationServletRegistration)servletContext.addServlet(getName(), this.servlet);
        registration.setAsyncSupported(true);
        registration.addMapping(urlMapping);
        registration.setLoadOnStartup(-1);
        registration.setMultipartConfig(multipartConfig);

    }

    public void addUrlMappings(String... urlMappings) {
        this.urlMappings.addAll(Arrays.asList(urlMappings));
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
}
