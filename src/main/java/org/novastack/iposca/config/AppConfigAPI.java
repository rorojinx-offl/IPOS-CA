package org.novastack.iposca.config;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

public class AppConfigAPI {
    public static byte[] encodeString(String s) {
        return s.getBytes(StandardCharsets.UTF_8);
    }

    public static String decodeByteToString(byte[] b) {
        return new String(b, StandardCharsets.UTF_8);
    }

    public static byte[] encodeInt(int i) {
        return ByteBuffer.allocate(Integer.BYTES).putInt(i).array();
    }

    public static int decodeByteToInt(byte[] b) throws IllegalArgumentException {
        if (b == null || b.length != Integer.BYTES) throw new IllegalArgumentException("Expected 4 bytes for int");

        return ByteBuffer.wrap(b).getInt();
    }
}
