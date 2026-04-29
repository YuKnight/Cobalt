package com.github.auties00.cobalt.registration.push.apns.plist.value;

import java.util.Objects;

/**
 * Plist string value, already decoded from its source encoding into
 * a Java {@link String}.
 *
 * @param value the decoded string
 */
public record PlistStringValue(String value) implements PlistValue {
    /**
     * Canonical constructor with non-{@code null} guard.
     *
     * @param value the string
     */
    public PlistStringValue {
        Objects.requireNonNull(value, "value");
    }
}
