package org.hxm.myspring.web;

import org.apache.catalina.Container;
import org.apache.catalina.LifecycleException;
import org.apache.catalina.Wrapper;
import org.apache.catalina.core.StandardContext;
import org.springframework.util.ClassUtils;

import javax.servlet.ServletException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Stream;

public class MyTomcatEmbeddedContext extends StandardContext {
    private MyTomcatStarter starter;

    void deferredLoadOnStartup() throws LifecycleException {
        doWithThreadContextClassLoader(getLoader().getClassLoader(),
                () -> getLoadOnStartupWrappers(findChildren()).forEach(this::load));
    }

    private Stream<Wrapper> getLoadOnStartupWrappers(Container[] children) {
        Map<Integer, List<Wrapper>> grouped = new TreeMap<>();
        for (Container child : children) {
            Wrapper wrapper = (Wrapper) child;
            int order = wrapper.getLoadOnStartup();
            if (order >= 0) {
                grouped.computeIfAbsent(order, (o) -> new ArrayList<>()).add(wrapper);
            }
        }
        return grouped.values().stream().flatMap(List::stream);
    }

    private void load(Wrapper wrapper) {
        try {
            wrapper.load();
        }
        catch (ServletException ex) {
            ex.printStackTrace();
        }
    }

    private void doWithThreadContextClassLoader(ClassLoader classLoader, Runnable code) {
        ClassLoader existingLoader = (classLoader != null) ? ClassUtils.overrideThreadContextClassLoader(classLoader)
                : null;
        try {
            code.run();
        }
        finally {
            if (existingLoader != null) {
                ClassUtils.overrideThreadContextClassLoader(existingLoader);
            }
        }
    }

    public void setStarter(MyTomcatStarter starter) {
        this.starter = starter;
    }

    public MyTomcatStarter getStarter() {
        return starter;
    }
}
