package org.greencheek.web.filter.memcached.cachekey.extraction;

import org.greencheek.web.filter.memcached.cachekey.CacheKeyElement;
import org.greencheek.web.filter.memcached.client.config.CacheConfigGlobals;
import org.greencheek.web.filter.memcached.io.IOUtils;
import org.greencheek.web.filter.memcached.io.ResizeableByteBuffer;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.io.InputStream;

/**
 * Created by dominictootell on 06/05/2014.
 */
public class RequestBodyAttributeExtractor implements KeyAttributeExtractor {

    public static final RequestBodyAttributeExtractor INSTANCE = new RequestBodyAttributeExtractor();

    @Override
    public CacheKeyElement getAttribute(HttpServletRequest request) {
        if(CacheConfigGlobals.METHODS_WITH_CONTENT.contains(request.getMethod())) {
            try {
                ResizeableByteBuffer byteBuffer = readBody(request, request.getContentLength());
                return new CacheKeyElement(byteBuffer.getBuf(),0,byteBuffer.size(),true);
            } catch(IOException e) {
                return CacheKeyElement.CACHE_KEY_ELEMENT_NOT_AVAILABLE;
            }
        } else {
            return CacheKeyElement.EMPTY_CACHE_KEY_ELEMENT;
        }
    }

    private ResizeableByteBuffer readBody(HttpServletRequest request, int contentLength) throws IOException {
        InputStream inputStream = request.getInputStream();
        return IOUtils.readStreamToResizeableByteBuffer(4096, inputStream, contentLength);
    }
}
