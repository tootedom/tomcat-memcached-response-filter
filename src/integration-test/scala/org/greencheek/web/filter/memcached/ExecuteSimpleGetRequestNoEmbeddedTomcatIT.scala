package org.greencheek.web.filter.memcached

import com.excilys.ebi.gatling.core.Predef._
import com.excilys.ebi.gatling.http.Predef._
import akka.util.duration._
import bootstrap._
import com.excilys.ebi.gatling.core.structure.ScenarioBuilder

/**
 * Created by dominictootell on 01/05/2014.
 */
class ExecuteSimpleGetRequestNoEmbeddedTomcatIT extends Simulation {
  val filterInitParams: java.util.Map[String, String] = new java.util.HashMap[String, String](1, 1.0f)

  var url1 = System.getProperty("url1","http://localhost:8080/examples/servlets/servlet/HelloWorldExample")
  var url2 = System.getProperty("url2","http://localhost:8080/examples/servlets/servlet/RequestInfoExample")
  var url3 = System.getProperty("url3","http://localhost:8080/examples/servlets/servlet/RequestHeaderExample")
  var url4 = System.getProperty("url4","http://localhost:8080/examples/jsp/jsp2/el/basic-arithmetic.jsp")

  System.out.println(url1)


  val httpConf = httpConfig
    .acceptHeader("text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8")
    .acceptCharsetHeader("ISO-8859-1,utf-8;q=0.7,*;q=0.3")
    .acceptLanguageHeader("en-US,en;q=0.8,fr;q=0.6")
    .acceptEncodingHeader("gzip,deflate,sdch")
    .userAgentHeader("Mozilla/5.0 (Macintosh; Intel Mac OS X 10_7_5) AppleWebKit/537.11 (KHTML, like Gecko) Chrome/23.0.1271.64 Safari/537.11")


  val headers_1 = Map(
    "Connection" -> "Keep-Alive"
  )



  val scn1 = scenarioBuilder(url1);
  val scn2 = scenarioBuilder(url2);
  val scn3 = scenarioBuilder(url3);
  val scn4 = scenarioBuilder(url4);



  setUp(scn1.users(50).ramp(100).protocolConfig(httpConf),
    scn2.users(50).ramp(100).protocolConfig(httpConf),
    scn3.users(50).ramp(100).protocolConfig(httpConf),
    scn4.users(50).ramp(100).protocolConfig(httpConf));


  def scenarioBuilder(url : String ) : ScenarioBuilder = {
    scenario("Scenario Name " + url)
      .during(60 seconds) {
      exec(http("request_1")
        .get(url)
        .headers(headers_1)
        .header("Content-Type", "application/json")
        .check(status.is(200))
      )
    }
  }
}
