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
 * response to a {@link SmaxGroupsUnlinkGroupsRequest}.
 *
 * @implNote {@code WASmaxGroupsUnlinkGroupsRPC.sendUnlinkGroupsRPC}
 *           tries {@code Success} → {@code ClientError} →
 *           {@code ServerError} in order and throws on no-match.
 *           Cobalt returns {@link Optional#empty()} on no-match.
 */
public sealed interface SmaxGroupsUnlinkGroupsResponse extends SmaxOperation.Response
        permits SmaxGroupsUnlinkGroupsResponse.Success, SmaxGroupsUnlinkGroupsResponse.ClientError, SmaxGroupsUnlinkGroupsResponse.ServerError {

    /**
     * Tries each {@link SmaxGroupsUnlinkGroupsResponse} variant in priority order and
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
    @WhatsAppWebExport(moduleName = "WASmaxGroupsUnlinkGroupsRPC",
            exports = "sendUnlinkGroupsRPC", adaptation = WhatsAppAdaptation.ADAPTED)
    static Optional<? extends SmaxGroupsUnlinkGroupsResponse> of(Node node, Node request) {
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
     * unlink request and returned a per-sub-group result row.
     *
     * @implNote {@code WASmaxInGroupsUnlinkGroupsResponseSuccess.parseUnlinkGroupsResponseSuccess}
     *           validates the IQ result envelope, requires the
     *           {@code <unlink unlink_type="sub_group">} skeleton,
     *           and enumerates the {@code <group/>} children. Each
     *           {@code <group/>} carries an optional
     *           {@code remove_orphaned_members="true"} echo plus an
     *           optional sub-group-error mixin (bad request /
     *           not-authorized / not-exist / not-acceptable /
     *           partial-server-error / server-error). Cobalt exposes
     *           the optional error tag as a typed
     *           {@link SmaxGroupsUnlinkGroupsResponse.Success.UnlinkedGroup#errorTag()}
     *           accessor.
     */
    @WhatsAppWebModule(moduleName = "WASmaxInGroupsUnlinkGroupsResponseSuccess")
    final class Success implements SmaxGroupsUnlinkGroupsResponse {
        /**
         * The per-sub-group result rows.
         */
        private final List<UnlinkedGroup> unlinkedGroups;

        /**
         * Constructs a new successful reply.
         *
         * @param unlinkedGroups the per-sub-group result rows; never
         *                       {@code null}
         * @throws NullPointerException if {@code unlinkedGroups} is
         *                              {@code null}
         */
        public Success(List<UnlinkedGroup> unlinkedGroups) {
            Objects.requireNonNull(unlinkedGroups, "unlinkedGroups cannot be null");
            this.unlinkedGroups = List.copyOf(unlinkedGroups);
        }

        /**
         * Returns the per-sub-group result rows.
         *
         * @return an unmodifiable list of result rows
         */
        public List<UnlinkedGroup> unlinkedGroups() {
            return unlinkedGroups;
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
        @WhatsAppWebExport(moduleName = "WASmaxInGroupsUnlinkGroupsResponseSuccess",
                exports = "parseUnlinkGroupsResponseSuccess",
                adaptation = WhatsAppAdaptation.ADAPTED)
        public static Optional<Success> of(Node node, Node request) {
            if (!SmaxIqResultResponseMixin.validate(node, request)) {
                return Optional.empty();
            }
            var unlink = node.getChild("unlink").orElse(null);
            if (unlink == null) {
                return Optional.empty();
            }
            if (!unlink.hasAttribute("unlink_type", "sub_group")) {
                return Optional.empty();
            }
            var unlinkedGroups = new ArrayList<UnlinkedGroup>();
            for (var groupNode : unlink.getChildren("group")) {
                var jid = groupNode.getAttributeAsJid("jid").orElse(null);
                if (jid == null) {
                    return Optional.empty();
                }
                var removeOrphaned = groupNode.hasAttribute("remove_orphaned_members", "true");
                String errorTag = null;
                for (var child : groupNode.children()) {
                    var description = child.description();
                    if (description == null) {
                        continue;
                    }
                    if (UnlinkedGroup.isErrorTag(description)) {
                        errorTag = description;
                        break;
                    }
                }
                unlinkedGroups.add(new UnlinkedGroup(jid, removeOrphaned, errorTag));
            }
            if (unlinkedGroups.isEmpty()) {
                return Optional.empty();
            }
            return Optional.of(new Success(unlinkedGroups));
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
            return Objects.equals(this.unlinkedGroups, that.unlinkedGroups);
        }

        @Override
        public int hashCode() {
            return Objects.hash(unlinkedGroups);
        }

        @Override
        public String toString() {
            return "SmaxGroupsUnlinkGroupsResponse.Success[unlinkedGroups=" + unlinkedGroups + ']';
        }

        /**
         * Per-sub-group result row inside a {@link Success}.
         *
         * <p>The optional {@link #errorTag()} captures the discriminator
         * tag emitted by the relay when a single sub-group fails to
         * unlink — possible values mirror the WA Web mixin family:
         * {@code "bad_request"}, {@code "not_authorized"},
         * {@code "not_exist"}, {@code "not_acceptable"},
         * {@code "partial_server_error"}, {@code "server_error"}.
         */
        @WhatsAppWebModule(moduleName = "WASmaxInGroupsUnlinkGroupsResponseSuccess")
        @WhatsAppWebModule(moduleName = "WASmaxInGroupsSubGroupBadRequestOrNotAuthorizedOrNotExistOrNotAcceptableOrPartialServerErrorOrServerErrorMixinGroup")
        public static final class UnlinkedGroup {
            /**
             * Returns whether {@code description} is one of the
             * documented sub-group error discriminator tags.
             *
             * @param description the child tag to test
             * @return {@code true} when the tag is a sub-group error
             *         discriminator
             */
            private static boolean isErrorTag(String description) {
                return "bad_request".equals(description)
                        || "not_authorized".equals(description)
                        || "not_exist".equals(description)
                        || "not_acceptable".equals(description)
                        || "partial_server_error".equals(description)
                        || "server_error".equals(description);
            }

            /**
             * The sub-group JID echoed by the relay.
             */
            private final Jid jid;

            /**
             * Whether the relay echoed the
             * {@code remove_orphaned_members="true"} attribute.
             */
            private final boolean removeOrphanedMembers;

            /**
             * The optional error-discriminator tag (one of
             * {@code "bad_request"}, {@code "not_authorized"},
             * {@code "not_exist"}, {@code "not_acceptable"},
             * {@code "partial_server_error"}, {@code "server_error"}).
             * {@code null} when the unlink succeeded for this
             * sub-group.
             */
            private final String errorTag;

            /**
             * Constructs an unlinked-group result row.
             *
             * @param jid                   the sub-group JID; never
             *                              {@code null}
             * @param removeOrphanedMembers whether the relay echoed
             *                              the eviction flag
             * @param errorTag              the optional
             *                              error-discriminator tag;
             *                              may be {@code null}
             * @throws NullPointerException if {@code jid} is
             *                              {@code null}
             */
            public UnlinkedGroup(Jid jid, boolean removeOrphanedMembers, String errorTag) {
                this.jid = Objects.requireNonNull(jid, "jid cannot be null");
                this.removeOrphanedMembers = removeOrphanedMembers;
                this.errorTag = errorTag;
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
             * Returns whether the eviction flag was echoed.
             *
             * @return {@code true} when the
             *         {@code remove_orphaned_members="true"}
             *         attribute is present
             */
            public boolean removeOrphanedMembers() {
                return removeOrphanedMembers;
            }

            /**
             * Returns the optional error-discriminator tag.
             *
             * @return an {@link Optional} carrying the tag, or empty
             *         when the unlink succeeded for this sub-group
             */
            public Optional<String> errorTag() {
                return Optional.ofNullable(errorTag);
            }

            @Override
            public boolean equals(Object obj) {
                if (obj == this) {
                    return true;
                }
                if (obj == null || obj.getClass() != this.getClass()) {
                    return false;
                }
                var that = (UnlinkedGroup) obj;
                return this.removeOrphanedMembers == that.removeOrphanedMembers
                        && Objects.equals(this.jid, that.jid)
                        && Objects.equals(this.errorTag, that.errorTag);
            }

            @Override
            public int hashCode() {
                return Objects.hash(jid, removeOrphanedMembers, errorTag);
            }

            @Override
            public String toString() {
                return "SmaxGroupsUnlinkGroupsResponse.Success.UnlinkedGroup[jid=" + jid
                        + ", removeOrphanedMembers=" + removeOrphanedMembers
                        + ", errorTag=" + errorTag + ']';
            }
        }
    }

    /**
     * The {@code ClientError} reply variant — the relay rejected the
     * request as malformed, unauthorised, or referencing a
     * non-existent parent / sub-group pair.
     *
     * @implNote {@code WASmaxInGroupsUnlinkGroupsResponseClientError.parseUnlinkGroupsResponseClientError}
     *           parses the {@code <error code text/>} child. Cobalt
     *           collapses to the raw {@code (code, text)} pair.
     */
    @WhatsAppWebModule(moduleName = "WASmaxInGroupsUnlinkGroupsResponseClientError")
    final class ClientError implements SmaxGroupsUnlinkGroupsResponse {
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
        @WhatsAppWebExport(moduleName = "WASmaxInGroupsUnlinkGroupsResponseClientError",
                exports = "parseUnlinkGroupsResponseClientError",
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
            return "SmaxGroupsUnlinkGroupsResponse.ClientError[errorCode=" + errorCode
                    + ", errorText=" + errorText + ']';
        }
    }

    /**
     * The {@code ServerError} reply variant — the relay encountered a
     * transient internal failure while processing the request.
     *
     * @implNote {@code WASmaxInGroupsUnlinkGroupsResponseServerError.parseUnlinkGroupsResponseServerError}
     *           delegates to {@code WASmaxInGroupsBaseServerErrorMixin}
     *           which Cobalt has consolidated under
     *           {@link SmaxBaseServerErrorMixin}.
     */
    @WhatsAppWebModule(moduleName = "WASmaxInGroupsUnlinkGroupsResponseServerError")
    final class ServerError implements SmaxGroupsUnlinkGroupsResponse {
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
        @WhatsAppWebExport(moduleName = "WASmaxInGroupsUnlinkGroupsResponseServerError",
                exports = "parseUnlinkGroupsResponseServerError",
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
            return "SmaxGroupsUnlinkGroupsResponse.ServerError[errorCode=" + errorCode
                    + ", errorText=" + errorText + ']';
        }
    }
}
