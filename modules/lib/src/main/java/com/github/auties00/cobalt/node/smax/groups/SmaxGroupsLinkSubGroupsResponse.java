package com.github.auties00.cobalt.node.smax.groups;

import com.github.auties00.cobalt.meta.annotation.WhatsAppWebExport;
import com.github.auties00.cobalt.meta.annotation.WhatsAppWebModule;
import com.github.auties00.cobalt.meta.model.WhatsAppAdaptation;
import com.github.auties00.cobalt.model.jid.Jid;
import com.github.auties00.cobalt.node.Node;
import com.github.auties00.cobalt.node.NodeBuilder;
import com.github.auties00.cobalt.node.smax.SmaxOperation;
import com.github.auties00.cobalt.node.smax.util.SmaxBaseServerErrorMixin;
import com.github.auties00.cobalt.node.smax.util.SmaxIqResultResponseMixin;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * Sealed family of inbound reply variants produced by the relay in
 * response to a {@link SmaxGroupsLinkSubGroupsRequest}.
 *
 * @implNote {@code WASmaxGroupsLinkSubGroupsRPC.sendLinkSubGroupsRPC}
 *           tries {@code Success} → {@code ClientError} →
 *           {@code ServerError} in order and throws on no-match.
 *           Cobalt returns {@link Optional#empty()} on no-match.
 */
public sealed interface SmaxGroupsLinkSubGroupsResponse extends SmaxOperation.Response
        permits SmaxGroupsLinkSubGroupsResponse.Success, SmaxGroupsLinkSubGroupsResponse.ClientError, SmaxGroupsLinkSubGroupsResponse.ServerError {

    /**
     * Tries each {@link SmaxGroupsLinkSubGroupsResponse} variant in priority order and
     * returns the first that parses cleanly.
     *
     * @param node    the inbound IQ stanza received from the relay;
     *                never {@code null}
     * @param request the original outbound stanza — used to validate
     *                echoed identifiers; never {@code null}
     * @return an {@link Optional} carrying the parsed variant, or
     *         {@link Optional#empty()} when no documented variant
     *         matched the stanza shape
     * @throws NullPointerException if either argument is {@code null}
     */
    @WhatsAppWebExport(moduleName = "WASmaxGroupsLinkSubGroupsRPC",
            exports = "sendLinkSubGroupsRPC", adaptation = WhatsAppAdaptation.ADAPTED)
    static Optional<? extends SmaxGroupsLinkSubGroupsResponse> of(Node node, Node request) {
        Objects.requireNonNull(node, "node cannot be null");
        Objects.requireNonNull(request, "request cannot be null");
        var success = Success.of(node, request);
        if (success.isPresent()) {
            return success;
        }
        var clientError = ClientError.of(node, request);
        if (clientError.isPresent()) {
            return clientError;
        }
        return ServerError.of(node, request);
    }

    /**
     * The {@code Success} reply variant — the relay processed every
     * link request and returned a per-group result row.
     *
     * <p>Each {@link LinkedGroup} entry preserves the original
     * sub-group JID, the optional hidden-group marker echo, and a
     * list of {@link LinkedGroup.ParticipantError} entries for
     * participants that could not be transferred (typically due to
     * privacy settings).
     *
     * @implNote {@code WASmaxInGroupsLinkSubGroupsResponseSuccess.parseLinkSubGroupsResponseSuccess}
     *           validates the IQ result envelope, requires the
     *           {@code <links><link link_type="sub_group">} skeleton,
     *           and enumerates the {@code <group/>} children. Cobalt
     *           preserves the same shape with typed accessors.
     */
    @WhatsAppWebModule(moduleName = "WASmaxInGroupsLinkSubGroupsResponseSuccess")
    final class Success implements SmaxGroupsLinkSubGroupsResponse {
        /**
         * The per-group result rows.
         */
        private final List<LinkedGroup> linkedGroups;

        /**
         * Constructs a new successful reply.
         *
         * @param linkedGroups the per-group result rows; never
         *                     {@code null}
         * @throws NullPointerException if {@code linkedGroups} is
         *                              {@code null}
         */
        public Success(List<LinkedGroup> linkedGroups) {
            Objects.requireNonNull(linkedGroups, "linkedGroups cannot be null");
            this.linkedGroups = List.copyOf(linkedGroups);
        }

        /**
         * Returns the per-group result rows.
         *
         * @return an unmodifiable list of result rows
         */
        public List<LinkedGroup> linkedGroups() {
            return linkedGroups;
        }

        /**
         * Tries to parse a {@link Success} variant from the given
         * inbound stanza.
         *
         * @param node    the inbound IQ stanza
         * @param request the original outbound request
         * @return an {@link Optional} carrying the parsed variant, or
         *         empty when the stanza does not match the success
         *         schema
         */
        @WhatsAppWebExport(moduleName = "WASmaxInGroupsLinkSubGroupsResponseSuccess",
                exports = "parseLinkSubGroupsResponseSuccess",
                adaptation = WhatsAppAdaptation.ADAPTED)
        public static Optional<Success> of(Node node, Node request) {
            if (!SmaxIqResultResponseMixin.validate(node, request)) {
                return Optional.empty();
            }
            var links = node.getChild("links").orElse(null);
            if (links == null) {
                return Optional.empty();
            }
            var link = links.getChild("link").orElse(null);
            if (link == null) {
                return Optional.empty();
            }
            if (!link.hasAttribute("link_type", "sub_group")) {
                return Optional.empty();
            }
            var linkedGroups = new ArrayList<LinkedGroup>();
            for (var groupNode : link.getChildren("group")) {
                var jid = groupNode.getAttributeAsJid("jid").orElse(null);
                if (jid == null) {
                    return Optional.empty();
                }
                var hiddenGroup = groupNode.getChild("hidden_group").isPresent();
                var participantErrors = new ArrayList<LinkedGroup.ParticipantError>();
                for (var participantNode : groupNode.getChildren("participant")) {
                    var participantJid = participantNode.getAttributeAsJid("jid").orElse(null);
                    if (participantJid == null) {
                        return Optional.empty();
                    }
                    var error = participantNode.getAttributeAsString("error").orElse(null);
                    if (error == null) {
                        return Optional.empty();
                    }
                    participantErrors.add(new LinkedGroup.ParticipantError(participantJid, error));
                }
                linkedGroups.add(new LinkedGroup(jid, hiddenGroup, participantErrors));
            }
            if (linkedGroups.isEmpty()) {
                return Optional.empty();
            }
            return Optional.of(new Success(linkedGroups));
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
            return Objects.equals(this.linkedGroups, that.linkedGroups);
        }

        @Override
        public int hashCode() {
            return Objects.hash(linkedGroups);
        }

        @Override
        public String toString() {
            return "SmaxGroupsLinkSubGroupsResponse.Success[linkedGroups=" + linkedGroups + ']';
        }

        /**
         * Per-group result row inside a {@link Success}.
         */
        @WhatsAppWebModule(moduleName = "WASmaxInGroupsLinkSubGroupsResponseSuccess")
        public static final class LinkedGroup {
            /**
             * The sub-group JID echoed by the relay.
             */
            private final Jid jid;

            /**
             * Whether the relay echoed a {@code <hidden_group/>}
             * marker for this sub-group.
             */
            private final boolean hiddenGroup;

            /**
             * Per-participant errors encountered while transferring
             * the sub-group's roster (empty when every participant
             * was admitted cleanly).
             */
            private final List<ParticipantError> participantErrors;

            /**
             * Constructs a linked-group result row.
             *
             * @param jid               the sub-group JID; never
             *                          {@code null}
             * @param hiddenGroup       whether the relay echoed the
             *                          hidden marker
             * @param participantErrors the per-participant errors;
             *                          never {@code null}
             * @throws NullPointerException if {@code jid} or
             *                              {@code participantErrors}
             *                              is {@code null}
             */
            public LinkedGroup(Jid jid, boolean hiddenGroup, List<ParticipantError> participantErrors) {
                this.jid = Objects.requireNonNull(jid, "jid cannot be null");
                this.hiddenGroup = hiddenGroup;
                Objects.requireNonNull(participantErrors, "participantErrors cannot be null");
                this.participantErrors = List.copyOf(participantErrors);
            }

            /**
             * Returns the sub-group JID.
             *
             * @return the sub-group JID; never {@code null}
             */
            public Jid jid() {
                return jid;
            }

            /**
             * Returns whether the relay echoed the hidden-group
             * marker.
             *
             * @return {@code true} when the
             *         {@code <hidden_group/>} marker is present
             */
            public boolean hiddenGroup() {
                return hiddenGroup;
            }

            /**
             * Returns the per-participant error rows.
             *
             * @return an unmodifiable list of participant errors
             */
            public List<ParticipantError> participantErrors() {
                return participantErrors;
            }

            @Override
            public boolean equals(Object obj) {
                if (obj == this) {
                    return true;
                }
                if (obj == null || obj.getClass() != this.getClass()) {
                    return false;
                }
                var that = (LinkedGroup) obj;
                return this.hiddenGroup == that.hiddenGroup
                        && Objects.equals(this.jid, that.jid)
                        && Objects.equals(this.participantErrors, that.participantErrors);
            }

            @Override
            public int hashCode() {
                return Objects.hash(jid, hiddenGroup, participantErrors);
            }

            @Override
            public String toString() {
                return "SmaxGroupsLinkSubGroupsResponse.Success.LinkedGroup[jid=" + jid
                        + ", hiddenGroup=" + hiddenGroup
                        + ", participantErrors=" + participantErrors + ']';
            }

            /**
             * Per-participant error row inside a {@link LinkedGroup}.
             *
             * <p>The {@code error} attribute always carries
             * {@code "403"} server-side — it indicates the
             * participant could not be transferred to the sub-group
             * because their privacy settings forbid the implicit
             * group add.
             */
            @WhatsAppWebModule(moduleName = "WASmaxInGroupsLinkSubGroupsResponseSuccess")
            public static final class ParticipantError {
                /**
                 * The participant JID that could not be transferred.
                 */
                private final Jid participantJid;

                /**
                 * The error code reported by the relay (always
                 * {@code "403"} in current schemas).
                 */
                private final String error;

                /**
                 * Constructs a participant-error entry.
                 *
                 * @param participantJid the participant JID; never
                 *                       {@code null}
                 * @param error          the error code; never
                 *                       {@code null}
                 * @throws NullPointerException if either argument is
                 *                              {@code null}
                 */
                public ParticipantError(Jid participantJid, String error) {
                    this.participantJid = Objects.requireNonNull(participantJid, "participantJid cannot be null");
                    this.error = Objects.requireNonNull(error, "error cannot be null");
                }

                /**
                 * Returns the participant JID.
                 *
                 * @return the participant JID; never {@code null}
                 */
                public Jid participantJid() {
                    return participantJid;
                }

                /**
                 * Returns the relay-reported error code.
                 *
                 * @return the error code; never {@code null}
                 */
                public String error() {
                    return error;
                }

                @Override
                public boolean equals(Object obj) {
                    if (obj == this) {
                        return true;
                    }
                    if (obj == null || obj.getClass() != this.getClass()) {
                        return false;
                    }
                    var that = (ParticipantError) obj;
                    return Objects.equals(this.participantJid, that.participantJid)
                            && Objects.equals(this.error, that.error);
                }

                @Override
                public int hashCode() {
                    return Objects.hash(participantJid, error);
                }

                @Override
                public String toString() {
                    return "SmaxGroupsLinkSubGroupsResponse.Success.LinkedGroup.ParticipantError[participantJid=" + participantJid
                            + ", error=" + error + ']';
                }
            }
        }
    }

    /**
     * The {@code ClientError} reply variant — the relay rejected the
     * request as malformed, unauthorised, or referencing a
     * non-existent parent / sub-group pair.
     *
     * @implNote {@code WASmaxInGroupsLinkSubGroupsResponseClientError.parseLinkSubGroupsResponseClientError}
     *           parses the {@code <error code text/>} child and routes
     *           it through
     *           {@code WASmaxInGroupsLinkSubGroupsClientError}.
     *           Cobalt collapses to the raw {@code (code, text)} pair.
     */
    @WhatsAppWebModule(moduleName = "WASmaxInGroupsLinkSubGroupsResponseClientError")
    final class ClientError implements SmaxGroupsLinkSubGroupsResponse {
        /**
         * The numeric server-side error code.
         */
        private final int errorCode;

        /**
         * The human-readable error text, when the relay supplied one.
         */
        private final String errorText;

        /**
         * Constructs a new client-error reply.
         *
         * @param errorCode the numeric error code
         * @param errorText the optional human-readable text; may be
         *                  {@code null}
         */
        public ClientError(int errorCode, String errorText) {
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
         * Returns the optional human-readable error text.
         *
         * @return an {@link Optional} carrying the error text, or
         *         empty when the relay omitted it
         */
        public Optional<String> errorText() {
            return Optional.ofNullable(errorText);
        }

        /**
         * Tries to parse a {@link ClientError} variant from the given
         * inbound stanza.
         *
         * @param node    the inbound IQ stanza
         * @param request the original outbound request
         * @return an {@link Optional} carrying the parsed variant, or
         *         empty when the stanza does not match the
         *         client-error schema
         */
        @WhatsAppWebExport(moduleName = "WASmaxInGroupsLinkSubGroupsResponseClientError",
                exports = "parseLinkSubGroupsResponseClientError",
                adaptation = WhatsAppAdaptation.ADAPTED)
        public static Optional<ClientError> of(Node node, Node request) {
            var envelope = SmaxBaseServerErrorMixin.parseClientError(node, request).orElse(null);
            if (envelope == null) {
                return Optional.empty();
            }
            return Optional.of(new ClientError(envelope.code(), envelope.text()));
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == this) {
                return true;
            }
            if (obj == null || obj.getClass() != this.getClass()) {
                return false;
            }
            var that = (ClientError) obj;
            return this.errorCode == that.errorCode && Objects.equals(this.errorText, that.errorText);
        }

        @Override
        public int hashCode() {
            return Objects.hash(errorCode, errorText);
        }

        @Override
        public String toString() {
            return "SmaxGroupsLinkSubGroupsResponse.ClientError[errorCode=" + errorCode
                    + ", errorText=" + errorText + ']';
        }
    }

    /**
     * The {@code ServerError} reply variant — the relay encountered a
     * transient internal failure while processing the request.
     *
     * @implNote {@code WASmaxInGroupsLinkSubGroupsResponseServerError.parseLinkSubGroupsResponseServerError}
     *           delegates to {@code WASmaxInGroupsBaseServerErrorMixin}
     *           which Cobalt has consolidated under
     *           {@link SmaxBaseServerErrorMixin}.
     */
    @WhatsAppWebModule(moduleName = "WASmaxInGroupsLinkSubGroupsResponseServerError")
    final class ServerError implements SmaxGroupsLinkSubGroupsResponse {
        /**
         * The numeric server-side error code.
         */
        private final int errorCode;

        /**
         * The human-readable error text, when the relay supplied one.
         */
        private final String errorText;

        /**
         * Constructs a new server-error reply.
         *
         * @param errorCode the numeric error code
         * @param errorText the optional human-readable text; may be
         *                  {@code null}
         */
        public ServerError(int errorCode, String errorText) {
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
         * Returns the optional human-readable error text.
         *
         * @return an {@link Optional} carrying the error text, or
         *         empty when the relay omitted it
         */
        public Optional<String> errorText() {
            return Optional.ofNullable(errorText);
        }

        /**
         * Tries to parse a {@link ServerError} variant from the given
         * inbound stanza.
         *
         * @param node    the inbound IQ stanza
         * @param request the original outbound request
         * @return an {@link Optional} carrying the parsed variant, or
         *         empty when the stanza does not match the
         *         server-error schema
         */
        @WhatsAppWebExport(moduleName = "WASmaxInGroupsLinkSubGroupsResponseServerError",
                exports = "parseLinkSubGroupsResponseServerError",
                adaptation = WhatsAppAdaptation.ADAPTED)
        public static Optional<ServerError> of(Node node, Node request) {
            var envelope = SmaxBaseServerErrorMixin.parseServerError(node, request).orElse(null);
            if (envelope == null) {
                return Optional.empty();
            }
            return Optional.of(new ServerError(envelope.code(), envelope.text()));
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == this) {
                return true;
            }
            if (obj == null || obj.getClass() != this.getClass()) {
                return false;
            }
            var that = (ServerError) obj;
            return this.errorCode == that.errorCode && Objects.equals(this.errorText, that.errorText);
        }

        @Override
        public int hashCode() {
            return Objects.hash(errorCode, errorText);
        }

        @Override
        public String toString() {
            return "SmaxGroupsLinkSubGroupsResponse.ServerError[errorCode=" + errorCode
                    + ", errorText=" + errorText + ']';
        }
    }
}
