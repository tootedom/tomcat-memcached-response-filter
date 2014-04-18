package org.greencheek.web.filter.memcached.client;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * Created by dominictootell on 18/04/2014.
 */
public class MaxAgeParserTest {

    @Test
    public void testMaxAgeIsReturned() {
        MaxAgeParser parser = new DefaultMaxAgeParser();
        MaxAgeParser regExpParser = new RegexMaxAgeParser();

        assertEquals(10,parser.maxAge("max-age=10",3));
        assertEquals(10,regExpParser.maxAge("max-age=10",3));
    }

    @Test
    public void testMaxAgeIsReturnedWhenSurrounded() {
        MaxAgeParser parser = new DefaultMaxAgeParser();
        MaxAgeParser regExpParser = new RegexMaxAgeParser();


        assertEquals(10,parser.maxAge("xxx=kkkk, max-age=10, revalidate",3));
        assertEquals(10,regExpParser.maxAge("xxx=kkkk, max-age=10, revalidate",3));

    }

    @Test
    public void testMaxAgeIsReturnedAtEnd() {
        MaxAgeParser parser = new DefaultMaxAgeParser();
        MaxAgeParser regExpParser = new RegexMaxAgeParser();


        assertEquals(10,parser.maxAge("xxx=kkkk, revalidate, max-age=10",3));
        assertEquals(10,regExpParser.maxAge("xxx=kkkk, revalidate, max-age=10",3));

    }

    @Test
    public void testMaxAgeIsReturnedAtStart() {
        MaxAgeParser parser = new DefaultMaxAgeParser();
        MaxAgeParser regExpParser = new RegexMaxAgeParser();


        assertEquals(10,parser.maxAge("max-age=10,revalidate,xxx=kkkk",3));
        assertEquals(10,regExpParser.maxAge("max-age=10,revalidate,xxx=kkkk",3));

    }

    @Test
    public void testMaxAgeWithCommaIsReturned() {
        MaxAgeParser parser = new DefaultMaxAgeParser();
        MaxAgeParser regExpParser = new RegexMaxAgeParser();


        assertEquals(10,parser.maxAge("max-age=10,",3));
        assertEquals(10,regExpParser.maxAge("max-age=10,",3));

    }

    @Test
    public void testMaxAgeDefaultIsReturned() {
        MaxAgeParser parser = new DefaultMaxAgeParser();
        MaxAgeParser regExpParser = new RegexMaxAgeParser();


        assertEquals(3,parser.maxAge("max-ag=10,",3));
        assertEquals(3,regExpParser.maxAge("max-ag=10,",3));

        assertEquals(3,parser.maxAge("",3));
        assertEquals(3,regExpParser.maxAge("",3));

    }

    @Test
    public void testMaxAgeWithNothingDefaultIsReturned() {
        MaxAgeParser parser = new DefaultMaxAgeParser();
        MaxAgeParser regExpParser = new RegexMaxAgeParser();


        assertEquals(3,parser.maxAge("max-age=",3));
        assertEquals(3,regExpParser.maxAge("max-age=",3));

        assertEquals(3,parser.maxAge("",3));
        assertEquals(3,regExpParser.maxAge("",3));

    }

    @Test
    public void testMaxAgeWithLargeZerosDefaultIsReturned() {
        MaxAgeParser parser = new DefaultMaxAgeParser();
        MaxAgeParser regExpParser = new RegexMaxAgeParser();


        assertEquals(0,parser.maxAge("max-age=0000",3));
        assertEquals(0,regExpParser.maxAge("max-age=0000",3));

    }

    @Test
    public void testMaxAgeWithNinesDefaultIsReturned() {
        MaxAgeParser parser = new DefaultMaxAgeParser();
        MaxAgeParser regExpParser = new RegexMaxAgeParser();


        assertEquals(9999999,parser.maxAge("max-age=9999999",3));
        assertEquals(9999999,regExpParser.maxAge("max-age=9999999",3));

    }
}
