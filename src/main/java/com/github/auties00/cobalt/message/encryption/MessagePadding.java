package com.github.auties00.cobalt.message.encryption;

import com.github.auties00.cobalt.util.SecureBytes;

import java.util.Arrays;
import java.util.Objects;

/**
 * Utility class for adding and removing padding from message payloads.
 * <p>
 * WhatsApp messages use PKCS#7 style padding with 1-16 bytes at the end of the plaintext.
 * All padding bytes contain the same value: the padding length.
 * <p>
 * The padding format is:
 * <pre>
 *   [original message bytes] [padding bytes (1-16, each byte = padding length)]
 * </pre>
 *
 * @apiNote WAWebSendMsgCommonApi.writeRandomPadMax16: uses PKCS#7 style where all padding
 * bytes have the same value (the padding length).
 */
public final class MessagePadding {
    private static final int MIN_PADDING = 1;
    private static final int MAX_PADDING = 16;

    private MessagePadding() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }

    /**
     * Adds PKCS#7 style padding to a plaintext message.
     * The padding length is random between 1-16 bytes.
     * All padding bytes contain the padding length value.
     *
     * @param plaintext the original plaintext bytes
     * @return the padded plaintext
     *
     * @apiNote WAWebSendMsgCommonApi.writeRandomPadMax16: PKCS#7 padding where all padding
     * bytes are set to the padding length value.
     */
    public static byte[] addPadding(byte[] plaintext) {
        Objects.requireNonNull(plaintext, "plaintext cannot be null");

        // Generate random padding length between 1 and 16
        var paddingLength = MIN_PADDING + (SecureBytes.random(1)[0] & 0x0F);

        var padded = new byte[plaintext.length + paddingLength];
        System.arraycopy(plaintext, 0, padded, 0, plaintext.length);

        // PKCS#7 padding: fill ALL padding bytes with the padding length value
        // Per WAWebSendMsgCommonApi.writeRandomPadMax16
        for (int i = plaintext.length; i < padded.length; i++) {
            padded[i] = (byte) paddingLength;
        }

        return padded;
    }

    /**
     * Removes padding from a decrypted message.
     * Reads the last byte to determine padding length and removes it.
     *
     * @param paddedPlaintext the padded plaintext bytes
     * @return the original plaintext without padding
     * @throws IllegalArgumentException if the padding is invalid
     */
    public static byte[] removePadding(byte[] paddedPlaintext) {
        Objects.requireNonNull(paddedPlaintext, "paddedPlaintext cannot be null");

        if (paddedPlaintext.length == 0) {
            throw new IllegalArgumentException("Padded plaintext cannot be empty");
        }

        // Last byte indicates padding length
        var paddingLength = paddedPlaintext[paddedPlaintext.length - 1] & 0xFF;

        if (paddingLength < MIN_PADDING || paddingLength > MAX_PADDING) {
            throw new IllegalArgumentException(
                    "Invalid padding length: " + paddingLength + " (expected " + MIN_PADDING + "-" + MAX_PADDING + ")"
            );
        }

        if (paddingLength > paddedPlaintext.length) {
            throw new IllegalArgumentException(
                    "Padding length " + paddingLength + " exceeds message length " + paddedPlaintext.length
            );
        }

        var originalLength = paddedPlaintext.length - paddingLength;
        return Arrays.copyOf(paddedPlaintext, originalLength);
    }

    /**
     * Validates that the padding in a message is valid PKCS#7 padding.
     * <p>
     * Validates that:
     * <ul>
     *   <li>The padding length (last byte) is between 1-16</li>
     *   <li>All padding bytes contain the same value (the padding length)</li>
     * </ul>
     *
     * @param paddedPlaintext the padded plaintext bytes
     * @return true if the padding is valid PKCS#7 padding, false otherwise
     */
    public static boolean isValidPadding(byte[] paddedPlaintext) {
        if (paddedPlaintext == null || paddedPlaintext.length == 0) {
            return false;
        }

        var paddingLength = paddedPlaintext[paddedPlaintext.length - 1] & 0xFF;
        if (paddingLength < MIN_PADDING || paddingLength > MAX_PADDING || paddingLength > paddedPlaintext.length) {
            return false;
        }

        // PKCS#7 validation: all padding bytes must have the same value
        var startIndex = paddedPlaintext.length - paddingLength;
        for (int i = startIndex; i < paddedPlaintext.length; i++) {
            if ((paddedPlaintext[i] & 0xFF) != paddingLength) {
                return false;
            }
        }

        return true;
    }
}
