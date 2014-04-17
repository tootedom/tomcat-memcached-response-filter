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
    public static final String CONTENT_TYPE_HEADER = "Content-Length";
    public static final String NO_CACHE_CLIENT_VALUE = "no-cache";
    public static final byte[] NEW_LINE = new byte[]{(byte)'\r',(byte)'\n'};
    public static final byte[] HEADER_NAME_SEPARATOR = new byte[]{':',' '};
    public static final TIntObjectMap<String> STATUS_CODES;

    static {
        TIntObjectMap<String> codes = new TIntObjectHashMap<String>(48,1.0f);
        codes.put(100,"100 Continue");
        codes.put(101,"101 Switching Protocols");
        codes.put(102,"102 Processing");
        codes.put(200,"200 OK");
        codes.put(201,"201 Created");
        codes.put(202,"202 Accepted");
        codes.put(203,"203 Non Authoritative Information");
        codes.put(204,"204 No Content");
        codes.put(205,"205 Reset Content");
        codes.put(206,"206 Partial Content");
        codes.put(207,"207 Multi-Status");
        codes.put(300,"300 Multiple Choices");
        codes.put(301,"301 Moved Permanently");
        codes.put(302,"302 Found");
        codes.put(303,"303 See Other");
        codes.put(304,"304 Not Modified");
        codes.put(305,"305 Use Proxy");
        codes.put(307,"307 Temporary Redirect");
        codes.put(400,"400 Bad Request");
        codes.put(401,"401 Unauthorized");
        codes.put(402,"402 Payment Required");
        codes.put(403,"403 Forbidden");
        codes.put(404,"404 Not Found");
        codes.put(405,"405 Method Not Allowed");
        codes.put(406,"406 Not Acceptable");
        codes.put(407,"407 Proxy Authentication Required");
        codes.put(408,"408 Request Timeout");
        codes.put(409,"409 Conflict");
        codes.put(410,"410 Gone");
        codes.put(411,"411 Length Required");
        codes.put(412,"412 Precondition Failed");
        codes.put(413,"413 Request Too Long");
        codes.put(414,"414 Request-URI Too Long");
        codes.put(415,"415 Unsupported Media Type");
        codes.put(416,"416 Requested Range Not Satisfiable");
        codes.put(417,"417 Expectation Failed");
        codes.put(419,"419 Insufficient Space On Resource");
        codes.put(420,"420 Method Failure");
        codes.put(422,"422 Unprocessable Entity");
        codes.put(423,"423 Locked");
        codes.put(424,"424 Failed Dependency");
        codes.put(500,"500 Internal Server Error");
        codes.put(501,"501 Not Implemented");
        codes.put(502,"502 Bad Gateway");
        codes.put(503,"503 Service Unavailable");
        codes.put(504,"504 Gateway Timeout");
        codes.put(505,"505 Http Version Not Supported");
        codes.put(507,"507 Insufficient Storage");
        STATUS_CODES = codes;
    }


    public static String getStatusCodeText(int code) {
        return STATUS_CODES.get(code);
    }
}
