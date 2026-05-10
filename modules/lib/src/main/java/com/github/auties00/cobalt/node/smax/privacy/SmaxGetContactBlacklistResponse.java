package com.github.auties00.cobalt.node.smax.privacy;

import com.github.auties00.cobalt.meta.annotation.WhatsAppWebExport;
import com.github.auties00.cobalt.meta.annotation.WhatsAppWebModule;
import com.github.auties00.cobalt.meta.model.WhatsAppAdaptation;
import com.github.auties00.cobalt.model.jid.Jid;
import com.github.auties00.cobalt.model.jid.JidServer;
import com.github.auties00.cobalt.node.Node;
import com.github.auties00.cobalt.node.NodeBuilder;
import com.github.auties00.cobalt.node.smax.SmaxOperation;
import com.github.auties00.cobalt.node.smax.util.SmaxIqErrorResponseMixin;
import com.github.auties00.cobalt.node.smax.util.SmaxIqResultResponseMixin;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * Sealed family of inbound reply variants produced by the relay.
 */
public sealed interface SmaxGetContactBlacklistResponse extends SmaxOperation.Response
        permits SmaxGetContactBlacklistResponse.SuccessLID, SmaxGetContactBlacklistResponse.Success, SmaxGetContactBlacklistResponse.Error {

    /**
     * Tries each {@link SmaxGetContactBlacklistResponse} variant in priority order and
     * returns the first that parses cleanly.
     *
     * @param node    the inbound IQ stanza received from the relay;
     *                never {@code null}
     * @param request the original outbound stanza. Used to validate
     *                echoed identifiers; never {@code null}
     * @return an {@link Optional} carrying the parsed variant, or
     *         empty when no documented variant matched
     * @throws NullPointerException if either argument is {@code null}
     */
    @WhatsAppWebExport(moduleName = "WASmaxPrivacyGetContactBlacklistRPC",
            exports = "sendGetContactBlacklistRPC", adaptation = WhatsAppAdaptation.ADAPTED)
    static Optional<? extends SmaxGetContactBlacklistResponse> of(Node node, Node request) {
        Objects.requireNonNull(node, "node cannot be null");
        Objects.requireNonNull(request, "request cannot be null");
        var successLid = SuccessLID.of(node, request);
        if (successLid.isPresent()) {
            return successLid;
        }
        var success = Success.of(node, request);
        if (success.isPresent()) {
            return success;
        }
        return Error.of(node, request);
    }

    /**
     * One {@code <user/>} child of the LID-addressed success variant.
     * carries an optional LID JID plus the
     * {@link SmaxGetContactBlacklistContactListId} discriminator.
     */
    final class LidUser {
        /**
         * The optional LID JID of this entry.
         */
        private final Jid jid;

        /**
         * The {@code contactListIds} discriminator. Never
         * {@code null}.
         */
        private final SmaxGetContactBlacklistContactListId contactListId;

        /**
         * Constructs a LID user entry.
         *
         * @param jid           the optional LID JID; may be
         *                      {@code null}
         * @param contactListId the discriminator; never {@code null}
         * @throws NullPointerException if {@code contactListId} is
         *                              {@code null}
         */
        public LidUser(Jid jid, SmaxGetContactBlacklistContactListId contactListId) {
            this.jid = jid;
            this.contactListId = Objects.requireNonNull(contactListId, "contactListId cannot be null");
        }

        /**
         * Returns the optional LID JID.
         *
         * @return an {@link Optional} carrying the JID, or empty when
         *         the relay omitted it
         */
        public Optional<Jid> jid() {
            return Optional.ofNullable(jid);
        }

        /**
         * Returns the {@code contactListIds} discriminator.
         *
         * @return the discriminator; never {@code null}
         */
        public SmaxGetContactBlacklistContactListId contactListId() {
            return contactListId;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == this) {
                return true;
            }
            if (obj == null || obj.getClass() != this.getClass()) {
                return false;
            }
            var that = (LidUser) obj;
            return Objects.equals(this.jid, that.jid)
                    && Objects.equals(this.contactListId, that.contactListId);
        }

        @Override
        public int hashCode() {
            return Objects.hash(jid, contactListId);
        }

        @Override
        public String toString() {
            return "SmaxGetContactBlacklistResponse.LidUser[jid=" + jid
                    + ", contactListId=" + contactListId + ']';
        }
    }

    /**
     * One {@code <user/>} child of the legacy PN-addressed success
     * variant. Carries a required PN JID plus an optional LID echo.
     */
    final class PnUser {
        /**
         * The legacy PN JID of this entry.
         */
        private final Jid jid;

        /**
         * The optional LID echo. Populated when the relay has
         * already migrated the entry.
         */
        private final Jid lid;

        /**
         * Constructs a PN user entry.
         *
         * @param jid the PN JID; never {@code null}
         * @param lid the optional LID echo; may be {@code null}
         * @throws NullPointerException if {@code jid} is {@code null}
         */
        public PnUser(Jid jid, Jid lid) {
            this.jid = Objects.requireNonNull(jid, "jid cannot be null");
            this.lid = lid;
        }

        /**
         * Returns the PN JID.
         *
         * @return the JID; never {@code null}
         */
        public Jid jid() {
            return jid;
        }

        /**
         * Returns the optional LID echo.
         *
         * @return an {@link Optional} carrying the LID JID, or empty
         *         when omitted
         */
        public Optional<Jid> lid() {
            return Optional.ofNullable(lid);
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == this) {
                return true;
            }
            if (obj == null || obj.getClass() != this.getClass()) {
                return false;
            }
            var that = (PnUser) obj;
            return Objects.equals(this.jid, that.jid)
                    && Objects.equals(this.lid, that.lid);
        }

        @Override
        public int hashCode() {
            return Objects.hash(jid, lid);
        }

        @Override
        public String toString() {
            return "SmaxGetContactBlacklistResponse.PnUser[jid=" + jid
                    + ", lid=" + lid + ']';
        }
    }

    /**
     * Validates the IQ-result envelope and extracts the inner
     * {@code <privacy/>} child common to both success variants.
     *
     * @param node    the inbound stanza
     * @param request the original outbound request
     * @return an {@link Optional} carrying the {@code <privacy/>}
     *         child, or empty when the envelope check fails or the
     *         child is missing
     */
    private static Optional<Node> validateSuccessEnvelope(Node node, Node request) {
        if (!SmaxIqResultResponseMixin.validate(node, request)) {
            return Optional.empty();
        }
        return node.getChild("privacy");
    }

    /**
     * Parses a single {@code <user/>} child as a {@link SmaxGetContactBlacklistContactListId}
     * discriminator, mirroring the WA Web disjunction priority
     * ({@code Username} → {@code PnJid} → {@code Empty}).
     *
     * @param userNode the {@code <user/>} child node
     * @return the parsed discriminator; never {@code null}
     */
    private static SmaxGetContactBlacklistContactListId parseContactListId(Node userNode) {
        var username = userNode.getAttributeAsString("username").orElse(null);
        if (username != null) {
            return new SmaxGetContactBlacklistContactListId.Username(username);
        }
        var pnJid = userNode.getAttributeAsString("pn_jid")
                .map(Jid::of)
                .orElse(null);
        if (pnJid != null) {
            return new SmaxGetContactBlacklistContactListId.PnJid(pnJid);
        }
        return new SmaxGetContactBlacklistContactListId.Empty();
    }

    /**
     * The {@code SuccessLID} reply variant. The relay returned a
     * {@code <privacy addressing_mode="lid">} envelope carrying the
     * LID-addressed contact-blacklist entries.
     */
    @WhatsAppWebModule(moduleName = "WASmaxInPrivacyGetContactBlacklistResponseSuccessLID")
    @WhatsAppWebModule(moduleName = "WASmaxInPrivacyDeprecatedIQResultResponseOptionalFromMixin")
    final class SuccessLID implements SmaxGetContactBlacklistResponse {
        /**
         * The optional list-side digest. Present only when the relay
         * emitted a {@code <list/>} child.
         */
        private final String listDhash;

        /**
         * The parsed list of LID-addressed user entries. Empty when
         * the relay omitted the {@code <list/>} child entirely.
         */
        private final List<LidUser> users;

        /**
         * Constructs a {@code SuccessLID} reply.
         *
         * @param listDhash the optional list digest; may be
         *                  {@code null}
         * @param users     the parsed list of users; never
         *                  {@code null}
         * @throws NullPointerException if {@code users} is
         *                              {@code null}
         */
        public SuccessLID(String listDhash, List<LidUser> users) {
            this.listDhash = listDhash;
            this.users = List.copyOf(Objects.requireNonNull(users, "users cannot be null"));
        }

        /**
         * Returns the optional list digest.
         *
         * @return an {@link Optional} carrying the digest, or empty
         *         when the {@code <list/>} child was omitted
         */
        public Optional<String> listDhash() {
            return Optional.ofNullable(listDhash);
        }

        /**
         * Returns the parsed list of LID-addressed user entries.
         *
         * @return an unmodifiable list of users; never {@code null}
         */
        public List<LidUser> users() {
            return users;
        }

        /**
         * Tries to parse a {@link SuccessLID} variant.
         *
         * @param node    the inbound stanza
         * @param request the original outbound request
         * @return an {@link Optional} carrying the parsed variant, or
         *         empty when the envelope shape does not match
         */
        @WhatsAppWebExport(moduleName = "WASmaxInPrivacyGetContactBlacklistResponseSuccessLID",
                exports = "parseGetContactBlacklistResponseSuccessLID",
                adaptation = WhatsAppAdaptation.ADAPTED)
        public static Optional<SuccessLID> of(Node node, Node request) {
            var privacy = validateSuccessEnvelope(node, request).orElse(null);
            if (privacy == null) {
                return Optional.empty();
            }
            if (!privacy.hasAttribute("addressing_mode", "lid")) {
                return Optional.empty();
            }
            var list = privacy.getChild("list").orElse(null);
            if (list == null) {
                return Optional.of(new SuccessLID(null, List.of()));
            }
            var dhash = list.getAttributeAsString("dhash").orElse(null);
            if (dhash == null) {
                return Optional.empty();
            }
            var users = new ArrayList<LidUser>();
            for (var child : list.getChildren("user")) {
                var jid = child.getAttributeAsString("jid")
                        .map(Jid::of)
                        .orElse(null);
                var contactListId = parseContactListId(child);
                users.add(new LidUser(jid, contactListId));
            }
            return Optional.of(new SuccessLID(dhash, Collections.unmodifiableList(users)));
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == this) {
                return true;
            }
            if (obj == null || obj.getClass() != this.getClass()) {
                return false;
            }
            var that = (SuccessLID) obj;
            return Objects.equals(this.listDhash, that.listDhash)
                    && Objects.equals(this.users, that.users);
        }

        @Override
        public int hashCode() {
            return Objects.hash(listDhash, users);
        }

        @Override
        public String toString() {
            return "SmaxGetContactBlacklistResponse.SuccessLID[listDhash=" + listDhash
                    + ", users=" + users + ']';
        }
    }

    /**
     * The {@code Success} reply variant. The legacy PN-addressed
     * success envelope. The {@code addressing_mode} attribute is
     * optional but, when present, must equal {@code "pn"}.
     */
    @WhatsAppWebModule(moduleName = "WASmaxInPrivacyGetContactBlacklistResponseSuccess")
    @WhatsAppWebModule(moduleName = "WASmaxInPrivacyDeprecatedIQResultResponseOptionalFromMixin")
    final class Success implements SmaxGetContactBlacklistResponse {
        /**
         * The optional list-side digest. Present only when the relay
         * emitted a {@code <list/>} child.
         */
        private final String listDhash;

        /**
         * The parsed list of PN-addressed user entries. Empty when
         * the relay omitted the {@code <list/>} child entirely.
         */
        private final List<PnUser> users;

        /**
         * Constructs a {@code Success} reply.
         *
         * @param listDhash the optional list digest; may be
         *                  {@code null}
         * @param users     the parsed list of users; never
         *                  {@code null}
         * @throws NullPointerException if {@code users} is
         *                              {@code null}
         */
        public Success(String listDhash, List<PnUser> users) {
            this.listDhash = listDhash;
            this.users = List.copyOf(Objects.requireNonNull(users, "users cannot be null"));
        }

        /**
         * Returns the optional list digest.
         *
         * @return an {@link Optional} carrying the digest, or empty
         *         when the {@code <list/>} child was omitted
         */
        public Optional<String> listDhash() {
            return Optional.ofNullable(listDhash);
        }

        /**
         * Returns the parsed list of PN-addressed user entries.
         *
         * @return an unmodifiable list of users; never {@code null}
         */
        public List<PnUser> users() {
            return users;
        }

        /**
         * Tries to parse a {@link Success} variant.
         *
         * @param node    the inbound stanza
         * @param request the original outbound request
         * @return an {@link Optional} carrying the parsed variant, or
         *         empty when the envelope shape does not match
         */
        @WhatsAppWebExport(moduleName = "WASmaxInPrivacyGetContactBlacklistResponseSuccess",
                exports = "parseGetContactBlacklistResponseSuccess",
                adaptation = WhatsAppAdaptation.ADAPTED)
        public static Optional<Success> of(Node node, Node request) {
            var privacy = validateSuccessEnvelope(node, request).orElse(null);
            if (privacy == null) {
                return Optional.empty();
            }
            var addressingMode = privacy.getAttributeAsString("addressing_mode").orElse(null);
            if (addressingMode != null && !addressingMode.equals("pn")) {
                return Optional.empty();
            }
            var list = privacy.getChild("list").orElse(null);
            if (list == null) {
                return Optional.of(new Success(null, List.of()));
            }
            var dhash = list.getAttributeAsString("dhash").orElse(null);
            if (dhash == null) {
                return Optional.empty();
            }
            var users = new ArrayList<PnUser>();
            for (var child : list.getChildren("user")) {
                var jid = child.getAttributeAsString("jid")
                        .map(Jid::of)
                        .orElse(null);
                if (jid == null) {
                    return Optional.empty();
                }
                var lid = child.getAttributeAsString("lid")
                        .map(Jid::of)
                        .orElse(null);
                users.add(new PnUser(jid, lid));
            }
            return Optional.of(new Success(dhash, Collections.unmodifiableList(users)));
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == this) {
                return true;
            }
            if (obj == null || obj.getClass() != this.getClass()) {
                return false;
            }
            var that = (Success) obj;
            return Objects.equals(this.listDhash, that.listDhash)
                    && Objects.equals(this.users, that.users);
        }

        @Override
        public int hashCode() {
            return Objects.hash(listDhash, users);
        }

        @Override
        public String toString() {
            return "SmaxGetContactBlacklistResponse.Success[listDhash=" + listDhash
                    + ", users=" + users + ']';
        }
    }

    /**
     * The {@code Error} reply variant. Covers every documented
     * {@code <iq type="error">} shape produced by the relay
     * ({@code BadRequest}, {@code FeatureNotImplemented},
     * {@code ServiceUnavailable}, {@code RateOverlimit},
     * {@code InternalServerError}). Collapsed to the universal
     * {@code (errorCode, errorText)} pair since the per-shape
     * disjunction carries no additional payload.
     */
    @WhatsAppWebModule(moduleName = "WASmaxInPrivacyGetContactBlacklistResponseError")
    @WhatsAppWebModule(moduleName = "WASmaxInPrivacyGetPrivacyListError")
    @WhatsAppWebModule(moduleName = "WASmaxInPrivacyDeprecatedIQErrorResponseOptionalFromMixin")
    final class Error implements SmaxGetContactBlacklistResponse {
        /**
         * The numeric server-side error code.
         */
        private final int errorCode;

        /**
         * The optional human-readable error text.
         */
        private final String errorText;

        /**
         * Constructs an {@code Error} reply.
         *
         * @param errorCode the numeric error code
         * @param errorText the optional error text; may be
         *                  {@code null}
         */
        public Error(int errorCode, String errorText) {
            this.errorCode = errorCode;
            this.errorText = errorText;
        }

        /**
         * Returns the numeric error code.
         *
         * @return the error code
         */
        public int errorCode() {
            return errorCode;
        }

        /**
         * Returns the optional error text.
         *
         * @return an {@link Optional} carrying the error text, or
         *         empty when omitted
         */
        public Optional<String> errorText() {
            return Optional.ofNullable(errorText);
        }

        /**
         * Tries to parse an {@link Error} variant.
         *
         * @param node    the inbound stanza
         * @param request the original outbound request
         * @return an {@link Optional} carrying the parsed variant, or
         *         empty when the envelope shape does not match
         */
        @WhatsAppWebExport(moduleName = "WASmaxInPrivacyGetContactBlacklistResponseError",
                exports = "parseGetContactBlacklistResponseError",
                adaptation = WhatsAppAdaptation.ADAPTED)
        public static Optional<Error> of(Node node, Node request) {
            if (!SmaxIqErrorResponseMixin.validate(node, request)) {
                return Optional.empty();
            }
            var envelope = SmaxIqErrorResponseMixin.parseError(node).orElse(null);
            if (envelope == null) {
                return Optional.empty();
            }
            return Optional.of(new Error(envelope.code(), envelope.text()));
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == this) {
                return true;
            }
            if (obj == null || obj.getClass() != this.getClass()) {
                return false;
            }
            var that = (Error) obj;
            return this.errorCode == that.errorCode
                    && Objects.equals(this.errorText, that.errorText);
        }

        @Override
        public int hashCode() {
            return Objects.hash(errorCode, errorText);
        }

        @Override
        public String toString() {
            return "SmaxGetContactBlacklistResponse.Error[errorCode=" + errorCode
                    + ", errorText=" + errorText + ']';
        }
    }
}
