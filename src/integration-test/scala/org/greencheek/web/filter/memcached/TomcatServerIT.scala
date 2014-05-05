package org.greencheek.web.filter.memcached

import java.io.File


import org.apache.catalina.Context
import org.apache.catalina.LifecycleException
import org.apache.catalina.LifecycleState
import org.apache.catalina.Wrapper
import org.apache.catalina.core.StandardContext
import org.apache.catalina.deploy.FilterDef
import org.apache.catalina.deploy.FilterMap
import org.apache.catalina.startup.Tomcat
import org.apache.naming.resources.VirtualDirContext
import java.io.File
import java.util.Arrays
import java.util.HashMap
import java.util.Map

/**
 * Created by dominictootell on 01/05/2014.
 */
class TomcatServerIT(context: String) {
  private final val mWorkingDir: String = System.getProperty("java.io.tmpdir")
  private final val docBase: File = new File(mWorkingDir)
  private final val DEFAULT_FILTER_PATTER: String = "/*"
  private final val DEFAULT_SERVLET2_FILTER: String = "org.greencheek.web.filter.memcached.PublishToMemcachedFilter"
  private final val DEFAULT_SERVLET3_FILTER: String = "org.greencheek.web.filter.memcached.Servlet3PublishToMemcachedFilter"

  @volatile private var tomcat: Tomcat = null
  private var contextName: String = null
  private var ctx: Context = null

  this.contextName = context
  tomcat = new Tomcat
  tomcat.setPort(0)
  tomcat.setBaseDir(mWorkingDir)
  tomcat.getHost.setAppBase(mWorkingDir)
  tomcat.getHost.setAutoDeploy(true)
  ctx = tomcat.addContext(tomcat.getHost, context, contextName.replace("/", ""), docBase.getAbsolutePath)
  ctx.setCrossContext(true)
  ctx.setPath(contextName)
  (ctx.asInstanceOf[StandardContext]).setProcessTlds(false)
  ctx.addParameter("com.sun.faces.forceLoadConfiguration", "false")
  val additionWebInfClasses: File = new File("target/classes")
  val resources: VirtualDirContext = new VirtualDirContext
  resources.setExtraResourcePaths("/WEB-INF/classes=" + additionWebInfClasses)
  ctx.setResources(resources)



  def shutdownTomcat {
    try {
      if (tomcat != null && tomcat.getServer != null && tomcat.getServer.getState != LifecycleState.DESTROYED) {
        if (tomcat.getServer.getState ne LifecycleState.STOPPED) {
          tomcat.stop
        }
        tomcat.destroy
        tomcat.getServer.await
      }
    }
    catch {
      case e: Exception => {
      }
    }

  }

  def setupServlet2Filter(memcachedUrl: String) {
    setupServlet2Filter(memcachedUrl, null, null)
  }

  def setupServlet3Filter(memcachedUrl: String) {
    setupServlet3Filter(memcachedUrl, null, null)
  }

  def setupServlet2Filter(memcachedUrl: String, url: String, filterConfig: Map[String, String]) {
    setupFilter(memcachedUrl, "memcached-s2filter", DEFAULT_SERVLET2_FILTER, url, false, filterConfig)
  }

  def setupServlet3Filter(memcachedUrl: String, name: String, url: String, filterConfig: Map[String, String]) {
    setupFilter(memcachedUrl, name, DEFAULT_SERVLET3_FILTER, url, true, filterConfig)
  }

  def setupServlet3Filter(memcachedUrl: String, url: String, filterConfig: Map[String, String]) {
    setupFilter(memcachedUrl, "memcached-s3filter", DEFAULT_SERVLET3_FILTER, url, true, filterConfig)
  }

  protected def getTomcatPort: Int = {
    return tomcat.getConnector.getLocalPort
  }

  def setupServlet(url: String, name: String, className: String, async: Boolean): String = {

    val wrapper: Wrapper = Tomcat.addServlet(ctx, name, className)
    wrapper.setAsyncSupported(async)
    wrapper.addMapping(url)
    var httpUrl: String = "http://localhost:{PORT}"
    if (contextName.startsWith("/")) {
      httpUrl += contextName
    }
    else {
      httpUrl += "/" + contextName
    }
    if (url.startsWith("/")) {
      httpUrl += url
    }
    else {
      httpUrl += "/" + url
    }
    httpUrl = httpUrl.replaceAll("^(.*)/\\*$", "$1")
    return httpUrl
  }

  def replacePort(url: String): String = {
    return url.replace("{PORT}", "" + getTomcatPort)
  }

  def setupFilter(memcachedUrl: String, filterName: String, filterClass: String, urlMatchingPattern: String, async: Boolean, filterParams: Map[String, String]) {
    var urlPattern : String = urlMatchingPattern
    var initParams : Map[String,String] = filterParams
    if (urlPattern == null) {
      urlPattern = DEFAULT_FILTER_PATTER
    }
    if (initParams == null) {
      initParams = new HashMap[String, String]
    }
    initParams.put(PublishToMemcachedFilter.MEMCACHED_HOSTS_PARAM, memcachedUrl)
    val filter: FilterDef = createFilterDef(filterName, filterClass)
    val filterMapping: FilterMap = createFilterMap(filterName, urlPattern)
    if (initParams != null) {
      import scala.collection.JavaConversions._
      for (entry <- initParams.entrySet) {
        filter.addInitParameter(entry.getKey, entry.getValue)
      }
    }
    filter.setAsyncSupported(if (async) "true" else "false")
    ctx.addFilterDef(filter)
    ctx.addFilterMap(filterMapping)
  }

  private def createFilterDef(filterName: String, filterClass: String): FilterDef = {
    val filterDef: FilterDef = new FilterDef
    filterDef.setFilterName(filterName)
    filterDef.setFilterClass(filterClass)
    return filterDef
  }

  private def createFilterMap(filterName: String, urlPattern: String): FilterMap = {
    val filterMap: FilterMap = new FilterMap
    filterMap.setFilterName(filterName)
    filterMap.addURLPattern(urlPattern)
    filterMap.setDispatcher("ASYNC")
    filterMap.setDispatcher("REQUEST")
    filterMap.setDispatcher("FORWARD")
    return filterMap
  }

  def setupTomcat(context: String) {

  }

  def startTomcat: Boolean = {
    try {
      tomcat.start
      return true
    }
    catch {
      case e: LifecycleException => {
        e.printStackTrace
        return false
      }
    }
  }



}
