package com.github.auties00.cobalt.registration.push.apns.plist.value;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * Plist array node — an ordered list of values.
 *
 * <p>The {@link #items()} accessor returns an unmodifiable view of
 * the backing list; the field itself is stored as-is to avoid the
 * defensive wrap on every construction.
 *
 * @param items the contained values
 */
public record PlistArrayValue(List<PlistValue> items) implements PlistValue {
    /**
     * Canonical constructor, defensive against {@code null}.
     *
     * @param items the items (non-{@code null})
     */
    public PlistArrayValue {
        Objects.requireNonNull(items, "items");
    }

    /**
     * Returns an unmodifiable view of the backing list.
     *
     * @return an unmodifiable list of the items
     */
    @Override
    public List<PlistValue> items() {
        return Collections.unmodifiableList(items);
    }
}
