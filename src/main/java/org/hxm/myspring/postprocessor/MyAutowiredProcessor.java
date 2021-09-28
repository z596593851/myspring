package org.hxm.myspring.postprocessor;

import org.hxm.myspring.annotation.MyAutowired;
import org.hxm.myspring.annotation.MyValue;
import org.hxm.myspring.asm.MyMergedAnnotation;
import org.hxm.myspring.asm.MyMergedAnnotations;
import org.hxm.myspring.factory.MyBeanDefinition;
import org.hxm.myspring.factory.MyBeanFactory;
import org.hxm.myspring.factory.MyInjectionMetadata;
import org.hxm.myspring.utils.MyClassUtil;
import java.lang.annotation.Annotation;
import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public class MyAutowiredProcessor implements MyBeanPostProcessor,MyBeanFactoryAware{

    private final Set<Class<? extends Annotation>> autowiredAnnotationTypes = new LinkedHashSet<>(4);

    private MyBeanFactory beanFactory;

    public MyAutowiredProcessor() {
        //AutowiredAnnotationBeanPostProcessor只处理@Value和@Autowired注解
        this.autowiredAnnotationTypes.add(MyAutowired.class);
        this.autowiredAnnotationTypes.add(MyValue.class);
    }

    @Override
    public void setBeanFactory(MyBeanFactory beanFactory){
        this.beanFactory=beanFactory;
    }

    public void postProcessMergedBeanDefinition(MyBeanDefinition beanDefinition, Class<?> beanType, String beanName){
    }

    @Override
    public void postProcessProperties(Object bean, String beanName) throws Throwable {
        MyInjectionMetadata metadata = buildAutowiringMetadata(bean.getClass());
        metadata.inject(bean, beanName);
    }

    /**
     * 将所有标注了 @MyAutowired 和 @MyValue 的成员变量封装成一个 MyInjectionMetadata 返回
     */
    public MyInjectionMetadata buildAutowiringMetadata(Class<?> clazz){
        List<MyInjectionMetadata.MyInjectedElement> elements = new ArrayList<>();
        Field[] result=clazz.getDeclaredFields();
        for(Field field:result){
            MyMergedAnnotation<?> ann=findAutowiredAnnotation(field);
            if(ann!=null){
                elements.add(new MyAutowiredFieldElement(field, true));
            }
        }
        return MyInjectionMetadata.forElements(elements);
    }

    private class MyAutowiredFieldElement extends MyInjectionMetadata.MyInjectedElement {
        private final boolean required;
        private volatile boolean cached = false;
        private volatile Object cachedFieldValue;

        public MyAutowiredFieldElement(Field field, boolean required) {
            super(field, null);
            this.required = required;
        }

        @Override
        protected void inject(Object bean, String beanName) throws Throwable {
            Field field = (Field) this.member;
            Set<String> autowiredBeanNames = new LinkedHashSet<>(1);
            Object value = beanFactory.resolveDependency(beanName,field,autowiredBeanNames);
            if (value != null) {
                MyClassUtil.makeAccessible(field);
                field.set(bean, value);
            }
        }
    }

    public MyMergedAnnotation<?> findAutowiredAnnotation(AccessibleObject ao){
        MyMergedAnnotations annotations = MyMergedAnnotations.from(ao);
        for (Class<? extends Annotation> type : this.autowiredAnnotationTypes) {
            MyMergedAnnotation<?> annotation = annotations.get(type);
            if (annotation.isPresent()) {
                return annotation;
            }
        }
        return null;
    }
}
