package com.github.auties00.cobalt.registration.push.apns.plist.value;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Objects;
import java.util.Optional;
import java.util.SequencedMap;

/**
 * Plist dictionary node. Backed by a sequenced map so iteration
 * order matches the source order — important for tests and for the
 * FairPlay signature, which is computed over the exact serialized
 * bytes.
 *
 * <p>The {@link #entries()} accessor returns an unmodifiable view of
 * the backing map; the field itself is stored as-is to avoid the
 * defensive wrap on every construction.
 *
 * @param entries the ordered entries
 */
public record PlistDictionaryValue(SequencedMap<String, PlistValue> entries) implements PlistValue {
    /**
     * Canonical constructor, defensive against {@code null}.
     *
     * @param entries the ordered entries (non-{@code null})
     */
    public PlistDictionaryValue {
        Objects.requireNonNull(entries, "entries");
    }

    /**
     * Returns an unmodifiable view of the backing map.
     *
     * @return an unmodifiable sequenced map of the entries
     */
    @Override
    public SequencedMap<String, PlistValue> entries() {
        return Collections.unmodifiableSequencedMap(entries);
    }

    /**
     * Returns the value stored under {@code key}, if any.
     *
     * @param key the lookup key
     * @return an {@link Optional} containing the value, or empty when
     *         absent
     */
    public Optional<PlistValue> get(String key) {
        return Optional.ofNullable(entries.get(key));
    }

    /**
     * Builder for constructing dictionaries fluently.
     *
     * @return a new builder
     */
    public static Builder builder() {
        return new Builder();
    }

    /**
     * Mutable builder that produces an immutable
     * {@link PlistDictionaryValue}.
     */
    public static final class Builder {
        /**
         * Insertion-ordered backing map.
         */
        private final LinkedHashMap<String, PlistValue> map = new LinkedHashMap<>();

        /**
         * Hidden constructor — instances are obtained from
         * {@link PlistDictionaryValue#builder()}.
         */
        private Builder() {
        }

        /**
         * Adds an arbitrary value.
         *
         * @param key   the key
         * @param value the value
         * @return this builder
         */
        public Builder put(String key, PlistValue value) {
            map.put(Objects.requireNonNull(key, "key"), Objects.requireNonNull(value, "value"));
            return this;
        }

        /**
         * Adds a string value.
         *
         * @param key   the key
         * @param value the string (non-{@code null})
         * @return this builder
         */
        public Builder put(String key, String value) {
            return put(key, new PlistStringValue(value));
        }

        /**
         * Adds a data value, retaining the array by reference for
         * zero-copy serialization. The caller must not mutate
         * {@code value} afterwards.
         *
         * @param key   the key
         * @param value the bytes
         * @return this builder
         */
        public Builder put(String key, byte[] value) {
            return put(key, new PlistDataValue(value));
        }

        /**
         * Adds a boolean value.
         *
         * @param key   the key
         * @param value the boolean
         * @return this builder
         */
        public Builder put(String key, boolean value) {
            return put(key, new PlistBooleanValue(value));
        }

        /**
         * Adds a 64-bit integer value.
         *
         * @param key   the key
         * @param value the integer
         * @return this builder
         */
        public Builder put(String key, long value) {
            return put(key, new PlistIntegerValue(value));
        }

        /**
         * Freezes the entries into an immutable
         * {@link PlistDictionaryValue}. The underlying map is copied
         * so later mutations of the builder do not bleed into the
         * returned dictionary.
         *
         * @return the resulting dictionary
         */
        public PlistDictionaryValue build() {
            return new PlistDictionaryValue(new LinkedHashMap<>(map));
        }
    }
}
