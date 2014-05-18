package org.greencheek.web.filter.memcached;

import com.ning.http.client.AsyncHttpClient;
import com.ning.http.client.AsyncHttpClientConfig;
import org.apache.catalina.Context;
import org.apache.catalina.LifecycleException;
import org.apache.catalina.LifecycleState;
import org.apache.catalina.Wrapper;
import org.apache.catalina.core.StandardContext;
import org.apache.catalina.deploy.FilterDef;
import org.apache.catalina.deploy.FilterMap;
import org.apache.catalina.startup.Tomcat;
import org.apache.naming.resources.VirtualDirContext;

import java.io.File;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by dominictootell on 20/04/2014.
 */
public class EmbeddedTomcatServer {
    private final static String mWorkingDir = System.getProperty("java.io.tmpdir");
    private final static File  docBase = new File(mWorkingDir);
    private final static String DEFAULT_FILTER_PATTER = "/*";
    private final static String DEFAULT_SERVLET2_FILTER = "org.greencheek.web.filter.memcached.PublishToMemcachedFilter";
    private final static String DEFAULT_SERVLET3_FILTER = "org.greencheek.web.filter.memcached.Servlet3PublishToMemcachedFilter";
    private final static String DEFAULT_HYSTRIX_FILTER = "org.greencheek.web.filter.memcached.HystrixPublishToMemcachedFilter";

    private volatile Tomcat tomcat;
    private String contextName;
    private Context ctx;
    private AsyncHttpClient asyncHttpClient;

    public final void shutdownTomcat()  {
        try {
            if (tomcat != null && tomcat.getServer() != null
                    && tomcat.getServer().getState() != LifecycleState.DESTROYED) {
                if (tomcat.getServer().getState() != LifecycleState.STOPPED) {
                    tomcat.stop();
                }
                tomcat.destroy();
                tomcat.getServer().await();
            }
        } catch (Exception e) {

        }


        if(asyncHttpClient!=null) {
            asyncHttpClient.close();
        }
    }


    public void setupServlet2Filter(String memcachedUrl) {
        setupServlet2Filter(memcachedUrl,null, null);
    }

    public void setupServlet3Filter(String memcachedUrl) {
        setupServlet3Filter(memcachedUrl,null, null);
    }


    public void setupServlet2Filter(String memcachedUrl,String url,Map<String,String> filterConfig) {
        setupFilter(memcachedUrl,"memcached-s2filter",DEFAULT_SERVLET2_FILTER,url,false, filterConfig);
    }

    public void setupServlet3Filter(String memcachedUrl,String name,String url,Map<String,String> filterConfig) {
        setupFilter(memcachedUrl,name,DEFAULT_SERVLET3_FILTER,url,true, filterConfig);
    }

    public void setupServlet3Filter(String memcachedUrl,String url,Map<String,String> filterConfig) {
        setupFilter(memcachedUrl,"memcached-s3filter",DEFAULT_SERVLET3_FILTER,url,true, filterConfig);
    }

    public void setupServletHystrixFilter(String memcachedUrl,String name,String url,Map<String,String> filterConfig) {
        setupFilter(memcachedUrl,name,DEFAULT_HYSTRIX_FILTER,url,true, filterConfig);
    }

    public void setupServletHystrixFilter(String memcachedUrl,String url,Map<String,String> filterConfig) {
        setupFilter(memcachedUrl,"memcached-hystrixfilter",DEFAULT_HYSTRIX_FILTER,url,true, filterConfig);
    }




    protected int getTomcatPort() {
        return tomcat.getConnector().getLocalPort();
    }

    public String setupServlet(String url, String name, String className, boolean async) {

        Wrapper wrapper = tomcat.addServlet(ctx,name,className);
        wrapper.setAsyncSupported(async);
        wrapper.addMapping(url);

        String httpUrl =  "http://localhost:{PORT}";
        if(contextName.startsWith("/")) {
            httpUrl += contextName;
        } else {
            httpUrl += "/" + contextName;
        }

        if(url.startsWith("/")) {
            httpUrl += url;
        } else {
            httpUrl += "/" + url;

        }
        httpUrl = httpUrl.replaceAll("^(.*)/\\*$","$1");
        return httpUrl;
    }

    public String replacePort(String url) {
        return url.replace("{PORT}",""+getTomcatPort());
    }

    public void setupFilter(String memcachedUrl,String filterName, String filterClass, String urlPatten, boolean async,Map<String,String> initParams) {
        if(urlPatten==null) {
            urlPatten = DEFAULT_FILTER_PATTER;
        }
        if(initParams==null) {
            initParams = new HashMap<String,String>();
        }

        initParams.put(PublishToMemcachedFilter.MEMCACHED_HOSTS_PARAM,memcachedUrl);


        FilterDef filter = createFilterDef(filterName,filterClass);
        FilterMap filterMapping = createFilterMap(filterName,urlPatten);

        if(initParams!=null) {
           for(Map.Entry<String,String> entry : initParams.entrySet()) {
               filter.addInitParameter(entry.getKey(),entry.getValue());
           }
        }
        filter.setAsyncSupported(async ? "true" : "false");
        ctx.addFilterDef(filter);
        ctx.addFilterMap(filterMapping);
    }

    private FilterDef createFilterDef(String filterName, String filterClass) {
        FilterDef filterDef = new FilterDef();
        filterDef.setFilterName(filterName);
        filterDef.setFilterClass(filterClass);
        return filterDef;
    }

    private FilterMap createFilterMap(String filterName, String urlPattern) {
        FilterMap filterMap = new FilterMap();
        filterMap.setFilterName(filterName);
        filterMap.addURLPattern(urlPattern);
        filterMap.setDispatcher("ASYNC");
        filterMap.setDispatcher("REQUEST");
        filterMap.setDispatcher("FORWARD");

        System.out.println(Arrays.toString(filterMap.getDispatcherNames()));
        return filterMap;
    }

    public void setupTomcat(String context) {
        this.contextName = context;
//        String webappDirLocation = "src/main/webapp/";
        tomcat = new Tomcat();
        tomcat.setPort(0);
        tomcat.setBaseDir(mWorkingDir);
        tomcat.getHost().setAppBase(mWorkingDir);
        tomcat.getHost().setAutoDeploy(true);

        ctx = tomcat.addContext(tomcat.getHost(),context,contextName.replace("/",""),docBase.getAbsolutePath());

        ctx.setCrossContext(true);
        ctx.setPath(contextName);
        ((StandardContext)ctx).setProcessTlds(false);  // disable tld processing.. we don't use any
        ctx.addParameter("com.sun.faces.forceLoadConfiguration","false");

        //declare an alternate location for your "WEB-INF/classes" dir:
        File additionWebInfClasses = new File("target/classes");
        VirtualDirContext resources = new VirtualDirContext();
        resources.setExtraResourcePaths("/WEB-INF/classes=" + additionWebInfClasses);
        ctx.setResources(resources);



//
        AsyncHttpClientConfig.Builder b = new AsyncHttpClientConfig.Builder();
        b.setRequestTimeoutInMs(10000);
        b.setConnectionTimeoutInMs(5000);
        b.setMaxRequestRetry(0);
        asyncHttpClient = new AsyncHttpClient(b.build());
//        searchurl = "http://localhost:" + getTomcatPort() +"/search/frequentlyrelatedto";

    }

    public boolean startTomcat() {
        try {
            tomcat.start();
            return true;
        } catch (LifecycleException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            return false;
        }
    }


    public AsyncHttpClient getHttpClient() {
        return asyncHttpClient;
    }
}
