package com.github.auties00.cobalt.node.smax.presence;

import com.github.auties00.cobalt.meta.annotation.WhatsAppWebExport;
import com.github.auties00.cobalt.meta.annotation.WhatsAppWebModule;
import com.github.auties00.cobalt.meta.model.WhatsAppAdaptation;
import com.github.auties00.cobalt.model.jid.Jid;
import com.github.auties00.cobalt.node.Node;
import com.github.auties00.cobalt.node.smax.SmaxOperation;
import java.util.Objects;
import java.util.Optional;

/**
 * Sealed family of inbound presence-update variants.
 *
 * @implNote The five permits track
 *           {@code WASmaxInPresencePresenceUpdates.parsePresenceUpdates}.
 *           Try {@code GroupAvailable}, then
 *           {@code GroupUnavailable}, then
 *           {@code LastSeenWithOtherValue}, then
 *           {@code UserUnavailable}, then {@code Available}.
 */
@WhatsAppWebModule(moduleName = "WASmaxInPresenceServerUpdateRequest")
@WhatsAppWebModule(moduleName = "WASmaxInPresencePresenceUpdates")
public sealed interface SmaxServerUpdateResponse extends SmaxOperation.Response
        permits SmaxServerUpdateResponse.GroupAvailable, SmaxServerUpdateResponse.GroupUnavailable,
        SmaxServerUpdateResponse.LastSeenWithOtherValue, SmaxServerUpdateResponse.UserUnavailable,
        SmaxServerUpdateResponse.Available {

    /**
     * Tries each {@link SmaxServerUpdateResponse} variant in the WA Web declared
     * order and returns the first that parses cleanly.
     *
     * @param node the inbound {@code <presence/>} stanza. Never
     *             {@code null}
     * @return an {@link Optional} carrying the parsed variant, or
     *         {@link Optional#empty()} when no documented variant
     *         matched the stanza shape
     * @throws NullPointerException if {@code node} is {@code null}
     */
    @WhatsAppWebExport(moduleName = "WASmaxPresenceServerUpdateRPC",
            exports = "receiveServerUpdateRPC", adaptation = WhatsAppAdaptation.ADAPTED)
    @WhatsAppWebExport(moduleName = "WASmaxInPresencePresenceUpdates",
            exports = "parsePresenceUpdates", adaptation = WhatsAppAdaptation.ADAPTED)
    static Optional<? extends SmaxServerUpdateResponse> of(Node node) {
        Objects.requireNonNull(node, "node cannot be null");
        var groupAvailable = GroupAvailable.of(node);
        if (groupAvailable.isPresent()) {
            return groupAvailable;
        }
        var groupUnavailable = GroupUnavailable.of(node);
        if (groupUnavailable.isPresent()) {
            return groupUnavailable;
        }
        var lastSeenWithOtherValue = LastSeenWithOtherValue.of(node);
        if (lastSeenWithOtherValue.isPresent()) {
            return lastSeenWithOtherValue;
        }
        var userUnavailable = UserUnavailable.of(node);
        if (userUnavailable.isPresent()) {
            return userUnavailable;
        }
        return Available.of(node);
    }

    /**
     * The {@code GroupAvailable} variant. The relay reports how
     * many members of a group are currently online.
     *
     * @implNote {@code WASmaxInPresenceGroupAvailableMixin.parseGroupAvailableMixin}
     *           validates {@code description == "presence"},
     *           parses the group JID from {@code from}, and the
     *           online member count from {@code count} (range
     *           {@code [1, 1024]}).
     */
    @WhatsAppWebModule(moduleName = "WASmaxInPresenceGroupAvailableMixin")
    final class GroupAvailable implements SmaxServerUpdateResponse {
        /**
         * The group JID the count applies to.
         */
        private final Jid from;

        /**
         * The number of currently-online members
         * ({@code [1, 1024]}).
         */
        private final int count;

        /**
         * Constructs a new {@code GroupAvailable} projection.
         *
         * @param from  the group JID. Never {@code null}
         * @param count the online-member count
         * @throws NullPointerException if {@code from} is
         *                              {@code null}
         */
        public GroupAvailable(Jid from, int count) {
            this.from = Objects.requireNonNull(from, "from cannot be null");
            this.count = count;
        }

        /**
         * Returns the group JID.
         *
         * @return the group JID. Never {@code null}
         */
        public Jid from() {
            return from;
        }

        /**
         * Returns the online-member count.
         *
         * @return the count
         */
        public int count() {
            return count;
        }

        /**
         * Tries to parse a {@link GroupAvailable} variant from the
         * given stanza.
         *
         * @param node the inbound presence stanza
         * @return an {@link Optional} carrying the parsed variant,
         *         or empty when the stanza does not match the
         *         {@code GroupAvailable} schema
         */
        @WhatsAppWebExport(moduleName = "WASmaxInPresenceGroupAvailableMixin",
                exports = "parseGroupAvailableMixin",
                adaptation = WhatsAppAdaptation.ADAPTED)
        public static Optional<GroupAvailable> of(Node node) {
            if (!node.hasDescription("presence")) {
                return Optional.empty();
            }
            var from = node.getAttributeAsJid("from").orElse(null);
            if (from == null) {
                return Optional.empty();
            }
            if (!"g.us".equals(from.server().toString())) {
                return Optional.empty();
            }
            var count = node.getAttributeAsInt("count");
            if (count.isEmpty()) {
                return Optional.empty();
            }
            if (count.getAsInt() < 1 || count.getAsInt() > 1024) {
                return Optional.empty();
            }
            return Optional.of(new GroupAvailable(from, count.getAsInt()));
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == this) {
                return true;
            }
            if (obj == null || obj.getClass() != this.getClass()) {
                return false;
            }
            var that = (GroupAvailable) obj;
            return this.count == that.count && Objects.equals(this.from, that.from);
        }

        @Override
        public int hashCode() {
            return Objects.hash(from, count);
        }

        @Override
        public String toString() {
            return "SmaxServerUpdateResponse.GroupAvailable[from=" + from
                    + ", count=" + count + ']';
        }
    }

    /**
     * The {@code GroupUnavailable} variant. The relay reports the
     * group has dropped to zero online members.
     *
     * @implNote {@code WASmaxInPresenceGroupUnavailableMixin.parseGroupUnavailableMixin}
     *           validates {@code description == "presence"},
     *           parses the group JID from {@code from}, and asserts
     *           {@code type == "unavailable"}.
     */
    @WhatsAppWebModule(moduleName = "WASmaxInPresenceGroupUnavailableMixin")
    final class GroupUnavailable implements SmaxServerUpdateResponse {
        /**
         * The group JID gone idle.
         */
        private final Jid from;

        /**
         * Constructs a new {@code GroupUnavailable} projection.
         *
         * @param from the group JID. Never {@code null}
         * @throws NullPointerException if {@code from} is
         *                              {@code null}
         */
        public GroupUnavailable(Jid from) {
            this.from = Objects.requireNonNull(from, "from cannot be null");
        }

        /**
         * Returns the group JID.
         *
         * @return the group JID. Never {@code null}
         */
        public Jid from() {
            return from;
        }

        /**
         * Tries to parse a {@link GroupUnavailable} variant from
         * the given stanza.
         *
         * @param node the inbound presence stanza
         * @return an {@link Optional} carrying the parsed variant,
         *         or empty when the stanza does not match the
         *         {@code GroupUnavailable} schema
         */
        @WhatsAppWebExport(moduleName = "WASmaxInPresenceGroupUnavailableMixin",
                exports = "parseGroupUnavailableMixin",
                adaptation = WhatsAppAdaptation.ADAPTED)
        public static Optional<GroupUnavailable> of(Node node) {
            if (!node.hasDescription("presence")) {
                return Optional.empty();
            }
            var from = node.getAttributeAsJid("from").orElse(null);
            if (from == null) {
                return Optional.empty();
            }
            if (!"g.us".equals(from.server().toString())) {
                return Optional.empty();
            }
            if (!node.hasAttribute("type", "unavailable")) {
                return Optional.empty();
            }
            return Optional.of(new GroupUnavailable(from));
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == this) {
                return true;
            }
            if (obj == null || obj.getClass() != this.getClass()) {
                return false;
            }
            var that = (GroupUnavailable) obj;
            return Objects.equals(this.from, that.from);
        }

        @Override
        public int hashCode() {
            return Objects.hash(from);
        }

        @Override
        public String toString() {
            return "SmaxServerUpdateResponse.GroupUnavailable[from=" + from + ']';
        }
    }

    /**
     * The {@code LastSeenWithOtherValue} variant. The relay reports
     * the peer is offline and the last-seen value is a
     * privacy-suppressed sentinel ({@code "deny"} / {@code "error"}
     * / {@code "none"}).
     *
     * @implNote {@code WASmaxInPresenceLastSeenWithOtherValueMixin.parseLastSeenWithOtherValueMixin}
     *           validates the user JID, asserts
     *           {@code type == "unavailable"}, and parses the
     *           optional {@code last} attribute against the
     *           {@code ENUM_DENY_ERROR_NONE} enum
     *           ({@code WASmaxInPresenceEnums}).
     */
    @WhatsAppWebModule(moduleName = "WASmaxInPresenceLastSeenWithOtherValueMixin")
    @WhatsAppWebModule(moduleName = "WASmaxInPresenceEnums")
    final class LastSeenWithOtherValue implements SmaxServerUpdateResponse {
        /**
         * The user JID gone offline.
         */
        private final Jid from;

        /**
         * The optional sentinel from the
         * {@code ENUM_DENY_ERROR_NONE} set.
         */
        private final String last;

        /**
         * Constructs a new {@code LastSeenWithOtherValue}
         * projection.
         *
         * @param from the user JID. Never {@code null}
         * @param last the optional sentinel. May be {@code null}
         * @throws NullPointerException if {@code from} is
         *                              {@code null}
         */
        public LastSeenWithOtherValue(Jid from, String last) {
            this.from = Objects.requireNonNull(from, "from cannot be null");
            this.last = last;
        }

        /**
         * Returns the user JID.
         *
         * @return the user JID. Never {@code null}
         */
        public Jid from() {
            return from;
        }

        /**
         * Returns the optional sentinel value.
         *
         * @return an {@link Optional} carrying the sentinel
         *         ({@code "deny"} / {@code "error"} / {@code "none"}),
         *         or empty when the relay omitted it
         */
        public Optional<String> last() {
            return Optional.ofNullable(last);
        }

        /**
         * Tries to parse a {@link LastSeenWithOtherValue} variant
         * from the given stanza.
         *
         * @param node the inbound presence stanza
         * @return an {@link Optional} carrying the parsed variant,
         *         or empty when the stanza does not match the
         *         {@code LastSeenWithOtherValue} schema
         */
        @WhatsAppWebExport(moduleName = "WASmaxInPresenceLastSeenWithOtherValueMixin",
                exports = "parseLastSeenWithOtherValueMixin",
                adaptation = WhatsAppAdaptation.ADAPTED)
        public static Optional<LastSeenWithOtherValue> of(Node node) {
            if (!node.hasDescription("presence")) {
                return Optional.empty();
            }
            var from = node.getAttributeAsJid("from").orElse(null);
            if (from == null) {
                return Optional.empty();
            }
            var server = from.server().toString();
            if (!"s.whatsapp.net".equals(server) && !"c.us".equals(server)) {
                return Optional.empty();
            }
            if (!node.hasAttribute("type", "unavailable")) {
                return Optional.empty();
            }
            var last = node.getAttributeAsString("last").orElse(null);
            if (last != null && !"deny".equals(last) && !"error".equals(last) && !"none".equals(last)) {
                return Optional.empty();
            }
            return Optional.of(new LastSeenWithOtherValue(from, last));
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == this) {
                return true;
            }
            if (obj == null || obj.getClass() != this.getClass()) {
                return false;
            }
            var that = (LastSeenWithOtherValue) obj;
            return Objects.equals(this.from, that.from)
                    && Objects.equals(this.last, that.last);
        }

        @Override
        public int hashCode() {
            return Objects.hash(from, last);
        }

        @Override
        public String toString() {
            return "SmaxServerUpdateResponse.LastSeenWithOtherValue[from=" + from
                    + ", last=" + last + ']';
        }
    }

    /**
     * The {@code UserUnavailable} variant. The relay reports the
     * peer is offline with a free-form {@code last} string (a
     * Unix-timestamp-as-text. The actual last-seen moment).
     *
     * @implNote {@code WASmaxInPresenceUserUnavailableMixin.parseUserUnavailableMixin}
     *           validates the user JID, asserts
     *           {@code type == "unavailable"}, and parses the
     *           optional free-form {@code last} attribute.
     */
    @WhatsAppWebModule(moduleName = "WASmaxInPresenceUserUnavailableMixin")
    final class UserUnavailable implements SmaxServerUpdateResponse {
        /**
         * The user JID gone offline.
         */
        private final Jid from;

        /**
         * The optional free-form {@code last} attribute (the
         * Unix-timestamp-as-text last-seen moment).
         */
        private final String last;

        /**
         * Constructs a new {@code UserUnavailable} projection.
         *
         * @param from the user JID. Never {@code null}
         * @param last the optional free-form {@code last}. May be
         *             {@code null}
         * @throws NullPointerException if {@code from} is
         *                              {@code null}
         */
        public UserUnavailable(Jid from, String last) {
            this.from = Objects.requireNonNull(from, "from cannot be null");
            this.last = last;
        }

        /**
         * Returns the user JID.
         *
         * @return the user JID. Never {@code null}
         */
        public Jid from() {
            return from;
        }

        /**
         * Returns the optional free-form {@code last} value.
         *
         * @return an {@link Optional} carrying the value, or empty
         *         when the relay omitted it
         */
        public Optional<String> last() {
            return Optional.ofNullable(last);
        }

        /**
         * Tries to parse a {@link UserUnavailable} variant from the
         * given stanza.
         *
         * @param node the inbound presence stanza
         * @return an {@link Optional} carrying the parsed variant,
         *         or empty when the stanza does not match the
         *         {@code UserUnavailable} schema
         */
        @WhatsAppWebExport(moduleName = "WASmaxInPresenceUserUnavailableMixin",
                exports = "parseUserUnavailableMixin",
                adaptation = WhatsAppAdaptation.ADAPTED)
        public static Optional<UserUnavailable> of(Node node) {
            if (!node.hasDescription("presence")) {
                return Optional.empty();
            }
            var from = node.getAttributeAsJid("from").orElse(null);
            if (from == null) {
                return Optional.empty();
            }
            var server = from.server().toString();
            if (!"s.whatsapp.net".equals(server) && !"c.us".equals(server)) {
                return Optional.empty();
            }
            if (!node.hasAttribute("type", "unavailable")) {
                return Optional.empty();
            }
            var last = node.getAttributeAsString("last").orElse(null);
            return Optional.of(new UserUnavailable(from, last));
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == this) {
                return true;
            }
            if (obj == null || obj.getClass() != this.getClass()) {
                return false;
            }
            var that = (UserUnavailable) obj;
            return Objects.equals(this.from, that.from)
                    && Objects.equals(this.last, that.last);
        }

        @Override
        public int hashCode() {
            return Objects.hash(from, last);
        }

        @Override
        public String toString() {
            return "SmaxServerUpdateResponse.UserUnavailable[from=" + from
                    + ", last=" + last + ']';
        }
    }

    /**
     * The {@code Available} variant. The relay reports the peer
     * is online (group or user JID), with optional {@code type}
     * {@code "available"} literal and optional free-form
     * {@code last}.
     *
     * @implNote {@code WASmaxInPresenceAvailableMixin.parseAvailableMixin}
     *           validates the JID against
     *           {@code GROUPJID_USERJID}, parses the optional
     *           {@code type} as the literal {@code "available"},
     *           and the optional free-form {@code last}.
     */
    @WhatsAppWebModule(moduleName = "WASmaxInPresenceAvailableMixin")
    final class Available implements SmaxServerUpdateResponse {
        /**
         * The peer JID. Either a group or a user.
         */
        private final Jid from;

        /**
         * The optional literal {@code "available"} type tag.
         */
        private final String type;

        /**
         * The optional free-form {@code last} attribute.
         */
        private final String last;

        /**
         * Constructs a new {@code Available} projection.
         *
         * @param from the peer JID. Never {@code null}
         * @param type the optional literal {@code "available"}
         *             type tag. May be {@code null}
         * @param last the optional free-form {@code last}. May be
         *             {@code null}
         * @throws NullPointerException if {@code from} is
         *                              {@code null}
         */
        public Available(Jid from, String type, String last) {
            this.from = Objects.requireNonNull(from, "from cannot be null");
            this.type = type;
            this.last = last;
        }

        /**
         * Returns the peer JID.
         *
         * @return the JID. Never {@code null}
         */
        public Jid from() {
            return from;
        }

        /**
         * Returns the optional {@code type} attribute.
         *
         * @return an {@link Optional} carrying the type, or empty
         *         when the relay omitted it
         */
        public Optional<String> type() {
            return Optional.ofNullable(type);
        }

        /**
         * Returns the optional free-form {@code last} value.
         *
         * @return an {@link Optional} carrying the value, or empty
         *         when the relay omitted it
         */
        public Optional<String> last() {
            return Optional.ofNullable(last);
        }

        /**
         * Tries to parse an {@link Available} variant from the
         * given stanza.
         *
         * @param node the inbound presence stanza
         * @return an {@link Optional} carrying the parsed variant,
         *         or empty when the stanza does not match the
         *         {@code Available} schema
         */
        @WhatsAppWebExport(moduleName = "WASmaxInPresenceAvailableMixin",
                exports = "parseAvailableMixin",
                adaptation = WhatsAppAdaptation.ADAPTED)
        public static Optional<Available> of(Node node) {
            if (!node.hasDescription("presence")) {
                return Optional.empty();
            }
            var from = node.getAttributeAsJid("from").orElse(null);
            if (from == null) {
                return Optional.empty();
            }
            var server = from.server().toString();
            if (!"g.us".equals(server)
                    && !"s.whatsapp.net".equals(server)
                    && !"c.us".equals(server)) {
                return Optional.empty();
            }
            var type = node.getAttributeAsString("type").orElse(null);
            if (type != null && !"available".equals(type)) {
                return Optional.empty();
            }
            var last = node.getAttributeAsString("last").orElse(null);
            return Optional.of(new Available(from, type, last));
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == this) {
                return true;
            }
            if (obj == null || obj.getClass() != this.getClass()) {
                return false;
            }
            var that = (Available) obj;
            return Objects.equals(this.from, that.from)
                    && Objects.equals(this.type, that.type)
                    && Objects.equals(this.last, that.last);
        }

        @Override
        public int hashCode() {
            return Objects.hash(from, type, last);
        }

        @Override
        public String toString() {
            return "SmaxServerUpdateResponse.Available[from=" + from
                    + ", type=" + type
                    + ", last=" + last + ']';
        }
    }
}
