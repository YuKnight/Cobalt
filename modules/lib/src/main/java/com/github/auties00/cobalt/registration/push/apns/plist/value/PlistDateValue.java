package com.github.auties00.cobalt.registration.push.apns.plist.value;

import java.time.Instant;
import java.util.Objects;

/**
 * Plist date value. Binary plist dates are stored as seconds since
 * 2001-01-01 UTC and converted here to a UTC {@link Instant}.
 *
 * @param value the instant
 */
public record PlistDateValue(Instant value) implements PlistValue {
    /**
     * Canonical constructor with non-{@code null} guard.
     *
     * @param value the instant
     */
    public PlistDateValue {
        Objects.requireNonNull(value, "value");
    }
}
