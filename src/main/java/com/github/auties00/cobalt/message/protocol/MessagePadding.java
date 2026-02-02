package com.github.auties00.cobalt.message.protocol;

import com.github.auties00.cobalt.util.SecureBytes;

import java.util.Arrays;
import java.util.Objects;

/**
 * Utility class for adding and removing padding from message payloads.
 * <p>
 * WhatsApp messages use 1-16 random padding bytes at the end of the plaintext
 * to obfuscate message length and provide traffic analysis resistance.
 * <p>
 * The padding format is:
 * <pre>
 *   [original message bytes] [random padding bytes (1-16)]
 * </pre>
 * The last byte indicates the padding length.
 */
public final class MessagePadding {
    private static final int MIN_PADDING = 1;
    private static final int MAX_PADDING = 16;

    private MessagePadding() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }

    /**
     * Adds random padding to a plaintext message.
     * The padding length is random between 1-16 bytes.
     * The last byte of the padded result indicates the padding length.
     *
     * @param plaintext the original plaintext bytes
     * @return the padded plaintext
     */
    public static byte[] addPadding(byte[] plaintext) {
        Objects.requireNonNull(plaintext, "plaintext cannot be null");

        // Generate random padding length between 1 and 16
        var paddingLength = MIN_PADDING + (SecureBytes.random(1)[0] & 0x0F);

        var padded = new byte[plaintext.length + paddingLength];
        System.arraycopy(plaintext, 0, padded, 0, plaintext.length);

        // Fill with random bytes
        SecureBytes.random(padded, plaintext.length, paddingLength - 1);

        // Last byte indicates padding length
        padded[padded.length - 1] = (byte) paddingLength;

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
     * Validates that the padding in a message is valid without removing it.
     *
     * @param paddedPlaintext the padded plaintext bytes
     * @return true if the padding appears valid, false otherwise
     */
    public static boolean isValidPadding(byte[] paddedPlaintext) {
        if (paddedPlaintext == null || paddedPlaintext.length == 0) {
            return false;
        }

        var paddingLength = paddedPlaintext[paddedPlaintext.length - 1] & 0xFF;
        return paddingLength >= MIN_PADDING
                && paddingLength <= MAX_PADDING
                && paddingLength <= paddedPlaintext.length;
    }
}
