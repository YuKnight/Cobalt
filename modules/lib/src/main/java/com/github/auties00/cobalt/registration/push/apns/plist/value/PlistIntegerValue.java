package com.github.auties00.cobalt.registration.push.apns.plist.value;

/**
 * Plist integer value. All plist integer widths (1, 2, 4, 8 bytes)
 * fit into a {@code long}; 16-byte integers raise an
 * {@link java.io.IOException} during parsing.
 *
 * @param value the integer value
 */
public record PlistIntegerValue(long value) implements PlistValue {
}
