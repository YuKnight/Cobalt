package com.github.auties00.cobalt.node.smax.biz;

import com.github.auties00.cobalt.meta.annotation.WhatsAppWebExport;
import com.github.auties00.cobalt.meta.annotation.WhatsAppWebModule;
import com.github.auties00.cobalt.meta.model.WhatsAppAdaptation;
import com.github.auties00.cobalt.node.Node;
import com.github.auties00.cobalt.node.smax.SmaxOperation;
import com.github.auties00.cobalt.node.smax.util.SmaxBaseServerErrorMixin;
import com.github.auties00.cobalt.node.smax.util.SmaxIqResultResponseMixin;
import java.util.Objects;
import java.util.Optional;

/**
 * Sealed family of inbound reply variants produced by the relay in
 * response to a {@link SmaxSetPrivacySettingRequest}.
 */
public sealed interface SmaxSetPrivacySettingResponse extends SmaxOperation.Response
        permits SmaxSetPrivacySettingResponse.Success, SmaxSetPrivacySettingResponse.ClientError, SmaxSetPrivacySettingResponse.ServerError {

    /**
     * Tries each {@link SmaxSetPrivacySettingResponse} variant in priority order and
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
    @WhatsAppWebExport(moduleName = "WASmaxBizSettingsSetPrivacySettingRPC",
            exports = "sendSetPrivacySettingRPC", adaptation = WhatsAppAdaptation.ADAPTED)
    @WhatsAppWebExport(moduleName = "WASmaxInBizSettingsPrivacySettingErrors",
            exports = "parsePrivacySettingErrors", adaptation = WhatsAppAdaptation.ADAPTED)
    static Optional<? extends SmaxSetPrivacySettingResponse> of(Node node, Node request) {
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
     * preference write and (optionally) echoed back the stored
     * value.
     *
     * <p>The {@code <privacy>} child of the reply may carry a
     * {@code <smb_data_sharing_with_meta_consent value="..."/>} echo
     * mirroring the post-write state. Cobalt surfaces this as an
     * optional {@code dataSharingConsent} field. WhatsApp Web
     * tolerates an empty {@code <privacy/>} reply, so the field is
     * not required.
     */
    @WhatsAppWebModule(moduleName = "WASmaxInBizSettingsSetPrivacySettingResponseSuccess")
    @WhatsAppWebModule(moduleName = "WASmaxInBizSettingsSmbDataSharingSettingMixin")
    @WhatsAppWebModule(moduleName = "WASmaxInBizSettingsSmbDataSharingSettingValueMixin")
    final class Success implements SmaxSetPrivacySettingResponse {
        /**
         * The optional echoed consent value; {@code null} when the
         * relay sent a bare {@code <privacy/>} reply.
         */
        private final String dataSharingConsent;

        /**
         * Constructs a new successful reply.
         *
         * @param dataSharingConsent the optional echoed consent
         *                           value; may be {@code null}
         */
        public Success(String dataSharingConsent) {
            this.dataSharingConsent = dataSharingConsent;
        }

        /**
         * Returns the optional echoed consent value.
         *
         * @return an {@link Optional} carrying the value, or empty
         *         when the relay omitted it
         */
        public Optional<String> dataSharingConsent() {
            return Optional.ofNullable(dataSharingConsent);
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
        @WhatsAppWebExport(moduleName = "WASmaxInBizSettingsSetPrivacySettingResponseSuccess",
                exports = "parseSetPrivacySettingResponseSuccess",
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
            String consent = null;
            var consentNode = privacy.getChild("smb_data_sharing_with_meta_consent").orElse(null);
            if (consentNode != null) {
                var value = consentNode.getAttributeAsString("value").orElse(null);
                // WASmaxInBizSettingsSmbDataSharingSettingValueMixin.parseSmbDataSharingSettingValueMixin:
                // attrStringEnum(e, "value", WASmaxInBizSettingsEnums.ENUM_FALSE_NOTSET_TRUE) under
                // optionalMerge — keep null when the inner mixin parse fails (success ? value : null
                // semantic in the JS) so a malformed echo doesn't corrupt the optional projection.
                if (value != null && SmaxBizSettingsFalseNotsetTrueFlag.of(value).isPresent()) {
                    consent = value;
                }
            }
            return Optional.of(new Success(consent));
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
            return "SmaxSetPrivacySettingResponse.Success[dataSharingConsent="
                    + dataSharingConsent + ']';
        }
    }

    /**
     * The {@code ClientError} reply variant. The relay rejected the
     * request with a documented privacy-setting error code in the
     * {@code 4xx} range.
     */
    @WhatsAppWebModule(moduleName = "WASmaxInBizSettingsSetPrivacySettingResponseError")
    @WhatsAppWebModule(moduleName = "WASmaxInBizSettingsPrivacySettingErrors")
    final class ClientError implements SmaxSetPrivacySettingResponse {
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
        @WhatsAppWebExport(moduleName = "WASmaxInBizSettingsSetPrivacySettingResponseError",
                exports = "parseSetPrivacySettingResponseError",
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
            return "SmaxSetPrivacySettingResponse.ClientError[errorCode=" + errorCode
                    + ", errorText=" + errorText + ']';
        }
    }

    /**
     * The {@code ServerError} reply variant. The relay encountered a
     * transient internal failure ({@code 5xx}) while processing the
     * request.
     */
    @WhatsAppWebModule(moduleName = "WASmaxInBizSettingsSetPrivacySettingResponseError")
    final class ServerError implements SmaxSetPrivacySettingResponse {
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
        @WhatsAppWebExport(moduleName = "WASmaxInBizSettingsSetPrivacySettingResponseError",
                exports = "parseSetPrivacySettingResponseError",
                adaptation = WhatsAppAdaptation.ADAPTED)
        @WhatsAppWebExport(moduleName = "WASmaxInBizSettingsPrivacySettingErrors",
                exports = "parsePrivacySettingErrors",
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
            return "SmaxSetPrivacySettingResponse.ServerError[errorCode=" + errorCode
                    + ", errorText=" + errorText + ']';
        }
    }
}
