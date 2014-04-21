package org.greencheek.web.filter.memcached.servlets;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Created by dominictootell on 20/04/2014.
 */
public class AddIntHeaderServlet extends HttpServlet {

    public static final String HEADER = "X-Int";
    public static final int VALUE = 10;

    protected void doGet( HttpServletRequest request,
                          HttpServletResponse response)
            throws ServletException, IOException {

        long now = System.currentTimeMillis();
        response.setHeader("Cache-Control", "max-age=" + 3);
        response.addIntHeader(HEADER,VALUE);
        response.getWriter().write("<html><body>GET Date Header response, Time:(" + now + ")</body></html>");
    }
}
