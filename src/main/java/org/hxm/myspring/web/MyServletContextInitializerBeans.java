package org.hxm.myspring.web;

import org.hxm.myspring.factory.MyBeanFactory;
import org.springframework.aop.scope.ScopedProxyUtils;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import javax.servlet.MultipartConfigElement;
import javax.servlet.Servlet;
import java.util.*;
import java.util.stream.Collectors;

public class MyServletContextInitializerBeans extends AbstractCollection<MyServletContextInitializer> {
    private final MultiValueMap<Class<?>, MyServletContextInitializer> initializers;

    private final List<Class<? extends MyServletContextInitializer>> initializerTypes;

    private List<MyServletContextInitializer> sortedList;

    public MyServletContextInitializerBeans(MyBeanFactory beanFactory,
                                            Class<? extends MyServletContextInitializer>... initializerTypes) {
        this.initializers = new LinkedMultiValueMap<>();
        this.initializerTypes = (initializerTypes.length != 0) ? Arrays.asList(initializerTypes)
                : Collections.singletonList(MyServletContextInitializer.class);
        addServletContextInitializerBeans(beanFactory);
        this.sortedList = this.initializers.values().stream().flatMap(Collection::stream).collect(Collectors.toList());
    }

    private void addServletContextInitializerBeans(MyBeanFactory beanFactory) {
        for (Class<? extends MyServletContextInitializer> initializerType : this.initializerTypes) {
            for (Map.Entry<String, ? extends MyServletContextInitializer> initializerBean : getOrderedBeansOfType(beanFactory,
                    initializerType)) {
                addServletContextInitializerBean(initializerBean.getValue());
            }
        }
    }

    private <T> List<Map.Entry<String, T>> getOrderedBeansOfType(MyBeanFactory beanFactory, Class<T> type) {
        return getOrderedBeansOfType(beanFactory, type, Collections.emptySet());
    }

    private <T>List<Map.Entry<String, T>> getOrderedBeansOfType(MyBeanFactory beanFactory, Class<T> type,
                                                                 Set<?> excludes) {
        String[] names = beanFactory.getBeanNamesForType(type, false);
        Map<String, T> map = new LinkedHashMap<>();
        for (String name : names) {
            if (!excludes.contains(name) && !ScopedProxyUtils.isScopedTarget(name)) {
                Object bean = null;
                try {
                    bean = beanFactory.getBean(name);
                } catch (Exception exception) {
                    exception.printStackTrace();
                }
                if (!excludes.contains(bean)) {
                    map.put(name, (T)bean);
                }
            }
        }
        List<Map.Entry<String, T>> beans = new ArrayList<>(map.entrySet());
        return beans;
    }

    private void addServletContextInitializerBean(MyServletContextInitializer initializer) {
        if (initializer instanceof MyServletRegistrationBean) {
            this.initializers.add(Servlet.class,initializer);
        }
    }

    private MultipartConfigElement getMultipartConfig(MyBeanFactory beanFactory) {
        List<Map.Entry<String, MultipartConfigElement>> beans = getOrderedBeansOfType(beanFactory,
                MultipartConfigElement.class);
        return beans.isEmpty() ? null : beans.get(0).getValue();
    }

    @Override
    public Iterator<MyServletContextInitializer> iterator() {
        return this.sortedList.iterator();
    }

    @Override
    public int size() {
        return this.sortedList.size();
    }
}
