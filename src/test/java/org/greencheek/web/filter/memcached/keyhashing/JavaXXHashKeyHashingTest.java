package org.greencheek.web.filter.memcached.keyhashing;


public class JavaXXHashKeyHashingTest extends XXHashKeyHashingTest{

    @Override
    KeyHashing getKeyXXKeyHasher() {
        return new JavaXXHashKeyHashing();
    }
}