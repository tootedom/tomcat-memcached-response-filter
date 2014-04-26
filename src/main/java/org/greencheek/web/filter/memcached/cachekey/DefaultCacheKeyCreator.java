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

    private final KeyHashing keyHashingUtil;
    private final String keySpec;
    private final KeySpecFactory keySpecFactory;
    private final List<KeyAttributeExtractor> extractors;
    private final int estimatedKeySize;



    public DefaultCacheKeyCreator(String keySpec, KeyHashing keyHashingUtil,
                                  KeySpecFactory keySpecFactory) {
        this.keyHashingUtil = keyHashingUtil;
        this.keySpec = keySpec;
        this.keySpecFactory = keySpecFactory;
        extractors = keySpecFactory.getKeySpecExtractors(keySpec);
        estimatedKeySize = 32 * extractors.size();

    }

//    public DefaultCacheKeyCreator(String keySpec, KeyHashing keyHashingUtil) {
//        this.keyHashingUtil = keyHashingUtil;
//        keySpec = keySpec.toLowerCase();
//        String[] keys = keySpec.split(KEY_SEPARATOR_CHAR);
//        List<String> keySet = new ArrayList<String>();
//        for(String item : keys) {
//            if(item == null || item.length()==0) continue;
//            keySet.add(item);
//
//            if(item.startsWith(PATH)) keyOrder.add(CacheKeyType.PATH);
//            else if(item.startsWith(QUERY_STRING)) keyOrder.add(CacheKeyType.QUERY_STRING);
//            else if(item.startsWith(REQUEST_METHOD)) keyOrder.add(CacheKeyType.REQUEST_METHOD);
//            else if(item.startsWith(PATH_AND_PARAMS)) keyOrder.add(CacheKeyType.PATH_AND_PARAMS);
//            else if(item.startsWith(SCHEME)) keyOrder.add(CacheKeyType.SCHEME);
//            else if(item.startsWith(COOKIES)) keyOrder.add(CacheKeyType.COOKIE);
//            else if(item.startsWith(HEADERS)) keyOrder.add(CacheKeyType.HEADER);
//            else if(item.startsWith(CONTENT_TYPE)) keyOrder.add(CacheKeyType.CONTENT_TYPE);
//        }
//
//        headerNames = parseKeySpecHeaders(keySet);
//        cookieNames = parseKeySpecCookies(keySet);
//
//        if(cookieNames.length==0) {
//            cookieNameSet = Collections.EMPTY_SET;
//        }
//        else {
//            cookieNameSet = new HashSet<String>(cookieNames.length,1.0f);
//            for(MultiValuedKey name : cookieNames) {
//                cookieNameSet.add(name.getValue());
//            }
//        }
//
//        if(headerNames.length==0) {
//            headerNamesSet = Collections.EMPTY_SET;
//        } else {
//            headerNamesSet = new HashSet<String>(headerNames.length,1.0f);
//            for(MultiValuedKey name : headerNames) {
//                headerNamesSet.add(name.getValue());
//            }
//        }
//
//        useHeaders = headerNames.length>0 ? true : false;
//        useCookies = cookieNames.length>0 ? true : false;
//    }

    public KeyHashing getKeyHashingUtil() {
        return keyHashingUtil;
    }




    @Override
    public String createCacheKey(HttpServletRequest request) {
        StringBuilder b = new StringBuilder(estimatedKeySize);

        for(KeyAttributeExtractor extractor : extractors) {
            CacheKeyElement keyElement = extractor.getAttribute(request);
            if(!keyElement.isAvailable()) {
                return null;
            }
            b.append(keyElement.getElement());
        }

        return keyHashingUtil.hash(b.toString());
//
//        int headersNum = 0;
//        int cookiesNum = 0;
//        StringBuilder b = new StringBuilder(keyOrder.size()*10);
//        Map<String,String> cookies = new HashMap<String,String>(cookieNames.length);
//        if(useCookies) {
//            cookies = parseCookies(request.getCookies(),cookieNameSet);
//
//        }
//        Map<String,String> headers = new HashMap<String,String>(headerNames.length);
//        if(useHeaders) {
//            headers = parseHeaders(request,request.getHeaderNames(),headerNamesSet);
//
//        }
//
//        for(CacheKeyType type : keyOrder) {
//            KeyAttributeExtractor attributeRequest = attributes[type.index];
//            CacheKeyElement element;
//            boolean isOptional = true;
//            boolean toBeSorted = false;
//            MultiValuedKey keyItem;
//
//            switch (type) {
//                case HEADER:
//                    keyItem = headerNames[headersNum++];
//                    isOptional = keyItem.isOptional();
//                    toBeSorted = keyItem.isToBeSorted();
//                    element = attributeRequest.getAttribute(request,headers,keyItem.getValue());
//                    break;
//                case COOKIE:
//                    keyItem = cookieNames[cookiesNum++];
//                    isOptional = keyItem.isOptional();
//                    element = attributeRequest.getAttribute(request,cookies,keyItem.getValue());
//                    break;
//                default:
//                    element = attributeRequest.getAttribute(request);
//
//            }
//            if(!element.isAvailable() && !isOptional) {
//                return null;
//            }
//
//            String elemValue = element.getElement();
//            if(toBeSorted) {
//                elemValue = sortValue(elemValue);
//            }
//            b.append(elemValue);
//
//        }
//        return keyHashingUtil.hash(b.toString());
    }







}
