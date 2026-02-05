package com.github.auties00.cobalt.message.send.util;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;

/**
 * Cryptographic utilities for token and binding generation.
 * <p>
 * Provides HKDF (HMAC-based Key Derivation Function) and HMAC operations
 * used by content binding and reporting token generation.
 *
 * @apiNote WACryptoHkdf.extractAndExpand
 */
public final class CryptoUtils {
    private static final String HMAC_SHA256 = "HmacSHA256";
    private static final int SHA256_OUTPUT_LENGTH = 32;

    private CryptoUtils() {
        throw new UnsupportedOperationException("Utility class");
    }

    /**
     * HKDF extract and expand using SHA-256.
     */
    public static byte[] hkdfExtractAndExpand(byte[] ikm, byte[] info, int length)
            throws NoSuchAlgorithmException, InvalidKeyException {
        var mac = Mac.getInstance(HMAC_SHA256);

        var salt = new byte[SHA256_OUTPUT_LENGTH];
        var saltKey = new SecretKeySpec(salt, HMAC_SHA256);
        mac.init(saltKey);
        var prk = mac.doFinal(ikm);

        var prkKey = new SecretKeySpec(prk, HMAC_SHA256);
        mac.init(prkKey);

        var result = new byte[length];
        var t = new byte[0];
        int offset = 0;
        int counter = 1;

        while (offset < length) {
            var input = new byte[t.length + info.length + 1];
            System.arraycopy(t, 0, input, 0, t.length);
            System.arraycopy(info, 0, input, t.length, info.length);
            input[input.length - 1] = (byte) counter;

            mac.init(prkKey);
            t = mac.doFinal(input);

            int copyLength = Math.min(t.length, length - offset);
            System.arraycopy(t, 0, result, offset, copyLength);
            offset += copyLength;
            counter++;
        }

        return result;
    }

    /**
     * Computes HMAC-SHA256 and truncates to the specified length.
     */
    public static byte[] hmacSha256Truncated(byte[] key, byte[] data, int truncateLength)
            throws NoSuchAlgorithmException, InvalidKeyException {
        var mac = Mac.getInstance(HMAC_SHA256);
        var keySpec = new SecretKeySpec(key, HMAC_SHA256);
        mac.init(keySpec);
        var hmac = mac.doFinal(data);

        if (truncateLength >= hmac.length) {
            return hmac;
        }
        return Arrays.copyOf(hmac, truncateLength);
    }

    /**
     * Computes full HMAC-SHA256.
     */
    public static byte[] hmacSha256(byte[] key, byte[] data)
            throws NoSuchAlgorithmException, InvalidKeyException {
        var mac = Mac.getInstance(HMAC_SHA256);
        var keySpec = new SecretKeySpec(key, HMAC_SHA256);
        mac.init(keySpec);
        return mac.doFinal(data);
    }
}
