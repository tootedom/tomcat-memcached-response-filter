package org.greencheek.web.filter.memcached.cachekey;

import org.greencheek.web.filter.memcached.keyhashing.KeyHashing;
import org.greencheek.web.filter.memcached.keyhashing.MessageDigestHashing;
import org.greencheek.web.filter.memcached.util.CustomSplitByChar;
import org.greencheek.web.filter.memcached.util.SplitByChar;

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
    public static final String VALUE_OPTIONAL_SEPARATOR_CHAR = "?";
    public static final String VALUE_SORTED_CHAR = "s";

    private final boolean useCookies;
    private final boolean useHeaders;

    private final MultiValuedKey[] cookieNames;
    private final MultiValuedKey[] headerNames;
    private final Set<String> headerNamesSet;
    private final Set<String> cookieNameSet;
    private final KeyHashing keyHashingUtil;
    private final SplitByChar splitter = new CustomSplitByChar();

    private final List<CacheKeyType> keyOrder = new ArrayList<CacheKeyType>(10);

    public DefaultCacheKeyCreator(String keySpec) {
        this(keySpec,new MessageDigestHashing());
    }

    public DefaultCacheKeyCreator(String keySpec, KeyHashing keyHashingUtil) {
        this.keyHashingUtil = keyHashingUtil;
        keySpec = keySpec.toLowerCase();
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
            for(MultiValuedKey name : cookieNames) {
                cookieNameSet.add(name.getValue());
            }
        }

        if(headerNames.length==0) {
            headerNamesSet = Collections.EMPTY_SET;
        } else {
            headerNamesSet = new HashSet<String>(headerNames.length,1.0f);
            for(MultiValuedKey name : headerNames) {
                headerNamesSet.add(name.getValue());
            }
        }

        useHeaders = headerNames.length>0 ? true : false;
        useCookies = cookieNames.length>0 ? true : false;
    }

    public KeyHashing getKeyHashingUtil() {
        return keyHashingUtil;
    }

    /**
     * Return a list of header names that have been been specified
     * in the cache key
     * @param keys
     * @return
     */
    private MultiValuedKey[] parseHeaders(List<String> keys) {
        return parseValues(keys,HEADERS);
    }

    /**
     * Returns a list of cookie names that have been specified in the
     * cache key
     * @param keys
     * @return
     */
    private MultiValuedKey[] parseCookies(List<String> keys) {
        return parseValues(keys,COOKIES);
    }

    private MultiValuedKey[] parseValues(List<String> keys, String name) {
        List<MultiValuedKey> headers = new ArrayList<MultiValuedKey>();
        for(String key : keys) {
            if(key.startsWith(name)) {
                String[] values = key.split(VALUE_SEPARATOR_CHAR);
                if(values!=null && values.length>1) {
                    boolean isOptional = key.endsWith(VALUE_OPTIONAL_SEPARATOR_CHAR);
                    boolean toSort = values.length>2 && values[2].equals(VALUE_SORTED_CHAR);

                    headers.add(new MultiValuedKey(values[1],isOptional,toSort));
                }
            }
        }
        return headers.toArray(new MultiValuedKey[headers.size()]);
    }

    public static class MultiValuedKey {
        private final String value;
        private final boolean optional;
        private final boolean sorted;

        public MultiValuedKey(String value, boolean optional, boolean sorted) {
            this.value = value;
            this.optional = optional;
            this.sorted = sorted;
        }

        public String getValue() {
            return value;
        }

        public boolean isOptional() {
            return this.optional;
        }

        public boolean isToBeSorted() {
            return this.sorted;
        }
    }

    /**
     * Actually takes the header values out of the request and
     * @param request
     * @param headerNames
     * @param namesRequested
     * @return
     */
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
                Enumeration<String> headerValues = request.getHeaders(headerName);
                if(headerValues==null) {
                    extractedHeaders.put(lowerHeaderName,null);
                    continue;
                }
                StringBuilder headerValue = new StringBuilder(32);
                while(headerValues.hasMoreElements()) {
                    headerValue.append(headerValues.nextElement()).append(',');
                }
                headerValue.deleteCharAt(headerValue.length()-1);
                extractedHeaders.put(lowerHeaderName,headerValue.toString());
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
            boolean isOptional = true;
            boolean toBeSorted = false;
            MultiValuedKey keyItem = null;

            switch (type) {
                case HEADER:
                    keyItem = headerNames[headersNum++];
                    isOptional = keyItem.isOptional();
                    toBeSorted = keyItem.isToBeSorted();
                    element = attributeRequest.getAttribute(request,headers,keyItem.getValue());
                    break;
                case COOKIE:
                    keyItem = cookieNames[cookiesNum++];
                    isOptional = keyItem.isOptional();
                    element = attributeRequest.getAttribute(request,cookies,keyItem.getValue());
                    break;
                default:
                    element = attributeRequest.getAttribute(request);

            }
            if(!element.isAvailable() && !isOptional) {
                enabled = false;
            }

            String elemValue = element.getElement();
            if(toBeSorted) {
                elemValue = sortValue(elemValue);
            }
            b.append(elemValue);

        }
        return new CacheKey(enabled,keyHashingUtil.hash(b.toString()));
    }


    private String sortValue(String value) {
        List<String> values = splitter.split(value,',');
        Collections.sort(values);
        return join(values,',',value.length());
    }

    private String join(List<String> values,char c, int expectedLength) {
        StringBuilder b = new StringBuilder(expectedLength);
        for(String value : values) {
            b.append(value).append(c);
        }
        b.deleteCharAt(b.length());
        return b.toString();
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
