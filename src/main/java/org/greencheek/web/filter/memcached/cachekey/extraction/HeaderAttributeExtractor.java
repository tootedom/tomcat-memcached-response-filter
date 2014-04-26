package org.greencheek.web.filter.memcached.cachekey.extraction;

import org.greencheek.web.filter.memcached.cachekey.CacheKeyElement;
import org.greencheek.web.filter.memcached.util.CharSeparatedValueSorter;
import org.greencheek.web.filter.memcached.util.JoinByChar;
import org.greencheek.web.filter.memcached.util.SplitByChar;

import javax.servlet.http.HttpServletRequest;
import java.util.*;

/**
 * Created by dominictootell on 13/04/2014.
 */
public class HeaderAttributeExtractor implements KeyAttributeExtractor {

    private final String headerName;
    private final boolean isOptional;
    private final boolean sortValue;
    private final CharSeparatedValueSorter valueSorter;

    public HeaderAttributeExtractor(String header, boolean isOptional,
                                    boolean toBeSorted,CharSeparatedValueSorter sorter) {
        this.headerName = header;
        this.isOptional = isOptional;
        this.sortValue = toBeSorted;
        this.valueSorter = sorter;
    }

    @Override
    public CacheKeyElement getAttribute(HttpServletRequest request) {
        String header = parseHeaders(request);
        if(header == null) {
            if(isOptional) {
                return CacheKeyElement.EMPTY_CACHE_KEY_ELEMENT;
            } else {
                return CacheKeyElement.CACHE_KEY_ELEMENT_NOT_AVAILABLE;
            }
        } else {
            if(sortValue) {
                return new CacheKeyElement(sortValue(header), true);
            } else {
                return new CacheKeyElement(header, true);
            }
        }
    }


    private String getMatchingHeaderName(HttpServletRequest request) {
        Enumeration<String> headerNames = request.getHeaderNames();
        if(headerNames==null || !headerNames.hasMoreElements()) return null;

        String headerName = null;
        while(headerNames.hasMoreElements()) {
            String name = headerNames.nextElement();
            if(name.toLowerCase().equals(this.headerName)) {
                headerName = name;
                break;
            }
        }
        return headerName;
    }
    /**
     * Actually takes the header values out of the request and
     * @param request
     * @return
     */
    private String parseHeaders(HttpServletRequest request) {
        String headerName = getMatchingHeaderName(request);
        if(headerName==null) {
            return null;
        }

        Enumeration<String> headerValues = request.getHeaders(headerName);
        if(headerValues==null || !headerValues.hasMoreElements()) {
            return null;
        } else {
            StringBuilder headerValue = new StringBuilder(32);
            while(headerValues.hasMoreElements()) {
                headerValue.append(headerValues.nextElement()).append(',');
            }
            headerValue.deleteCharAt(headerValue.length() - 1);
            return headerValue.toString();
        }

    }

    private String sortValue(String value) {
        return valueSorter.sort(value,',');
    }

//    private String join(List<String> values,char c, int expectedLength) {
//        StringBuilder b = new StringBuilder(expectedLength);
//        for(String value : values) {
//            b.append(value).append(c);
//        }
//        b.deleteCharAt(b.length()-1);
//        return b.toString();
//    }

}
