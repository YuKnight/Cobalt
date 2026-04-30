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
 * The optional {@code <action/>} projection. Banner deep-link
 * triple.
 */
@WhatsAppWebModule(moduleName = "WASmaxInBizCtwaActionBannerSuggestionRequest")
public final class SmaxBannerSuggestionAction {
    /**
     * The optional cross-platform deep-link URL.
     */
    private final String deepLink;

    /**
     * The optional iOS-local deep-link URL.
     */
    private final String localLink;

    /**
     * The optional Android-local deep-link URL.
     */
    private final String localAndroidLink;

    /**
     * Constructs a new action projection.
     *
     * @param deepLink         the optional deep link; may be
     *                         {@code null}
     * @param localLink        the optional iOS-local link; may be
     *                         {@code null}
     * @param localAndroidLink the optional Android-local link; may
     *                         be {@code null}
     */
    public SmaxBannerSuggestionAction(String deepLink, String localLink, String localAndroidLink) {
        this.deepLink = deepLink;
        this.localLink = localLink;
        this.localAndroidLink = localAndroidLink;
    }

    /**
     * Returns the optional deep-link URL.
     *
     * @return an {@link Optional} carrying the URL, or empty
     */
    public Optional<String> deepLink() {
        return Optional.ofNullable(deepLink);
    }

    /**
     * Returns the optional iOS-local link.
     *
     * @return an {@link Optional} carrying the link, or empty
     */
    public Optional<String> localLink() {
        return Optional.ofNullable(localLink);
    }

    /**
     * Returns the optional Android-local link.
     *
     * @return an {@link Optional} carrying the link, or empty
     */
    public Optional<String> localAndroidLink() {
        return Optional.ofNullable(localAndroidLink);
    }

    /**
     * Tries to parse the projection from the given node.
     *
     * @param node the {@code <action/>} node
     * @return an {@link Optional} carrying the projection, or empty
     *         when the node does not match the documented schema
     */
    @WhatsAppWebExport(moduleName = "WASmaxInBizCtwaActionBannerSuggestionRequest",
            exports = "parseBannerSuggestionRequestCtwaSuggestionBannerAction",
            adaptation = WhatsAppAdaptation.ADAPTED)
    public static Optional<SmaxBannerSuggestionAction> of(Node node) {
        Objects.requireNonNull(node, "node cannot be null");
        if (!node.hasDescription("action")) {
            return Optional.empty();
        }
        var deepLink = node.getAttributeAsString("deep_link").orElse(null);
        var localLink = node.getAttributeAsString("local_link").orElse(null);
        var localAndroidLink = node.getAttributeAsString("local_android_link").orElse(null);
        return Optional.of(new SmaxBannerSuggestionAction(deepLink, localLink, localAndroidLink));
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (obj == null || obj.getClass() != this.getClass()) {
            return false;
        }
        var that = (SmaxBannerSuggestionAction) obj;
        return Objects.equals(this.deepLink, that.deepLink)
                && Objects.equals(this.localLink, that.localLink)
                && Objects.equals(this.localAndroidLink, that.localAndroidLink);
    }

    @Override
    public int hashCode() {
        return Objects.hash(deepLink, localLink, localAndroidLink);
    }

    @Override
    public String toString() {
        return "SmaxBannerSuggestionAction[deepLink=" + deepLink
                + ", localLink=" + localLink
                + ", localAndroidLink=" + localAndroidLink + ']';
    }
}
