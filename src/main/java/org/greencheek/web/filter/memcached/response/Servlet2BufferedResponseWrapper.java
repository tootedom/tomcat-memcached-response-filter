package org.greencheek.web.filter.memcached.response;

import org.greencheek.web.filter.memcached.dateformatting.DateHeaderFormatter;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Created by dominictootell on 19/04/2014.
 */
public class Servlet2BufferedResponseWrapper extends BufferedResponseWrapper {

    public int status = 200;
    public final ConcurrentHashMap<String,List<String>> headers = new ConcurrentHashMap<String, List<String>>(4);
    public final DateHeaderFormatter dateHeaderFormatter;
    public Servlet2BufferedResponseWrapper(DateHeaderFormatter dateFormatter,
                                           int memcachedContentBufferSize, HttpServletResponse response) {
        super(memcachedContentBufferSize, response);
        this.dateHeaderFormatter = dateFormatter;
    }

    public void setStatus(int sc) {
        status = sc;
        super.setStatus(sc);
    }

    public void setStatus(int sc, String sm) {
        status = sc;
        super.setStatus(sc, sm);
    }

    public void sendError(int sc) throws IOException {
        status = sc;
        super.sendError(sc);
    }

    public void sendError(int sc, String sm) throws IOException {
        status = sc;
        super.sendError(sc, sm);
    }

    public void setHeader(String name, String value) {
        List<String> values = new CopyOnWriteArrayList<String>();
        values.add(value);
        headers.put(name,values);
        super.setHeader(name, value);
    }

    public void addHeader(String name, String value) {
        List<String> values = headers.get(name);
        if( values == null ) {
            values = new CopyOnWriteArrayList<String>();
            values.add(value);
            headers.put(name, values);
        } else {
            values.add(value);
        }
        super.addHeader(name, value);
    }

    public String getHeader(String name)
    {
        List<String> values = headers.get(name);
        if(values!=null) {
            StringBuilder b = new StringBuilder(20);
            for(String s : values) {
                b.append(s).append(',');
            }

            b.setLength(b.length()-1);
            return b.toString();
        }
        return null;
    }

    public void addIntHeader(String name, int value) {
        addHeader(name, "" + value);
    }

    public void addDateHeader(String name, long date) {
        String dateString = dateHeaderFormatter.toDate(date);
        addHeader(name,dateString);
    }

    public Collection<String> getHeaders(String name) {
        return headers.get(name);
    }

    public Collection<String> getHeaderNames() {
        return headers.keySet();
    }

    public int getStatus() {
        return status;
    }
}
