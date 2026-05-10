package com.github.auties00.cobalt.node.smax.biz;

import com.github.auties00.cobalt.meta.annotation.WhatsAppWebModule;

import java.util.Objects;

/**
 * Single {@code (id, type)} media entry. Used by both the outbound
 * primary {@code <media/>} child and the 0..10 outbound/inbound
 * {@code <media_list/>} children.
 */
@WhatsAppWebModule(moduleName = "WASmaxOutBizCtwaNativeAdUploadAdMediaRequest")
@WhatsAppWebModule(moduleName = "WASmaxInBizCtwaNativeAdUploadAdMediaResponseSuccess")
public final class SmaxUploadAdMediaMediaEntry {
    /**
     * The relay-allocated media identifier.
     */
    private final String id;

    /**
     * The media's enum kind ({@link SmaxUploadAdMediaMediaType#IMAGE} or
     * {@link SmaxUploadAdMediaMediaType#VIDEO}).
     */
    private final SmaxUploadAdMediaMediaType type;

    /**
     * Constructs a new entry.
     *
     * @param id   the media identifier; never {@code null}
     * @param type the media type; never {@code null}
     * @throws NullPointerException if either argument is {@code null}
     */
    public SmaxUploadAdMediaMediaEntry(String id, SmaxUploadAdMediaMediaType type) {
        this.id = Objects.requireNonNull(id, "id cannot be null");
        this.type = Objects.requireNonNull(type, "type cannot be null");
    }

    /**
     * Returns the media identifier.
     *
     * @return the id; never {@code null}
     */
    public String id() {
        return id;
    }

    /**
     * Returns the media type.
     *
     * @return the type; never {@code null}
     */
    public SmaxUploadAdMediaMediaType type() {
        return type;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (obj == null || obj.getClass() != this.getClass()) {
            return false;
        }
        var that = (SmaxUploadAdMediaMediaEntry) obj;
        return Objects.equals(this.id, that.id) && this.type == that.type;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, type);
    }

    @Override
    public String toString() {
        return "SmaxUploadAdMediaMediaEntry[id=" + id + ", type=" + type + ']';
    }
}
