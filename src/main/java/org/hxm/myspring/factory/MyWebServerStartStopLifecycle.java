package org.hxm.myspring.factory;

import org.hxm.myspring.web.MyTomcatWebServer;

public class MyWebServerStartStopLifecycle implements MyLifecycle{

    private MyApplicationContext applicationContext;
    private MyTomcatWebServer webServer;
    private volatile boolean running;

    public MyWebServerStartStopLifecycle(MyApplicationContext applicationContext, MyTomcatWebServer webServer){
        this.applicationContext=applicationContext;
        this.webServer=webServer;
    }

    @Override
    public void start() {
        this.webServer.start();
        this.running = true;
    }

    @Override
    public int getPhase() {
        return 0;
    }
}
