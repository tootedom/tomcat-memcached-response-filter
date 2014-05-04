package org.greencheek.web.filter.memcached.client.config;

import org.greencheek.web.filter.memcached.util.CustomSplitByChar;
import org.greencheek.web.filter.memcached.util.SplitByChar;

import javax.servlet.http.HttpServletRequest;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Created by dominictootell on 04/05/2014.
 */
public class CommaSeparatedCacheableMethods implements CacheableMethods {

    public final Set<String> requestMethodsToCache;


    public CommaSeparatedCacheableMethods(String commaSeparatedMethodString, SplitByChar charSplitter) {
        requestMethodsToCache = new HashSet<String>(8);

        List<String> methods = charSplitter.split(commaSeparatedMethodString,',');
        for(String method : methods) {
            requestMethodsToCache.addAll(CacheConfigGlobals.permutate(method));
        }
    }

    @Override
    public boolean isCacheable(HttpServletRequest request) {
        return requestMethodsToCache.contains(request.getMethod());
    }

    public static void main(String[] args) {
        CommaSeparatedCacheableMethods methods = new CommaSeparatedCacheableMethods("GET",new CustomSplitByChar());
        System.out.println(methods.requestMethodsToCache);
    }
}
