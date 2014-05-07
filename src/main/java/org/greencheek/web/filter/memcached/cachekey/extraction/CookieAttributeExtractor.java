package org.greencheek.web.filter.memcached.cachekey.extraction;

import org.greencheek.web.filter.memcached.cachekey.CacheKeyElement;
import org.greencheek.web.filter.memcached.client.config.CacheConfigGlobals;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;

/**
 * Created by dominictootell on 13/04/2014.
 */
public class CookieAttributeExtractor implements KeyAttributeExtractor {

    private final String cookieNameToExtract;
    private final boolean isOptional;

    public CookieAttributeExtractor(String name,boolean isOptional) {
        this.cookieNameToExtract = name;
        this.isOptional = isOptional;
    }

    @Override
    public CacheKeyElement getAttribute(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();

        if(cookies == null) {
            if (!isOptional) {
                return CacheKeyElement.CACHE_KEY_ELEMENT_NOT_AVAILABLE;
            } else if (cookies == null) {
                return CacheKeyElement.EMPTY_CACHE_KEY_ELEMENT;
            }
        }

        for(Cookie cookie : cookies) {
            if(cookie.getName().equalsIgnoreCase(cookieNameToExtract)) {
                return new CacheKeyElement(CacheConfigGlobals.getBytes(cookieToString(cookie)), true);
            }
        }

        return  CacheKeyElement.CACHE_KEY_ELEMENT_NOT_AVAILABLE;
    }

    private String cookieToString(Cookie cookie) {
        StringBuffer b = new StringBuffer(64);
        b.append(cookie.getName()).append('=').append(cookie.getValue());

        if(cookie.getVersion()==1) {
            b.append ("; Version=1");

            String comment = cookie.getComment();
            // Comment=comment
            if ( comment!=null ) {
                b.append("; Comment=");
                b.append(comment);
            }
        }

        String domain = cookie.getDomain();
        // Add domain information, if present
        if (domain!=null) {
            b.append("; Domain=").append(domain);
        }

        int maxAge = cookie.getMaxAge();
        // Max-Age=secs ... or use old "Expires" format
        if (maxAge >= 0) {
            b.append("; Max-Age=");
            b.append(maxAge);
        }


        String path = cookie.getPath();
        // Path=path
        if (path!=null) {
            b.append("; Path=").append(path);
        }

        // Secure
        if (cookie.getSecure()) {
            b.append("; Secure");
        }

        // HttpOnly
        if (cookie.isHttpOnly()) {
            b.append("; HttpOnly");
        }

        return b.toString();
    }


}
