package com.github.auties00.cobalt.node.smax.biz;

import com.github.auties00.cobalt.meta.annotation.WhatsAppWebExport;
import com.github.auties00.cobalt.meta.annotation.WhatsAppWebModule;
import com.github.auties00.cobalt.meta.model.WhatsAppAdaptation;
import com.github.auties00.cobalt.node.Node;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * The {@code <native_actions_mixin/>} grandchild projection.
 * Bundles 0..50 {@link SmaxBannerSuggestionNativeAction} entries
 * harvested from the parent node's {@code <native_action/>}
 * children.
 */
@WhatsAppWebModule(moduleName = "WASmaxInBizCtwaActionNativeActionsMixinMixin")
public final class SmaxBannerSuggestionNativeActionsMixin {
    /**
     * The {@code <native_action/>} entries (0..50).
     */
    private final List<SmaxBannerSuggestionNativeAction> nativeAction;

    /**
     * Constructs a new mixin projection.
     *
     * @param nativeAction the native-action entries; may be
     *                     {@code null} (treated as empty)
     */
    public SmaxBannerSuggestionNativeActionsMixin(List<SmaxBannerSuggestionNativeAction> nativeAction) {
        this.nativeAction = nativeAction == null ? List.of() : List.copyOf(nativeAction);
    }

    /**
     * Returns the native-action entries.
     *
     * @return an unmodifiable list of 0..50 entries; never
     *         {@code null}
     */
    public List<SmaxBannerSuggestionNativeAction> nativeAction() {
        return nativeAction;
    }

    /**
     * Tries to parse the projection from the given parent node by
     * harvesting its {@code <native_action/>} children.
     *
     * @param node the parent node
     * @return an {@link Optional} carrying the projection, or empty
     *         when the node does not match the documented schema
     * @throws NullPointerException if {@code node} is {@code null}
     */
    @WhatsAppWebExport(moduleName = "WASmaxInBizCtwaActionNativeActionsMixinMixin",
            exports = "parseNativeActionsMixinMixin",
            adaptation = WhatsAppAdaptation.ADAPTED)
    public static Optional<SmaxBannerSuggestionNativeActionsMixin> of(Node node) {
        Objects.requireNonNull(node, "node cannot be null");
        // WASmaxParseUtils.mapChildrenWithTag(t, "native_action", 0, 50, e):
        // first enforce the 0..50 cardinality, then parse each child.
        var nativeActionNodes = node.streamChildren("native_action").toList();
        if (nativeActionNodes.size() > 50) {
            return Optional.empty();
        }
        var nativeActions = new ArrayList<SmaxBannerSuggestionNativeAction>(nativeActionNodes.size());
        for (var nativeActionNode : nativeActionNodes) {
            var parsed = SmaxBannerSuggestionNativeAction.of(nativeActionNode);
            if (parsed.isEmpty()) {
                return Optional.empty();
            }
            nativeActions.add(parsed.get());
        }
        return Optional.of(new SmaxBannerSuggestionNativeActionsMixin(nativeActions));
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (obj == null || obj.getClass() != this.getClass()) {
            return false;
        }
        var that = (SmaxBannerSuggestionNativeActionsMixin) obj;
        return Objects.equals(this.nativeAction, that.nativeAction);
    }

    @Override
    public int hashCode() {
        return Objects.hash(nativeAction);
    }

    @Override
    public String toString() {
        return "SmaxBannerSuggestionNativeActionsMixin[nativeAction=" + nativeAction + ']';
    }
}
