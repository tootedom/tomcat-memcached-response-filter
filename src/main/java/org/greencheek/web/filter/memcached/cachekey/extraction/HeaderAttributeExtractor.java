package org.greencheek.web.filter.memcached.cachekey.extraction;

import org.greencheek.web.filter.memcached.cachekey.CacheKeyElement;
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
    private final SplitByChar splitByChar;

    public HeaderAttributeExtractor(String header, boolean isOptional,
                                    boolean toBeSorted, SplitByChar splitByChar) {
        this.headerName = header;
        this.isOptional = isOptional;
        this.sortValue = toBeSorted;
        this.splitByChar = splitByChar;
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

    /**
     * Actually takes the header values out of the request and
     * @param request
     * @return
     */
    private String parseHeaders(HttpServletRequest request) {

        Enumeration<String> headerValues = request.getHeaders(this.headerName);
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
        List<String> values = splitByChar.split(value,',');
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

}
