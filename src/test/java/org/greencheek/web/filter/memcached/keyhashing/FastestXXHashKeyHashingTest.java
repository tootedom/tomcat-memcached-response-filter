package org.greencheek.web.filter.memcached.keyhashing;

import static org.junit.Assert.*;

public class FastestXXHashKeyHashingTest extends XXHashKeyHashingTest{

    @Override
    KeyHashing getKeyXXKeyHasher() {
        return new FastestXXHashKeyHashing();
    }
}