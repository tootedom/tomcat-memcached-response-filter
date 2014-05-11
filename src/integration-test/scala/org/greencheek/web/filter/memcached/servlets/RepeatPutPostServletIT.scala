package org.greencheek.web.filter.memcached.servlets

import javax.servlet.http.{HttpServletResponse, HttpServletRequest, HttpServlet}
import java.io.IOException
import javax.servlet.ServletException
import org.greencheek.web.filter.memcached.io.IOUtils

/**
 * Created by dominictootell on 01/05/2014.
 */
class RepeatPutPostServletIT extends HttpServlet {

  @throws(classOf[IOException])
  @throws(classOf[ServletException])
  override protected def service(req: HttpServletRequest, resp: HttpServletResponse) : Unit  = {

    val now: Long = System.currentTimeMillis
    resp.addHeader("Cache-Control", "max-age=" + 3)
    resp.addDateHeader("X-Now", now)
    resp.addHeader("X-Method", req.getMethod)
    resp.getWriter.write("<html><body>GET Date Header response, content:(" + content(req) + ")</body></html>")
  }

  def content(request: HttpServletRequest): String = {
    return new String(IOUtils.readStreamToBytes(4096, request.getInputStream, request.getContentLength), "UTF-8")
  }
}
