package org.greencheek.web.filter.memcached.cachekey;

import org.greencheek.web.filter.memcached.cachekey.extraction.*;
import org.greencheek.web.filter.memcached.keyhashing.KeyHashing;
import org.greencheek.web.filter.memcached.keyhashing.MessageDigestHashing;
import org.greencheek.web.filter.memcached.util.CustomSplitByChar;
import org.greencheek.web.filter.memcached.util.SplitByChar;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import java.util.*;

/**
 * Created by dominictootell on 13/04/2014.
 */
public class DefaultCacheKeyCreator implements CacheKeyCreator {

    private enum CacheKeyType {
        PATH(0),REQUEST_METHOD(1),PATH_AND_PARAMS(2),
        QUERY_STRING(3),SCHEME(4),HEADER(5),COOKIE(6),CONTENT_TYPE(7);

        public int index;

        CacheKeyType(int index) {
            this.index = index;
        }
    }

    private KeyAttributeExtractor[] attributes = new KeyAttributeExtractor[] {
            PathAttributeExtractor.INSTANCE, MethodAttributeExtractor.INSTANCE,
            PathAndQueryAttributeExtractor.INSTANCE,
            QueryAttributeExtractor.INSTANCE, SchemeAttributeExtractor.INSTANCE,
            HeaderAttributeExtractor.INSTANCE, CookieAttributeExtractor.INSTANCE, ContentTypeAttributeExtractor.INSTANCE
    };



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
            else if(item.startsWith(CONTENT_TYPE)) keyOrder.add(CacheKeyType.CONTENT_TYPE);
        }

        headerNames = parseKeySpecHeaders(keySet);
        cookieNames = parseKeySpecCookies(keySet);

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
    private MultiValuedKey[] parseKeySpecHeaders(List<String> keys) {
        return parseValues(keys,HEADERS);
    }

    /**
     * Returns a list of cookie names that have been specified in the
     * cache key
     * @param keys
     * @return
     */
    private MultiValuedKey[] parseKeySpecCookies(List<String> keys) {
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
                    String value = values[1];
                    if(!toSort && isOptional) {
                        value = value.substring(0,value.length()-1);
                    }
                    headers.add(new MultiValuedKey(value,isOptional,toSort));
                }
            }
        }
        return headers.toArray(new MultiValuedKey[headers.size()]);
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
        if(headerNames == null) return extractedHeaders;

        while(headerNames.hasMoreElements()) {
            String headerName = headerNames.nextElement();
            String lowerHeaderName = headerName.toLowerCase();

            if(namesRequested.contains(lowerHeaderName)) {
                Enumeration<String> headerValues = request.getHeaders(headerName);
                if(headerValues==null) {
                    extractedHeaders.put(lowerHeaderName,"");
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
    public String createCacheKey(HttpServletRequest request) {

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
            KeyAttributeExtractor attributeRequest = attributes[type.index];
            CacheKeyElement element;
            boolean isOptional = true;
            boolean toBeSorted = false;
            MultiValuedKey keyItem;

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
                return null;
            }

            String elemValue = element.getElement();
            if(toBeSorted) {
                elemValue = sortValue(elemValue);
            }
            b.append(elemValue);

        }
        return keyHashingUtil.hash(b.toString());
    }







}
