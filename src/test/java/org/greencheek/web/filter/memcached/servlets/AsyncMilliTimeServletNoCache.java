package org.greencheek.web.filter.memcached.servlets;

import org.apache.juli.logging.Log;
import org.apache.juli.logging.LogFactory;

import javax.servlet.AsyncContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Created by dominictootell on 20/04/2014.
 */
public class AsyncMilliTimeServletNoCache extends HttpServlet {
    private static final long serialVersionUID = 1L;

    private static final Log log = LogFactory.getLog(AsyncMilliTimeServletNoCache.class);

    @Override
    protected void service(final HttpServletRequest req, final HttpServletResponse resp) throws ServletException, IOException {
        resp.setContentType("text/plain");
        final AsyncContext actx = req.startAsync();
        actx.setTimeout(Long.MAX_VALUE);
        Runnable run = new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.currentThread().setName("Async2-Thread");
                    log.info("Putting AsyncThread to sleep");
                    Thread.sleep(2 * 1000);
                    log.info("Writing data.");
                    ((HttpServletResponse)actx.getResponse()).setHeader("Cache-Control","no-store");
                    actx.getResponse().getWriter().write("Output from background thread. Time:(" + System.currentTimeMillis() + ")\n");
                    actx.complete();
                } catch (InterruptedException x) {
                    log.error("Async2", x);
                } catch (IllegalStateException x) {
                    log.error("Async2", x);
                } catch (IOException x) {
                    log.error("Async2", x);
                }
            }
        };
        Thread t = new Thread(run);
        t.start();
    }
}
