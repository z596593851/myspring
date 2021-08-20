package org.hxm.myspring.factory;

import org.apache.catalina.*;
import org.apache.catalina.connector.Connector;
import org.apache.catalina.core.AprLifecycleListener;
import org.apache.catalina.loader.WebappLoader;
import org.apache.catalina.startup.Tomcat;
import org.apache.catalina.util.LifecycleBase;
import org.apache.catalina.webresources.AbstractResourceSet;
import org.apache.catalina.webresources.EmptyResource;
import org.apache.catalina.webresources.StandardRoot;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.tomcat.util.modeler.Registry;
import org.apache.tomcat.util.scan.StandardJarScanFilter;
import org.hxm.myspring.utils.MyClassUtil;
import org.hxm.myspring.web.*;
import org.springframework.boot.web.embedded.tomcat.TomcatEmbeddedWebappClassLoader;
import org.springframework.boot.web.servlet.server.Jsp;
import org.springframework.util.ReflectionUtils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.*;
import java.util.stream.Collectors;

public class MyTomcatServletWebServerFactory {
    private final StaticResourceJars staticResourceJars = new StaticResourceJars();
    private Map<Locale, Charset> localeCharsetMappings = new HashMap<>();
    private boolean registerDefaultServlet = true;
    protected final Log logger = LogFactory.getLog(getClass());
    private String displayName="application";
    private String contextPath = "";
    private static final Charset DEFAULT_CHARSET = StandardCharsets.UTF_8;
    public static final String DEFAULT_PROTOCOL = "org.apache.coyote.http11.Http11NioProtocol";
    private int port = 8080;
    private File baseDirectory;
    private boolean disableMBeanRegistry = true;
    private String protocol = DEFAULT_PROTOCOL;
    private Charset uriEncoding = DEFAULT_CHARSET;
    private final MyDocumentRoot documentRoot = new MyDocumentRoot(this.logger);
    private Jsp jsp = new Jsp();
    private static final Set<Class<?>> NO_CLASSES = Collections.emptySet();private List<LifecycleListener> contextLifecycleListeners = getDefaultLifecycleListeners();

    public MyTomcatWebServer getWebServer(MyServletContextInitializer... initializers){
        if (this.disableMBeanRegistry) {
            Registry.disableRegistry();
        }
        Tomcat tomcat = new Tomcat();
        File baseDir = (this.baseDirectory != null) ? this.baseDirectory : createTempDir("tomcat");
        tomcat.setBaseDir(baseDir.getAbsolutePath());
        Connector connector = new Connector(this.protocol);
        connector.setThrowOnFailure(true);
        tomcat.getService().addConnector(connector);
        customizeConnector(connector);
        tomcat.setConnector(connector);
        tomcat.getHost().setAutoDeploy(false);
        tomcat.getEngine().setBackgroundProcessorDelay(10);
        prepareContext(tomcat.getHost(), initializers);
        return getTomcatWebServer(tomcat);
    }

    protected void prepareContext(Host host, MyServletContextInitializer[] initializers) {
        File documentRoot = getValidDocumentRoot();
        MyTomcatEmbeddedContext context = new MyTomcatEmbeddedContext();
        if (documentRoot != null) {
            context.setResources(new LoaderHidingResourceRoot(context));
        }
        context.setName(getContextPath());
        context.setDisplayName(getDisplayName());
        context.setPath(getContextPath());
        File docBase = (documentRoot != null) ? documentRoot : createTempDir("tomcat-docbase");
        context.setDocBase(docBase.getAbsolutePath());
        context.addLifecycleListener(new Tomcat.FixContextListener());
        context.setParentClassLoader(MyClassUtil.getDefaultClassLoader());
        resetDefaultLocaleMapping(context);
        addLocaleMappings(context);
        try {
            context.setCreateUploadTargets(true);
        }
        catch (NoSuchMethodError ex) {
            // Tomcat is < 8.5.39. Continue.
        }
//        configureTldPatterns(context);
        WebappLoader loader = new WebappLoader();
        loader.setLoaderClass(TomcatEmbeddedWebappClassLoader.class.getName());
        loader.setDelegate(true);
        context.setLoader(loader);
        if (isRegisterDefaultServlet()) {
            addDefaultServlet(context);
        }
        context.addLifecycleListener(new StaticResourceConfigurer(context));
        host.addChild(context);
        configureContext(context, initializers);
//        postProcessContext(context);
    }

