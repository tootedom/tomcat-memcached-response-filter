package org.greencheek.web.filter.memcached.cachekey;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.*;

/**
 * Created by dominictootell on 13/04/2014.
 */
public class DefaultCacheKeyCreator implements CacheKeyCreator {

    private enum CacheKeyType {
        PATH(0),REQUEST_METHOD(1),PATH_AND_PARAMS(2),
        QUERY_STRING(3),SCHEME(4),HEADER(5),COOKIE(6);

        public int index;

        CacheKeyType(int index) {
            this.index = index;
        }
    }

    private GetRequestAttribute[] attributes = new GetRequestAttribute[] {
            GetPathRequestAttribute.INSTANCE,GetMethodRequestAttribute.INSTANCE,
            GetPathAndQueryRequestAttribute.INSTANCE,
            GetQueryStringRequestAttribute.INSTANCE,GetSchemeRequestAttribute.INSTANCE,
            GetHeaderRequestAttribute.INSTANCE,GetCookieRequestAttribute.INSTANCE
    };

    private static final String REQUEST_METHOD = "request_method";
    private static final String PATH_AND_PARAMS = "request_uri";
    private static final String PATH = "uri";
    private static final String QUERY_STRING = "args";
    private static final String HEADERS = "header";
    private static final String COOKIES = "cookie";
    private static final String SCHEME = "scheme";
    public static final String KEY_SEPARATOR_CHAR = "\\$";
    public static final String VALUE_SEPARATOR_CHAR = "_";

    private final boolean useCookies;
    private final boolean useHeaders;

    private final String[] cookieNames;
    private final String[] headerNames;
    private final Set<String> headerNamesSet;
    private final Set<String> cookieNameSet;

    private final List<CacheKeyType> keyOrder = new ArrayList<CacheKeyType>(10);

    public DefaultCacheKeyCreator(String keySpec) {
        String[] keys = keySpec.split(KEY_SEPARATOR_CHAR);
        List<String> keySet = new ArrayList<String>();
        for(String item : keys) {
            if(item == null || item.length()==0) continue;
            keySet.add(item);

            if(item.startsWith(PATH)) keyOrder.add(CacheKeyType.PATH);
            else if(item.startsWith(QUERY_STRING)) keyOrder.add(CacheKeyType.QUERY_STRING);
            else if(item.startsWith(REQUEST_METHOD)) keyOrder.add(CacheKeyType.REQUEST_METHOD);
            else if(item.startsWith(PATH_AND_PARAMS)) keyOrder.add(CacheKeyType.PATH_AND_PARAMS);
            else if(item.startsWith(SCHEME)) keyOrder.add(CacheKeyType.SCHEME);
            else if(item.startsWith(COOKIES)) keyOrder.add(CacheKeyType.COOKIE);
            else if(item.startsWith(HEADERS)) keyOrder.add(CacheKeyType.HEADER);
        }

        headerNames = parseHeaders(keySet);
        cookieNames = parseCookies(keySet);

        if(cookieNames.length==0) {
            cookieNameSet = Collections.EMPTY_SET;
        }
        else {
            cookieNameSet = new HashSet<String>(cookieNames.length,1.0f);
            for(String name : cookieNames) {
                cookieNameSet.add(name);
            }
        }

        if(headerNames.length==0) {
            headerNamesSet = Collections.EMPTY_SET;
        } else {
            headerNamesSet = new HashSet<String>(headerNames.length,1.0f);
            for(String name : headerNames) {
                headerNamesSet.add(name);
            }
        }

        useHeaders = headerNames.length>0 ? true : false;
        useCookies = cookieNames.length>0 ? true : false;
    }

    private String[] parseHeaders(List<String> keys) {
        return parseValues(keys,HEADERS);
    }

    private String[] parseCookies(List<String> keys) {
        return parseValues(keys,COOKIES);
    }

    private String[] parseValues(List<String> keys, String name) {
        List<String> headers = new ArrayList<String>();
        for(String key : keys) {
            if(key.startsWith(name)) {
                String[] values = key.split(VALUE_SEPARATOR_CHAR);
                if(values!=null && values.length>1) {
                    headers.add(values[1].toLowerCase());
                }
            }
        }
        return headers.toArray(new String[headers.size()]);
    }

    private Map<String,String> parseHeaders(HttpServletRequest request,
                                            Enumeration<String> headerNames,
                                            Set<String> namesRequested) {

        Map<String,String> extractedHeaders = new HashMap<String, String>(namesRequested.size(),1.0f);
        for(String header : namesRequested) {
            extractedHeaders.put(header,"");
        }
        if(headerNames == null) return extractedHeaders;

        while(headerNames.hasMoreElements()) {
            String headerName = headerNames.nextElement();
            String lowerHeaderName = headerName.toLowerCase();

            if(namesRequested.contains(lowerHeaderName)) {
                extractedHeaders.put(lowerHeaderName,request.getHeader(headerName));
            }
        }
        return extractedHeaders;
    }

    @Override
    public CacheKey createCacheKey(HttpServletRequest request) {
        String key;
        boolean enabled = true;

        int headersNum = 0;
        int cookiesNum = 0;
        StringBuilder b = new StringBuilder(keyOrder.size()*10);
        Map<String,String> cookies = new HashMap<String,String>(cookieNames.length);
        if(useCookies) {
            cookies = parseCookies(request.getCookies(),cookieNameSet);

        }
        Map<String,String> headers = new HashMap<String,String>(headerNames.length);
        if(useHeaders) {
            headers = parseHeaders(request,request.getHeaderNames(),headerNamesSet);

        }

        for(CacheKeyType type : keyOrder) {
            GetRequestAttribute attributeRequest = attributes[type.index];
            CacheKeyElement element;
            switch (type) {
                case HEADER:
                    element = attributeRequest.getAttribute(request,headers,headerNames[headersNum++]);
                    b.append(element.getElement());
                    break;
                case COOKIE:
                    element = attributeRequest.getAttribute(request,cookies,cookieNames[cookiesNum++]);
                    b.append(element.getElement());
                    break;
                default:
                    element = attributeRequest.getAttribute(request);
                    b.append(element.getElement());
            }
            if(!element.isAvailable()) {
                enabled = false;
            }

        }
        return new CacheKey(enabled,b.toString());
    }


    private Map<String,String> parseCookies(Cookie[] cookies,Set<String> cookieNames) {
        Map<String,String> cookieValues = new HashMap<String,String>(cookieNames.size());
        if(cookies == null) return cookieValues;
        for(String cookieName : cookieNames) {
            cookieValues.put(cookieName,"");
        }

        for(Cookie c : cookies) {
            String cName = c.getName().toLowerCase();
            if(cookieNames.contains(cName)) {
                cookieValues.put(cName,cookieToString(c));
            }
        }

        return cookieValues;
    }

    public String cookieToString(Cookie cookie) {
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
