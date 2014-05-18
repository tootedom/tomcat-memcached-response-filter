package org.greencheek.web.filter.memcached.client;

import org.greencheek.web.filter.memcached.domain.CachedResponse;

import javax.servlet.http.HttpServletRequest;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * Created by dominictootell on 14/04/2014.
 */
public interface FilterMemcachedFetching {
    public CachedResponse getCachedContent(String key);
    public Map<String,CachedResponse> getCachedContent(Collection<String> keys);
}
