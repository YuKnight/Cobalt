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
 * Single {@code <native_action/>} entry from the
 * {@code native_actions_mixin} grandchild — describes a per-platform
 * deep-link target.
 */
@WhatsAppWebModule(moduleName = "WASmaxInBizCtwaActionNativeActionsMixinMixin")
public final class SmaxBannerSuggestionNativeAction {
    /**
     * The {@code platform} attribute (e.g. {@code "ios"},
     * {@code "android"}).
     */
    private final String platform;

    /**
     * The {@code min_app_version} attribute.
     */
    private final String minAppVersion;

    /**
     * The {@code local_link} attribute.
     */
    private final String localLink;

    /**
     * The optional {@code universal_link} attribute.
     */
    private final String universalLink;

    /**
     * Constructs a new native-action entry.
     *
     * @param platform      the platform; never {@code null}
     * @param minAppVersion the minimum app version; never
     *                      {@code null}
     * @param localLink     the local link; never {@code null}
     * @param universalLink the optional universal link; may be
     *                      {@code null}
     * @throws NullPointerException if any of {@code platform},
     *                              {@code minAppVersion} or
     *                              {@code localLink} is
     *                              {@code null}
     */
    public SmaxBannerSuggestionNativeAction(String platform, String minAppVersion,
                        String localLink, String universalLink) {
        this.platform = Objects.requireNonNull(platform, "platform cannot be null");
        this.minAppVersion = Objects.requireNonNull(minAppVersion, "minAppVersion cannot be null");
        this.localLink = Objects.requireNonNull(localLink, "localLink cannot be null");
        this.universalLink = universalLink;
    }

    /**
     * Returns the platform identifier.
     *
     * @return the platform; never {@code null}
     */
    public String platform() {
        return platform;
    }

    /**
     * Returns the minimum app version.
     *
     * @return the version; never {@code null}
     */
    public String minAppVersion() {
        return minAppVersion;
    }

    /**
     * Returns the local link.
     *
     * @return the link; never {@code null}
     */
    public String localLink() {
        return localLink;
    }

    /**
     * Returns the optional universal link.
     *
     * @return an {@link Optional} carrying the link, or empty
     */
    public Optional<String> universalLink() {
        return Optional.ofNullable(universalLink);
    }

    /**
     * Tries to parse the entry from the given node.
     *
     * @param node the {@code <native_action/>} node
     * @return an {@link Optional} carrying the parsed entry, or
     *         empty when the node does not match the documented
     *         schema
     */
    @WhatsAppWebExport(moduleName = "WASmaxInBizCtwaActionNativeActionsMixinMixin",
            exports = "parseNativeActionsMixinNativeAction",
            adaptation = WhatsAppAdaptation.ADAPTED)
    public static Optional<SmaxBannerSuggestionNativeAction> of(Node node) {
        Objects.requireNonNull(node, "node cannot be null");
        if (!node.hasDescription("native_action")) {
            return Optional.empty();
        }
        var platform = node.getAttributeAsString("platform").orElse(null);
        if (platform == null) {
            return Optional.empty();
        }
        var minAppVersion = node.getAttributeAsString("min_app_version").orElse(null);
        if (minAppVersion == null) {
            return Optional.empty();
        }
        var localLink = node.getAttributeAsString("local_link").orElse(null);
        if (localLink == null) {
            return Optional.empty();
        }
        var universalLink = node.getAttributeAsString("universal_link").orElse(null);
        return Optional.of(new SmaxBannerSuggestionNativeAction(platform, minAppVersion, localLink, universalLink));
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (obj == null || obj.getClass() != this.getClass()) {
            return false;
        }
        var that = (SmaxBannerSuggestionNativeAction) obj;
        return Objects.equals(this.platform, that.platform)
                && Objects.equals(this.minAppVersion, that.minAppVersion)
                && Objects.equals(this.localLink, that.localLink)
                && Objects.equals(this.universalLink, that.universalLink);
    }

    @Override
    public int hashCode() {
        return Objects.hash(platform, minAppVersion, localLink, universalLink);
    }

    @Override
    public String toString() {
        return "SmaxBannerSuggestionNativeAction[platform=" + platform
                + ", minAppVersion=" + minAppVersion
                + ", localLink=" + localLink
                + ", universalLink=" + universalLink + ']';
    }
}
