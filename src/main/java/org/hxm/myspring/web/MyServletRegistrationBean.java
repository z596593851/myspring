package org.hxm.myspring.web;

import org.springframework.util.Assert;

import javax.servlet.Servlet;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletRegistration;
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
        ServletRegistration.Dynamic registration = servletContext.addServlet(getName(), this.servlet);
        registration.setAsyncSupported(true);
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
