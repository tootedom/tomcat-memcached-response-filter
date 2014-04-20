package org.greencheek.web.filter.memcached.servlets;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Created by dominictootell on 20/04/2014.
 */
public class MaxAgeServlet extends HttpServlet {

    public static final int DEFAULT_MAX_AGE = 1;

    protected void doGet( HttpServletRequest request,
                          HttpServletResponse response)
            throws ServletException, IOException {

        int age = DEFAULT_MAX_AGE;
        try {
            age = Integer.parseInt(request.getParameter("maxage"));
        } catch (NumberFormatException e) {

        }

        response.addHeader("Cache-Control","max-age="+age);
        response.getWriter().write("<html><body>GET response</body></html>");
    }
}
