package com.github.auties00.cobalt.node.smax.account;

import com.github.auties00.cobalt.meta.annotation.WhatsAppWebExport;
import com.github.auties00.cobalt.meta.annotation.WhatsAppWebModule;
import com.github.auties00.cobalt.meta.model.WhatsAppAdaptation;
import com.github.auties00.cobalt.model.jid.JidServer;
import com.github.auties00.cobalt.node.Node;
import com.github.auties00.cobalt.node.NodeBuilder;
import com.github.auties00.cobalt.node.smax.SmaxOperation;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/**
 * Sealed family of inbound reply variants produced by the relay in
 * response to a {@link SmaxBrPaymentCreateCustomPaymentMethodRequest}.
 *
 * @implNote {@code WASmaxBrPaymentCreateCustomPaymentMethodRPC.sendCreateCustomPaymentMethodRPC}
 *           tries {@code Success} → {@code IQError} in order and
 *           throws on no-match. Cobalt returns
 *           {@link Optional#empty()} on no-match.
 */
public sealed interface SmaxBrPaymentCreateCustomPaymentMethodResponse extends SmaxOperation.Response
        permits SmaxBrPaymentCreateCustomPaymentMethodResponse.Success, SmaxBrPaymentCreateCustomPaymentMethodResponse.IqError {

    /**
     * Tries each {@link SmaxBrPaymentCreateCustomPaymentMethodResponse} variant in priority order and
     * returns the first that parses cleanly.
     *
     * @param node    the inbound IQ stanza received from the relay;
     *                never {@code null}
     * @param request the original outbound stanza — used to
     *                validate echoed identifiers; never
     *                {@code null}
     * @return an {@link Optional} carrying the parsed variant, or
     *         {@link Optional#empty()} when no documented variant
     *         matched
     * @throws NullPointerException if either argument is
     *                              {@code null}
     */
    @WhatsAppWebExport(moduleName = "WASmaxBrPaymentCreateCustomPaymentMethodRPC",
            exports = "sendCreateCustomPaymentMethodRPC",
            adaptation = WhatsAppAdaptation.ADAPTED)
    static Optional<? extends SmaxBrPaymentCreateCustomPaymentMethodResponse> of(Node node, Node request) {
        Objects.requireNonNull(node, "node cannot be null");
        Objects.requireNonNull(request, "request cannot be null");
        var success = Success.of(node, request);
        if (success.isPresent()) {
            return success;
        }
        return IqError.of(node, request);
    }

    /**
     * Validates the {@code <iq type="result"/>} envelope of a
     * Brazilian-payments reply by cross-checking
     * {@code from}/{@code id} against the request and asserting
     * {@code type="result"}.
     *
     * @param reply   the inbound IQ stanza
     * @param request the original outbound IQ
     * @return {@code true} when the envelope echo-checks pass
     *
     * @implNote {@code WASmaxInBrPaymentIQResultResponseMixin.parseIQResultResponseMixin}.
     */
    @WhatsAppWebExport(moduleName = "WASmaxInBrPaymentIQResultResponseMixin",
            exports = "parseIQResultResponseMixin", adaptation = WhatsAppAdaptation.ADAPTED)
    private static boolean validateIqResultEnvelope(Node reply, Node request) {
        if (!reply.hasDescription("iq")) {
            return false;
        }
        if (!reply.hasAttribute("type", "result")) {
            return false;
        }
        var requestId = request.getAttributeAsString("id").orElse(null);
        if (requestId == null) {
            return false;
        }
        if (!reply.hasAttribute("id", requestId)) {
            return false;
        }
        var requestTo = request.getAttributeAsString("to").orElse(null);
        return requestTo == null || reply.hasAttribute("from", requestTo);
    }

    /**
     * The {@code Success} reply variant — the relay registered the
     * custom payment method and echoed the canonical
     * {@code <custom_payment_method/>} projection back.
     *
     * @implNote {@code WASmaxInBrPaymentCreateCustomPaymentMethodResponseSuccess.parseCreateCustomPaymentMethodResponseSuccess}
     *           validates the {@code <iq type="result">} envelope,
     *           cross-checks the {@code <account action/>} echo
     *           against the request, and projects the
     *           {@code <custom_payment_method/>} child via
     *           {@code WASmaxInBrPaymentCustomPaymentMethodMixin.parseCustomPaymentMethodMixin}.
     */
    @WhatsAppWebModule(moduleName = "WASmaxInBrPaymentCreateCustomPaymentMethodResponseSuccess")
    @WhatsAppWebModule(moduleName = "WASmaxInBrPaymentCustomPaymentMethodMixin")
    @WhatsAppWebModule(moduleName = "WASmaxInBrPaymentMethodBaseMixin")
    @WhatsAppWebModule(moduleName = "WASmaxInBrPaymentCustomPaymentMethodMetaDataInfoMixin")
    @WhatsAppWebModule(moduleName = "WASmaxInBrPaymentCustomPaymentMethodMetaDataMixin")
    final class Success implements SmaxBrPaymentCreateCustomPaymentMethodResponse {
        /**
         * The echoed custom-payment-method type — one of
         * {@code "PAYONDELIVERY"}, {@code "PIXKEY"}.
         */
        private final String customPaymentMethodType;

        /**
         * The optional echoed {@code country} attribute (always
         * {@code "BR"} when present).
         */
        private final String country;

        /**
         * The optional creation timestamp string echoed by the
         * relay.
         */
        private final String created;

        /**
         * The optional flow enum literal — {@code "P2P"} or
         * {@code "P2M"}.
         */
        private final String flow;

        /**
         * The credential-id assigned by the relay.
         */
        private final String credentialId;

        /**
         * The optional {@code p2p-eligible} flag — {@code "0"} /
         * {@code "1"}.
         */
        private final String p2pEligible;

        /**
         * The optional {@code p2m-eligible} flag — {@code "0"} /
         * {@code "1"}.
         */
        private final String p2mEligible;

        /**
         * The optional metadata key-value pairs echoed by the
         * relay.
         */
        private final Map<String, String> metadata;

        /**
         * Constructs a new successful reply.
         *
         * @param customPaymentMethodType the method type; never
         *                                {@code null}
         * @param country                 the optional country; may
         *                                be {@code null}
         * @param created                 the optional creation
         *                                timestamp; may be
         *                                {@code null}
         * @param flow                    the optional flow; may be
         *                                {@code null}
         * @param credentialId            the credential-id; never
         *                                {@code null}
         * @param p2pEligible             the optional p2p flag;
         *                                may be {@code null}
         * @param p2mEligible             the optional p2m flag;
         *                                may be {@code null}
         * @param metadata                the optional metadata
         *                                pairs; never {@code null}
         * @throws NullPointerException if any non-nullable argument
         *                              is {@code null}
         */
        public Success(String customPaymentMethodType,
                       String country,
                       String created,
                       String flow,
                       String credentialId,
                       String p2pEligible,
                       String p2mEligible,
                       Map<String, String> metadata) {
            this.customPaymentMethodType = Objects.requireNonNull(customPaymentMethodType, "customPaymentMethodType cannot be null");
            this.country = country;
            this.created = created;
            this.flow = flow;
            this.credentialId = Objects.requireNonNull(credentialId, "credentialId cannot be null");
            this.p2pEligible = p2pEligible;
            this.p2mEligible = p2mEligible;
            this.metadata = metadata == null
                    ? Map.of()
                    : Collections.unmodifiableMap(new LinkedHashMap<>(metadata));
        }

        /**
         * Returns the method type.
         *
         * @return the type; never {@code null}
         */
        public String customPaymentMethodType() {
            return customPaymentMethodType;
        }

        /**
         * Returns the optional country.
         *
         * @return an {@link Optional} carrying the country, or
         *         empty when omitted
         */
        public Optional<String> country() {
            return Optional.ofNullable(country);
        }

        /**
         * Returns the optional creation timestamp.
         *
         * @return an {@link Optional} carrying the timestamp, or
         *         empty when omitted
         */
        public Optional<String> created() {
            return Optional.ofNullable(created);
        }

        /**
         * Returns the optional flow.
         *
         * @return an {@link Optional} carrying the flow, or empty
         *         when omitted
         */
        public Optional<String> flow() {
            return Optional.ofNullable(flow);
        }

        /**
         * Returns the credential-id.
         *
         * @return the id; never {@code null}
         */
        public String credentialId() {
            return credentialId;
        }

        /**
         * Returns the optional p2p-eligible flag.
         *
         * @return an {@link Optional} carrying the flag, or empty
         *         when omitted
         */
        public Optional<String> p2pEligible() {
            return Optional.ofNullable(p2pEligible);
        }

        /**
         * Returns the optional p2m-eligible flag.
         *
         * @return an {@link Optional} carrying the flag, or empty
         *         when omitted
         */
        public Optional<String> p2mEligible() {
            return Optional.ofNullable(p2mEligible);
        }

        /**
         * Returns the metadata pairs.
         *
         * @return an unmodifiable map; never {@code null}
         */
        public Map<String, String> metadata() {
            return metadata;
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
        @WhatsAppWebExport(moduleName = "WASmaxInBrPaymentCreateCustomPaymentMethodResponseSuccess",
                exports = "parseCreateCustomPaymentMethodResponseSuccess",
                adaptation = WhatsAppAdaptation.ADAPTED)
        public static Optional<Success> of(Node node, Node request) {
            if (!validateIqResultEnvelope(node, request)) {
                return Optional.empty();
            }
            var account = node.getChild("account").orElse(null);
            if (account == null) {
                return Optional.empty();
            }
            // Echo-check the action attribute against the request's <account action=…>
            var requestAccount = request.getChild("account").orElse(null);
            if (requestAccount == null) {
                return Optional.empty();
            }
            var requestAction = requestAccount.getAttributeAsString("action").orElse(null);
            if (requestAction == null || !account.hasAttribute("action", requestAction)) {
                return Optional.empty();
            }
            var customPaymentMethod = account.getChild("custom_payment_method").orElse(null);
            if (customPaymentMethod == null) {
                return Optional.empty();
            }
            var type = customPaymentMethod.getAttributeAsString("type").orElse(null);
            if (type == null || (!type.equals("PAYONDELIVERY") && !type.equals("PIXKEY"))) {
                return Optional.empty();
            }
            var country = customPaymentMethod.getAttributeAsString("country").orElse(null);
            if (country != null && !"BR".equals(country)) {
                return Optional.empty();
            }
            var created = customPaymentMethod.getAttributeAsString("created").orElse(null);
            var flow = customPaymentMethod.getAttributeAsString("flow").orElse(null);
            if (flow != null && !flow.equals("P2M") && !flow.equals("P2P")) {
                return Optional.empty();
            }
            var credentialId = customPaymentMethod.getAttributeAsString("credential-id").orElse(null);
            if (credentialId == null) {
                return Optional.empty();
            }
            var p2pEligible = customPaymentMethod.getAttributeAsString("p2p-eligible").orElse(null);
            if (p2pEligible != null && !p2pEligible.equals("0") && !p2pEligible.equals("1")) {
                return Optional.empty();
            }
            var p2mEligible = customPaymentMethod.getAttributeAsString("p2m-eligible").orElse(null);
            if (p2mEligible != null && !p2mEligible.equals("0") && !p2mEligible.equals("1")) {
                return Optional.empty();
            }
            // metadata_info → metadata*
            var metadataMap = new LinkedHashMap<String, String>();
            var metadataInfo = customPaymentMethod.getChild("metadata_info").orElse(null);
            if (metadataInfo != null) {
                var metadataNodes = metadataInfo.getChildren("metadata");
                if (metadataNodes.size() < 1 || metadataNodes.size() > 5) {
                    return Optional.empty();
                }
                for (var entry : metadataNodes) {
                    var key = entry.getAttributeAsString("key").orElse(null);
                    var value = entry.getAttributeAsString("value").orElse(null);
                    if (key == null || value == null) {
                        return Optional.empty();
                    }
                    metadataMap.put(key, value);
                }
            }
            return Optional.of(new Success(type, country, created, flow, credentialId,
                    p2pEligible, p2mEligible, metadataMap));
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
            return Objects.equals(this.customPaymentMethodType, that.customPaymentMethodType)
                    && Objects.equals(this.country, that.country)
                    && Objects.equals(this.created, that.created)
                    && Objects.equals(this.flow, that.flow)
                    && Objects.equals(this.credentialId, that.credentialId)
                    && Objects.equals(this.p2pEligible, that.p2pEligible)
                    && Objects.equals(this.p2mEligible, that.p2mEligible)
                    && Objects.equals(this.metadata, that.metadata);
        }

        @Override
        public int hashCode() {
            return Objects.hash(customPaymentMethodType, country, created, flow, credentialId,
                    p2pEligible, p2mEligible, metadata);
        }

        @Override
        public String toString() {
            return "SmaxBrPaymentCreateCustomPaymentMethodResponse.Success[customPaymentMethodType="
                    + customPaymentMethodType
                    + ", country=" + country
                    + ", created=" + created
                    + ", flow=" + flow
                    + ", credentialId=" + credentialId
                    + ", p2pEligible=" + p2pEligible
                    + ", p2mEligible=" + p2mEligible
                    + ", metadata=" + metadata + ']';
        }
    }

    /**
     * The {@code IqError} reply variant — the relay rejected the
     * registration with an {@code <iq type="error"/>} envelope
     * carrying the canonical {@code (code, text)} pair.
     *
     * @implNote {@code WASmaxInBrPaymentCreateCustomPaymentMethodResponseIQErrorWithCodeAndReason.parseCreateCustomPaymentMethodResponseIQErrorWithCodeAndReason}
     *           composes
     *           {@code WASmaxInBrPaymentIQErrorGenericResponseMixin.parseIQErrorGenericResponseMixin}
     *           which validates the IQ envelope echo, asserts
     *           {@code type="error"}, then extracts the
     *           {@code <error code="…" text="…"/>} child requiring
     *           {@code code >= 1}.
     */
    @WhatsAppWebModule(moduleName = "WASmaxInBrPaymentCreateCustomPaymentMethodResponseIQErrorWithCodeAndReason")
    @WhatsAppWebModule(moduleName = "WASmaxInBrPaymentIQErrorGenericResponseMixin")
    final class IqError implements SmaxBrPaymentCreateCustomPaymentMethodResponse {
        /**
         * The numeric error code (always {@code >= 1}).
         */
        private final int errorCode;

        /**
         * The human-readable error text echoed by the relay.
         */
        private final String errorText;

        /**
         * Constructs a new error reply.
         *
         * @param errorCode the numeric error code
         * @param errorText the human-readable text; never
         *                  {@code null}
         * @throws NullPointerException if {@code errorText} is
         *                              {@code null}
         */
        public IqError(int errorCode, String errorText) {
            this.errorCode = errorCode;
            this.errorText = Objects.requireNonNull(errorText, "errorText cannot be null");
        }

        /**
         * Returns the numeric error code.
         *
         * @return the code
         */
        public int errorCode() {
            return errorCode;
        }

        /**
         * Returns the human-readable error text.
         *
         * @return the text; never {@code null}
         */
        public Optional<String> errorText() {
            return Optional.of(errorText);
        }

        /**
         * Tries to parse an {@link IqError} variant from the given
         * inbound stanza.
         *
         * @param node    the inbound IQ stanza
         * @param request the original outbound request
         * @return an {@link Optional} carrying the parsed variant,
         *         or empty when the stanza does not match the
         *         iq-error schema
         */
        @WhatsAppWebExport(moduleName = "WASmaxInBrPaymentCreateCustomPaymentMethodResponseIQErrorWithCodeAndReason",
                exports = "parseCreateCustomPaymentMethodResponseIQErrorWithCodeAndReason",
                adaptation = WhatsAppAdaptation.ADAPTED)
        @WhatsAppWebExport(moduleName = "WASmaxInBrPaymentIQErrorGenericResponseMixin",
                exports = "parseIQErrorGenericResponseMixin",
                adaptation = WhatsAppAdaptation.ADAPTED)
        public static Optional<IqError> of(Node node, Node request) {
            if (!node.hasDescription("iq")) {
                return Optional.empty();
            }
            if (!node.hasAttribute("type", "error")) {
                return Optional.empty();
            }
            var requestId = request.getAttributeAsString("id").orElse(null);
            if (requestId == null) {
                return Optional.empty();
            }
            if (!node.hasAttribute("id", requestId)) {
                return Optional.empty();
            }
            var requestTo = request.getAttributeAsString("to").orElse(null);
            if (requestTo == null || !node.hasAttribute("from", requestTo)) {
                return Optional.empty();
            }
            var errorChild = node.getChild("error").orElse(null);
            if (errorChild == null) {
                return Optional.empty();
            }
            var text = errorChild.getAttributeAsString("text").orElse(null);
            if (text == null) {
                return Optional.empty();
            }
            var codeOpt = errorChild.getAttributeAsInt("code");
            if (codeOpt.isEmpty()) {
                return Optional.empty();
            }
            var code = codeOpt.getAsInt();
            if (code < 1) {
                return Optional.empty();
            }
            return Optional.of(new IqError(code, text));
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == this) {
                return true;
            }
            if (obj == null || obj.getClass() != this.getClass()) {
                return false;
            }
            var that = (IqError) obj;
            return this.errorCode == that.errorCode
                    && Objects.equals(this.errorText, that.errorText);
        }

        @Override
        public int hashCode() {
            return Objects.hash(errorCode, errorText);
        }

        @Override
        public String toString() {
            return "SmaxBrPaymentCreateCustomPaymentMethodResponse.IqError[errorCode=" + errorCode
                    + ", errorText=" + errorText + ']';
        }
    }
}
