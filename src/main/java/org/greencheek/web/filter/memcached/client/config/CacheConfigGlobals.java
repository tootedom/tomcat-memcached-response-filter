package org.greencheek.web.filter.memcached.client.config;

import gnu.trove.map.TIntObjectMap;
import gnu.trove.map.hash.TIntObjectHashMap;
import gnu.trove.set.TIntSet;
import gnu.trove.set.hash.TIntHashSet;
import org.greencheek.web.filter.memcached.cachekey.DollarStringKeySpecFactory;
import org.greencheek.web.filter.memcached.cachekey.KeySpecFactory;
import org.greencheek.web.filter.memcached.keyhashing.JavaXXHashKeyHashing;
import org.greencheek.web.filter.memcached.keyhashing.KeyHashing;
import org.greencheek.web.filter.memcached.keyhashing.MessageDigestHashing;
import org.greencheek.web.filter.memcached.keyhashing.XXHashKeyHashing;
import org.greencheek.web.filter.memcached.util.*;

import java.io.UnsupportedEncodingException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Created by dominictootell on 16/04/2014.
 */
public class CacheConfigGlobals {

    public final static int DEFAULT_MAX_CACHEABLE_RESPONSE_BODY = 8192*2; //16k
    public final static int DEFAULT_INITIAL_CACHEABLE_RESPONSE_BODY = 4096; // 4k
    public final static int DEFAULT_MAX_CACHE_KEY_SIZE = 8192; //8kb

    public static final SplitByChar DEFAULT_CHAR_SPLITTER = new CustomSplitByChar();
    public static final JoinByChar DEFAULT_CHAR_JOINER = new CustomJoinByChar();
    public static final CharSeparatedValueSorter DEFAULT_CHAR_SEPARATED_VALUE_SORTER = new SplittingCharSeparatedValueSorter(DEFAULT_CHAR_SPLITTER,DEFAULT_CHAR_JOINER);
    public static final KeyHashing DEFAULT_MESSAGE_HASHING = new JavaXXHashKeyHashing();
    public static final KeySpecFactory DEFAULT_KEY_SPEC_FACTORY = new DollarStringKeySpecFactory(DEFAULT_CHAR_SPLITTER,DEFAULT_CHAR_SEPARATED_VALUE_SORTER);

    public static final String DEFAULT_CACHE_KEY = "$scheme$request_method$uri$args?$header_accept?$header_accept-encoding_s?";
    public static final int DEFAULT_MAX_POST_BODY_SIZE = 8192;
    public static final int DEFAULT_INITIAL_POST_BODY_SIZE = 2048;
    public final static String DEFAULT_CACHE_STATUS_HEADER_NAME = "X-Cache";
    public final static String DEFAULT_CACHE_MISS_HEADER_VALUE = "MISS";
    public final static String DEFAULT_CACHE_HIT_HEADER_VALUE = "HIT";
    public final static String DEFAULT_CACHEABLE_RESPONSE_CODES = "200, 203, 204, 205, 300, 301, 410";

    public static final String CACHE_CONTROL_HEADER = "Cache-Control";
    public static final String CONTENT_LENGTH_HEADER = "Content-Length";
    public static final String CONTENT_TYPE_HEADER = "Content-Type";
    public static final String DEFAULT_CONTENT_TYPE_HEADER_VALUE = "application/octet-stream";
    public static final byte[] DEFAULT_CONTENT_TYPE_HEADER_VALUE_AS_BYTES = getBytes(DEFAULT_CONTENT_TYPE_HEADER_VALUE);
    public static final byte[] CONTENT_TYPE_HEADER_AS_BYTES = getBytes(CONTENT_TYPE_HEADER);
    public static final byte[] CONTENT_LENGTH_HEADER_AS_BYTES = getBytes(CONTENT_LENGTH_HEADER);
    public static final String[] NO_CACHE_CLIENT_VALUE = new String[]{"no-cache"};
    public static final byte[] NEW_LINE = new byte[]{(byte)'\r',(byte)'\n'};
    public static final byte[] HEADER_NAME_SEPARATOR = new byte[]{':',' '};
    public static final TIntObjectMap<byte[]> STATUS_CODES;
    public static final TIntSet CACHEABLE_RESPONSE_CODES;

    public static final char CHAR_ZERO = '0';

    public static final Set<String> METHODS_WITH_CONTENT;

    static {
        Set<String> putAndPost = new HashSet<String>(24,1.0f);
        putAndPost.addAll(permutate("PUT"));
        putAndPost.addAll(permutate("POST"));
        METHODS_WITH_CONTENT = putAndPost;
    }

    static {
        CACHEABLE_RESPONSE_CODES = commaSeparatedIntStringToIntSet(DEFAULT_CACHEABLE_RESPONSE_CODES);
    }

