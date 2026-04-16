package com.github.auties00.cobalt.util;

import javax.crypto.spec.GCMParameterSpec;

/**
 * Utility for deriving a 96-bit AES-GCM nonce from a monotonically
 * increasing 64-bit counter.
 *
 * <p>The nonce layout is four leading zero bytes followed by the 64-bit
 * big-endian encoding of the counter, matching the Signal protocol nonce
 * derivation used by WhatsApp's Noise and Signal ciphers.
 *
 * @implNote Mirrors the nonce construction used throughout WA Web's
 *     {@code WANoiseCipher} and {@code WASignalCipher} modules; the
 *     authentication tag length is fixed at 128 bits.
 */
public final class GcmUtils {
    /**
     * Builds an AES-GCM parameter spec whose IV is derived from
     * {@code counter} using the standard zero-prefixed big-endian layout.
     *
     * @param counter the monotonically increasing counter value
     * @return a fresh {@link GCMParameterSpec} with a 128-bit tag and the
     *         derived 12-byte IV
     */
    public static GCMParameterSpec createNonce(long counter) {
        var iv = new byte[12];
        iv[4] = (byte) (counter >> 56);
        iv[5] = (byte) (counter >> 48);
        iv[6] = (byte) (counter >> 40);
        iv[7] = (byte) (counter >> 32);
        iv[8] = (byte) (counter >> 24);
        iv[9] = (byte) (counter >> 16);
        iv[10] = (byte) (counter >> 8);
        iv[11] = (byte) (counter);
        return new GCMParameterSpec(128, iv);
    }
}
