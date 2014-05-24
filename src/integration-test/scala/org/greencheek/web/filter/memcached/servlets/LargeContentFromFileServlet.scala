package org.greencheek.web.filter.memcached.servlets

import java.io.{File, IOException}
import javax.servlet.ServletException
import javax.servlet.http.{HttpServlet, HttpServletResponse, HttpServletRequest}
import org.apache.commons.io.FileUtils

/**
 * Created by dominictootell on 22/05/2014.
 */
class LargeContentFromFileServlet extends HttpServlet {

  val content: Array[Byte] = FileUtils.readFileToByteArray(new File(Thread.currentThread.getContextClassLoader.getResource("relatedcontent.txt").getFile))
  var age: Int = 3

  @throws(classOf[IOException])
  @throws(classOf[ServletException])
  override protected def service(req: HttpServletRequest, resp: HttpServletResponse) : Unit  = {
    resp.addHeader("X-LastTime", "" + System.currentTimeMillis)
    resp.addHeader("Cache-Control", "max-age=" + age)
    resp.getOutputStream.write(content)
  }
}
