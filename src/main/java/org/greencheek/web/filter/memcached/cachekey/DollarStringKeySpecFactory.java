package org.greencheek.web.filter.memcached.cachekey;

import org.greencheek.web.filter.memcached.cachekey.extraction.*;
import org.greencheek.web.filter.memcached.util.CharSeparatedValueSorter;
import org.greencheek.web.filter.memcached.util.JoinByChar;
import org.greencheek.web.filter.memcached.util.SplitByChar;

import java.util.*;

/**
 * Created by dominictootell on 25/04/2014.
 */
public class DollarStringKeySpecFactory implements KeySpecFactory{

    /**
     * represent GET|PUT|POST etc
     */
    public static final String REQUEST_METHOD = "request_method";

    /**
     * respresents the url path and the query parameters
     */
    public static final String PATH_AND_PARAMS = "request_uri";

    /**
     * represents just the url path
     */
    public static final String PATH = "uri";

    /**
     * repreents just the query parameters
     */
    public static final String QUERY_STRING = "args";

    /**
     * represents that a header is to be used.  header_accept
     */
    public static final String HEADERS = "header";

    /**
     * represents that a cookie can be used.
     */
    public static final String COOKIES = "cookie";

    /**
     * represents that the https/http can be used
     */
    public static final String SCHEME = "scheme";

    /**
     * represents that the content_type of the request can be used
     */
    public static final String CONTENT_TYPE = "content_type";
    public static final char KEY_SEPARATOR_CHAR = '$';
    public static final char VALUE_SEPARATOR_CHAR = '_';
    public static final String VALUE_OPTIONAL_SEPARATOR_CHAR = "?";
    public static final String VALUE_SORTED_CHAR = "_s?";
    public static final String VALUE_SORTED_CHAR_NO_OPTIONAL_CHAR = "_s";


    private final SplitByChar charSplitter;
    private final CharSeparatedValueSorter valueSorter;

    public DollarStringKeySpecFactory(SplitByChar splitByChar,CharSeparatedValueSorter valueSorter) {
        this.charSplitter = splitByChar;
        this.valueSorter = valueSorter;
    }

    private KeyAttributeExtractor parseKeyElementValue(String keyElementValue) {
        if(keyElementValue.startsWith(PATH)) return PathAttributeExtractor.INSTANCE;
        else if(keyElementValue.startsWith(QUERY_STRING)) return QueryAttributeExtractor.INSTANCE;
        else if(keyElementValue.startsWith(REQUEST_METHOD)) return MethodAttributeExtractor.INSTANCE;
        else if(keyElementValue.startsWith(PATH_AND_PARAMS)) return PathAndQueryAttributeExtractor.INSTANCE;
        else if(keyElementValue.startsWith(SCHEME)) return SchemeAttributeExtractor.INSTANCE;
        else if(keyElementValue.startsWith(COOKIES)) return parseCookieKey(keyElementValue);
        else if(keyElementValue.startsWith(HEADERS)) return parseHeaderKey(keyElementValue);
        else if(keyElementValue.startsWith(CONTENT_TYPE)) return ContentTypeAttributeExtractor.INSTANCE;
        return null;
    }

    private KeyAttributeExtractor parseCookieKey(String cookieKey) {
        boolean isOptional = isOptional(cookieKey);
        List<String> keyAndValue = getKeyNameAndValue(cookieKey);
        if(keyAndValue.size()<2) {
            return null;
        }
        else {
            return new CookieAttributeExtractor(keyAndValue.get(1),isOptional);
        }
    }


    private KeyAttributeExtractor parseHeaderKey(String cookieKey) {
        boolean isOptional = isOptional(cookieKey);
        boolean toBeSorted = isValueToBeSorted(cookieKey);

        List<String> keyAndValue = getKeyNameAndValue(cookieKey);
        if(keyAndValue.size()<2) {
            return null;
        }
        else {
            return new HeaderAttributeExtractor(keyAndValue.get(1),isOptional,toBeSorted,valueSorter);
        }
    }

    @Override
    public List<KeyAttributeExtractor> getKeySpecExtractors(String keySpec) {
        if(keySpec == null || keySpec.trim().length()==0) return Collections.EMPTY_LIST;

        List<String> keyItems = charSplitter.split(keySpec.toLowerCase(), KEY_SEPARATOR_CHAR);

        List<KeyAttributeExtractor> extractors = new ArrayList<KeyAttributeExtractor>(keyItems.size());

        for(String keyItem : keyItems) {
            KeyAttributeExtractor extractor = parseKeyElementValue(keyItem);
            extractors.add(extractor);
        }

        return extractors;
//
//        String[] keys = keySpec.split(KEY_SEPARATOR_CHAR);
//        List<KeyAttributeExtractor> keySet = new ArrayList<KeyAttributeExtractor>();
//        for(String item : keys) {
//            if(item == null || item.length()==0) continue;
//            keySet.add(item);
//
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
    }




    private boolean isOptional(String keyValue) {
        return keyValue.endsWith(VALUE_OPTIONAL_SEPARATOR_CHAR);
    }

    private boolean isValueToBeSorted(String keyValue) {
        return keyValue.endsWith(VALUE_SORTED_CHAR_NO_OPTIONAL_CHAR) || keyValue.endsWith(VALUE_SORTED_CHAR);
    }

    private List<String> getKeyNameAndValue(String keyValue) {
        return charSplitter.split(keyValue, VALUE_SEPARATOR_CHAR);
    }


}
