package com.github.auties00.cobalt.node.smax.chatstate;

import com.github.auties00.cobalt.meta.annotation.WhatsAppWebExport;
import com.github.auties00.cobalt.meta.annotation.WhatsAppWebModule;
import com.github.auties00.cobalt.meta.model.WhatsAppAdaptation;
import com.github.auties00.cobalt.model.jid.Jid;
import com.github.auties00.cobalt.node.NodeBuilder;
import com.github.auties00.cobalt.node.smax.SmaxOperation;
import java.util.Objects;
import java.util.Optional;

/**
 * The {@code SmaxClientNotificationComposing} state-type — the user is currently typing
 * (or recording an audio note when {@link #hasComposingMediaAudio()}
 * is {@code true}).
 *
 * @implNote {@code WASmaxOutChatstateComposingMixin.mergeComposingMixin}
 *           emits {@code smax("composing", {media:OPTIONAL_LITERAL("audio")})}.
 */
@WhatsAppWebModule(moduleName = "WASmaxOutChatstateComposingMixin")
public final class SmaxClientNotificationComposing implements SmaxClientNotificationStateType {
    /**
     * Whether the {@code media="audio"} marker should be emitted
     * (the user is recording a voice note rather than typing
     * text).
     */
    private final boolean hasComposingMediaAudio;

    /**
     * Constructs a new {@code SmaxClientNotificationComposing} state-type.
     *
     * @param hasComposingMediaAudio whether to emit the
     *                               {@code media="audio"} marker
     */
    public SmaxClientNotificationComposing(boolean hasComposingMediaAudio) {
        this.hasComposingMediaAudio = hasComposingMediaAudio;
    }

    /**
     * Returns whether the audio marker is set.
     *
     * @return {@code true} when {@code media="audio"} should be
     *         emitted
     */
    public boolean hasComposingMediaAudio() {
        return hasComposingMediaAudio;
    }

    /**
     * Builds the {@code <composing/>} child node.
     *
     * @return a {@link NodeBuilder} carrying the child
     */
    @Override
    @WhatsAppWebExport(moduleName = "WASmaxOutChatstateComposingMixin",
            exports = "mergeComposingMixin", adaptation = WhatsAppAdaptation.DIRECT)
    public NodeBuilder toNode() {
        return new NodeBuilder()
                .description("composing")
                .attribute("media", "audio", hasComposingMediaAudio);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (obj == null || obj.getClass() != this.getClass()) {
            return false;
        }
        var that = (SmaxClientNotificationComposing) obj;
        return this.hasComposingMediaAudio == that.hasComposingMediaAudio;
    }

    @Override
    public int hashCode() {
        return Objects.hash(hasComposingMediaAudio);
    }

    @Override
    public String toString() {
        return "SmaxClientNotificationComposing[hasComposingMediaAudio="
                + hasComposingMediaAudio + ']';
    }
}
