package com.github.auties00.cobalt.node.smax.biz;

import com.github.auties00.cobalt.meta.annotation.WhatsAppWebExport;
import com.github.auties00.cobalt.meta.annotation.WhatsAppWebModule;
import com.github.auties00.cobalt.meta.model.WhatsAppAdaptation;
import com.github.auties00.cobalt.model.jid.Jid;
import com.github.auties00.cobalt.model.jid.JidServer;
import com.github.auties00.cobalt.node.Node;
import com.github.auties00.cobalt.node.NodeBuilder;
import com.github.auties00.cobalt.node.smax.SmaxOperation;
import com.github.auties00.cobalt.node.smax.util.SmaxBaseServerErrorMixin;
import com.github.auties00.cobalt.node.smax.util.SmaxIqErrorResponseMixin;
import com.github.auties00.cobalt.node.smax.util.SmaxIqResultResponseMixin;
import java.util.Objects;
import java.util.Optional;

/**
 * Sealed family of inbound reply variants produced by the relay in
 * response to a {@link SmaxGetAccessTokenAndSessionCookiesRequest}.
 *
 * @implNote {@code WASmaxBizCtwaAdAccountGetAccessTokenAndSessionCookiesRPC.sendGetAccessTokenAndSessionCookiesRPC}
 *           tries {@code Success} → {@code TooManyAttempts} →
 *           {@code IncorrectNonce} → {@code Error} → fallback
 *           {@code ServerError} in order.
 */
