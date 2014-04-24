package org.greencheek.web.filter.memcached.servlets;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Enumeration;

/**
 * Created by dominictootell on 20/04/2014.
 */
public class AddDateServlet extends HttpServlet {

    protected void doGet( HttpServletRequest request,
                          HttpServletResponse response)
            throws ServletException, IOException {

        long now = System.currentTimeMillis();
        response.addHeader("Cache-Control", "max-age=" + 3);
        response.addDateHeader("X-Now", now);
        StringBuilder b = new StringBuilder();
        Enumeration<String> headerNames = request.getHeaderNames();
        while(headerNames.hasMoreElements()) {
            b.append(headerNames.nextElement()).append("\r\n");
        }
        response.getWriter().write("<html><body>GET Date Header response, Time:(" + now + ") "+ b.toString()+"</body></html>");
    }
}
