package org.greencheek.web.filter.memcached.client;

import org.greencheek.web.filter.memcached.io.ResizeableByteBuffer;
import org.greencheek.web.filter.memcached.response.BufferedResponseWrapper;

import javax.servlet.http.HttpServletRequest;
import java.util.Collection;
import java.util.Map;
import java.util.Set;

/**
 * Created by dominictootell on 06/04/2014.
 */
public interface FilterMemcachedStorage {
    public void writeToCache(HttpServletRequest theRequest, BufferedResponseWrapper theResponse);
}
