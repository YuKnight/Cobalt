package com.github.auties00.cobalt.node.smax.biz;

import com.github.auties00.cobalt.meta.annotation.WhatsAppWebExport;
import com.github.auties00.cobalt.meta.annotation.WhatsAppWebModule;
import com.github.auties00.cobalt.meta.model.WhatsAppAdaptation;
import com.github.auties00.cobalt.model.jid.JidServer;
import com.github.auties00.cobalt.node.Node;
import com.github.auties00.cobalt.node.NodeBuilder;
import com.github.auties00.cobalt.node.smax.SmaxOperation;
import com.github.auties00.cobalt.node.smax.util.SmaxBaseServerErrorMixin;
import com.github.auties00.cobalt.node.smax.util.SmaxIqResultResponseMixin;
import java.util.Objects;
import java.util.Optional;

/**
 * Sealed family of inbound reply variants produced by the relay in
 * response to a {@link SmaxGetPrivacySettingRequest}.
 */
public sealed interface SmaxGetPrivacySettingResponse extends SmaxOperation.Response
        permits SmaxGetPrivacySettingResponse.Success, SmaxGetPrivacySettingResponse.ClientError, SmaxGetPrivacySettingResponse.ServerError {

    /**
     * Tries each {@link SmaxGetPrivacySettingResponse} variant in priority order and
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
    @WhatsAppWebExport(moduleName = "WASmaxBizSettingsGetPrivacySettingRPC",
            exports = "sendGetPrivacySettingRPC", adaptation = WhatsAppAdaptation.ADAPTED)
    @WhatsAppWebExport(moduleName = "WASmaxInBizSettingsPrivacySettingErrors",
            exports = "parsePrivacySettingErrors", adaptation = WhatsAppAdaptation.ADAPTED)
    static Optional<? extends SmaxGetPrivacySettingResponse> of(Node node, Node request) {
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
     * current SMB-data-sharing consent value.
     */
    @WhatsAppWebModule(moduleName = "WASmaxInBizSettingsGetPrivacySettingResponseSuccess")
    @WhatsAppWebModule(moduleName = "WASmaxInBizSettingsSmbDataSharingSettingMixin")
    @WhatsAppWebModule(moduleName = "WASmaxInBizSettingsSmbDataSharingSettingValueMixin")
    final class Success implements SmaxGetPrivacySettingResponse {
        /**
         * The {@code value} attribute of the
         * {@code <smb_data_sharing_with_meta_consent>} child. One of
         * the {@code "true"} / {@code "false"} / {@code "notset"}
         * enum literals.
         */
        private final String dataSharingConsent;

        /**
         * Constructs a new successful reply.
         *
         * @param dataSharingConsent the consent enum value; never
         *                           {@code null}
         * @throws NullPointerException if {@code dataSharingConsent}
         *                              is {@code null}
         */
        public Success(String dataSharingConsent) {
            this.dataSharingConsent = Objects.requireNonNull(dataSharingConsent,
                    "dataSharingConsent cannot be null");
        }

        /**
         * Returns the consent enum value.
         *
         * @return the consent value; never {@code null}
         */
        public String dataSharingConsent() {
            return dataSharingConsent;
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
        @WhatsAppWebExport(moduleName = "WASmaxInBizSettingsGetPrivacySettingResponseSuccess",
                exports = "parseGetPrivacySettingResponseSuccess",
                adaptation = WhatsAppAdaptation.ADAPTED)
        public static Optional<Success> of(Node node, Node request) {
            Objects.requireNonNull(node, "node cannot be null");
            Objects.requireNonNull(request, "request cannot be null");
            if (!SmaxIqResultResponseMixin.validate(node, request)) {
                return Optional.empty();
            }
            var privacy = node.getChild("privacy").orElse(null);
            if (privacy == null) {
                return Optional.empty();
            }
            var consent = privacy.getChild("smb_data_sharing_with_meta_consent").orElse(null);
            if (consent == null) {
                return Optional.empty();
            }
            var value = consent.getAttributeAsString("value").orElse(null);
            // WASmaxInBizSettingsSmbDataSharingSettingValueMixin.parseSmbDataSharingSettingValueMixin:
            // attrStringEnum(e, "value", WASmaxInBizSettingsEnums.ENUM_FALSE_NOTSET_TRUE)
            // — strict dictionary match; reject any value outside {"true","false","notset"}.
            if (SmaxBizSettingsFalseNotsetTrueFlag.of(value).isEmpty()) {
                return Optional.empty();
            }
            return Optional.of(new Success(value));
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
            return Objects.equals(this.dataSharingConsent, that.dataSharingConsent);
        }

        @Override
        public int hashCode() {
            return Objects.hash(dataSharingConsent);
        }

        @Override
        public String toString() {
            return "SmaxGetPrivacySettingResponse.Success[dataSharingConsent="
                    + dataSharingConsent + ']';
        }
    }

    /**
     * The {@code ClientError} reply variant. The relay rejected the
     * request with a documented privacy-setting error code in the
     * {@code 4xx} range.
     */
    @WhatsAppWebModule(moduleName = "WASmaxInBizSettingsGetPrivacySettingResponseError")
    @WhatsAppWebModule(moduleName = "WASmaxInBizSettingsPrivacySettingErrors")
    final class ClientError implements SmaxGetPrivacySettingResponse {
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
        @WhatsAppWebExport(moduleName = "WASmaxInBizSettingsGetPrivacySettingResponseError",
                exports = "parseGetPrivacySettingResponseError",
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
            return "SmaxGetPrivacySettingResponse.ClientError[errorCode=" + errorCode
                    + ", errorText=" + errorText + ']';
        }
    }

    /**
     * The {@code ServerError} reply variant. The relay encountered a
     * transient internal failure ({@code 5xx}) while processing the
     * request.
     */
    @WhatsAppWebModule(moduleName = "WASmaxInBizSettingsGetPrivacySettingResponseError")
    final class ServerError implements SmaxGetPrivacySettingResponse {
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
        @WhatsAppWebExport(moduleName = "WASmaxInBizSettingsGetPrivacySettingResponseError",
                exports = "parseGetPrivacySettingResponseError",
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
            return "SmaxGetPrivacySettingResponse.ServerError[errorCode=" + errorCode
                    + ", errorText=" + errorText + ']';
        }
    }
}
