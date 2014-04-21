package org.greencheek.web.filter.memcached.servlets;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;

/**
 * Created by dominictootell on 20/04/2014.
 */
public class JSESSIONIDServlet extends HttpServlet {

    public static final String HEADER = "SESSION_ID";

    protected void doGet( HttpServletRequest request,
                          HttpServletResponse response)
            throws ServletException, IOException {

        HttpSession session = request.getSession(true);

        long now = System.currentTimeMillis();
        if(session.getAttribute(HEADER)==null) {
            session.setAttribute(HEADER, now);
        } else {
            now = ((Long)session.getAttribute(HEADER)).longValue();
        }
        response.setHeader(HEADER,session.getId());
        response.addHeader("Cache-Control", "max-age=" + 3);
        response.addDateHeader("X-Now",now);
        response.getWriter().write("<html><body>GET Date Header response, Time:(" + now + "),Session:("+session.getId()+")</body></html>");
    }
}
