package org.greencheek.web.filter.memcached.cachekey.extraction;

import org.greencheek.web.filter.memcached.cachekey.CacheKeyElement;
import org.greencheek.web.filter.memcached.client.config.CacheConfigGlobals;
import org.greencheek.web.filter.memcached.io.ResizeableByteBuffer;
import org.greencheek.web.filter.memcached.util.CharSeparatedValueSorter;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;

/**
 * Created by dominictootell on 10/05/2014.
 */
public class ParamsAttributeExtractor implements KeyAttributeExtractor {

    private static final byte EQUALS = '=';
    private static final byte AMP = '&';
    private static final byte COMMA = ',';

    private final boolean isOptional;
    private final boolean sortValues;
    private final CharSeparatedValueSorter sorter;

    public ParamsAttributeExtractor(boolean isOptional,
                                    boolean toBeSorted,CharSeparatedValueSorter sorter) {
        this.isOptional = isOptional;
        this.sortValues = toBeSorted;
        this.sorter = sorter;
    }

    @Override
    public CacheKeyElement getAttribute(HttpServletRequest request) {
        Map<String,String[]> params =  request.getParameterMap();

        if(params==null || params.size()==0) {
            return getNotAvailableElement();
        } else {
            ResizeableByteBuffer buffer;
            if(sortValues) {
                buffer = sortParamerersValues(params);
            } else {
                buffer = unsortedParameterValues(params);
            }

            return new CacheKeyElement(buffer.getBuf(),0,buffer.size(),true);
        }
    }

    private CacheKeyElement getNotAvailableElement() {
        if(isOptional) {
            return CacheKeyElement.EMPTY_CACHE_KEY_ELEMENT;
        } else {
            return CacheKeyElement.CACHE_KEY_ELEMENT_NOT_AVAILABLE;
        }
    }

    private ResizeableByteBuffer unsortedParameterValues(Map<String,String[]> params) {
        ResizeableByteBuffer buffer = new ResizeableByteBuffer(256,ResizeableByteBuffer.MAX_ARRAY_SIZE);

        for(Map.Entry<String,String[]> entry : params.entrySet()) {
            buffer.append(CacheConfigGlobals.getBytes(entry.getKey()));
            buffer.append(EQUALS);
            String[] values = entry.getValue();

            if(values!=null && values.length>0) {
                for (String value : entry.getValue()) {
                    buffer.append(CacheConfigGlobals.getBytes(value));
                    buffer.append(COMMA);
                }
                buffer.setSize(buffer.size() - 1);
            }
            buffer.append(AMP);
        }
        buffer.setSize(buffer.size()-1);
        return buffer;
    }

    private ResizeableByteBuffer sortParamerersValues(Map<String,String[]> params) {
        StringBuilder headerValue = new StringBuilder(256);

        for(Map.Entry<String,String[]> entry : params.entrySet()) {
            headerValue.append(entry.getKey()).append('=');
            String[] values = entry.getValue();

            if(values!=null && values.length>0) {
                for (String value : entry.getValue()) {
                    headerValue.append(value).append(',');
                }
                headerValue.setLength(headerValue.length()-1);
            }
            headerValue.append('&');
        }

        headerValue.setLength(headerValue.length()-1);
        String sorted = sorter.sort(headerValue.toString(),'&');
        return toResizeableByteBuffer(sorted);
    }

    private ResizeableByteBuffer toResizeableByteBuffer(String value) {
        byte[] content = CacheConfigGlobals.getBytes(value);
        ResizeableByteBuffer buffer = new ResizeableByteBuffer(content.length,content.length);
        buffer.append(content);
        return buffer;
    }
}
