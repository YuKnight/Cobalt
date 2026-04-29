package com.github.auties00.cobalt.node.smax.biz;

import com.github.auties00.cobalt.meta.annotation.WhatsAppWebExport;
import com.github.auties00.cobalt.meta.annotation.WhatsAppWebModule;
import com.github.auties00.cobalt.meta.model.WhatsAppAdaptation;
import com.github.auties00.cobalt.model.jid.Jid;
import com.github.auties00.cobalt.node.Node;
import com.github.auties00.cobalt.node.smax.SmaxOperation;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Optional;

/**
 * The {@code <config/>} projection — banner lifecycle attributes.
 */
@WhatsAppWebModule(moduleName = "WASmaxInBizCtwaActionBannerSuggestionRequest")
public final class SmaxBannerSuggestionConfig {
    /**
     * The expiration timestamp (epoch seconds, &gt;= 1).
     */
    private final long expiresAt;

    /**
     * The display style.
     */
    private final SmaxBannerSuggestionBannerDisplay display;

    /**
     * The revocation marker.
     */
    private final SmaxBannerSuggestionFalseTrueFlag revoked;

    /**
     * Constructs a new config projection.
     *
     * @param expiresAt the expiration timestamp
     * @param display   the display style; never {@code null}
     * @param revoked   the revocation marker; never {@code null}
     * @throws NullPointerException     if {@code display} or
     *                                  {@code revoked} is
     *                                  {@code null}
     * @throws IllegalArgumentException if {@code expiresAt < 1}
     */
    public SmaxBannerSuggestionConfig(long expiresAt, SmaxBannerSuggestionBannerDisplay display, SmaxBannerSuggestionFalseTrueFlag revoked) {
        if (expiresAt < 1) {
            throw new IllegalArgumentException("expiresAt must be >= 1");
        }
        this.expiresAt = expiresAt;
        this.display = Objects.requireNonNull(display, "display cannot be null");
        this.revoked = Objects.requireNonNull(revoked, "revoked cannot be null");
    }

    /**
     * Returns the expiration timestamp.
     *
     * @return the timestamp in epoch seconds
     */
    public long expiresAt() {
        return expiresAt;
    }

    /**
     * Returns the display style.
     *
     * @return the style; never {@code null}
     */
    public SmaxBannerSuggestionBannerDisplay display() {
        return display;
    }

    /**
     * Returns the revocation marker.
     *
     * @return the marker; never {@code null}
     */
    public SmaxBannerSuggestionFalseTrueFlag revoked() {
        return revoked;
    }

    /**
     * Tries to parse the projection from the given node.
     *
     * @param node the {@code <config/>} node
     * @return an {@link Optional} carrying the projection, or empty
     *         when the node does not match the documented schema
     */
    public static Optional<SmaxBannerSuggestionConfig> of(Node node) {
        Objects.requireNonNull(node, "node cannot be null");
        if (!node.hasDescription("config")) {
            return Optional.empty();
        }
        var expiresAt = node.getAttributeAsLong("expires_at");
        if (expiresAt.isEmpty() || expiresAt.getAsLong() < 1) {
            return Optional.empty();
        }
        var displayStr = node.getAttributeAsString("display").orElse(null);
        var display = SmaxBannerSuggestionBannerDisplay.of(displayStr).orElse(null);
        if (display == null) {
            return Optional.empty();
        }
        var revokedStr = node.getAttributeAsString("revoked").orElse(null);
        var revoked = SmaxBannerSuggestionFalseTrueFlag.of(revokedStr).orElse(null);
        if (revoked == null) {
            return Optional.empty();
        }
        return Optional.of(new SmaxBannerSuggestionConfig(expiresAt.getAsLong(), display, revoked));
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (obj == null || obj.getClass() != this.getClass()) {
            return false;
        }
        var that = (SmaxBannerSuggestionConfig) obj;
        return this.expiresAt == that.expiresAt
                && this.display == that.display
                && this.revoked == that.revoked;
    }

    @Override
    public int hashCode() {
        return Objects.hash(expiresAt, display, revoked);
    }

    @Override
    public String toString() {
        return "SmaxBannerSuggestionConfig[expiresAt=" + expiresAt
                + ", display=" + display
                + ", revoked=" + revoked + ']';
    }
}
