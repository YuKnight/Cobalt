package com.github.auties00.cobalt.node.iq.usync;

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
 * Sealed family of inbound reply variants produced by the relay.
 *
 * @implNote {@code WAWebUsync.usyncParser} projects the
 *           {@code <usync><result/><list/></usync>} structure
 *           into a {@code (perProtocolErrors, perProtocolRefresh,
 *           userResults)} triple. Cobalt mirrors the projection
 *           on {@link IqUsyncResponse.Success}.
 */
public sealed interface IqUsyncResponse extends IqOperation.Response
        permits IqUsyncResponse.Success, IqUsyncResponse.ClientError, IqUsyncResponse.ServerError {

    /**
     * Tries each {@link IqUsyncResponse} variant in priority order and returns
     * the first that parses cleanly.
     *
     * @param node    the inbound IQ stanza received from the relay.
     *                Never {@code null}
     * @param request the original outbound stanza. Used to validate
     *                echoed identifiers. Never {@code null}
     * @return an {@link Optional} carrying the parsed variant, or
     *         {@link Optional#empty()} when no documented variant
     *         matched the stanza shape
     * @throws NullPointerException if either argument is {@code null}
     */
    @WhatsAppWebExport(moduleName = "WAWebUsync",
            exports = "USyncQuery", adaptation = WhatsAppAdaptation.ADAPTED)
    static Optional<? extends IqUsyncResponse> of(Node node, Node request) {
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
     * The {@code Success} reply variant. The relay returned the
     * per-user attribute projections plus per-protocol envelope
     * status (errors and refresh hints).
     */
    @WhatsAppWebModule(moduleName = "WAWebUsync")
    final class Success implements IqUsyncResponse {
        /**
         * The list of per-protocol envelopes returned in the
         * {@code <result/>} grandchild. One entry per protocol
         * that the relay was queried for.
         */
        private final List<ProtocolEnvelope> protocolEnvelopes;

        /**
         * The list of per-user results returned in the
         * {@code <list/>} grandchild. One entry per user the
         * relay successfully resolved (relay drops users with no
         * resolvable JID).
         */
        private final List<UserResult> userResults;

        /**
         * Constructs a new successful reply.
         *
         * @param protocolEnvelopes the per-protocol envelopes.
         *                          Never {@code null}
         * @param userResults       the per-user results. Never
         *                          {@code null}
         * @throws NullPointerException if any reference argument
         *                              is {@code null}
         */
        public Success(List<ProtocolEnvelope> protocolEnvelopes, List<UserResult> userResults) {
            Objects.requireNonNull(protocolEnvelopes, "protocolEnvelopes cannot be null");
            Objects.requireNonNull(userResults, "userResults cannot be null");
            this.protocolEnvelopes = List.copyOf(protocolEnvelopes);
            this.userResults = List.copyOf(userResults);
        }

        /**
         * Returns the unmodifiable list of per-protocol envelopes.
         *
         * @return the envelopes. Never {@code null}
         */
        public List<ProtocolEnvelope> protocolEnvelopes() {
            return protocolEnvelopes;
        }

        /**
         * Returns the unmodifiable list of per-user results.
         *
         * @return the results. Never {@code null}
         */
        public List<UserResult> userResults() {
            return userResults;
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
         *
         * @implNote {@code WAWebUsync.usyncParser} projects
         *           {@code child("usync").{child("result"),
         *           child("list")}} into the per-protocol error
         *           map and the per-user result list. Cobalt
         *           preserves the structure with explicit
         *           {@link ProtocolEnvelope} and
         *           {@link UserResult} nested types.
         */
        @WhatsAppWebExport(moduleName = "WAWebUsync",
                exports = "USyncQuery", adaptation = WhatsAppAdaptation.ADAPTED)
        public static Optional<Success> of(Node node, Node request) {
            if (!SmaxIqResultResponseMixin.validate(node, request)) {
                return Optional.empty();
            }
            var usyncChild = node.getChild("usync").orElse(null);
            if (usyncChild == null) {
                return Optional.empty();
            }
            var resultChild = usyncChild.getChild("result").orElse(null);
            var listChild = usyncChild.getChild("list").orElse(null);
            var envelopes = new ArrayList<ProtocolEnvelope>();
            if (resultChild != null) {
                for (var protocolNode : resultChild.children()) {
                    envelopes.add(ProtocolEnvelope.of(protocolNode));
                }
            }
            var users = new ArrayList<UserResult>();
            if (listChild != null) {
                for (var userNode : listChild.getChildren("user")) {
                    var userJid = userNode.getAttributeAsJid("jid").orElse(null);
                    var pnJid = userNode.getAttributeAsJid("pn_jid").orElse(null);
                    var payloads = new ArrayList<Node>(userNode.children());
                    users.add(new UserResult(userJid, pnJid, payloads));
                }
            }
            return Optional.of(new Success(envelopes, users));
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
            return Objects.equals(this.protocolEnvelopes, that.protocolEnvelopes)
                    && Objects.equals(this.userResults, that.userResults);
        }

        @Override
        public int hashCode() {
            return Objects.hash(protocolEnvelopes, userResults);
        }

        @Override
        public String toString() {
            return "IqUsyncResponse.Success[protocolEnvelopes=" + protocolEnvelopes
                    + ", userResults=" + userResults + ']';
        }
    }

    /**
     * Per-protocol envelope projected from one grandchild of the
     * inbound {@code <result/>} child. Carries either an
     * {@code <error/>} sub-envelope (per-protocol failure with
     * optional backoff hint) or a {@code refresh} attribute
     * (per-protocol cache TTL hint).
     *
     * @implNote {@code WAWebUsync.usyncParser} projects
     *           {@code maybeChild("error").{code, text, backoff}}
     *           or {@code attrInt("refresh", 0)} per protocol.
     *           Cobalt models both projections.
     */
    @WhatsAppWebModule(moduleName = "WAWebUsync")
    final class ProtocolEnvelope {
        /**
         * The protocol tag this envelope corresponds to (e.g.
         * {@code "devices"}, {@code "contact"}). Routed from the
         * grandchild's tag name.
         */
        private final String protocol;

        /**
         * The numeric per-protocol error code, when the envelope
         * carries an {@code <error/>} sub-envelope. {@code null}
         * indicates "no error".
         */
        private final Integer errorCode;

        /**
         * The human-readable per-protocol error text, when the
         * relay supplied one.
         */
        private final String errorText;

        /**
         * The per-protocol error backoff hint, in seconds. WA Web
         * stores these in the {@code WAWebUsyncBackoff} module
         * for the next call. {@code null} when not supplied.
         */
        private final Integer errorBackoff;

        /**
         * The per-protocol cache TTL hint, in seconds. {@code null}
         * when not supplied (the protocol has no per-protocol
         * cache).
         */
        private final Integer refresh;

        /**
         * Constructs a new envelope.
         *
         * @param protocol     the protocol tag. Never {@code null}
         * @param errorCode    the optional error code
         * @param errorText    the optional error text
         * @param errorBackoff the optional backoff hint in seconds
         * @param refresh      the optional refresh hint in seconds
         * @throws NullPointerException if {@code protocol} is
         *                              {@code null}
         */
        public ProtocolEnvelope(String protocol, Integer errorCode, String errorText,
                                Integer errorBackoff, Integer refresh) {
            this.protocol = Objects.requireNonNull(protocol, "protocol cannot be null");
            this.errorCode = errorCode;
            this.errorText = errorText;
            this.errorBackoff = errorBackoff;
            this.refresh = refresh;
        }

        /**
         * Returns the protocol tag.
         *
         * @return the tag. Never {@code null}
         */
        public String protocol() {
            return protocol;
        }

        /**
         * Returns the optional per-protocol error code.
         *
         * @return an {@link Optional} carrying the code, or empty
         *         when the envelope has no error
         */
        public Optional<Integer> errorCode() {
            return Optional.ofNullable(errorCode);
        }

        /**
         * Returns the optional per-protocol error text.
         *
         * @return an {@link Optional} carrying the text, or empty
         */
        public Optional<String> errorText() {
            return Optional.ofNullable(errorText);
        }

        /**
         * Returns the optional per-protocol backoff hint in
         * seconds.
         *
         * @return an {@link Optional} carrying the seconds, or
         *         empty
         */
        public Optional<Integer> errorBackoff() {
            return Optional.ofNullable(errorBackoff);
        }

        /**
         * Returns the optional per-protocol refresh hint in
         * seconds.
         *
         * @return an {@link Optional} carrying the seconds, or
         *         empty
         */
        public Optional<Integer> refresh() {
            return Optional.ofNullable(refresh);
        }

        /**
         * Parses an envelope from the given protocol grandchild.
         *
         * @param protocolNode the protocol grandchild. Never
         *                     {@code null}
         * @return the parsed envelope
         */
        @WhatsAppWebExport(moduleName = "WAWebUsync",
                exports = "usyncParser", adaptation = WhatsAppAdaptation.ADAPTED)
        public static ProtocolEnvelope of(Node protocolNode) {
            Objects.requireNonNull(protocolNode, "protocolNode cannot be null");
            var protocol = protocolNode.description();
            var errorChild = protocolNode.getChild("error").orElse(null);
            Integer errorCode = null;
            String errorText = null;
            Integer errorBackoff = null;
            Integer refresh = null;
            if (errorChild != null) {
                var codeOpt = errorChild.getAttributeAsInt("code");
                errorCode = codeOpt.isPresent() ? codeOpt.getAsInt() : null;
                errorText = errorChild.getAttributeAsString("text").orElse(null);
                var backoffOpt = errorChild.getAttributeAsInt("backoff");
                errorBackoff = backoffOpt.isPresent() ? backoffOpt.getAsInt() : null;
            } else if (protocolNode.hasAttribute("refresh")) {
                refresh = protocolNode.getAttributeAsInt("refresh", 0);
            }
            return new ProtocolEnvelope(protocol, errorCode, errorText, errorBackoff, refresh);
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == this) {
                return true;
            }
            if (obj == null || obj.getClass() != this.getClass()) {
                return false;
            }
            var that = (ProtocolEnvelope) obj;
            return Objects.equals(this.protocol, that.protocol)
                    && Objects.equals(this.errorCode, that.errorCode)
                    && Objects.equals(this.errorText, that.errorText)
                    && Objects.equals(this.errorBackoff, that.errorBackoff)
                    && Objects.equals(this.refresh, that.refresh);
        }

        @Override
        public int hashCode() {
            return Objects.hash(protocol, errorCode, errorText, errorBackoff, refresh);
        }

        @Override
        public String toString() {
            return "IqUsyncResponse.ProtocolEnvelope[protocol=" + protocol
                    + ", errorCode=" + errorCode
                    + ", errorText=" + errorText
                    + ", errorBackoff=" + errorBackoff
                    + ", refresh=" + refresh + ']';
        }
    }

    /**
     * Per-user result projected from one {@code <user/>}
     * grandchild of the inbound {@code <list/>} child.
     *
     * <p>Carries the resolved JIDs plus the raw per-protocol
     * payload nodes ({@code <devices/>}, {@code <contact/>},
     * {@code <picture/>}, etc.) which the caller routes through
     * the protocol-specific parsers depending on which protocols
     * were requested.
     *
     * @implNote {@code WAWebUsync.m()} projects
     *           {@code attrDeviceJid("jid")} +
     *           {@code attrDeviceJid("pn_jid")} plus the
     *           per-protocol payload subtrees. Cobalt keeps the
     *           payload subtrees as raw {@link Node} entries so
     *           the caller can route through whichever
     *           protocol-specific parser is appropriate.
     */
    @WhatsAppWebModule(moduleName = "WAWebUsync")
    final class UserResult {
        /**
         * The optional primary user JID returned by the relay.
         */
        private final Jid userJid;

        /**
         * The optional phone-number JID returned by the relay.
         */
        private final Jid pnJid;

        /**
         * The list of per-protocol payload nodes returned for
         * this user.
         */
        private final List<Node> protocolPayloads;

        /**
         * Constructs a new user result.
         *
         * @param userJid          the optional user JID
         * @param pnJid            the optional phone JID
         * @param protocolPayloads the per-protocol payload nodes.
         *                         Never {@code null}
         * @throws NullPointerException if {@code protocolPayloads}
         *                              is {@code null}
         */
        public UserResult(Jid userJid, Jid pnJid, List<Node> protocolPayloads) {
            this.userJid = userJid;
            this.pnJid = pnJid;
            Objects.requireNonNull(protocolPayloads, "protocolPayloads cannot be null");
            this.protocolPayloads = List.copyOf(protocolPayloads);
        }

        /**
         * Returns the optional primary user JID.
         *
         * @return an {@link Optional} carrying the JID, or empty
         */
        public Optional<Jid> userJid() {
            return Optional.ofNullable(userJid);
        }

        /**
         * Returns the optional phone-number JID.
         *
         * @return an {@link Optional} carrying the JID, or empty
         */
        public Optional<Jid> pnJid() {
            return Optional.ofNullable(pnJid);
        }

        /**
         * Returns the unmodifiable list of per-protocol payload
         * nodes.
         *
         * @return the payloads. Never {@code null}
         */
        public List<Node> protocolPayloads() {
            return protocolPayloads;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == this) {
                return true;
            }
            if (obj == null || obj.getClass() != this.getClass()) {
                return false;
            }
            var that = (UserResult) obj;
            return Objects.equals(this.userJid, that.userJid)
                    && Objects.equals(this.pnJid, that.pnJid)
                    && Objects.equals(this.protocolPayloads, that.protocolPayloads);
        }

        @Override
        public int hashCode() {
            return Objects.hash(userJid, pnJid, protocolPayloads);
        }

        @Override
        public String toString() {
            return "IqUsyncResponse.UserResult[userJid=" + userJid
                    + ", pnJid=" + pnJid
                    + ", protocolPayloads=" + protocolPayloads + ']';
        }
    }

    /**
     * The {@code ClientError} reply variant. The relay rejected
     * the entire usync as malformed or unauthorised.
     */
    @WhatsAppWebModule(moduleName = "WAWebUsync")
    final class ClientError implements IqUsyncResponse {
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
         * @param errorText the optional human-readable text. May be
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
         * @return an {@link Optional} carrying the error text, or empty
         *         when the relay omitted it
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
        @WhatsAppWebExport(moduleName = "WAWebUsync",
                exports = "USyncQuery", adaptation = WhatsAppAdaptation.ADAPTED)
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
            return "IqUsyncResponse.ClientError[errorCode=" + errorCode
                    + ", errorText=" + errorText + ']';
        }
    }

    /**
     * The {@code ServerError} reply variant. The relay encountered a
     * transient internal failure while processing the usync.
     */
    @WhatsAppWebModule(moduleName = "WAWebUsync")
    final class ServerError implements IqUsyncResponse {
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
         * @param errorText the optional human-readable text. May be
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
         * @return an {@link Optional} carrying the error text, or empty
         *         when the relay omitted it
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
        @WhatsAppWebExport(moduleName = "WAWebUsync",
                exports = "USyncQuery", adaptation = WhatsAppAdaptation.ADAPTED)
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
            return "IqUsyncResponse.ServerError[errorCode=" + errorCode
                    + ", errorText=" + errorText + ']';
        }
    }
}
