package com.github.auties00.cobalt.node.smax.chatstate;

import com.github.auties00.cobalt.meta.annotation.WhatsAppWebExport;
import com.github.auties00.cobalt.meta.annotation.WhatsAppWebModule;
import com.github.auties00.cobalt.meta.model.WhatsAppAdaptation;
import com.github.auties00.cobalt.model.jid.Jid;
import com.github.auties00.cobalt.node.Node;
import com.github.auties00.cobalt.node.smax.SmaxOperation;
import java.util.Objects;
import java.util.Optional;

/**
 * Sealed disjunction over the two state-type child shapes
 * documented by
 * {@code WASmaxInChatstateStateTypes.parseStateTypes}.
 */
@WhatsAppWebModule(moduleName = "WASmaxInChatstateStateTypes")
public sealed interface SmaxServerNotificationStateType permits SmaxServerNotificationStateType.Composing, SmaxServerNotificationStateType.Paused {

    /**
     * Tries each {@link SmaxServerNotificationStateType} variant in WA Web declared
     * order and returns the first that parses cleanly.
     *
     * @param node the inbound {@code <chatstate/>} stanza
     * @return an {@link Optional} carrying the parsed variant
     */
    @WhatsAppWebExport(moduleName = "WASmaxInChatstateStateTypes",
            exports = "parseStateTypes", adaptation = WhatsAppAdaptation.ADAPTED)
    static Optional<? extends SmaxServerNotificationStateType> of(Node node) {
        Objects.requireNonNull(node, "node cannot be null");
        var composing = Composing.of(node);
        if (composing.isPresent()) {
            return composing;
        }
        return Paused.of(node);
    }

    /**
     * The {@code Composing} state-type — the peer is currently
     * typing.
     *
     * @implNote {@code WASmaxInChatstateComposingMixin.parseComposingMixin}
     *           extracts the {@code <composing/>} child and parses
     *           the optional {@code media} attribute as the literal
     *           {@code "audio"}.
     */
    @WhatsAppWebModule(moduleName = "WASmaxInChatstateComposingMixin")
    final class Composing implements SmaxServerNotificationStateType {
        /**
         * The optional literal {@code "audio"} {@code media}
         * attribute.
         */
        private final String composingMedia;

        /**
         * Constructs a new {@code Composing} variant.
         *
         * @param composingMedia the optional {@code media}
         *                       attribute; may be {@code null}
         */
        public Composing(String composingMedia) {
            this.composingMedia = composingMedia;
        }

        /**
         * Returns the optional {@code media} attribute.
         *
         * @return an {@link Optional} carrying the value, or empty
         *         when the peer is typing text rather than
         *         recording audio
         */
        public Optional<String> composingMedia() {
            return Optional.ofNullable(composingMedia);
        }

        /**
         * Tries to parse a {@link Composing} variant.
         *
         * @param node the inbound chatstate stanza
         * @return an {@link Optional} carrying the parsed variant
         */
        @WhatsAppWebExport(moduleName = "WASmaxInChatstateComposingMixin",
                exports = "parseComposingMixin",
                adaptation = WhatsAppAdaptation.ADAPTED)
        public static Optional<Composing> of(Node node) {
            if (!node.hasDescription("chatstate")) {
                return Optional.empty();
            }
            var composingChild = node.getChild("composing").orElse(null);
            if (composingChild == null) {
                return Optional.empty();
            }
            var media = composingChild.getAttributeAsString("media").orElse(null);
            if (media != null && !"audio".equals(media)) {
                return Optional.empty();
            }
            return Optional.of(new Composing(media));
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == this) {
                return true;
            }
            if (obj == null || obj.getClass() != this.getClass()) {
                return false;
            }
            var that = (Composing) obj;
            return Objects.equals(this.composingMedia, that.composingMedia);
        }

        @Override
        public int hashCode() {
            return Objects.hash(composingMedia);
        }

        @Override
        public String toString() {
            return "SmaxServerNotificationStateType.Composing[composingMedia="
                    + composingMedia + ']';
        }
    }

    /**
     * The {@code Paused} state-type — the peer has stopped typing.
     *
     * @implNote {@code WASmaxInChatstatePausedMixin.parsePausedMixin}
     *           validates the presence of a bare
     *           {@code <paused/>} child.
     */
    @WhatsAppWebModule(moduleName = "WASmaxInChatstatePausedMixin")
    final class Paused implements SmaxServerNotificationStateType {
        /**
         * Constructs a new {@code Paused} variant.
         */
        public Paused() {
        }

        /**
         * Tries to parse a {@link Paused} variant.
         *
         * @param node the inbound chatstate stanza
         * @return an {@link Optional} carrying the parsed variant
         */
        @WhatsAppWebExport(moduleName = "WASmaxInChatstatePausedMixin",
                exports = "parsePausedMixin",
                adaptation = WhatsAppAdaptation.ADAPTED)
        public static Optional<Paused> of(Node node) {
            if (!node.hasDescription("chatstate")) {
                return Optional.empty();
            }
            if (node.getChild("paused").isEmpty()) {
                return Optional.empty();
            }
            return Optional.of(new Paused());
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == this) {
                return true;
            }
            return obj != null && obj.getClass() == this.getClass();
        }

        @Override
        public int hashCode() {
            return Paused.class.hashCode();
        }

        @Override
        public String toString() {
            return "SmaxServerNotificationStateType.Paused[]";
        }
    }
}
