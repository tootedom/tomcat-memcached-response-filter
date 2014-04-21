package org.greencheek.web.filter.memcached.servlets;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Created by dominictootell on 20/04/2014.
 */
public class StatusCode404Servlet extends HttpServlet {

    public static final String HEADER = "X-Status";
    public static final String VALUE = "404";

    protected void doGet( HttpServletRequest request,
                          HttpServletResponse response)
            throws ServletException, IOException {

        long now = System.currentTimeMillis();
        response.addHeader("Cache-Control", "max-age=" + 3);
        response.setHeader(HEADER,VALUE);
        response.setStatus(404);
        response.getWriter().write("<html><body>GET Date Header response, Time:(" + now + ")</body></html>");
    }
}
