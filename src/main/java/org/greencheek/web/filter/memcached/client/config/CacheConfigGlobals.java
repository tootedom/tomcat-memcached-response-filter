package org.greencheek.web.filter.memcached.client.config;

import gnu.trove.decorator.TIntObjectMapDecorator;
import gnu.trove.map.TIntObjectMap;
import gnu.trove.map.hash.TIntObjectHashMap;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by dominictootell on 16/04/2014.
 */
public class CacheConfigGlobals {
    public static final String CACHE_CONTROL_HEADER = "Cache-Control";
    public static final String NO_CACHE_CLIENT_VALUE = "no-cache";
    public static final byte[] NEW_LINE = new byte[]{(byte)'\r',(byte)'\n'};
    public static final byte[] HEADER_NAME_SEPARATOR = new byte[]{':',' '};
    public static final TIntObjectMap<String> STATUS_CODES;

    static {
        TIntObjectMap<String> codes = new TIntObjectHashMap<String>(48,1.0f);
        codes.put(100,"Continue");
        codes.put(101,"Switching Protocols");
        codes.put(102,"Processing");
        codes.put(200,"OK");
        codes.put(201,"Created");
        codes.put(202,"Accepted");
        codes.put(203,"Non Authoritative Information");
        codes.put(204,"No Content");
        codes.put(205,"Reset Content");
        codes.put(206,"Partial Content");
        codes.put(207,"Multi-Status");
        codes.put(300,"Multiple Choices");
        codes.put(301,"Moved Permanently");
        codes.put(302,"Found");
        codes.put(303,"See Other");
        codes.put(304,"Not Modified");
        codes.put(305,"Use Proxy");
        codes.put(307,"Temporary Redirect");
        codes.put(400,"Bad Request");
        codes.put(401,"Unauthorized");
        codes.put(402,"Payment Required");
        codes.put(403,"Forbidden");
        codes.put(404,"Not Found");
        codes.put(405,"Method Not Allowed");
        codes.put(406,"Not Acceptable");
        codes.put(407,"Proxy Authentication Required");
        codes.put(408,"Request Timeout");
        codes.put(409,"Conflict");
        codes.put(410,"Gone");
        codes.put(411,"Length Required");
        codes.put(412,"Precondition Failed");
        codes.put(413,"Request Too Long");
        codes.put(414,"Request-URI Too Long");
        codes.put(415,"Unsupported Media Type");
        codes.put(416,"Requested Range Not Satisfiable");
        codes.put(417,"Expectation Failed");
        codes.put(419,"Insufficient Space On Resource");
        codes.put(420,"Method Failure");
        codes.put(422,"Unprocessable Entity");
        codes.put(423,"Locked");
        codes.put(424,"Failed Dependency");
        codes.put(500,"Internal Server Error");
        codes.put(501,"Not Implemented");
        codes.put(502,"Bad Gateway");
        codes.put(503,"Service Unavailable");
        codes.put(504,"Gateway Timeout");
        codes.put(505,"Http Version Not Supported");
        codes.put(507,"Insufficient Storage");
        STATUS_CODES = codes;
    }


    public static String getStatusCodeText(int code) {
        return STATUS_CODES.get(code);
    }
}
