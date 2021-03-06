package org.greencheek.web.filter.memcached.servlets;

import org.apache.commons.io.FileUtils;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;

/**
 * Created by dominictootell on 20/04/2014.
 */
public class LargeContentServlet extends HttpServlet {

    public static final byte[] CONTENT;
    static {
        File is = new File(Thread.currentThread().getContextClassLoader().getResource("relatedcontent.txt").getFile());
        byte[] content;
        try {
            content = FileUtils.readFileToByteArray(is);
        } catch (IOException e) {
            content = new byte[0];
        }
        CONTENT = content;
    }



    public static final int DEFAULT_MAX_AGE = 3;

    protected void doGet( HttpServletRequest request,
                          HttpServletResponse response)
            throws ServletException, IOException {

        int age = DEFAULT_MAX_AGE;
        try {
            String ageParam = request.getParameter("maxage");
            if(ageParam==null || ageParam.trim().length()==0) {
                age = Integer.parseInt(ageParam);
            }
        } catch (NumberFormatException e) {

        }

        response.addHeader("X-LastTime","" + System.currentTimeMillis());
        response.addHeader("Cache-Control","max-age="+age);
        response.getOutputStream().write(CONTENT);
    }
}
