package org.hxm.myspring.factory;
import java.util.*;

public class MyLifecycleProcessor {
    private MyBeanFactory beanFactory;

    public void setBeanFactory(MyBeanFactory beanFactory) {
        this.beanFactory=beanFactory;
    }

    public void onRefresh() {
        Map<String, MyLifecycle> lifecycleBeans=getLifecycleBeans();
        Map<Integer, MyLifecycleGroup> phases = new HashMap<>();
        lifecycleBeans.forEach((beanName, bean) -> {
            int phase = bean.getPhase();
            MyLifecycleGroup group = phases.get(phase);
            if (group == null) {
                group = new MyLifecycleGroup(phase, lifecycleBeans);
                phases.put(phase, group);
            }
            group.add(beanName, bean);
        });
        if (!phases.isEmpty()) {
            List<Integer> keys = new ArrayList<>(phases.keySet());
            Collections.sort(keys);
            for (Integer key : keys) {
                phases.get(key).start();
            }
        }
    }

    protected Map<String, MyLifecycle> getLifecycleBeans() {
        Map<String, MyLifecycle> beans = new LinkedHashMap<>();
        String[] beanNames = beanFactory.getBeanNamesForType(MyLifecycle.class, false);
        for (String beanName : beanNames) {
            Object bean = beanFactory.getBean(beanName);
            if (bean != this && bean instanceof MyLifecycle) {
                beans.put(beanName, (MyLifecycle) bean);
            }
        }
        return beans;
    }



    private static class MyLifecycleGroup {
        private final int phase;
        private final Map<String, ? extends MyLifecycle> lifecycleBeans;
        private final List<MyLifecycleGroupMember> members = new ArrayList<>();

        public MyLifecycleGroup(int phase, Map<String, ? extends MyLifecycle> lifecycleBeans) {
            this.phase = phase;
            this.lifecycleBeans = lifecycleBeans;
        }

        public void add(String name, MyLifecycle bean){
            this.members.add(new MyLifecycleGroupMember(name, bean));
        }

        public void start() {
            if (this.members.isEmpty()) {
                return;
            }

            Collections.sort(this.members);
            for (MyLifecycleGroupMember member : this.members) {
                MyLifecycle bean=this.lifecycleBeans.remove(member.name);
                bean.start();
            }
        }


    }

    private static class MyLifecycleGroupMember implements Comparable<MyLifecycleGroupMember> {
        private final String name;

        private final MyLifecycle bean;

        MyLifecycleGroupMember(String name, MyLifecycle bean) {
            this.name = name;
            this.bean = bean;
        }

        @Override
        public int compareTo(MyLifecycleGroupMember other) {
            int thisPhase = this.bean.getPhase();
            int otherPhase = other.bean.getPhase();
            return Integer.compare(thisPhase, otherPhase);
        }
    }
}
