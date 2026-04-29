package com.github.auties00.cobalt.node.iq.biz;

import java.util.Objects;

/**
 * One website slot — the relay supports up to two.
 */
public final class IqEditBusinessProfileWebsite {
    /**
     * The website URL — empty string clears the slot.
     */
    private final String url;

    /**
     * Constructs a slot.
     *
     * @param url the URL; never {@code null}
     * @throws NullPointerException if {@code url} is {@code null}
     */
    public IqEditBusinessProfileWebsite(String url) {
        this.url = Objects.requireNonNull(url, "url cannot be null");
    }

    /**
     * Returns the URL.
     *
     * @return the URL; never {@code null}
     */
    public String url() {
        return url;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (obj == null || obj.getClass() != this.getClass()) {
            return false;
        }
        var that = (IqEditBusinessProfileWebsite) obj;
        return Objects.equals(this.url, that.url);
    }

    @Override
    public int hashCode() {
        return Objects.hash(url);
    }

    @Override
    public String toString() {
        return "IqEditBusinessProfileWebsite[url=" + url + ']';
    }
}