    private static List<LifecycleListener> getDefaultLifecycleListeners() {
        AprLifecycleListener aprLifecycleListener = new AprLifecycleListener();
        return AprLifecycleListener.isAprAvailable() ? new ArrayList<>(Arrays.asList(aprLifecycleListener))
                : new ArrayList<>();
    }

    protected void configureContext(Context context, MyServletContextInitializer[] initializers) {
        MyTomcatStarter starter = new MyTomcatStarter(initializers);
        if (context instanceof MyTomcatEmbeddedContext) {
            MyTomcatEmbeddedContext embeddedContext = (MyTomcatEmbeddedContext) context;
            embeddedContext.setStarter(starter);
            embeddedContext.setFailCtxIfServletStartFails(true);
        }
        context.addServletContainerInitializer(starter, NO_CLASSES);
        for (LifecycleListener lifecycleListener : this.contextLifecycleListeners) {
            context.addLifecycleListener(lifecycleListener);
        }
    }

    private final class StaticResourceConfigurer implements LifecycleListener {

        private final Context context;

        private StaticResourceConfigurer(Context context) {
            this.context = context;
        }

        @Override
        public void lifecycleEvent(LifecycleEvent event) {
            if (event.getType().equals(Lifecycle.CONFIGURE_START_EVENT)) {
                addResourceJars(getUrlsOfJarsWithMetaInfResources());
            }
        }

        protected final List<URL> getUrlsOfJarsWithMetaInfResources() {
            return staticResourceJars.getUrls();
        }

        private void addResourceJars(List<URL> resourceJarUrls) {
            for (URL url : resourceJarUrls) {
                String path = url.getPath();
                if (path.endsWith(".jar") || path.endsWith(".jar!/")) {
                    String jar = url.toString();
                    if (!jar.startsWith("jar:")) {
                        // A jar file in the file system. Convert to Jar URL.
                        jar = "jar:" + jar + "!/";
                    }
                    addResourceSet(jar);
                }
                else {
                    addResourceSet(url.toString());
                }
            }
        }

        private void addResourceSet(String resource) {
            try {
                if (isInsideNestedJar(resource)) {
                    // It's a nested jar but we now don't want the suffix because Tomcat
                    // is going to try and locate it as a root URL (not the resource
                    // inside it)
                    resource = resource.substring(0, resource.length() - 2);
                }
                URL url = new URL(resource);
                String path = "/META-INF/resources";
                this.context.getResources().createWebResourceSet(WebResourceRoot.ResourceSetType.RESOURCE_JAR, "/", url, path);
            }
            catch (Exception ex) {
                // Ignore (probably not a directory)
            }
        }

        private boolean isInsideNestedJar(String dir) {
            return dir.indexOf("!/") < dir.lastIndexOf("!/");
        }

    }

    private void addDefaultServlet(Context context) {
        Wrapper defaultServlet = context.createWrapper();
        defaultServlet.setName("default");
        defaultServlet.setServletClass("org.apache.catalina.servlets.DefaultServlet");
        defaultServlet.addInitParameter("debug", "0");
        defaultServlet.addInitParameter("listings", "false");
        defaultServlet.setLoadOnStartup(1);
        // Otherwise the default location of a Spring DispatcherServlet cannot be set
        defaultServlet.setOverridable(true);
        context.addChild(defaultServlet);
        context.addServletMappingDecoded("/", "default");
    }

    public boolean isRegisterDefaultServlet() {
        return this.registerDefaultServlet;
    }

    private void addLocaleMappings(MyTomcatEmbeddedContext context) {
        getLocaleCharsetMappings().forEach(
                (locale, charset) -> context.addLocaleEncodingMappingParameter(locale.toString(), charset.toString()));
    }

