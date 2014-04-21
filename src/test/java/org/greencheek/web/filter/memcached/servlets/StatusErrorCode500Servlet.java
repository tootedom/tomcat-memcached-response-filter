package org.greencheek.web.filter.memcached.servlets;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Created by dominictootell on 20/04/2014.
 */
public class StatusErrorCode500Servlet extends HttpServlet {

    public static final String HEADER = "X-Status";
    public static final String VALUE = "500";
    public static final String ERROR_MSG = "Contract an Administrator";

    protected void doGet( HttpServletRequest request,
                          HttpServletResponse response)
            throws ServletException, IOException {

        long now = System.currentTimeMillis();
        response.setHeader("Cache-Control", "max-age=" + 3);
        response.setHeader(HEADER, VALUE);
        response.sendError(500,ERROR_MSG);
        response.getWriter().write("<html><body>GET Date Header response, Time:(" + now + ")</body></html>");
    }
}