public sealed interface SmaxGetAccessTokenAndSessionCookiesResponse extends SmaxOperation.Response
        permits SmaxGetAccessTokenAndSessionCookiesResponse.Success, SmaxGetAccessTokenAndSessionCookiesResponse.TooManyAttempts,
        SmaxGetAccessTokenAndSessionCookiesResponse.IncorrectNonce, SmaxGetAccessTokenAndSessionCookiesResponse.ClientError, SmaxGetAccessTokenAndSessionCookiesResponse.ServerError {

    /**
     * Tries each {@link SmaxGetAccessTokenAndSessionCookiesResponse} variant in priority order and
     * returns the first that parses cleanly.
     *
     * @param node    the inbound IQ stanza received from the relay;
     *                never {@code null}
     * @param request the original outbound stanza. Used to validate
     *                echoed identifiers; never {@code null}
     * @return an {@link Optional} carrying the parsed variant, or
     *         {@link Optional#empty()} when no documented variant
     *         matched the stanza shape
     * @throws NullPointerException if either argument is {@code null}
     */
    @WhatsAppWebExport(moduleName = "WASmaxBizCtwaAdAccountGetAccessTokenAndSessionCookiesRPC",
            exports = "sendGetAccessTokenAndSessionCookiesRPC",
            adaptation = WhatsAppAdaptation.ADAPTED)
    static Optional<? extends SmaxGetAccessTokenAndSessionCookiesResponse> of(Node node, Node request) {
        Objects.requireNonNull(node, "node cannot be null");
        Objects.requireNonNull(request, "request cannot be null");
        var success = Success.of(node, request);
        if (success.isPresent()) {
            return success;
        }
        var tooManyAttempts = TooManyAttempts.of(node, request);
        if (tooManyAttempts.isPresent()) {
            return tooManyAttempts;
        }
        var incorrectNonce = IncorrectNonce.of(node, request);
        if (incorrectNonce.isPresent()) {
            return incorrectNonce;
        }
        var clientError = ClientError.of(node, request);
        if (clientError.isPresent()) {
            return clientError;
        }
        return ServerError.of(node, request);
    }

    /**
     * The {@code Success} reply variant. The relay validated the
     * verification code and supplied the access token, session
     * cookies, and business-person identity.
     *
     * @implNote {@code WASmaxInBizCtwaAdAccountGetAccessTokenAndSessionCookiesResponseSuccess.parseGetAccessTokenAndSessionCookiesResponseSuccess}
     *           validates the {@code <iq from id type="result">}
     *           envelope, asserts the
     *           {@code <access_token>} / {@code <session_cookies>} /
     *           {@code <business_person id="..."/>} children, then
     *           projects the optional {@code <token_type>} child
     *           (content enum {@code "STRONG"} / {@code "WEAK"}).
     */
    @WhatsAppWebModule(moduleName = "WASmaxInBizCtwaAdAccountGetAccessTokenAndSessionCookiesResponseSuccess")
    final class Success implements SmaxGetAccessTokenAndSessionCookiesResponse {
        /**
         * The element-content of the {@code <access_token>} child.
         * The bearer token used to authenticate to the Facebook Graph
         * API.
         */
        private final String accessToken;

        /**
         * The element-content of the {@code <session_cookies>}
         * child. JSON-encoded blob of session cookies for the
         * Facebook Ads Manager web UI.
         */
        private final String sessionCookies;

        /**
         * The {@code id} attribute of the {@code <business_person>}
         * child. The Facebook business-person identifier the token
         * is scoped to.
         */
        private final String businessPersonId;

        /**
         * The optional {@code <token_type>} content (the
         * {@code "STRONG"} / {@code "WEAK"} enum); may be
         * {@code null} when the relay omitted the child.
         */
        private final String tokenType;

        /**
         * Constructs a new successful reply.
         *
         * @param accessToken      the access token; never
         *                         {@code null}
         * @param sessionCookies   the session-cookies blob; never
         *                         {@code null}
         * @param businessPersonId the business-person identifier;
         *                         never {@code null}
         * @param tokenType        the optional token type; may be
         *                         {@code null}
         * @throws NullPointerException if any of {@code accessToken},
         *                              {@code sessionCookies}, or
         *                              {@code businessPersonId} is
         *                              {@code null}
         */
        public Success(String accessToken, String sessionCookies,
                       String businessPersonId, String tokenType) {
            this.accessToken = Objects.requireNonNull(accessToken, "accessToken cannot be null");
            this.sessionCookies = Objects.requireNonNull(sessionCookies, "sessionCookies cannot be null");
            this.businessPersonId = Objects.requireNonNull(businessPersonId, "businessPersonId cannot be null");
            this.tokenType = tokenType;
        }

        /**
         * Returns the access token.
         *
         * @return the access token; never {@code null}
         */
        public String accessToken() {
            return accessToken;
        }

        /**
         * Returns the session-cookies blob.
         *
         * @return the session cookies; never {@code null}
         */
        public String sessionCookies() {
            return sessionCookies;
        }

        /**
         * Returns the business-person identifier.
         *
         * @return the business-person ID; never {@code null}
         */
        public String businessPersonId() {
            return businessPersonId;
        }

        /**
         * Returns the optional token-type marker.
         *
         * @return an {@link Optional} carrying the token type
         *         ({@code "STRONG"} or {@code "WEAK"}), or empty
         *         when the relay omitted the {@code <token_type>}
         *         child
         */
        public Optional<String> tokenType() {
            return Optional.ofNullable(tokenType);
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
        @WhatsAppWebExport(moduleName = "WASmaxInBizCtwaAdAccountGetAccessTokenAndSessionCookiesResponseSuccess",
                exports = "parseGetAccessTokenAndSessionCookiesResponseSuccess",
                adaptation = WhatsAppAdaptation.ADAPTED)
        public static Optional<Success> of(Node node, Node request) {
            Objects.requireNonNull(node, "node cannot be null");
            Objects.requireNonNull(request, "request cannot be null");
            if (!SmaxIqResultResponseMixin.validate(node, request)) {
                return Optional.empty();
            }
            var accessTokenNode = node.getChild("access_token").orElse(null);
            if (accessTokenNode == null) {
                return Optional.empty();
            }
            var accessToken = accessTokenNode.toContentString().orElse(null);
            if (accessToken == null) {
                return Optional.empty();
            }
            var sessionCookiesNode = node.getChild("session_cookies").orElse(null);
            if (sessionCookiesNode == null) {
                return Optional.empty();
            }
            var sessionCookies = sessionCookiesNode.toContentString().orElse(null);
            if (sessionCookies == null) {
                return Optional.empty();
            }
            var businessPersonNode = node.getChild("business_person").orElse(null);
            if (businessPersonNode == null) {
                return Optional.empty();
            }
            var businessPersonId = businessPersonNode.getAttributeAsString("id").orElse(null);
            if (businessPersonId == null) {
                return Optional.empty();
            }
            var tokenTypeNode = node.getChild("token_type").orElse(null);
            String tokenType = null;
            if (tokenTypeNode != null) {
                tokenType = tokenTypeNode.toContentString().orElse(null);
                if (tokenType == null) {
                    return Optional.empty();
                }
            }
            return Optional.of(new Success(accessToken, sessionCookies, businessPersonId, tokenType));
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
            return Objects.equals(this.accessToken, that.accessToken)
                    && Objects.equals(this.sessionCookies, that.sessionCookies)
                    && Objects.equals(this.businessPersonId, that.businessPersonId)
                    && Objects.equals(this.tokenType, that.tokenType);
        }

        @Override
        public int hashCode() {
            return Objects.hash(accessToken, sessionCookies, businessPersonId, tokenType);
        }

        @Override
        public String toString() {
            return "SmaxGetAccessTokenAndSessionCookiesResponse.Success[accessToken=" + accessToken
                    + ", sessionCookies=" + sessionCookies
                    + ", businessPersonId=" + businessPersonId
                    + ", tokenType=" + tokenType + ']';
        }
    }

    /**
     * The {@code TooManyAttempts} reply variant. The relay refused
     * to validate further verification codes because the user has
     * exhausted the rate limit for this nonce.
     *
     * <p>Identified by the literal
     * {@code <error code="431" text="TOO_MANY_ATTEMPTS"/>} pair on
     * the {@code <error/>} child of the {@code <iq type="error">}
     * envelope.
     *
     * @implNote {@code WASmaxInBizCtwaAdAccountGetAccessTokenAndSessionCookiesResponseTooManyAttempts.parseGetAccessTokenAndSessionCookiesResponseTooManyAttempts}.
     */
    @WhatsAppWebModule(moduleName = "WASmaxInBizCtwaAdAccountGetAccessTokenAndSessionCookiesResponseTooManyAttempts")
    final class TooManyAttempts implements SmaxGetAccessTokenAndSessionCookiesResponse {
        /**
         * Constructs a new too-many-attempts reply.
         */
        public TooManyAttempts() {
        }

        /**
         * Tries to parse a {@link TooManyAttempts} variant from the
         * given inbound stanza.
         *
         * @param node    the inbound IQ stanza
         * @param request the original outbound request
         * @return an {@link Optional} carrying the parsed variant, or
         *         empty when the stanza does not match the literal
         *         {@code 431} / {@code TOO_MANY_ATTEMPTS} schema
         */
        @WhatsAppWebExport(moduleName = "WASmaxInBizCtwaAdAccountGetAccessTokenAndSessionCookiesResponseTooManyAttempts",
                exports = "parseGetAccessTokenAndSessionCookiesResponseTooManyAttempts",
                adaptation = WhatsAppAdaptation.ADAPTED)
        public static Optional<TooManyAttempts> of(Node node, Node request) {
            Objects.requireNonNull(node, "node cannot be null");
            Objects.requireNonNull(request, "request cannot be null");
            if (!SmaxIqErrorResponseMixin.validate(node, request)) {
                return Optional.empty();
            }
            var error = node.getChild("error").orElse(null);
            if (error == null) {
                return Optional.empty();
            }
            if (!error.hasAttribute("code", "431")) {
                return Optional.empty();
            }
            if (!error.hasAttribute("text", "TOO_MANY_ATTEMPTS")) {
                return Optional.empty();
            }
            return Optional.of(new TooManyAttempts());
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
            return TooManyAttempts.class.hashCode();
        }

        @Override
        public String toString() {
            return "SmaxGetAccessTokenAndSessionCookiesResponse.TooManyAttempts[]";
        }
    }

    /**
     * The {@code IncorrectNonce} reply variant. The user-supplied
     * verification code did not match the relay-side nonce.
     *
     * <p>Identified by the literal
     * {@code <error code="432" text="INCORRECT_NONCE"/>} pair on
     * the {@code <error/>} child of the {@code <iq type="error">}
     * envelope.
     *
     * @implNote {@code WASmaxInBizCtwaAdAccountGetAccessTokenAndSessionCookiesResponseIncorrectNonce.parseGetAccessTokenAndSessionCookiesResponseIncorrectNonce}.
     */
    @WhatsAppWebModule(moduleName = "WASmaxInBizCtwaAdAccountGetAccessTokenAndSessionCookiesResponseIncorrectNonce")
    final class IncorrectNonce implements SmaxGetAccessTokenAndSessionCookiesResponse {
        /**
         * Constructs a new incorrect-nonce reply.
         */
        public IncorrectNonce() {
        }

        /**
         * Tries to parse an {@link IncorrectNonce} variant from the
         * given inbound stanza.
         *
         * @param node    the inbound IQ stanza
         * @param request the original outbound request
         * @return an {@link Optional} carrying the parsed variant, or
         *         empty when the stanza does not match the literal
         *         {@code 432} / {@code INCORRECT_NONCE} schema
         */
        @WhatsAppWebExport(moduleName = "WASmaxInBizCtwaAdAccountGetAccessTokenAndSessionCookiesResponseIncorrectNonce",
                exports = "parseGetAccessTokenAndSessionCookiesResponseIncorrectNonce",
                adaptation = WhatsAppAdaptation.ADAPTED)
        public static Optional<IncorrectNonce> of(Node node, Node request) {
            Objects.requireNonNull(node, "node cannot be null");
            Objects.requireNonNull(request, "request cannot be null");
            if (!SmaxIqErrorResponseMixin.validate(node, request)) {
                return Optional.empty();
            }
            var error = node.getChild("error").orElse(null);
            if (error == null) {
                return Optional.empty();
            }
            if (!error.hasAttribute("code", "432")) {
                return Optional.empty();
            }
            if (!error.hasAttribute("text", "INCORRECT_NONCE")) {
                return Optional.empty();
            }
            return Optional.of(new IncorrectNonce());
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
            return IncorrectNonce.class.hashCode();
        }

        @Override
        public String toString() {
            return "SmaxGetAccessTokenAndSessionCookiesResponse.IncorrectNonce[]";
        }
    }

    /**
     * The {@code ClientError} reply variant. The relay rejected the
     * request with a documented common-ad-account error code in the
     * {@code 4xx} range that is NOT one of the dedicated
     * {@code 431}/{@code 432} literals.
     *
     * @implNote {@code WASmaxInBizCtwaAdAccountGetAccessTokenAndSessionCookiesResponseError.parseGetAccessTokenAndSessionCookiesResponseError}
     *           routes the {@code <error/>} child through
     *           {@code WASmaxInBizCtwaAdAccountCommonAdAccountErrors}.
     *           Cobalt collapses to the raw {@code (code, text)}
     *           pair via the shared
     *           {@link SmaxBaseServerErrorMixin#parseClientError}.
     */
    @WhatsAppWebModule(moduleName = "WASmaxInBizCtwaAdAccountGetAccessTokenAndSessionCookiesResponseError")
    @WhatsAppWebModule(moduleName = "WASmaxInBizCtwaAdAccountCommonAdAccountErrors")
    final class ClientError implements SmaxGetAccessTokenAndSessionCookiesResponse {
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
         *         client-error schema, or when the literal
         *         {@code 431}/{@code 432} pair would have matched a
         *         dedicated variant
         */
        @WhatsAppWebExport(moduleName = "WASmaxInBizCtwaAdAccountGetAccessTokenAndSessionCookiesResponseError",
                exports = "parseGetAccessTokenAndSessionCookiesResponseError",
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
            return "SmaxGetAccessTokenAndSessionCookiesResponse.ClientError[errorCode=" + errorCode
                    + ", errorText=" + errorText + ']';
        }
    }

    /**
     * The {@code ServerError} reply variant. The relay encountered a
     * transient internal failure ({@code 5xx}) while processing the
     * request.
     *
     * @implNote Sourced from the {@code 5xx} arms of
     *           {@code WASmaxInBizCtwaAdAccountCommonAdAccountErrors};
     *           Cobalt routes through the shared
     *           {@link SmaxBaseServerErrorMixin}.
     */
    @WhatsAppWebModule(moduleName = "WASmaxInBizCtwaAdAccountGetAccessTokenAndSessionCookiesResponseError")
    final class ServerError implements SmaxGetAccessTokenAndSessionCookiesResponse {
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
            return "SmaxGetAccessTokenAndSessionCookiesResponse.ServerError[errorCode=" + errorCode
                    + ", errorText=" + errorText + ']';
        }
    }
}