    private void resetDefaultLocaleMapping(MyTomcatEmbeddedContext context) {
        context.addLocaleEncodingMappingParameter(Locale.ENGLISH.toString(), DEFAULT_CHARSET.displayName());
        context.addLocaleEncodingMappingParameter(Locale.FRENCH.toString(), DEFAULT_CHARSET.displayName());
    }

    private static final class LoaderHidingResourceRoot extends StandardRoot {

        private LoaderHidingResourceRoot(MyTomcatEmbeddedContext context) {
            super(context);
        }

        @Override
        protected WebResourceSet createMainResourceSet() {
            return new LoaderHidingWebResourceSet(super.createMainResourceSet());
        }

    }

    private static final class LoaderHidingWebResourceSet extends AbstractResourceSet {

        private final WebResourceSet delegate;

        private final Method initInternal;

        private LoaderHidingWebResourceSet(WebResourceSet delegate) {
            this.delegate = delegate;
            try {
                this.initInternal = LifecycleBase.class.getDeclaredMethod("initInternal");
                this.initInternal.setAccessible(true);
            }
            catch (Exception ex) {
                throw new IllegalStateException(ex);
            }
        }

        @Override
        public WebResource getResource(String path) {
            if (path.startsWith("/org/hxm/myspring")) {
                return new EmptyResource(getRoot(), path);
            }
            return this.delegate.getResource(path);
        }

        @Override
        public String[] list(String path) {
            return this.delegate.list(path);
        }

        @Override
        public Set<String> listWebAppPaths(String path) {
            return this.delegate.listWebAppPaths(path).stream()
                    .filter((webAppPath) -> !webAppPath.startsWith("/org/springframework/boot"))
                    .collect(Collectors.toSet());
        }

        @Override
        public boolean mkdir(String path) {
            return this.delegate.mkdir(path);
        }

        @Override
        public boolean write(String path, InputStream is, boolean overwrite) {
            return this.delegate.write(path, is, overwrite);
        }

        @Override
        public URL getBaseUrl() {
            return this.delegate.getBaseUrl();
        }

        @Override
        public void setReadOnly(boolean readOnly) {
            this.delegate.setReadOnly(readOnly);
        }

        @Override
        public boolean isReadOnly() {
            return this.delegate.isReadOnly();
        }

        @Override
        public void gc() {
            this.delegate.gc();
        }

        @Override
        protected void initInternal() throws LifecycleException {
            if (this.delegate instanceof LifecycleBase) {
                try {
                    ReflectionUtils.invokeMethod(this.initInternal, this.delegate);
                }
                catch (Exception ex) {
                    throw new LifecycleException(ex);
                }
            }
        }

    }


    protected final File getValidDocumentRoot() {
        return this.documentRoot.getValidDirectory();
    }

    private void skipAllTldScanning(MyTomcatEmbeddedContext context) {
        StandardJarScanFilter filter = new StandardJarScanFilter();
        filter.setTldSkip("*.jar");
        context.getJarScanner().setJarScanFilter(filter);
    }

    protected MyTomcatWebServer getTomcatWebServer(Tomcat tomcat) {
        return new MyTomcatWebServer(tomcat, getPort() >= 0);
    }

    protected void customizeConnector(Connector connector) {
        int port = Math.max(getPort(), 0);
        connector.setPort(port);
        if (getUriEncoding() != null) {
            connector.setURIEncoding(getUriEncoding().name());
        }
        // Don't bind to the socket prematurely if ApplicationContext is slow to start
        connector.setProperty("bindOnInit", "false");
    }



    protected final File createTempDir(String prefix) {
        try {
            File tempDir = Files.createTempDirectory(prefix + "." + getPort() + ".").toFile();
            tempDir.deleteOnExit();
            return tempDir;
        } catch (IOException ex) {
            throw new RuntimeException(
                    "Unable to create tempDir. java.io.tmpdir is set to " + System.getProperty("java.io.tmpdir"), ex);
        }
    }

    public int getPort(){
        return this.port;
    }

    public Charset getUriEncoding() {
        return this.uriEncoding;
    }

    public String getContextPath() {
        return this.contextPath;
    }

    public String getDisplayName() {
        return this.displayName;
    }

    public Map<Locale, Charset> getLocaleCharsetMappings() {
        return this.localeCharsetMappings;
    }
}
