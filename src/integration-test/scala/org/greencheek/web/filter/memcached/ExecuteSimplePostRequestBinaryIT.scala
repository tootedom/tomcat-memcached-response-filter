package org.greencheek.web.filter.memcached

import com.excilys.ebi.gatling.core.Predef._
import com.excilys.ebi.gatling.http.Predef._
import akka.util.duration._
import bootstrap._
import org.greencheek.web.filter.memcached.client.config.CacheConfigGlobals

/**
 * Created by dominictootell on 01/05/2014.
 */
class ExecuteSimplePostRequestBinaryIT extends Simulation {
  val filterInitParams: java.util.Map[String, String] = new java.util.HashMap[String, String](1, 1.0f)
  filterInitParams.put(PublishToMemcachedFilter.MEMCACHED_USE_BINARY,"true")
  filterInitParams.put(PublishToMemcachedFilter.MEMCACHED_MAX_POST_BODY_SIZE,"5526")
  filterInitParams.put(PublishToMemcachedFilter.MEMCACHED_INITIAL_POST_BODY_SIZE,"5526")
  filterInitParams.put(PublishToMemcachedFilter.MEMCACHED_ESTIMATED_CACHE_KEY_SIZE,"6144")
  filterInitParams.put(PublishToMemcachedFilter.MEMCACHED_CACHEABLE_METHODS,"post,get")
  filterInitParams.put(PublishToMemcachedFilter.MEMCACHED_KEY_PARAM,CacheConfigGlobals.DEFAULT_CACHE_KEY+"$body")

  val content5526bytes : String = "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"

  val server = new TomcatServerIT("/filter");
  server.setupServlet3Filter(System.getProperty("memcached.hosts","localhost:11211"), null, filterInitParams)
  var url: String = server.setupServlet("/simple/*", "simple", "org.greencheek.web.filter.memcached.servlets.RepeatPutPostServletIT", false)
  server.startTomcat
  url = server.replacePort(url)
  System.out.println(url)

  sys.ShutdownHookThread {
    server.shutdownTomcat
  }

  val httpConf = httpConfig
    .baseURL(url)
    .acceptHeader("text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8")
    .acceptCharsetHeader("ISO-8859-1,utf-8;q=0.7,*;q=0.3")
    .acceptLanguageHeader("en-US,en;q=0.8,fr;q=0.6")
    .acceptEncodingHeader("gzip,deflate,sdch")
    .userAgentHeader("Mozilla/5.0 (Macintosh; Intel Mac OS X 10_7_5) AppleWebKit/537.11 (KHTML, like Gecko) Chrome/23.0.1271.64 Safari/537.11")

  val headers_1 = Map(
    "Connection" -> "Keep-Alive"
  )

  val scn = scenario("Scenario Name")
    .during(60 seconds) {
    exec(http("request_1")
      .post("/hey")
      .body(content5526bytes)
      .headers(headers_1)
      .header("Content-Type","application/json")
      .check(status.is(200) )
    )
  }


  setUp(scn.users(150).ramp(100).protocolConfig(httpConf))
}