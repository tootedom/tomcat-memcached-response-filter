package org.greencheek.web.filter.memcached.servlets;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Created by dominictootell on 20/04/2014.
 */
public class NoCacheServlet extends HttpServlet {

    protected void doGet( HttpServletRequest request,
                          HttpServletResponse response)
            throws ServletException, IOException {


        response.addHeader("Cache-Control", "no-cache");
        response.getWriter().write("<html><body>GET NOCACHE response, Time:(" + System.currentTimeMillis() + ")</body></html>");
    }
}
