package org.greencheek.web.filter.memcached

import com.excilys.ebi.gatling.core.Predef._
import com.excilys.ebi.gatling.http.Predef._
import akka.util.duration._
import bootstrap._


/**
 * Created by dominictootell on 01/05/2014.
 */
class ExecuteLargeContentGetRequestIT extends Simulation {
  val filterInitParams: java.util.Map[String, String] = new java.util.HashMap[String, String](1, 1.0f)

  val server = new TomcatServerIT("/filter");
  server.setupServlet3Filter(System.getProperty("memcached.hosts","localhost:11211"), null, filterInitParams)
  var url: String = server.setupServlet("/large/*", "simple", "org.greencheek.web.filter.memcached.servlets.LargeContentFromFileServlet", false)
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
      .get("/hey")
      .headers(headers_1)
      .header("Content-Type","application/json")
      .check(status.is(200) )
    )
  }


  setUp(scn.users(150).ramp(100).protocolConfig(httpConf))
}
