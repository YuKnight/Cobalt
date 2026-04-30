package com.github.auties00.cobalt.node.iq.group;

import com.github.auties00.cobalt.meta.annotation.WhatsAppWebExport;
import com.github.auties00.cobalt.meta.annotation.WhatsAppWebModule;
import com.github.auties00.cobalt.meta.model.WhatsAppAdaptation;
import com.github.auties00.cobalt.model.jid.Jid;
import com.github.auties00.cobalt.node.Node;
import com.github.auties00.cobalt.node.iq.IqOperation;
import com.github.auties00.cobalt.node.smax.util.SmaxBaseServerErrorMixin;
import com.github.auties00.cobalt.node.smax.util.SmaxIqResultResponseMixin;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * Sealed family of inbound reply variants produced by the relay in
 * response to an {@link IqGroupExitRequest}.
 *
 * @implNote {@code WAWebGroupExitJob} parses the reply via
 *           {@code leaveGroupsResultParser} and
 *           {@code leaveCommunitiesResultParser} and rejects on a
 *           non-result envelope by throwing
 *           {@code ServerStatusCodeError}; Cobalt splits the
 *           rejected branch into typed
 *           {@code ClientError}/{@code ServerError} variants and
 *           returns {@link Optional#empty()} on no-match.
 */
@WhatsAppWebModule(moduleName = "WAWebGroupExitJob")
public sealed interface IqGroupExitResponse extends IqOperation.Response
        permits IqGroupExitResponse.Success, IqGroupExitResponse.ClientError, IqGroupExitResponse.ServerError {

    /**
     * Tries each {@link IqGroupExitResponse} variant in priority order and
     * returns the first that parses cleanly.
     *
     * @param node    the inbound IQ stanza received from the relay;
     *                never {@code null}
     * @param request the original outbound stanza — used to
     *                validate echoed identifiers and to discover
     *                the grandchild-shape mode; never {@code null}
     * @return an {@link Optional} carrying the parsed variant, or
     *         {@link Optional#empty()} when no documented variant
     *         matched the stanza shape
     * @throws NullPointerException if either argument is
     *                              {@code null}
     */
    @WhatsAppWebExport(moduleName = "WAWebGroupExitJob",
            exports = "leaveGroup", adaptation = WhatsAppAdaptation.ADAPTED)
    @WhatsAppWebExport(moduleName = "WAWebGroupExitJob",
            exports = "leaveCommunity", adaptation = WhatsAppAdaptation.ADAPTED)
    @WhatsAppWebExport(moduleName = "WAWebGroupExitJob",
            exports = "leaveCommunities", adaptation = WhatsAppAdaptation.ADAPTED)
    static Optional<? extends IqGroupExitResponse> of(Node node, Node request) {
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
     * The {@code Success} reply variant — carries one
     * {@link LeaveResult} per requested target.
     *
     * <p>Each {@link LeaveResult} contains the echoed target JID
     * and a {@code code} integer mirroring the {@code error}
     * grandchild attribute (defaulting to {@code 200} when the
     * relay omits it, signalling a per-target success).
     *
     * @implNote {@code WAWebGroupExitJob.leaveGroupsResultParser}
     *           and {@code leaveCommunitiesResultParser} both
     *           project every {@code <leave>} grandchild to a
     *           {@code {id, code}} pair where {@code code} defaults
     *           to {@code 200}; Cobalt mirrors that exactly via
     *           {@link LeaveResult}.
     */
    @WhatsAppWebModule(moduleName = "WAWebGroupExitJob")
    final class Success implements IqGroupExitResponse {
        /**
         * Per-target reply projection — pairs the echoed target
         * JID with its (possibly partial) leave outcome code.
         */
        public static final class LeaveResult {
            /**
             * The echoed target JID.
             */
            private final Jid jid;

            /**
             * The per-target outcome code; {@code 200} on
             * success, otherwise the relay's per-target error
             * code.
             */
            private final int code;

            /**
             * Constructs a per-target leave result.
             *
             * @param jid  the echoed target JID; never
             *             {@code null}
             * @param code the per-target outcome code
             * @throws NullPointerException if {@code jid} is
             *                              {@code null}
             */
            public LeaveResult(Jid jid, int code) {
                this.jid = Objects.requireNonNull(jid, "jid cannot be null");
                this.code = code;
            }

            /**
             * Returns the echoed target JID.
             *
             * @return the target JID; never {@code null}
             */
            public Jid jid() {
                return jid;
            }

            /**
             * Returns the per-target outcome code.
             *
             * @return the outcome code
             */
            public int code() {
                return code;
            }

            @Override
            public boolean equals(Object obj) {
                if (obj == this) {
                    return true;
                }
                if (obj == null || obj.getClass() != this.getClass()) {
                    return false;
                }
                var that = (LeaveResult) obj;
                return this.code == that.code
                        && Objects.equals(this.jid, that.jid);
            }

            @Override
            public int hashCode() {
                return Objects.hash(jid, code);
            }

            @Override
            public String toString() {
                return "IqGroupExitResponse.Success.LeaveResult[jid=" + jid
                        + ", code=" + code + ']';
            }
        }

        /**
         * The list of per-target outcome projections.
         */
        private final List<LeaveResult> results;

        /**
         * Constructs a new successful reply.
         *
         * @param results the per-target outcome list; never
         *                {@code null}
         * @throws NullPointerException if {@code results} is
         *                              {@code null}
         */
        public Success(List<LeaveResult> results) {
            Objects.requireNonNull(results, "results cannot be null");
            this.results = List.copyOf(results);
        }

        /**
         * Returns the list of per-target outcome projections.
         *
         * @return an unmodifiable list of {@link LeaveResult}
         *         entries; never {@code null}
         */
        public List<LeaveResult> results() {
            return results;
        }

        /**
         * Tries to parse a {@link Success} variant from the given
         * inbound stanza.
         *
         * <p>The grandchild-shape mode is recovered from the
         * outbound {@code request} by inspecting whether its
         * {@code <leave>} child holds {@code <group>} or
         * {@code <linked_groups>} grandchildren; this is the same
         * cue WA Web uses to dispatch between the two parsers.
         *
         * @param node    the inbound IQ stanza
         * @param request the original outbound request
         * @return an {@link Optional} carrying the parsed variant,
         *         or empty when the stanza does not match the
         *         success schema
         *
         * @implNote {@code WAWebGroupExitJob.leaveGroupsResultParser}
         *           and {@code leaveCommunitiesResultParser}:
         *           {@code child("leave").mapChildren(c -> {id:
         *           groupJidToWid(c.attrGroupJid("id" |
         *           "parent_group_jid")), code:
         *           c.maybeAttrInt("error") ?? 200})}.
         */
        @WhatsAppWebExport(moduleName = "WAWebGroupExitJob",
                exports = "leaveGroupsResultParser",
                adaptation = WhatsAppAdaptation.ADAPTED)
        @WhatsAppWebExport(moduleName = "WAWebGroupExitJob",
                exports = "leaveCommunitiesResultParser",
                adaptation = WhatsAppAdaptation.ADAPTED)
        public static Optional<Success> of(Node node, Node request) {
            if (!SmaxIqResultResponseMixin.validate(node, request)) {
                return Optional.empty();
            }
            var leaveChild = node.getChild("leave").orElse(null);
            if (leaveChild == null) {
                return Optional.empty();
            }
            var requestLeaveChild = request.getChild("leave").orElse(null);
            if (requestLeaveChild == null) {
                return Optional.empty();
            }
            String jidAttribute;
            String grandchildTag;
            if (requestLeaveChild.hasChild("linked_groups")) {
                grandchildTag = "linked_groups";
                jidAttribute = "parent_group_jid";
            } else {
                grandchildTag = "group";
                jidAttribute = "id";
            }
            var results = new ArrayList<LeaveResult>();
            var children = leaveChild.streamChildren(grandchildTag).toList();
            for (var child : children) {
                var jid = child.getAttributeAsJid(jidAttribute).orElse(null);
                if (jid == null) {
                    return Optional.empty();
                }
                var code = child.getAttributeAsInt("error", 200);
                results.add(new LeaveResult(jid, code));
            }
            return Optional.of(new Success(results));
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
            return Objects.equals(this.results, that.results);
        }

        @Override
        public int hashCode() {
            return Objects.hash(results);
        }

        @Override
        public String toString() {
            return "IqGroupExitResponse.Success[results=" + results + ']';
        }
    }

    /**
     * The {@code ClientError} reply variant — the relay rejected
     * the leave as malformed or unauthorised.
     *
     * @implNote {@code WAWebGroupExitJob} surfaces every non-result
     *           envelope as a {@code ServerStatusCodeError}; Cobalt
     *           narrows the {@code [400, 500)} band to this
     *           variant via
     *           {@link SmaxBaseServerErrorMixin#parseClientError(Node, Node)}.
     */
    @WhatsAppWebModule(moduleName = "WAWebGroupExitJob")
    final class ClientError implements IqGroupExitResponse {
        /**
         * The numeric server-side error code.
         */
        private final int errorCode;

        /**
         * The human-readable error text, when the relay supplied
         * one.
         */
        private final String errorText;

        /**
         * Constructs a new client-error reply.
         *
         * @param errorCode the numeric error code
         * @param errorText the optional human-readable text; may
         *                  be {@code null}
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
         * Tries to parse a {@link ClientError} variant from the
         * given inbound stanza.
         *
         * @param node    the inbound IQ stanza
         * @param request the original outbound request
         * @return an {@link Optional} carrying the parsed variant,
         *         or empty when the stanza does not match the
         *         client-error schema
         */
        @WhatsAppWebExport(moduleName = "WAWebGroupExitJob",
                exports = "leaveGroupsResultParser",
                adaptation = WhatsAppAdaptation.ADAPTED)
        @WhatsAppWebExport(moduleName = "WAWebGroupExitJob",
                exports = "leaveCommunitiesResultParser",
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
            return this.errorCode == that.errorCode
                    && Objects.equals(this.errorText, that.errorText);
        }

        @Override
        public int hashCode() {
            return Objects.hash(errorCode, errorText);
        }

        @Override
        public String toString() {
            return "IqGroupExitResponse.ClientError[errorCode=" + errorCode
                    + ", errorText=" + errorText + ']';
        }
    }

    /**
     * The {@code ServerError} reply variant — the relay
     * encountered a transient internal failure while processing
     * the leave.
     *
     * @implNote Mirrors the {@code [500, ...)} band of
     *           {@code ServerStatusCodeError}; Cobalt routes via
     *           {@link SmaxBaseServerErrorMixin#parseServerError(Node, Node)}.
     */
    @WhatsAppWebModule(moduleName = "WAWebGroupExitJob")
    final class ServerError implements IqGroupExitResponse {
        /**
         * The numeric server-side error code.
         */
        private final int errorCode;

        /**
         * The human-readable error text, when the relay supplied
         * one.
         */
        private final String errorText;

        /**
         * Constructs a new server-error reply.
         *
         * @param errorCode the numeric error code
         * @param errorText the optional human-readable text; may
         *                  be {@code null}
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
         * Tries to parse a {@link ServerError} variant from the
         * given inbound stanza.
         *
         * @param node    the inbound IQ stanza
         * @param request the original outbound request
         * @return an {@link Optional} carrying the parsed variant,
         *         or empty when the stanza does not match the
         *         server-error schema
         */
        @WhatsAppWebExport(moduleName = "WAWebGroupExitJob",
                exports = "leaveGroupsResultParser",
                adaptation = WhatsAppAdaptation.ADAPTED)
        @WhatsAppWebExport(moduleName = "WAWebGroupExitJob",
                exports = "leaveCommunitiesResultParser",
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
            return this.errorCode == that.errorCode
                    && Objects.equals(this.errorText, that.errorText);
        }

        @Override
        public int hashCode() {
            return Objects.hash(errorCode, errorText);
        }

        @Override
        public String toString() {
            return "IqGroupExitResponse.ServerError[errorCode=" + errorCode
                    + ", errorText=" + errorText + ']';
        }
    }
}
