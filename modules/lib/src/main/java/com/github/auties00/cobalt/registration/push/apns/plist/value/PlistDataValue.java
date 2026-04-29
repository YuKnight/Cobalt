package com.github.auties00.cobalt.registration.push.apns.plist.value;

import java.util.Arrays;
import java.util.Objects;

/**
 * Plist {@code <data>} value, stored as a slice over a backing
 * buffer to allow zero-copy parsing of binary plists. Use
 * {@link #toByteArray()} to obtain a defensive copy when ownership
 * is needed.
 *
 * @param source the backing buffer
 * @param offset the start offset within {@code source}
 * @param length the number of bytes
 */
public record PlistDataValue(byte[] source, int offset, int length) implements PlistValue {
    /**
     * Canonical constructor that validates the slice bounds.
     *
     * @param source the backing buffer
     * @param offset the start offset within {@code source}
     * @param length the number of bytes
     * @throws IndexOutOfBoundsException if the slice escapes the
     *                                   buffer
     */
    public PlistDataValue {
        Objects.requireNonNull(source, "source");
        Objects.checkFromIndexSize(offset, length, source.length);
    }

    /**
     * Wraps {@code bytes} as a {@code PlistDataValue} without
     * copying. The caller must not mutate {@code bytes} after this
     * call.
     *
     * @param bytes the source bytes
     */
    public PlistDataValue(byte[] bytes) {
        this(bytes, 0, bytes.length);
    }

    /**
     * Returns a freshly allocated copy of the stored bytes.
     *
     * @return a copy of the slice
     */
    public byte[] toByteArray() {
        return Arrays.copyOfRange(source, offset, offset + length);
    }
}
