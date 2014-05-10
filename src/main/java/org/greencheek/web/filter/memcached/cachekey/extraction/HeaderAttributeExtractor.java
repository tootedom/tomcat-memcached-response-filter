package org.greencheek.web.filter.memcached.cachekey.extraction;

import org.greencheek.web.filter.memcached.cachekey.CacheKeyElement;
import org.greencheek.web.filter.memcached.client.config.CacheConfigGlobals;
import org.greencheek.web.filter.memcached.io.ResizeableByteBuffer;
import org.greencheek.web.filter.memcached.util.CharSeparatedValueSorter;

import javax.servlet.http.HttpServletRequest;
import java.util.*;

/**
 * Created by dominictootell on 13/04/2014.
 */
public class HeaderAttributeExtractor implements KeyAttributeExtractor {

    private final String headerName;
    private final boolean isOptional;
    private final boolean sortValue;
    private final int maxSingleHeaderValueSize;
    private final CharSeparatedValueSorter valueSorter;

    public HeaderAttributeExtractor(String header, boolean isOptional,
                                    boolean toBeSorted,CharSeparatedValueSorter sorter,
                                    int maxSingleHeaderValueSize) {
        this.headerName = header;
        this.isOptional = isOptional;
        this.sortValue = toBeSorted;
        this.valueSorter = sorter;
        this.maxSingleHeaderValueSize = maxSingleHeaderValueSize;
    }

    @Override
    public CacheKeyElement getAttribute(HttpServletRequest request) {
        ResizeableByteBuffer header = parseHeaders(request,sortValue);

        if(header == null || !header.canWrite()) {
            return getNotAvailableElement();
        } else {
            return new CacheKeyElement(header.getBuf(),0,header.position(), true);
        }
    }

    private CacheKeyElement getNotAvailableElement() {
        if(isOptional) {
            return CacheKeyElement.EMPTY_CACHE_KEY_ELEMENT;
        } else {
            return CacheKeyElement.CACHE_KEY_ELEMENT_NOT_AVAILABLE;
        }
    }


    private String getMatchingHeaderName(HttpServletRequest request) {
        Enumeration<String> headerNames = request.getHeaderNames();
        if(headerNames==null || !headerNames.hasMoreElements()) return null;

        String headerName = null;
        while(headerNames.hasMoreElements()) {
            String name = headerNames.nextElement();
            if(name.equalsIgnoreCase(this.headerName)) {
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
    private ResizeableByteBuffer parseHeaders(HttpServletRequest request, boolean sortValue) {
        String headerName = getMatchingHeaderName(request);
        if(headerName==null) {
            return null;
        }

        Enumeration<String> headerValues = request.getHeaders(headerName);
        if(headerValues==null || !headerValues.hasMoreElements()) {
            return null;
        }

        if(sortValue) {
            String sortedHeader = sortHeaderValues(headerValues);
            byte[] bytes = CacheConfigGlobals.getBytes(sortedHeader);
            ResizeableByteBuffer buffer = new ResizeableByteBuffer(bytes.length, maxSingleHeaderValueSize);
            buffer.append(bytes);
            return buffer;
        } else {
            ResizeableByteBuffer buffer = new ResizeableByteBuffer(64, maxSingleHeaderValueSize);

            while(headerValues.hasMoreElements()) {
                buffer.append(CacheConfigGlobals.getBytes(headerValues.nextElement()));
                buffer.append((byte) 44);
            }
            buffer.setSize(buffer.size()-1);
            return buffer;
        }
    }

    private String sortHeaderValues(Enumeration<String> headerValues) {
        StringBuilder headerValue = new StringBuilder(64);

        while(headerValues.hasMoreElements()) {
            headerValue.append(headerValues.nextElement()).append(',');
        }
        headerValue.setLength(headerValue.length()-1);
        return sortValue(headerValue.toString());
    }

    private String sortValue(String value) {
        return valueSorter.sort(value,',');
    }

}
