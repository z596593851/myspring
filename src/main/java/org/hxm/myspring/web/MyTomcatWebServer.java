package org.hxm.myspring.web;

import org.apache.catalina.*;
import org.apache.catalina.connector.Connector;
import org.apache.catalina.startup.Tomcat;
import org.apache.naming.ContextBindings;

import javax.naming.NamingException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

public class MyTomcatWebServer {
    private final Tomcat tomcat;
    private final boolean autoStart;
    private volatile boolean started;
    private static final AtomicInteger containerCounter = new AtomicInteger(-1);
    private final Map<Service, Connector[]> serviceConnectors = new HashMap<>();

    public MyTomcatWebServer(Tomcat tomcat,boolean autoStart){
        this.tomcat = tomcat;
        this.autoStart = autoStart;
        initialize();
    }

    private void initialize(){
        try {
            addInstanceIdToEngineName();
            Context context = findContext();
            context.addLifecycleListener((event) -> {
                if (context.equals(event.getSource()) && Lifecycle.START_EVENT.equals(event.getType())) {
                    // Remove service connectors so that protocol binding doesn't
                    // happen when the service is started.
                    removeServiceConnectors();
                }
            });
            this.tomcat.start();
            try {
                ContextBindings.bindClassLoader(context, context.getNamingToken(), getClass().getClassLoader());
            }
            catch (NamingException ex) {
                // Naming is not enabled. Continue
            }
            startDaemonAwaitThread();
        } catch (LifecycleException e) {
            e.printStackTrace();
        }
    }

    private void startDaemonAwaitThread() {
        Thread awaitThread = new Thread("container-" + (containerCounter.get())) {

            @Override
            public void run() {
                MyTomcatWebServer.this.tomcat.getServer().await();
            }

        };
        awaitThread.setContextClassLoader(getClass().getClassLoader());
        awaitThread.setDaemon(false);
        awaitThread.start();
    }


    private void removeServiceConnectors() {
        for (Service service : this.tomcat.getServer().findServices()) {
            Connector[] connectors = service.findConnectors().clone();
            this.serviceConnectors.put(service, connectors);
            for (Connector connector : connectors) {
                service.removeConnector(connector);
            }
        }
    }

    private Context findContext() {
        for (Container child : this.tomcat.getHost().findChildren()) {
            if (child instanceof Context) {
                return (Context) child;
            }
        }
        throw new IllegalStateException("The host does not contain a Context");
    }
    private void addInstanceIdToEngineName() {
        int instanceId = containerCounter.incrementAndGet();
        if (instanceId > 0) {
            Engine engine = this.tomcat.getEngine();
            engine.setName(engine.getName() + "-" + instanceId);
        }
    }

    private void stopProtocolHandler(Connector connector) {
        try {
            connector.getProtocolHandler().stop();
        }
        catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void addPreviouslyRemovedConnectors() {
        Service[] services = this.tomcat.getServer().findServices();
        for (Service service : services) {
            Connector[] connectors = this.serviceConnectors.get(service);
            if (connectors != null) {
                for (Connector connector : connectors) {
                    service.addConnector(connector);
                    if (!this.autoStart) {
                        stopProtocolHandler(connector);
                    }
                }
                this.serviceConnectors.remove(service);
            }
        }
    }

    private void performDeferredLoadOnStartup() {
        try {
            for (Container child : this.tomcat.getHost().findChildren()) {
                if (child instanceof MyTomcatEmbeddedContext) {
                    ((MyTomcatEmbeddedContext) child).deferredLoadOnStartup();
                }
            }
        }
        catch (Exception ex) {
            ex.printStackTrace();
        }
    }
    private void checkThatConnectorsHaveStarted() {
        checkConnectorHasStarted(this.tomcat.getConnector());
        for (Connector connector : this.tomcat.getService().findConnectors()) {
            checkConnectorHasStarted(connector);
        }
    }

    private void checkConnectorHasStarted(Connector connector) {
        if (LifecycleState.FAILED.equals(connector.getState())) {
            throw new RuntimeException("error port:"+connector.getPort());
        }
    }

    public void start(){
        if (this.started) {
            return;
        }
        addPreviouslyRemovedConnectors();
        Connector connector = this.tomcat.getConnector();
        if (connector != null && this.autoStart) {
            performDeferredLoadOnStartup();
        }
        checkThatConnectorsHaveStarted();
        this.started = true;

    }
}
