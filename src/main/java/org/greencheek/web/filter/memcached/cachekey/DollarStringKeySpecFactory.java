package org.greencheek.web.filter.memcached.cachekey;

import org.greencheek.web.filter.memcached.cachekey.extraction.*;
import org.greencheek.web.filter.memcached.util.CharSeparatedValueSorter;
import org.greencheek.web.filter.memcached.util.SplitByChar;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * Created by dominictootell on 25/04/2014.
 */
public class DollarStringKeySpecFactory implements KeySpecFactory {

    private static final Logger logger = LoggerFactory.getLogger(DollarStringKeySpecFactory.class);


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
    public static final String ARGS_STRING = "args";
    public static final String QUERY_STRING = "query";

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
     * represents that the request body can be used
     */
    public static final String BODY = "body";


    /**
     * represents that the content_type of the request can be used
     */
    public static final String CONTENT_TYPE = "content_type";
    public static final char KEY_SEPARATOR_CHAR = '$';
    public static final char VALUE_SEPARATOR_CHAR = '_';
    public static final String VALUE_OPTIONAL_SEPARATOR_CHAR = "?";
    public static final String VALUE_SORTED_CHAR = "_s?";
    public static final String VALUE_SORTED_CHAR_NO_OPTIONAL_CHAR = "_s";

    private static final String BODY_CHECKER = KEY_SEPARATOR_CHAR + BODY;


    private final SplitByChar charSplitter;
    private final CharSeparatedValueSorter valueSorter;

    public DollarStringKeySpecFactory(SplitByChar splitByChar,CharSeparatedValueSorter valueSorter) {
        this.charSplitter = splitByChar;
        this.valueSorter = valueSorter;
    }

    private KeyAttributeExtractor parseKeyElementValue(String keyElementValue) {
        if(keyElementValue.startsWith(PATH)) return PathAttributeExtractor.INSTANCE;
        else if(keyElementValue.startsWith(QUERY_STRING) || keyElementValue.startsWith(ARGS_STRING)) {
            if(isOptional(keyElementValue)) {
                return QueryAttributeExtractor.IS_OPTIONAL_INSTANCE;
            } else {
                return QueryAttributeExtractor.IS_REQUIRED_INSTANCE;
            }
        }
        else if(keyElementValue.startsWith(REQUEST_METHOD)) return MethodAttributeExtractor.INSTANCE;
        else if(keyElementValue.startsWith(PATH_AND_PARAMS)) {
            if(isOptional(keyElementValue)) {
                return PathAndQueryAttributeExtractor.IS_OPTIONAL_INSTANCE;
            } else {
                return PathAndQueryAttributeExtractor.IS_REQUIRED_INSTANCE;
            }
        }

        else if(keyElementValue.startsWith(SCHEME)) return SchemeAttributeExtractor.INSTANCE;
        else if(keyElementValue.startsWith(COOKIES)) return parseCookieKey(keyElementValue);
        else if(keyElementValue.startsWith(HEADERS)) return parseHeaderKey(keyElementValue);
        else if(keyElementValue.startsWith(CONTENT_TYPE)) return ContentTypeAttributeExtractor.INSTANCE;
        else if(keyElementValue.startsWith(BODY)) return RequestBodyAttributeExtractor.INSTANCE;
        return null;
    }

    private KeyAttributeExtractor parseCookieKey(String cookieKey) {
        boolean isOptional = isOptional(cookieKey);
        String value = getKeyValue(cookieKey,isOptional,false);
        if(value==null) {
            return null;
        }
        else {
            return new CookieAttributeExtractor(value,isOptional);
        }
    }


    private KeyAttributeExtractor parseHeaderKey(String headerKey) {
        boolean isOptional = isOptional(headerKey);
        boolean toBeSorted = isValueToBeSorted(headerKey);

        String value = getKeyValue(headerKey,isOptional,toBeSorted);
        if(value==null) {
            return null;
        }
        else {
            return new HeaderAttributeExtractor(value,isOptional,toBeSorted,valueSorter);
        }
    }

    private String getKeyValue(String keyAndValue,boolean optional,boolean sorted) {
        List<String> splitValues = getKeyNameAndValue(keyAndValue);
        if(splitValues.size()<2 || splitValues.get(1).length()==0) {
            return null;
        } else {
            String value = splitValues.get(1);
            if(!sorted && optional) {
                return value.substring(0,value.length()-1);
            } else {
                return value;
            }
        }
    }

    @Override
    public List<KeyAttributeExtractor> getKeySpecExtractors(String keySpec) {
        if(keySpec == null || keySpec.trim().length()==0) return Collections.EMPTY_LIST;

        List<String> keyItems = charSplitter.split(keySpec.toLowerCase(), KEY_SEPARATOR_CHAR);

        List<KeyAttributeExtractor> extractors = new ArrayList<KeyAttributeExtractor>(keyItems.size());

        for(String keyItem : keyItems) {
            KeyAttributeExtractor extractor = parseKeyElementValue(keyItem);
            if(extractor!=null) {
                extractors.add(extractor);
            } else {
                logger.warn("Unable to parse key element: {}",keyItem);
            }

        }

        return extractors;
    }

    @Override
    public boolean requiresBody(String keySpec) {
        return keySpec.contains(BODY_CHECKER);
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
