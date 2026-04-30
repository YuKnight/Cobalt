package com.github.auties00.cobalt.node.smax.biz;

import com.github.auties00.cobalt.meta.annotation.WhatsAppWebExport;
import com.github.auties00.cobalt.meta.annotation.WhatsAppWebModule;
import com.github.auties00.cobalt.meta.model.WhatsAppAdaptation;
import com.github.auties00.cobalt.node.Node;
import com.github.auties00.cobalt.node.NodeBuilder;
import com.github.auties00.cobalt.node.smax.SmaxOperation;
import com.github.auties00.cobalt.node.smax.util.SmaxBaseServerErrorMixin;
import com.github.auties00.cobalt.node.smax.util.SmaxIqResultResponseMixin;
import java.util.Locale;
import java.util.Objects;
import java.util.Optional;
import java.util.OptionalInt;

/**
 * Sealed family of inbound reply variants produced by the relay in
 * response to a {@link SmaxGetBusinessEligibilityRequest}.
 *
 * @implNote {@code WASmaxBizMarketingMessageGetBusinessEligibilityRPC.sendGetBusinessEligibilityRPC}
 *           tries {@code Success} → {@code Error} in order. Cobalt
 *           collapses the {@code Error} arm into the
 *           {@code ClientError}/{@code ServerError} pair via the
 *           shared {@link SmaxBaseServerErrorMixin} helpers.
 */
