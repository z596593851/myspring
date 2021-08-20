package org.hxm.myspring.web;

import javax.servlet.ServletContainerInitializer;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import java.util.Set;

public class MyTomcatStarter  implements ServletContainerInitializer {

    private final MyServletContextInitializer[] initializers;

    public MyTomcatStarter(MyServletContextInitializer[] initializers) {
        this.initializers = initializers;
    }

    @Override
    public void onStartup(Set<Class<?>> classes, ServletContext servletContext) throws ServletException {
        try {
            for (MyServletContextInitializer initializer : this.initializers) {
                initializer.onStartup(servletContext);
            }
        }
        catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}
