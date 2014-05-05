package org.greencheek.web.filter.memcached.keyhashing;


import org.junit.Test;

import static org.junit.Assert.assertEquals;

public abstract class XXHashKeyHashingTest {

    abstract KeyHashing getKeyXXKeyHasher();


    @Test
    public void testHashing() {
        assertEquals("1401757748", getKeyXXKeyHasher().hash("Hello "));

        assertEquals("-234433905", getKeyXXKeyHasher().hash("Hello"));

        assertEquals("-2032373643", getKeyXXKeyHasher().hash("AB"));

        assertEquals("2072705615", getKeyXXKeyHasher().hash("CD"));

        assertEquals("46947589", getKeyXXKeyHasher().hash(""));
    }


}