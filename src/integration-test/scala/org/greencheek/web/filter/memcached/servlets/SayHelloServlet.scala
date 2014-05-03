package org.greencheek.web.filter.memcached.servlets

import javax.servlet.http.{HttpServletResponse, HttpServletRequest, HttpServlet}
import java.io.IOException
import javax.servlet.ServletException

/**
 * Created by dominictootell on 01/05/2014.
 */
class SayHelloServlet extends HttpServlet {

  @throws(classOf[IOException])
  @throws(classOf[ServletException])
  override protected def service(req: HttpServletRequest, resp: HttpServletResponse) : Unit  = {
    resp.setContentType("text/plain")
    resp.getWriter.println("hello")
  }
}
