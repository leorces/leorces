package com.leorces.persistence.postgres.utils;


import java.nio.ByteBuffer;
import java.security.SecureRandom;
import java.util.concurrent.atomic.AtomicInteger;


/**
 * Utility class for generating unique identifiers.
 * <p>
 * This generator creates MongoDB ObjectId-like identifiers consisting of:
 * - 4 bytes representing the seconds since Unix epoch
 * - 5 bytes of random value
 * - 3 bytes of incrementing counter
 * <p>
 * The resulting ID is a 24-character hexadecimal string.
 */
public class IdGenerator {

    /**
     * Hexadecimal characters for encoding
     */
    private static final char[] HEX_CHARS =
            {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f'};

    /**
     * Maximum value for 24-bit random value (2^24 - 1)
     */
    private static final int MAX_24_BIT_VALUE = 16777215;

    /**
     * Maximum value for 16-bit random value (2^15 - 1, using signed short)
     */
    private static final int MAX_15_BIT_VALUE = 32767;

    /**
     * Thread-safe counter for ensuring uniqueness
     */
    private static final AtomicInteger NEXT_COUNTER = new AtomicInteger(new SecureRandom().nextInt());


    /**
     * Private constructor to prevent instantiation of utility class.
     */
    private IdGenerator() {
        throw new UnsupportedOperationException("Utility class");
    }


    /**
     * Generates a new unique identifier.
     *
     * @return a 24-character hexadecimal string representing a unique ID
     */
    public static String getNewId() {
        var buffer = createIdBuffer();
        return convertToHexString(buffer.array());
    }


    private static ByteBuffer createIdBuffer() {
        var buffer = ByteBuffer.allocate(12);
        writeTimestamp(buffer);
        writeRandomValues(buffer);
        writeCounter(buffer);
        return buffer;
    }


    private static void writeTimestamp(ByteBuffer buffer) {
        int unixTime = (int) (System.currentTimeMillis() / 1000);
        buffer.put((byte) (unixTime >>> 24));
        buffer.put((byte) (unixTime >>> 16));
        buffer.put((byte) (unixTime >>> 8));
        buffer.put((byte) unixTime);
    }


    private static void writeRandomValues(ByteBuffer buffer) {
        var secureRandom = new SecureRandom();
        int randomValue1 = secureRandom.nextInt(MAX_24_BIT_VALUE + 1);
        short randomValue2 = (short) secureRandom.nextInt(MAX_15_BIT_VALUE + 1);

        buffer.put((byte) (randomValue1 >>> 16));
        buffer.put((byte) (randomValue1 >>> 8));
        buffer.put((byte) randomValue1);
        buffer.put((byte) (randomValue2 >>> 8));
        buffer.put((byte) randomValue2);
    }


    private static void writeCounter(ByteBuffer buffer) {
        int counter = NEXT_COUNTER.getAndIncrement() & MAX_24_BIT_VALUE;
        buffer.put((byte) (counter >>> 16));
        buffer.put((byte) (counter >>> 8));
        buffer.put((byte) counter);
    }


    private static String convertToHexString(byte[] bytes) {
        var chars = new char[24];
        int i = 0;
        for (byte b : bytes) {
            chars[i++] = HEX_CHARS[(b >>> 4) & 15];
            chars[i++] = HEX_CHARS[b & 15];
        }
        return new String(chars);
    }

}

