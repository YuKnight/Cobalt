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
        // WASmaxInChatstateStateSource.parseStateSource: try parseFromUserMixin first
        var fromUser = FromUser.of(node);
        if (fromUser.isPresent()) {
            return fromUser;
        }
        // WASmaxInChatstateStateSource.parseStateSource: fall back to parseFromGroupMixin
        return FromGroup.of(node);
    }

    /**
     * Reports whether the given JID's server is one accepted by
     * {@code WAJids.validateUserJid}, i.e. a phone, interop, msgr,
     * lid, or bot user JID.
     *
     * <p>This helper centralises the {@code USERJID_USERJID_USERJID}
     * and {@code USERJID_USERJID_USERJID_USERJID} JID-enum admit set
     * documented by {@code WASmaxInChatstateEnums}: each validator slot
     * resolves to {@code WAJids.validateUserJid}, whose regular expressions
     * accept the {@code @s.whatsapp.net}, {@code @interop}, {@code @msgr},
     * {@code @lid}, and {@code @bot} server domains.
     * @param jid the JID to test; never {@code null}
     * @return {@code true} when {@code jid.server()} is one of the
     *         user-JID server domains, {@code false} otherwise
     */
    @WhatsAppWebExport(moduleName = "WAJids",
            exports = "validateUserJid", adaptation = WhatsAppAdaptation.ADAPTED)
    private static boolean isUserJidServer(Jid jid) {
        var server = jid.server().toString();
        return "s.whatsapp.net".equals(server)
                || "interop".equals(server)
                || "msgr".equals(server)
                || "lid".equals(server)
                || "bot".equals(server);
    }

    /**
     * The {@code FromUser} variant. The chat-state event was
     * raised by a 1:1 peer.
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
            // WASmaxInChatstateFromUserMixin.parseFromUserMixin: assertTag(e,"chatstate")
            if (!node.hasDescription("chatstate")) {
                return Optional.empty();
            }
            // WASmaxInChatstateFromUserMixin.parseFromUserMixin: attrJidEnum(e,"from",USERJID_USERJID_USERJID_USERJID)
            // The USERJID_USERJID_USERJID_USERJID enum runs WAJids.validateUserJid four times,
            // which admits phone (s.whatsapp.net), interop, msgr, lid, and bot user JIDs.
            var from = node.getAttributeAsJid("from").orElse(null);
            if (from == null) {
                return Optional.empty();
            }
            if (!isUserJidServer(from)) {
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
     * The {@code FromGroup} variant. The chat-state event was
     * raised by a participant of a group chat.
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
            // WASmaxInChatstateFromGroupMixin.parseFromGroupMixin: assertTag(e,"chatstate")
            if (!node.hasDescription("chatstate")) {
                return Optional.empty();
            }
            // WASmaxInChatstateFromGroupMixin.parseFromGroupMixin: attrGroupJid(e,"from")
            var from = node.getAttributeAsJid("from").orElse(null);
            if (from == null || !"g.us".equals(from.server().toString())) {
                return Optional.empty();
            }
            // WASmaxInChatstateFromGroupMixin.parseFromGroupMixin: attrJidEnum(e,"participant",USERJID_USERJID_USERJID)
            var participant = node.getAttributeAsJid("participant").orElse(null);
            if (participant == null) {
                return Optional.empty();
            }
            if (!isUserJidServer(participant)) {
                return Optional.empty();
            }
            // WASmaxInChatstateFromGroupMixin.parseFromGroupMixin: optional(attrUserJid,e,"participant_pn")
            // optional() only fails when the attribute is present AND the inner validator rejects it;
            // an absent attribute yields a null value with success=true.
            Jid participantPn = null;
            if (node.getAttribute("participant_pn").isPresent()) {
                participantPn = node.getAttributeAsJid("participant_pn").orElse(null);
                if (participantPn == null || !isUserJidServer(participantPn)) {
                    return Optional.empty();
                }
            }
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
