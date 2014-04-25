package org.greencheek.web.filter.memcached.cachekey;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Set;

/**
 * Created by dominictootell on 13/04/2014.
 *
 * given a http request object a cache key is generated to be looked up
 * in memcached
 *
 */
public interface CacheKeyCreator {
    String createCacheKey(HttpServletRequest request);
}
