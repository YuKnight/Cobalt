package com.github.auties00.cobalt.node.smax.biz;

import com.github.auties00.cobalt.meta.annotation.WhatsAppWebExport;
import com.github.auties00.cobalt.meta.annotation.WhatsAppWebModule;
import com.github.auties00.cobalt.meta.model.WhatsAppAdaptation;
import com.github.auties00.cobalt.node.Node;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * The {@code <banner/>} grandchild projection. Bundles
 * {@link SmaxBannerSuggestionConfig}, {@link SmaxBannerSuggestionContent}, {@link SmaxBannerSuggestionAction} and
 * {@link SmaxBannerSuggestionNativeAction} sub-trees.
 */
@WhatsAppWebModule(moduleName = "WASmaxInBizCtwaActionBannerSuggestionRequest")
public final class SmaxBannerSuggestionBanner {
    /**
     * The mandatory {@code <config/>} projection.
     */
    private final SmaxBannerSuggestionConfig config;

    /**
     * The mandatory {@code <content/>} projection.
     */
    private final SmaxBannerSuggestionContent content;

    /**
     * The optional {@code <action/>} projection.
     */
    private final SmaxBannerSuggestionAction action;

    /**
     * The optional native-actions list (0..50 entries).
     */
    private final List<SmaxBannerSuggestionNativeAction> nativeActions;

    /**
     * Constructs a new banner projection.
     *
     * @param config        the config projection; never
     *                      {@code null}
     * @param content       the content projection; never
     *                      {@code null}
     * @param action        the optional action projection; may be
     *                      {@code null}
     * @param nativeActions the native actions list; may be
     *                      {@code null} (treated as empty)
     * @throws NullPointerException if {@code config} or
     *                              {@code content} is {@code null}
     */
    public SmaxBannerSuggestionBanner(SmaxBannerSuggestionConfig config, SmaxBannerSuggestionContent content, SmaxBannerSuggestionAction action, List<SmaxBannerSuggestionNativeAction> nativeActions) {
        this.config = Objects.requireNonNull(config, "config cannot be null");
        this.content = Objects.requireNonNull(content, "content cannot be null");
        this.action = action;
        this.nativeActions = nativeActions == null ? List.of() : List.copyOf(nativeActions);
    }

    /**
     * Returns the config projection.
     *
     * @return the config; never {@code null}
     */
    public SmaxBannerSuggestionConfig config() {
        return config;
    }

    /**
     * Returns the content projection.
     *
     * @return the content; never {@code null}
     */
    public SmaxBannerSuggestionContent content() {
        return content;
    }

    /**
     * Returns the optional action projection.
     *
     * @return an {@link Optional} carrying the projection, or empty
     */
    public Optional<SmaxBannerSuggestionAction> action() {
        return Optional.ofNullable(action);
    }

    /**
     * Returns the native-actions list.
     *
     * @return an unmodifiable list of 0..50 entries; never
     *         {@code null}
     */
    public List<SmaxBannerSuggestionNativeAction> nativeActions() {
        return nativeActions;
    }

    /**
     * Tries to parse the projection from the given node.
     *
     * @param node the {@code <banner/>} node
     * @return an {@link Optional} carrying the projection, or empty
     *         when the node does not match the documented schema
     */
    @WhatsAppWebExport(moduleName = "WASmaxInBizCtwaActionBannerSuggestionRequest",
            exports = "parseBannerSuggestionRequestCtwaSuggestionBanner",
            adaptation = WhatsAppAdaptation.ADAPTED)
    public static Optional<SmaxBannerSuggestionBanner> of(Node node) {
        Objects.requireNonNull(node, "node cannot be null");
        // WASmaxParseUtils.assertTag(t, "banner")
        if (!node.hasDescription("banner")) {
            return Optional.empty();
        }
        // WASmaxParseUtils.flattenedChildWithTag(t, "config")
        var configNode = node.getChild("config").orElse(null);
        if (configNode == null) {
            return Optional.empty();
        }
        var config = SmaxBannerSuggestionConfig.of(configNode).orElse(null);
        if (config == null) {
            return Optional.empty();
        }
        // WASmaxParseUtils.flattenedChildWithTag(t, "content")
        var contentNode = node.getChild("content").orElse(null);
        if (contentNode == null) {
            return Optional.empty();
        }
        var content = SmaxBannerSuggestionContent.of(contentNode).orElse(null);
        if (content == null) {
            return Optional.empty();
        }
        // WASmaxParseUtils.optionalChildWithTag(t, "action", e)
        SmaxBannerSuggestionAction action = null;
        var actionNode = node.getChild("action").orElse(null);
        if (actionNode != null) {
            var parsed = SmaxBannerSuggestionAction.of(actionNode);
            if (parsed.isEmpty()) {
                return Optional.empty();
            }
            action = parsed.get();
        }
        // WASmaxInBizCtwaActionNativeActionsMixinMixin.parseNativeActionsMixinMixin(t):
        // delegate to the mixin so the 0..50 cardinality check fires
        // before any native-action child is parsed.
        var nativeActionsMixin = SmaxBannerSuggestionNativeActionsMixin.of(node).orElse(null);
        if (nativeActionsMixin == null) {
            return Optional.empty();
        }
        return Optional.of(new SmaxBannerSuggestionBanner(config, content, action,
                nativeActionsMixin.nativeAction()));
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (obj == null || obj.getClass() != this.getClass()) {
            return false;
        }
        var that = (SmaxBannerSuggestionBanner) obj;
        return Objects.equals(this.config, that.config)
                && Objects.equals(this.content, that.content)
                && Objects.equals(this.action, that.action)
                && Objects.equals(this.nativeActions, that.nativeActions);
    }

    @Override
    public int hashCode() {
        return Objects.hash(config, content, action, nativeActions);
    }

    @Override
    public String toString() {
        return "SmaxBannerSuggestionBanner[config=" + config
                + ", content=" + content
                + ", action=" + action
                + ", nativeActions=" + nativeActions + ']';
    }
}
