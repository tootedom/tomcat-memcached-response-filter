package org.greencheek.web.filter.memcached.servlets;

import org.greencheek.web.filter.memcached.io.IOUtils;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Enumeration;

/**
 * Created by dominictootell on 20/04/2014.
 */
public class PutPostServlet extends HttpServlet {

    protected void service( HttpServletRequest request,
                          HttpServletResponse response)
            throws ServletException, IOException {

        long now = System.currentTimeMillis();
        response.addHeader("Cache-Control", "max-age=" + 3);
        response.addDateHeader("X-Now",now);
        response.addHeader("X-Method", request.getMethod());
        StringBuilder b = new StringBuilder();
        Enumeration<String> headerNames = request.getHeaderNames();
        while(headerNames.hasMoreElements()) {
            b.append(headerNames.nextElement()).append("\r\n");
        }
        response.getWriter().write("<html><body>GET Date Header response, content:(" + content(request) + ") "+ b.toString()+"</body></html>");
    }


    public String content(HttpServletRequest request) throws IOException{
        return new String(IOUtils.readStreamToBytes(4096, request.getInputStream(), request.getContentLength()),"UTF-8");
    }
}
