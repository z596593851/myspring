package org.hxm.myspring.myevent;

public class Sub {

    @Subscribe
    public void handle1(String s){
        System.out.println("订阅者："+s);
    }

    @Subscribe
    public void handle2(String s1,String s2){
        System.out.println("订阅者："+s1+","+"s2");
    }
}
