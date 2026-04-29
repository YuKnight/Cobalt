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
 * Sealed disjunction over the two state-source mixins documented
 * by {@code WASmaxInChatstateStateSource.parseStateSource}.
 */
@WhatsAppWebModule(moduleName = "WASmaxInChatstateStateSource")
public sealed interface SmaxServerNotificationStateSource permits SmaxServerNotificationStateSource.FromUser, SmaxServerNotificationStateSource.FromGroup {

    /**
     * Tries each {@link SmaxServerNotificationStateSource} variant in WA Web declared
     * order and returns the first that parses cleanly.
     *
     * @param node the inbound {@code <chatstate/>} stanza
     * @return an {@link Optional} carrying the parsed variant
     */
    @WhatsAppWebExport(moduleName = "WASmaxInChatstateStateSource",
            exports = "parseStateSource", adaptation = WhatsAppAdaptation.ADAPTED)
    static Optional<? extends SmaxServerNotificationStateSource> of(Node node) {
        Objects.requireNonNull(node, "node cannot be null");
        var fromUser = FromUser.of(node);
        if (fromUser.isPresent()) {
            return fromUser;
        }
        return FromGroup.of(node);
    }

    /**
     * The {@code FromUser} variant — the chat-state event was
     * raised by a 1:1 peer.
     *
     * @implNote {@code WASmaxInChatstateFromUserMixin.parseFromUserMixin}
     *           validates the user JID against the
     *           {@code USERJID_USERJID_USERJID_USERJID} enum.
     */
    @WhatsAppWebModule(moduleName = "WASmaxInChatstateFromUserMixin")
    final class FromUser implements SmaxServerNotificationStateSource {
        /**
         * The user JID raising the event.
         */
        private final Jid from;

        /**
         * Constructs a new {@code FromUser} variant.
         *
         * @param from the user JID; never {@code null}
         * @throws NullPointerException if {@code from} is
         *                              {@code null}
         */
        public FromUser(Jid from) {
            this.from = Objects.requireNonNull(from, "from cannot be null");
        }

        /**
         * Returns the user JID.
         *
         * @return the JID; never {@code null}
         */
        public Jid from() {
            return from;
        }

        /**
         * Tries to parse a {@link FromUser} variant.
         *
         * @param node the inbound chatstate stanza
         * @return an {@link Optional} carrying the parsed variant
         */
        @WhatsAppWebExport(moduleName = "WASmaxInChatstateFromUserMixin",
                exports = "parseFromUserMixin", adaptation = WhatsAppAdaptation.ADAPTED)
        public static Optional<FromUser> of(Node node) {
            if (!node.hasDescription("chatstate")) {
                return Optional.empty();
            }
            var from = node.getAttributeAsJid("from").orElse(null);
            if (from == null) {
                return Optional.empty();
            }
            var server = from.server().toString();
            if (!"s.whatsapp.net".equals(server) && !"c.us".equals(server)
                    && !"lid".equals(server)) {
                return Optional.empty();
            }
            return Optional.of(new FromUser(from));
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == this) {
                return true;
            }
            if (obj == null || obj.getClass() != this.getClass()) {
                return false;
            }
            var that = (FromUser) obj;
            return Objects.equals(this.from, that.from);
        }

        @Override
        public int hashCode() {
            return Objects.hash(from);
        }

        @Override
        public String toString() {
            return "SmaxServerNotificationStateSource.FromUser[from=" + from + ']';
        }
    }

    /**
     * The {@code FromGroup} variant — the chat-state event was
     * raised by a participant of a group chat.
     *
     * @implNote {@code WASmaxInChatstateFromGroupMixin.parseFromGroupMixin}
     *           validates the group {@code from} JID, the
     *           participant's lid/wid {@code participant} JID, and
     *           the optional phone-number-form
     *           {@code participant_pn}.
     */
    @WhatsAppWebModule(moduleName = "WASmaxInChatstateFromGroupMixin")
    final class FromGroup implements SmaxServerNotificationStateSource {
        /**
         * The group JID hosting the chat.
         */
        private final Jid from;

        /**
         * The participant raising the event.
         */
        private final Jid participant;

        /**
         * The optional phone-number-form participant JID.
         */
        private final Jid participantPn;

        /**
         * Constructs a new {@code FromGroup} variant.
         *
         * @param from          the group JID; never {@code null}
         * @param participant   the participant JID; never
         *                      {@code null}
         * @param participantPn the optional pn-form participant
         *                      JID; may be {@code null}
         * @throws NullPointerException if {@code from} or
         *                              {@code participant} is
         *                              {@code null}
         */
        public FromGroup(Jid from, Jid participant, Jid participantPn) {
            this.from = Objects.requireNonNull(from, "from cannot be null");
            this.participant = Objects.requireNonNull(participant, "participant cannot be null");
            this.participantPn = participantPn;
        }

        /**
         * Returns the group JID.
         *
         * @return the JID; never {@code null}
         */
        public Jid from() {
            return from;
        }

        /**
         * Returns the participant JID.
         *
         * @return the JID; never {@code null}
         */
        public Jid participant() {
            return participant;
        }

        /**
         * Returns the optional pn-form participant JID.
         *
         * @return an {@link Optional} carrying the JID, or empty
         *         when the relay omitted it
         */
        public Optional<Jid> participantPn() {
            return Optional.ofNullable(participantPn);
        }

        /**
         * Tries to parse a {@link FromGroup} variant.
         *
         * @param node the inbound chatstate stanza
         * @return an {@link Optional} carrying the parsed variant
         */
        @WhatsAppWebExport(moduleName = "WASmaxInChatstateFromGroupMixin",
                exports = "parseFromGroupMixin", adaptation = WhatsAppAdaptation.ADAPTED)
        public static Optional<FromGroup> of(Node node) {
            if (!node.hasDescription("chatstate")) {
                return Optional.empty();
            }
            var from = node.getAttributeAsJid("from").orElse(null);
            if (from == null || !"g.us".equals(from.server().toString())) {
                return Optional.empty();
            }
            var participant = node.getAttributeAsJid("participant").orElse(null);
            if (participant == null) {
                return Optional.empty();
            }
            var participantServer = participant.server().toString();
            if (!"s.whatsapp.net".equals(participantServer) && !"c.us".equals(participantServer)
                    && !"lid".equals(participantServer)) {
                return Optional.empty();
            }
            var participantPn = node.getAttributeAsJid("participant_pn").orElse(null);
            return Optional.of(new FromGroup(from, participant, participantPn));
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == this) {
                return true;
            }
            if (obj == null || obj.getClass() != this.getClass()) {
                return false;
            }
            var that = (FromGroup) obj;
            return Objects.equals(this.from, that.from)
                    && Objects.equals(this.participant, that.participant)
                    && Objects.equals(this.participantPn, that.participantPn);
        }

        @Override
        public int hashCode() {
            return Objects.hash(from, participant, participantPn);
        }

        @Override
        public String toString() {
            return "SmaxServerNotificationStateSource.FromGroup[from=" + from
                    + ", participant=" + participant
                    + ", participantPn=" + participantPn + ']';
        }
    }
}
