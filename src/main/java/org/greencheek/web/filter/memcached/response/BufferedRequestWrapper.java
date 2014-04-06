package org.greencheek.web.filter.memcached.response;

import javax.servlet.AsyncContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import javax.servlet.http.HttpServletResponse;

/**
 * Created by dominictootell on 06/04/2014.
 */
public class BufferedRequestWrapper extends HttpServletRequestWrapper {

    private final BufferedResponseWrapper response;
    private final HttpServletRequest request;

    /**
     * Constructs a request object wrapping the given request.
     *
     * @param request
     * @throws IllegalArgumentException if the request is null
     */
    public BufferedRequestWrapper(HttpServletRequest request, BufferedResponseWrapper response) {
        super(request);
        this.request = request;
        this.response = response;
    }

    public AsyncContext startAsync() {
        return super.startAsync(request,response);
    }


}
