package org.hxm.myspring.myevent;

public class Pub {
    private EventBus eventBus;

    public Pub(){
        this.eventBus=new EventBus();
    }

    public void regist(Object observer){
        eventBus.register(observer);
    }

    public void pub(String s){
        eventBus.post(s);
    }

    public static void main(String[] args) {
        Pub p=new Pub();
        Sub s=new Sub();
        p.regist(s);
        p.pub("发布");
    }
}
