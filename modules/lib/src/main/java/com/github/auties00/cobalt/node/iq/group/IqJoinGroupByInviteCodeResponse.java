package com.github.auties00.cobalt.node.iq.group;

import com.github.auties00.cobalt.meta.annotation.WhatsAppWebExport;
import com.github.auties00.cobalt.meta.annotation.WhatsAppWebModule;
import com.github.auties00.cobalt.meta.model.WhatsAppAdaptation;
import com.github.auties00.cobalt.model.jid.Jid;
import com.github.auties00.cobalt.node.Node;
import com.github.auties00.cobalt.node.iq.IqOperation;
import com.github.auties00.cobalt.node.smax.util.SmaxBaseServerErrorMixin;
import com.github.auties00.cobalt.node.smax.util.SmaxIqResultResponseMixin;
import java.util.Objects;
import java.util.Optional;

/**
 * Sealed family of inbound reply variants.
 */
public sealed interface IqJoinGroupByInviteCodeResponse extends IqOperation.Response
        permits IqJoinGroupByInviteCodeResponse.Success, IqJoinGroupByInviteCodeResponse.UnexpectedJoinShape,
                IqJoinGroupByInviteCodeResponse.ClientError, IqJoinGroupByInviteCodeResponse.ServerError {

    /**
     * Tries each {@link IqJoinGroupByInviteCodeResponse} variant in priority order.
     *
     * @param node    the inbound IQ stanza; never {@code null}
     * @param request the original outbound stanza; never {@code null}
     * @return an {@link Optional} carrying the parsed variant, or
     *         empty when no documented variant matched
     * @throws NullPointerException if either argument is
     *                              {@code null}
     */
    @WhatsAppWebExport(moduleName = "WAWebGroupInviteJob",
            exports = "joinGroupViaInvite",
            adaptation = WhatsAppAdaptation.ADAPTED)
    static Optional<? extends IqJoinGroupByInviteCodeResponse> of(Node node, Node request) {
        Objects.requireNonNull(node, "node cannot be null");
        Objects.requireNonNull(request, "request cannot be null");
        var success = Success.of(node, request);
        if (success.isPresent()) {
            return success;
        }
        var unexpected = UnexpectedJoinShape.of(node, request);
        if (unexpected.isPresent()) {
            return unexpected;
        }
        var clientError = ClientError.of(node, request);
        if (clientError.isPresent()) {
            return clientError;
        }
        return ServerError.of(node, request);
    }

    /**
     * The {@code Success} reply variant — the relay accepted the
     * invite code and either admitted the caller (open-join) or
     * queued the request for moderator approval (approval-gated).
     *
     * <p>{@link #isMembershipApprovalPending()} discriminates the
     * two paths: {@code true} means the entry is queued for
     * approval, {@code false} means the caller is already a
     * member.
     *
     * @implNote {@code WAWebGroupInviteJob.joinGroupViaInviteParser}:
     *           {@code maybeChild(t ? "membership_approval_request"
     *           : "group")} -> {@code {gid: groupJidToWid(...)}};
     *           the alternate child triggers
     *           {@code UnexpectedJoinGroupViaInviteResponse}.
     */
    @WhatsAppWebModule(moduleName = "WAWebGroupInviteJob")
    final class Success implements IqJoinGroupByInviteCodeResponse {
        /**
         * The group JID the caller joined (or whose membership
         * approval was queued).
         */
        private final Jid groupJid;

        /**
         * {@code true} when the entry is queued for moderator
         * approval, {@code false} when the caller has already been
         * admitted.
         */
        private final boolean membershipApprovalPending;

        /**
         * Constructs a successful reply.
         *
         * @param groupJid                  the group JID; never
         *                                  {@code null}
         * @param membershipApprovalPending {@code true} when the
         *                                  entry is queued for
         *                                  approval
         * @throws NullPointerException if {@code groupJid} is
         *                              {@code null}
         */
        public Success(Jid groupJid, boolean membershipApprovalPending) {
            this.groupJid = Objects.requireNonNull(groupJid, "groupJid cannot be null");
            this.membershipApprovalPending = membershipApprovalPending;
        }

        /**
         * Returns the group JID.
         *
         * @return the JID; never {@code null}
         */
        public Jid groupJid() {
            return groupJid;
        }

        /**
         * Returns whether the entry is queued for moderator
         * approval.
         *
         * @return {@code true} when queued, {@code false} when
         *         already admitted
         */
        public boolean isMembershipApprovalPending() {
            return membershipApprovalPending;
        }

        /**
         * Tries to parse a {@link Success} variant.
         *
         * @param node    the inbound IQ stanza
         * @param request the original outbound request
         * @return an {@link Optional} carrying the parsed variant,
         *         or empty when the stanza does not match the
         *         success schema
         */
        @WhatsAppWebExport(moduleName = "WAWebGroupInviteJob",
                exports = "joinGroupViaInviteParser",
                adaptation = WhatsAppAdaptation.ADAPTED)
        public static Optional<Success> of(Node node, Node request) {
            if (!SmaxIqResultResponseMixin.validate(node, request)) {
                return Optional.empty();
            }
            // joinGroupViaInviteParser also asserts from="g.us".
            var fromAttr = node.getAttributeAsJid("from").orElse(null);
            if (fromAttr == null || !"g.us".equals(fromAttr.toString())) {
                return Optional.empty();
            }
            // Try the open-join shape first.
            var groupChild = node.getChild("group").orElse(null);
            if (groupChild != null) {
                var gid = groupChild.getAttributeAsJid("jid").orElse(null);
                if (gid == null) {
                    return Optional.empty();
                }
                return Optional.of(new Success(gid, false));
            }
            // Then the approval-gated shape.
            var approvalChild = node.getChild("membership_approval_request").orElse(null);
            if (approvalChild != null) {
                var gid = approvalChild.getAttributeAsJid("jid").orElse(null);
                if (gid == null) {
                    return Optional.empty();
                }
                return Optional.of(new Success(gid, true));
            }
            return Optional.empty();
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
            return this.membershipApprovalPending == that.membershipApprovalPending
                    && Objects.equals(this.groupJid, that.groupJid);
        }

        @Override
        public int hashCode() {
            return Objects.hash(groupJid, membershipApprovalPending);
        }

        @Override
        public String toString() {
            return "IqJoinGroupByInviteCodeResponse.Success[groupJid=" + groupJid
                    + ", membershipApprovalPending=" + membershipApprovalPending + ']';
        }
    }

    /**
     * The {@code UnexpectedJoinShape} reply variant — the relay
     * accepted the invite code but routed the caller through a
     * different gating mode than expected.
     *
     * <p>WA Web throws an {@code UnexpectedJoinGroupViaInviteResponse}
     * carrying the actual group JID and a boolean indicating the
     * actual shape; Cobalt surfaces both as typed fields and routes
     * the variant through the {@link IqJoinGroupByInviteCodeResponse#of(Node, Node)} chain.
     *
     * @implNote {@code WAWebGroupInviteJob.joinGroupViaInviteParser}:
     *           when the expected child is absent and the alternate
     *           is present, throws
     *           {@code UnexpectedJoinGroupViaInviteResponse(gid, !t)}.
     *           Cobalt always returns this variant whenever the
     *           expected child is absent and the alternate present
     *           — the dispatcher layer correlates against the
     *           Request flag to surface the discrepancy.
     */
    @WhatsAppWebModule(moduleName = "WAWebGroupInviteJob")
    final class UnexpectedJoinShape implements IqJoinGroupByInviteCodeResponse {
        /**
         * The group JID parsed from the alternate child.
         */
        private final Jid groupJid;

        /**
         * The actual gating mode shipped by the relay:
         * {@code true} when the relay returned a
         * {@code <membership_approval_request>}, {@code false}
         * when it returned a {@code <group>}.
         */
        private final boolean actualMembershipApprovalPending;

        /**
         * Constructs a new unexpected-shape reply.
         *
         * @param groupJid                       the group JID;
         *                                       never {@code null}
         * @param actualMembershipApprovalPending the actual gating
         *                                       mode
         * @throws NullPointerException if {@code groupJid} is
         *                              {@code null}
         */
        public UnexpectedJoinShape(Jid groupJid, boolean actualMembershipApprovalPending) {
            this.groupJid = Objects.requireNonNull(groupJid, "groupJid cannot be null");
            this.actualMembershipApprovalPending = actualMembershipApprovalPending;
        }

        /**
         * Returns the group JID parsed from the alternate child.
         *
         * @return the JID; never {@code null}
         */
        public Jid groupJid() {
            return groupJid;
        }

        /**
         * Returns the actual gating mode.
         *
         * @return {@code true} when the relay queued for approval,
         *         {@code false} when it admitted directly
         */
        public boolean actualMembershipApprovalPending() {
            return actualMembershipApprovalPending;
        }

        /**
         * Tries to parse an {@link UnexpectedJoinShape} variant —
         * matches when the IQ envelope is a {@code result} but
         * neither the {@code <group>} nor the
         * {@code <membership_approval_request>} child is the one
         * the caller expected.
         *
         * <p>Without access to the original request flag at parse
         * time the variant cannot be reliably detected from the
         * stanza alone; this static factory therefore returns
         * {@link Optional#empty()} and leaves shape correlation to
         * the dispatcher, which compares
         * {@link Success#isMembershipApprovalPending()} against
         * {@link IqJoinGroupByInviteCodeRequest#expectsMembershipApproval()} to surface
         * the mismatch.
         *
         * @param node    the inbound IQ stanza
         * @param request the original outbound request
         * @return always {@link Optional#empty()}
         */
        @WhatsAppWebExport(moduleName = "WAWebGroupInviteJob",
                exports = "joinGroupViaInviteParser",
                adaptation = WhatsAppAdaptation.ADAPTED)
        public static Optional<UnexpectedJoinShape> of(Node node, Node request) {
            Objects.requireNonNull(node, "node cannot be null");
            Objects.requireNonNull(request, "request cannot be null");
            return Optional.empty();
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == this) {
                return true;
            }
            if (obj == null || obj.getClass() != this.getClass()) {
                return false;
            }
            var that = (UnexpectedJoinShape) obj;
            return this.actualMembershipApprovalPending == that.actualMembershipApprovalPending
                    && Objects.equals(this.groupJid, that.groupJid);
        }

        @Override
        public int hashCode() {
            return Objects.hash(groupJid, actualMembershipApprovalPending);
        }

        @Override
        public String toString() {
            return "IqJoinGroupByInviteCodeResponse.UnexpectedJoinShape[groupJid=" + groupJid
                    + ", actualMembershipApprovalPending=" + actualMembershipApprovalPending + ']';
        }
    }

    /**
     * The {@code ClientError} reply variant — {@code 4xx} rejection
     * (typically {@code 401} for an expired/revoked invite code,
     * {@code 403} when the caller is banned from the group,
     * {@code 404} when the group no longer exists).
     */
    @WhatsAppWebModule(moduleName = "WAWebGroupInviteJob")
    final class ClientError implements IqJoinGroupByInviteCodeResponse {
        /**
         * The numeric server-side error code.
         */
        private final int errorCode;

        /**
         * The optional human-readable error text.
         */
        private final String errorText;

        /**
         * Constructs a client-error reply.
         *
         * @param errorCode the numeric error code
         * @param errorText the optional text; may be {@code null}
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
         * Returns the optional error text.
         *
         * @return an {@link Optional} carrying the text, or empty
         *         when omitted
         */
        public Optional<String> errorText() {
            return Optional.ofNullable(errorText);
        }

        /**
         * Tries to parse a {@link ClientError} variant.
         *
         * @param node    the inbound IQ stanza
         * @param request the original outbound request
         * @return an {@link Optional} carrying the parsed variant,
         *         or empty when the stanza does not match the
         *         client-error schema
         */
        @WhatsAppWebExport(moduleName = "WAWebGroupInviteJob",
                exports = "joinGroupViaInvite",
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
            return "IqJoinGroupByInviteCodeResponse.ClientError[errorCode=" + errorCode
                    + ", errorText=" + errorText + ']';
        }
    }

    /**
     * The {@code ServerError} reply variant — {@code 5xx} transient
     * failure.
     */
    @WhatsAppWebModule(moduleName = "WAWebGroupInviteJob")
    final class ServerError implements IqJoinGroupByInviteCodeResponse {
        /**
         * The numeric server-side error code.
         */
        private final int errorCode;

        /**
         * The optional human-readable error text.
         */
        private final String errorText;

        /**
         * Constructs a server-error reply.
         *
         * @param errorCode the numeric error code
         * @param errorText the optional text; may be {@code null}
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
         * Returns the optional error text.
         *
         * @return an {@link Optional} carrying the text, or empty
         *         when omitted
         */
        public Optional<String> errorText() {
            return Optional.ofNullable(errorText);
        }

        /**
         * Tries to parse a {@link ServerError} variant.
         *
         * @param node    the inbound IQ stanza
         * @param request the original outbound request
         * @return an {@link Optional} carrying the parsed variant,
         *         or empty when the stanza does not match the
         *         server-error schema
         */
        @WhatsAppWebExport(moduleName = "WAWebGroupInviteJob",
                exports = "joinGroupViaInvite",
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
            return "IqJoinGroupByInviteCodeResponse.ServerError[errorCode=" + errorCode
                    + ", errorText=" + errorText + ']';
        }
    }
}
