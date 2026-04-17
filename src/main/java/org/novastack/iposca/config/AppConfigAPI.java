package org.novastack.iposca.config;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

/**
 * Class that translates between primitive types (mainly ints and strings) and byte arrays.
 * */
public class AppConfigAPI {
    /**
     * Encodes a string into a byte array.
     * @param s The string to encode.
     * @return The encoded string as a byte array.
     * */
    public static byte[] encodeString(String s) {
        return s.getBytes(StandardCharsets.UTF_8);
    }

    /**
     * Decodes a byte array into a string.
     * @param b The byte array to decode.
     * @return The decoded string.
     * */
    public static String decodeByteToString(byte[] b) {
        return new String(b, StandardCharsets.UTF_8);
    }

    /**
     * Encodes an integer into a byte array.
     * @param i The integer to encode.
     * @return The encoded integer as a byte array.
     * */
    public static byte[] encodeInt(int i) {
        return ByteBuffer.allocate(Integer.BYTES).putInt(i).array();
    }

    /**
     * Decodes a byte array into an integer.
     * @param b The byte array to decode.
     * @return The decoded integer.
     * @throws IllegalArgumentException If the byte array is not of length 4.*/
    public static int decodeByteToInt(byte[] b) throws IllegalArgumentException {
        //Check if the byte array is of length 4 to check if it is an integer.
        if (b == null || b.length != Integer.BYTES) throw new IllegalArgumentException("Expected 4 bytes for int");

        return ByteBuffer.wrap(b).getInt();
    }
}