public sealed interface SmaxGetBusinessEligibilityResponse extends SmaxOperation.Response
        permits SmaxGetBusinessEligibilityResponse.Success, SmaxGetBusinessEligibilityResponse.ClientError, SmaxGetBusinessEligibilityResponse.ServerError {

    /**
     * Tries each {@link SmaxGetBusinessEligibilityResponse} variant in priority order and
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
    @WhatsAppWebExport(moduleName = "WASmaxBizMarketingMessageGetBusinessEligibilityRPC",
            exports = "sendGetBusinessEligibilityRPC",
            adaptation = WhatsAppAdaptation.ADAPTED)
    static Optional<? extends SmaxGetBusinessEligibilityResponse> of(Node node, Node request) {
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
     * The {@code Success} reply variant. Carries 0..3 optional
     * feature-eligibility projections.
     *
     * @implNote {@code WASmaxInBizMarketingMessageGetBusinessEligibilityResponseSuccess.parseGetBusinessEligibilityResponseSuccess}
     *           validates the {@code <iq from id type="result">}
     *           envelope, then optionally projects the three
     *           top-level children: {@code <meta_verified/>},
     *           {@code <marketing_messages/>}, and {@code <genai/>}.
     */
    @WhatsAppWebModule(moduleName = "WASmaxInBizMarketingMessageGetBusinessEligibilityResponseSuccess")
    final class Success implements SmaxGetBusinessEligibilityResponse {
        /**
         * The optional {@code <meta_verified/>} projection.
         */
        private final MetaVerified metaVerified;

        /**
         * The optional {@code <marketing_messages/>} projection.
         */
        private final MarketingMessages marketingMessages;

        /**
         * The optional {@code <genai/>} projection.
         */
        private final Genai genai;

        /**
         * Constructs a new successful reply.
         *
         * @param metaVerified      the optional Meta-Verified
         *                          projection; may be {@code null}
         * @param marketingMessages the optional marketing-messages
         *                          projection; may be {@code null}
         * @param genai             the optional GenAI projection;
         *                          may be {@code null}
         */
        public Success(MetaVerified metaVerified,
                       MarketingMessages marketingMessages,
                       Genai genai) {
            this.metaVerified = metaVerified;
            this.marketingMessages = marketingMessages;
            this.genai = genai;
        }

        /**
         * Returns the optional Meta-Verified projection.
         *
         * @return an {@link Optional} carrying the projection, or
         *         empty when the relay omitted the
         *         {@code <meta_verified/>} child
         */
        public Optional<MetaVerified> metaVerified() {
            return Optional.ofNullable(metaVerified);
        }

        /**
         * Returns the optional marketing-messages projection.
         *
         * @return an {@link Optional} carrying the projection, or
         *         empty when the relay omitted the
         *         {@code <marketing_messages/>} child
         */
        public Optional<MarketingMessages> marketingMessages() {
            return Optional.ofNullable(marketingMessages);
        }

        /**
         * Returns the optional GenAI projection.
         *
         * @return an {@link Optional} carrying the projection, or
         *         empty when the relay omitted the
         *         {@code <genai/>} child
         */
        public Optional<Genai> genai() {
            return Optional.ofNullable(genai);
        }

        /**
         * Tries to parse a {@link Success} variant from the given
         * inbound stanza.
         *
         * @param node    the inbound IQ stanza
         * @param request the original outbound request
         * @return an {@link Optional} carrying the parsed variant,
         *         or empty when the stanza does not match the
         *         success schema
         */
        @WhatsAppWebExport(moduleName = "WASmaxInBizMarketingMessageGetBusinessEligibilityResponseSuccess",
                exports = "parseGetBusinessEligibilityResponseSuccess",
                adaptation = WhatsAppAdaptation.ADAPTED)
        public static Optional<Success> of(Node node, Node request) {
            Objects.requireNonNull(node, "node cannot be null");
            Objects.requireNonNull(request, "request cannot be null");
            if (!SmaxIqResultResponseMixin.validate(node, request)) {
                return Optional.empty();
            }
            MetaVerified metaVerified = null;
            var metaVerifiedNode = node.getChild("meta_verified").orElse(null);
            if (metaVerifiedNode != null) {
                var parsed = MetaVerified.of(metaVerifiedNode);
                if (parsed.isEmpty()) {
                    return Optional.empty();
                }
                metaVerified = parsed.get();
            }
            MarketingMessages marketingMessages = null;
            var marketingNode = node.getChild("marketing_messages").orElse(null);
            if (marketingNode != null) {
                var parsed = MarketingMessages.of(marketingNode);
                if (parsed.isEmpty()) {
                    return Optional.empty();
                }
                marketingMessages = parsed.get();
            }
            Genai genai = null;
            var genaiNode = node.getChild("genai").orElse(null);
            if (genaiNode != null) {
                var parsed = Genai.of(genaiNode);
                if (parsed.isEmpty()) {
                    return Optional.empty();
                }
                genai = parsed.get();
            }
            return Optional.of(new Success(metaVerified, marketingMessages, genai));
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
            return Objects.equals(this.metaVerified, that.metaVerified)
                    && Objects.equals(this.marketingMessages, that.marketingMessages)
                    && Objects.equals(this.genai, that.genai);
        }

        @Override
        public int hashCode() {
            return Objects.hash(metaVerified, marketingMessages, genai);
        }

        @Override
        public String toString() {
            return "SmaxGetBusinessEligibilityResponse.Success[metaVerified=" + metaVerified
                    + ", marketingMessages=" + marketingMessages
                    + ", genai=" + genai + ']';
        }

        /**
         * The {@code <meta_verified/>} child projection. The
         * Meta-Verified eligibility status plus optional
         * onboarding-flow toggles.
         */
        @WhatsAppWebModule(moduleName = "WASmaxInBizMarketingMessageGetBusinessEligibilityResponseSuccess")
        public static final class MetaVerified {
            /**
             * The mandatory {@code status} attribute.
             */
            private final SmaxGetBusinessEligibilityFailSuccessStatus status;

            /**
             * The optional
             * {@code should_show_privacy_interstitial_to_new_users}
             * attribute.
             */
            private final SmaxGetBusinessEligibilityFalseTrueFlag shouldShowPrivacyInterstitialToNewUsers;

            /**
             * The optional {@code additional_params} attribute.
             * Relay-side opaque payload.
             */
            private final String additionalParams;

            /**
             * Constructs a new projection.
             *
             * @param status                                  the
             *                                                eligibility
             *                                                status;
             *                                                never
             *                                                {@code null}
             * @param shouldShowPrivacyInterstitialToNewUsers the
             *                                                optional
             *                                                privacy-interstitial
             *                                                toggle; may
             *                                                be
             *                                                {@code null}
             * @param additionalParams                        the
             *                                                optional
             *                                                opaque
             *                                                params; may
             *                                                be
             *                                                {@code null}
             * @throws NullPointerException if {@code status} is
             *                              {@code null}
             */
            public MetaVerified(SmaxGetBusinessEligibilityFailSuccessStatus status,
                                SmaxGetBusinessEligibilityFalseTrueFlag shouldShowPrivacyInterstitialToNewUsers,
                                String additionalParams) {
                this.status = Objects.requireNonNull(status, "status cannot be null");
                this.shouldShowPrivacyInterstitialToNewUsers = shouldShowPrivacyInterstitialToNewUsers;
                this.additionalParams = additionalParams;
            }

            /**
             * Returns the eligibility status.
             *
             * @return the status; never {@code null}
             */
            public SmaxGetBusinessEligibilityFailSuccessStatus status() {
                return status;
            }

            /**
             * Returns the optional privacy-interstitial toggle.
             *
             * @return an {@link Optional} carrying the toggle, or
             *         empty when the relay omitted the attribute
             */
            public Optional<SmaxGetBusinessEligibilityFalseTrueFlag> shouldShowPrivacyInterstitialToNewUsers() {
                return Optional.ofNullable(shouldShowPrivacyInterstitialToNewUsers);
            }

            /**
             * Returns the optional opaque params.
             *
             * @return an {@link Optional} carrying the params, or
             *         empty when the relay omitted the attribute
             */
            public Optional<String> additionalParams() {
                return Optional.ofNullable(additionalParams);
            }

            /**
             * Tries to parse the projection from the given node.
             *
             * @param node the {@code <meta_verified/>} node
             * @return an {@link Optional} carrying the projection,
             *         or empty when the node does not match the
             *         documented schema
             */
            @WhatsAppWebExport(moduleName = "WASmaxInBizMarketingMessageGetBusinessEligibilityResponseSuccess",
                    exports = "parseGetBusinessEligibilityResponseSuccessMetaVerified",
                    adaptation = WhatsAppAdaptation.ADAPTED)
            public static Optional<MetaVerified> of(Node node) {
                Objects.requireNonNull(node, "node cannot be null");
                if (!node.hasDescription("meta_verified")) {
                    return Optional.empty();
                }
                var statusStr = node.getAttributeAsString("status").orElse(null);
                var status = SmaxGetBusinessEligibilityFailSuccessStatus.of(statusStr).orElse(null);
                if (status == null) {
                    return Optional.empty();
                }
                SmaxGetBusinessEligibilityFalseTrueFlag flag = null;
                var flagStr = node.getAttributeAsString("should_show_privacy_interstitial_to_new_users").orElse(null);
                if (flagStr != null) {
                    flag = SmaxGetBusinessEligibilityFalseTrueFlag.of(flagStr).orElse(null);
                    if (flag == null) {
                        return Optional.empty();
                    }
                }
                var additional = node.getAttributeAsString("additional_params").orElse(null);
                return Optional.of(new MetaVerified(status, flag, additional));
            }

            @Override
            public boolean equals(Object obj) {
                if (obj == this) {
                    return true;
                }
                if (obj == null || obj.getClass() != this.getClass()) {
                    return false;
                }
                var that = (MetaVerified) obj;
                return this.status == that.status
                        && this.shouldShowPrivacyInterstitialToNewUsers
                                == that.shouldShowPrivacyInterstitialToNewUsers
                        && Objects.equals(this.additionalParams, that.additionalParams);
            }

            @Override
            public int hashCode() {
                return Objects.hash(status, shouldShowPrivacyInterstitialToNewUsers, additionalParams);
            }

            @Override
            public String toString() {
                return "SmaxGetBusinessEligibilityResponse.Success.MetaVerified[status=" + status
                        + ", shouldShowPrivacyInterstitialToNewUsers="
                        + shouldShowPrivacyInterstitialToNewUsers
                        + ", additionalParams=" + additionalParams + ']';
            }
        }

        /**
         * The {@code <marketing_messages/>} child projection. The
         * marketing-messages eligibility plus the optional
         * expiration timestamp.
         */
        @WhatsAppWebModule(moduleName = "WASmaxInBizMarketingMessageGetBusinessEligibilityResponseSuccess")
        public static final class MarketingMessages {
            /**
             * The mandatory {@code status} attribute.
             */
            private final SmaxGetBusinessEligibilityMarketingMessagesStatus status;

            /**
             * The optional {@code expiration} attribute (epoch
             * seconds).
             */
            private final Integer expiration;

            /**
             * Constructs a new projection.
             *
             * @param status     the marketing-messages status;
             *                   never {@code null}
             * @param expiration the optional expiration timestamp;
             *                   may be {@code null}
             * @throws NullPointerException if {@code status} is
             *                              {@code null}
             */
            public MarketingMessages(SmaxGetBusinessEligibilityMarketingMessagesStatus status, Integer expiration) {
                this.status = Objects.requireNonNull(status, "status cannot be null");
                this.expiration = expiration;
            }

            /**
             * Returns the marketing-messages status.
             *
             * @return the status; never {@code null}
             */
            public SmaxGetBusinessEligibilityMarketingMessagesStatus status() {
                return status;
            }

            /**
             * Returns the optional expiration timestamp.
             *
             * @return an {@link OptionalInt} carrying the
             *         timestamp, or empty when the relay omitted
             *         the attribute
             */
            public OptionalInt expiration() {
                if (expiration == null) {
                    return OptionalInt.empty();
                }
                return OptionalInt.of(expiration);
            }

            /**
             * Tries to parse the projection from the given node.
             *
             * @param node the {@code <marketing_messages/>} node
             * @return an {@link Optional} carrying the projection,
             *         or empty when the node does not match the
             *         documented schema
             */
            @WhatsAppWebExport(moduleName = "WASmaxInBizMarketingMessageGetBusinessEligibilityResponseSuccess",
                    exports = "parseGetBusinessEligibilityResponseSuccessMarketingMessages",
                    adaptation = WhatsAppAdaptation.ADAPTED)
            public static Optional<MarketingMessages> of(Node node) {
                Objects.requireNonNull(node, "node cannot be null");
                if (!node.hasDescription("marketing_messages")) {
                    return Optional.empty();
                }
                var statusStr = node.getAttributeAsString("status").orElse(null);
                var status = SmaxGetBusinessEligibilityMarketingMessagesStatus.of(statusStr).orElse(null);
                if (status == null) {
                    return Optional.empty();
                }
                Integer expiration = null;
                var expirationStr = node.getAttributeAsString("expiration").orElse(null);
                if (expirationStr != null) {
                    try {
                        int parsed = Integer.parseInt(expirationStr);
                        if (parsed < 0) {
                            return Optional.empty();
                        }
                        expiration = parsed;
                    } catch (NumberFormatException e) {
                        return Optional.empty();
                    }
                }
                return Optional.of(new MarketingMessages(status, expiration));
            }

            @Override
            public boolean equals(Object obj) {
                if (obj == this) {
                    return true;
                }
                if (obj == null || obj.getClass() != this.getClass()) {
                    return false;
                }
                var that = (MarketingMessages) obj;
                return this.status == that.status && Objects.equals(this.expiration, that.expiration);
            }

            @Override
            public int hashCode() {
                return Objects.hash(status, expiration);
            }

            @Override
            public String toString() {
                return "SmaxGetBusinessEligibilityResponse.Success.MarketingMessages[status=" + status
                        + ", expiration=" + expiration + ']';
            }
        }

        /**
         * The {@code <genai/>} child projection. The GenAI feature
         * eligibility status.
         */
        @WhatsAppWebModule(moduleName = "WASmaxInBizMarketingMessageGetBusinessEligibilityResponseSuccess")
        public static final class Genai {
            /**
             * The mandatory {@code status} attribute.
             */
            private final SmaxGetBusinessEligibilityFailSuccessStatus status;

            /**
             * Constructs a new projection.
             *
             * @param status the GenAI status; never {@code null}
             * @throws NullPointerException if {@code status} is
             *                              {@code null}
             */
            public Genai(SmaxGetBusinessEligibilityFailSuccessStatus status) {
                this.status = Objects.requireNonNull(status, "status cannot be null");
            }

            /**
             * Returns the GenAI status.
             *
             * @return the status; never {@code null}
             */
            public SmaxGetBusinessEligibilityFailSuccessStatus status() {
                return status;
            }

            /**
             * Tries to parse the projection from the given node.
             *
             * @param node the {@code <genai/>} node
             * @return an {@link Optional} carrying the projection,
             *         or empty when the node does not match the
             *         documented schema
             */
            @WhatsAppWebExport(moduleName = "WASmaxInBizMarketingMessageGetBusinessEligibilityResponseSuccess",
                    exports = "parseGetBusinessEligibilityResponseSuccessGenai",
                    adaptation = WhatsAppAdaptation.ADAPTED)
            public static Optional<Genai> of(Node node) {
                Objects.requireNonNull(node, "node cannot be null");
                if (!node.hasDescription("genai")) {
                    return Optional.empty();
                }
                var statusStr = node.getAttributeAsString("status").orElse(null);
                var status = SmaxGetBusinessEligibilityFailSuccessStatus.of(statusStr).orElse(null);
                if (status == null) {
                    return Optional.empty();
                }
                return Optional.of(new Genai(status));
            }

            @Override
            public boolean equals(Object obj) {
                if (obj == this) {
                    return true;
                }
                if (obj == null || obj.getClass() != this.getClass()) {
                    return false;
                }
                var that = (Genai) obj;
                return this.status == that.status;
            }

            @Override
            public int hashCode() {
                return Objects.hash(status);
            }

            @Override
            public String toString() {
                return "SmaxGetBusinessEligibilityResponse.Success.Genai[status=" + status + ']';
            }
        }
    }

    /**
     * The {@code ClientError} reply variant. The relay rejected the
     * request with a {@code 4xx} error code.
     *
     * @implNote {@code WASmaxInBizMarketingMessageGetBusinessEligibilityResponseError.parseGetBusinessEligibilityResponseError}
     *           routes the {@code <error/>} child through
     *           {@code WASmaxInBizMarketingMessageIQErrorBadRequestOrForbiddenOrInternalServerErrorOrServiceUnavailableMixinGroup};
     *           Cobalt collapses to the raw {@code (code, text)}
     *           pair via the shared {@link SmaxBaseServerErrorMixin}.
     */
    @WhatsAppWebModule(moduleName = "WASmaxInBizMarketingMessageGetBusinessEligibilityResponseError")
    @WhatsAppWebModule(moduleName = "WASmaxInBizMarketingMessageIQErrorBadRequestOrForbiddenOrInternalServerErrorOrServiceUnavailableMixinGroup")
    final class ClientError implements SmaxGetBusinessEligibilityResponse {
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
         * Tries to parse a {@link ClientError} variant from the
         * given inbound stanza.
         *
         * @param node    the inbound IQ stanza
         * @param request the original outbound request
         * @return an {@link Optional} carrying the parsed variant,
         *         or empty when the stanza does not match the
         *         client-error schema
         */
        @WhatsAppWebExport(moduleName = "WASmaxInBizMarketingMessageGetBusinessEligibilityResponseError",
                exports = "parseGetBusinessEligibilityResponseError",
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
            return "SmaxGetBusinessEligibilityResponse.ClientError[errorCode=" + errorCode
                    + ", errorText=" + errorText + ']';
        }
    }

    /**
     * The {@code ServerError} reply variant. The relay encountered
     * a transient internal failure ({@code 5xx}).
     *
     * @implNote Sourced from the {@code 5xx} arms of
     *           {@code WASmaxInBizMarketingMessageIQErrorBadRequestOrForbiddenOrInternalServerErrorOrServiceUnavailableMixinGroup};
     *           Cobalt routes through the shared
     *           {@link SmaxBaseServerErrorMixin}.
     */
    @WhatsAppWebModule(moduleName = "WASmaxInBizMarketingMessageGetBusinessEligibilityResponseError")
    final class ServerError implements SmaxGetBusinessEligibilityResponse {
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
         * Tries to parse a {@link ServerError} variant from the
         * given inbound stanza.
         *
         * @param node    the inbound IQ stanza
         * @param request the original outbound request
         * @return an {@link Optional} carrying the parsed variant,
         *         or empty when the stanza does not match the
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
            return "SmaxGetBusinessEligibilityResponse.ServerError[errorCode=" + errorCode
                    + ", errorText=" + errorText + ']';
        }
    }
}
