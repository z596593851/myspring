package org.hxm.myspring.web;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;

public interface MyServletContextInitializer {
    void onStartup(ServletContext servletContext) throws ServletException;
}
