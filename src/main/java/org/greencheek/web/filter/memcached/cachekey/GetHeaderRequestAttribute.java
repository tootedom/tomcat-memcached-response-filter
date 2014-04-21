package org.greencheek.web.filter.memcached.cachekey;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;

/**
 * Created by dominictootell on 13/04/2014.
 */
public class GetHeaderRequestAttribute implements GetRequestAttribute {

    public static final GetHeaderRequestAttribute INSTANCE = new GetHeaderRequestAttribute();

    @Override
    public CacheKeyElement getAttribute(HttpServletRequest request, Object... extra) {
        Map<String,String> headers = (Map<String,String>)extra[0];
        String header = headers.get(extra[1].toString());
        return new CacheKeyElement(header,header==null ? false : true);
    }
}
