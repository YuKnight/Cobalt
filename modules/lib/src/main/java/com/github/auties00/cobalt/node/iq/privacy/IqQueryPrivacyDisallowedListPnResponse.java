package com.github.auties00.cobalt.node.iq.privacy;

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
 */
public sealed interface IqQueryPrivacyDisallowedListPnResponse extends IqOperation.Response
        permits IqQueryPrivacyDisallowedListPnResponse.Success, IqQueryPrivacyDisallowedListPnResponse.ClientError, IqQueryPrivacyDisallowedListPnResponse.ServerError {

    /**
     * Tries each {@link IqQueryPrivacyDisallowedListPnResponse} variant in priority order.
     *
     * @param node    the inbound IQ stanza. Never {@code null}
     * @param request the original outbound stanza. Never {@code null}
     * @return an {@link Optional} carrying the parsed variant, or
     *         empty when no documented variant matched
     * @throws NullPointerException if either argument is
     *                              {@code null}
     */
    @WhatsAppWebExport(moduleName = "WAWebQueryPrivacyDisallowedListPnJob",
            exports = "queryPrivacyDisallowedListPn",
            adaptation = WhatsAppAdaptation.ADAPTED)
    static Optional<? extends IqQueryPrivacyDisallowedListPnResponse> of(Node node, Node request) {
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
     * The {@code Success} reply variant. The relay accepted the
     * query and either confirmed the local cache is authoritative
     * ({@link Match}) or shipped the up-to-date authoritative list
     * ({@link Mismatch}).
     */
    @WhatsAppWebModule(moduleName = "WAWebQueryPrivacyDisallowedListPnJob")
    sealed interface Success extends IqQueryPrivacyDisallowedListPnResponse permits Match, Mismatch {

        /**
         * Tries to parse a {@link Success} variant.
         *
         * @param node    the inbound IQ stanza
         * @param request the original outbound request
         * @return an {@link Optional} carrying the parsed variant,
         *         or empty when the stanza does not match the
         *         success schema
         */
        @WhatsAppWebExport(moduleName = "WAWebQueryPrivacyDisallowedListPnJob",
                exports = "getPrivacyDisallowedListParser",
                adaptation = WhatsAppAdaptation.ADAPTED)
        static Optional<? extends Success> of(Node node, Node request) {
            if (!SmaxIqResultResponseMixin.validate(node, request)) {
                return Optional.empty();
            }
            var privacy = node.getChild("privacy").orElse(null);
            if (privacy == null) {
                return Optional.empty();
            }
            var list = privacy.getChild("list").orElse(null);
            if (list == null) {
                return Optional.of(new Match());
            }
            var dhash = list.getAttributeAsString("dhash").orElse(null);
            if (dhash == null) {
                return Optional.empty();
            }
            var users = new ArrayList<Jid>();
            for (var userNode : list.getChildren("user")) {
                var jid = userNode.getAttributeAsJid("jid").orElse(null);
                if (jid == null) {
                    continue;
                }
                users.add(jid);
            }
            return Optional.of(new Mismatch(users, dhash));
        }
    }

    /**
     * The {@code Match} sub-variant. The relay omitted the
     * {@code <list>} grandchild, signalling the local copy of
     * the disallowed list is still authoritative.
     */
    @WhatsAppWebModule(moduleName = "WAWebQueryPrivacyDisallowedListPnJob")
    final class Match implements Success {
        /**
         * Constructs a {@code Match} reply.
         */
        public Match() {
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
            return Match.class.hashCode();
        }

        @Override
        public String toString() {
            return "IqQueryPrivacyDisallowedListPnResponse.Match[]";
        }
    }

    /**
     * The {@code Mismatch} sub-variant. The relay shipped the
     * authoritative {@code <list dhash="..."><user jid="..."/>...
     * </list>} envelope. The caller must replace the local cache
     * with this snapshot.
     */
    @WhatsAppWebModule(moduleName = "WAWebQueryPrivacyDisallowedListPnJob")
    final class Mismatch implements Success {
        /**
         * The authoritative user list shipped by the relay.
         */
        private final List<Jid> users;

        /**
         * The relay-side digest of the shipped list. Clients
         * persist this and replay it as the {@code dhash} attribute
         * on subsequent {@code WAWebSetPrivacyJob} mutations.
         */
        private final String dhash;

        /**
         * Constructs a {@code Mismatch} reply.
         *
         * @param users the authoritative user list. Never
         *              {@code null}
         * @param dhash the relay-side digest. Never {@code null}
         * @throws NullPointerException if either argument is
         *                              {@code null}
         */
        public Mismatch(List<Jid> users, String dhash) {
            Objects.requireNonNull(users, "users cannot be null");
            this.users = List.copyOf(users);
            this.dhash = Objects.requireNonNull(dhash, "dhash cannot be null");
        }

        /**
         * Returns the authoritative user list.
         *
         * @return an unmodifiable list. Never {@code null}
         */
        public List<Jid> users() {
            return users;
        }

        /**
         * Returns the relay-side digest of the user list.
         *
         * @return the digest string. Never {@code null}
         */
        public String dhash() {
            return dhash;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == this) {
                return true;
            }
            if (obj == null || obj.getClass() != this.getClass()) {
                return false;
            }
            var that = (Mismatch) obj;
            return Objects.equals(this.users, that.users)
                    && Objects.equals(this.dhash, that.dhash);
        }

        @Override
        public int hashCode() {
            return Objects.hash(users, dhash);
        }

        @Override
        public String toString() {
            return "IqQueryPrivacyDisallowedListPnResponse.Mismatch[users=" + users
                    + ", dhash=" + dhash + ']';
        }
    }

    /**
     * The {@code ClientError} reply variant. The relay rejected
     * the query with a {@code 4xx} error code.
     */
    @WhatsAppWebModule(moduleName = "WAWebQueryPrivacyDisallowedListPnJob")
    final class ClientError implements IqQueryPrivacyDisallowedListPnResponse {
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
         * @param errorText the optional text. May be {@code null}
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
        @WhatsAppWebExport(moduleName = "WAWebQueryPrivacyDisallowedListPnJob",
                exports = "queryPrivacyDisallowedListPn",
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
            return "IqQueryPrivacyDisallowedListPnResponse.ClientError[errorCode="
                    + errorCode + ", errorText=" + errorText + ']';
        }
    }

    /**
     * The {@code ServerError} reply variant. Transient internal
     * failure ({@code 5xx} error code).
     */
    @WhatsAppWebModule(moduleName = "WAWebQueryPrivacyDisallowedListPnJob")
    final class ServerError implements IqQueryPrivacyDisallowedListPnResponse {
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
         * @param errorText the optional text. May be {@code null}
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
        @WhatsAppWebExport(moduleName = "WAWebQueryPrivacyDisallowedListPnJob",
                exports = "queryPrivacyDisallowedListPn",
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
            return "IqQueryPrivacyDisallowedListPnResponse.ServerError[errorCode="
                    + errorCode + ", errorText=" + errorText + ']';
        }
    }
}
