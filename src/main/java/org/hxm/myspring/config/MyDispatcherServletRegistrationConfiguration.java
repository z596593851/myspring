package org.hxm.myspring.config;

import org.hxm.myspring.annotation.MyBean;
import org.hxm.myspring.annotation.MyConfiguration;
import org.hxm.myspring.web.MyDispatcherServlet;
import org.hxm.myspring.web.MyDispatcherServletRegistrationBean;
import org.hxm.myspring.web.MyServletRegistrationBean;

@MyConfiguration
public class MyDispatcherServletRegistrationConfiguration {

    @MyBean(name = "myServletRegistrationBean")
    public MyDispatcherServletRegistrationBean myDispatcherServletRegistrationBean(){
        MyDispatcherServletRegistrationBean registration=new MyDispatcherServletRegistrationBean(new MyDispatcherServlet(),"/");
        registration.addUrlMappings("/*");
        registration.setName("myDispatcherServlet");
        return registration;
        //dispatcherServletRegistration
    }
}