    static {
        TIntObjectMap<byte[]> codes = new TIntObjectHashMap<byte[]>(48,1.0f);
        codes.put(100,getBytes("100 Continue"));
        codes.put(101,getBytes("101 Switching Protocols"));
        codes.put(102,getBytes("102 Processing"));
        codes.put(200,getBytes("200 OK"));
        codes.put(201,getBytes("201 Created"));
        codes.put(202,getBytes("202 Accepted"));
        codes.put(203,getBytes("203 Non Authoritative Information"));
        codes.put(204,getBytes("204 No Content"));
        codes.put(205,getBytes("205 Reset Content"));
        codes.put(206,getBytes("206 Partial Content"));
        codes.put(207,getBytes("207 Multi-Status"));
        codes.put(300,getBytes("300 Multiple Choices"));
        codes.put(301,getBytes("301 Moved Permanently"));
        codes.put(302,getBytes("302 Found"));
        codes.put(303,getBytes("303 See Other"));
        codes.put(304,getBytes("304 Not Modified"));
        codes.put(305,getBytes("305 Use Proxy"));
        codes.put(307,getBytes("307 Temporary Redirect"));
        codes.put(400,getBytes("400 Bad Request"));
        codes.put(401,getBytes("401 Unauthorized"));
        codes.put(402,getBytes("402 Payment Required"));
        codes.put(403,getBytes("403 Forbidden"));
        codes.put(404,getBytes("404 Not Found"));
        codes.put(405,getBytes("405 Method Not Allowed"));
        codes.put(406,getBytes("406 Not Acceptable"));
        codes.put(407,getBytes("407 Proxy Authentication Required"));
        codes.put(408,getBytes("408 Request Timeout"));
        codes.put(409,getBytes("409 Conflict"));
        codes.put(410,getBytes("410 Gone"));
        codes.put(411,getBytes("411 Length Required"));
        codes.put(412,getBytes("412 Precondition Failed"));
        codes.put(413,getBytes("413 Request Too Long"));
        codes.put(414,getBytes("414 Request-URI Too Long"));
        codes.put(415,getBytes("415 Unsupported Media Type"));
        codes.put(416,getBytes("416 Requested Range Not Satisfiable"));
        codes.put(417,getBytes("417 Expectation Failed"));
        codes.put(419,getBytes("419 Insufficient Space On Resource"));
        codes.put(420,getBytes("420 Method Failure"));
        codes.put(422,getBytes("422 Unprocessable Entity"));
        codes.put(423,getBytes("423 Locked"));
        codes.put(424,getBytes("424 Failed Dependency"));
        codes.put(500,getBytes("500 Internal Server Error"));
        codes.put(501,getBytes("501 Not Implemented"));
        codes.put(502,getBytes("502 Bad Gateway"));
        codes.put(503,getBytes("503 Service Unavailable"));
        codes.put(504,getBytes("504 Gateway Timeout"));
        codes.put(505,getBytes("505 Http Version Not Supported"));
        codes.put(507,getBytes("507 Insufficient Storage"));
        STATUS_CODES = codes;
    }


    public static byte[] getStatusCodeText(int code) {
        return STATUS_CODES.get(code);
    }

    public static byte[] getBytes(String content) {
        try {
            return content.getBytes("UTF-8");
        } catch (UnsupportedEncodingException e) {
            return content.getBytes();
        }
    }

    public static byte[] getASCIIBytes(String content) {
        try {
            return content.getBytes("US-ASCII");
        } catch (UnsupportedEncodingException e) {
            return content.getBytes();
        }
    }

    /**
     * Converts a positive int to its ASCII representation
     *
     * @param num
     * @return the byte array of representing the ascii
     */
    public static byte[] toByteArray(int num) {
        if(num<10) {
            return new byte[]{(byte)(num+CHAR_ZERO)};
        }

        int len = (int)(Math.log10(num));
        byte[] log = new byte[len+1];
        int i=len;
        do {
            long n = (num % 10)+CHAR_ZERO;
            log[i--] = (byte)n;
            num /=10;

        } while(num>9);
        log[i] = (byte)(num+CHAR_ZERO);

        return log;
    }


    public static final Set<String> DEFAULT_REQUEST_METHODS_TO_CACHE;
    static {
        Set<String> methods = new HashSet<String>(9);
        methods.addAll(permutate("GET"));
        DEFAULT_REQUEST_METHODS_TO_CACHE = methods;
    }


    public static Set<String> permutate( String s )
    {
        Set<String> listPermutations = new HashSet<String>();

        char[] array = s.toLowerCase().toCharArray();
        int iterations = (1 << array.length) - 1;

        for( int i = 0; i <= iterations; i++ )
        {
            for( int j = 0; j < array.length; j++ )
                array[j] = (i & (1<<j)) != 0
                        ? Character.toUpperCase( array[j] )
                        : Character.toLowerCase( array[j] );
            listPermutations.add(new String(array));
        }
        return listPermutations;
    }

    public static TIntSet commaSeparatedIntStringToIntSet(String ints) {
        if(ints==null) return new TIntHashSet(1);

        List<String> listOfCodes = DEFAULT_CHAR_SPLITTER.split(ints,',');
        if(listOfCodes==null || listOfCodes.size()==0) return new TIntHashSet(1);

        TIntSet statusCodes = new TIntHashSet(listOfCodes.size(),1.0f);
        for(String s : listOfCodes) {
            try {
                statusCodes.add(Integer.parseInt(s.trim()));
            } catch(NumberFormatException e) {

            }
        }
        return statusCodes;
    }

    public static int parseIntValue(String value,int defaultValue) {
        try {
            return Integer.parseInt(value);
        } catch(NumberFormatException e){
            return defaultValue;
        }
    }
}
