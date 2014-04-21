package org.greencheek.web.filter.memcached.cachekey;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Set;

/**
 * Created by dominictootell on 13/04/2014.
 */
public interface CacheKeyCreator {
    CacheKey createCacheKey(HttpServletRequest request);
}
