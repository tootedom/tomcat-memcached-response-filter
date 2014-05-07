package org.greencheek.web.filter.memcached.cachekey.extraction;

import org.greencheek.web.filter.memcached.cachekey.CacheKeyElement;
import org.greencheek.web.filter.memcached.client.config.CacheConfigGlobals;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by dominictootell on 13/04/2014.
 */
public class MethodAttributeExtractor implements KeyAttributeExtractor {
    public static final MethodAttributeExtractor INSTANCE = new MethodAttributeExtractor();

    public static final CacheKeyElement CACHE_KEY_GET_ELEMENT = new CacheKeyElement(CacheConfigGlobals.getASCIIBytes("GET"),true);
    public static final CacheKeyElement CACHE_KEY_PUT_ELEMENT = new CacheKeyElement(CacheConfigGlobals.getASCIIBytes("PUT"),true);
    public static final CacheKeyElement CACHE_KEY_POST_ELEMENT = new CacheKeyElement(CacheConfigGlobals.getASCIIBytes("POST"),true);
    public static final CacheKeyElement CACHE_KEY_HEAD_ELEMENT = new CacheKeyElement(CacheConfigGlobals.getASCIIBytes("HEAD"),true);
    public static final CacheKeyElement CACHE_KEY_DELETE_ELEMENT = new CacheKeyElement(CacheConfigGlobals.getASCIIBytes("DELETE"),true);
    public static final CacheKeyElement CACHE_KEY_OPTIONS_ELEMENT = new CacheKeyElement(CacheConfigGlobals.getASCIIBytes("OPTIONS"),true);

    public static final Map<String,CacheKeyElement> PREBUILD_ELEMENTS;

    static {
        Map<String,CacheKeyElement> elements = new HashMap<String,CacheKeyElement>(240,1.0f);
        addCacheKeyElement("GET",CACHE_KEY_GET_ELEMENT,elements);
        addCacheKeyElement("PUT",CACHE_KEY_PUT_ELEMENT,elements);
        addCacheKeyElement("POST",CACHE_KEY_POST_ELEMENT,elements);
        addCacheKeyElement("DELETE",CACHE_KEY_DELETE_ELEMENT,elements);
        addCacheKeyElement("OPTIONS",CACHE_KEY_OPTIONS_ELEMENT,elements);
        addCacheKeyElement("HEAD",CACHE_KEY_HEAD_ELEMENT,elements);
        PREBUILD_ELEMENTS = elements;
    }

    private static void addCacheKeyElement(String stringToPermute,CacheKeyElement referencedInstance,
                                           Map<String,CacheKeyElement> prebuildCachedInstances) {

        for(String name : CacheConfigGlobals.permutate(stringToPermute)) {
            prebuildCachedInstances.put(name,referencedInstance);
        }
    }

    @Override
    public CacheKeyElement getAttribute(HttpServletRequest request) {
        CacheKeyElement instance = PREBUILD_ELEMENTS.get(request.getMethod());
        if(instance==null) {
            return new CacheKeyElement(CacheConfigGlobals.getASCIIBytes(request.getMethod()), true);
        } else {
            return instance;
        }
    }
}
