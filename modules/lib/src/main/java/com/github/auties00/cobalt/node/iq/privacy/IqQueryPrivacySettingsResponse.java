package com.github.auties00.cobalt.node.iq.privacy;

import com.github.auties00.cobalt.meta.annotation.WhatsAppWebExport;
import com.github.auties00.cobalt.meta.annotation.WhatsAppWebModule;
import com.github.auties00.cobalt.meta.model.WhatsAppAdaptation;
import com.github.auties00.cobalt.node.Node;
import com.github.auties00.cobalt.node.iq.IqOperation;
import com.github.auties00.cobalt.node.smax.util.SmaxBaseServerErrorMixin;
import com.github.auties00.cobalt.node.smax.util.SmaxIqResultResponseMixin;

import java.util.EnumMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/**
 * Sealed family of inbound reply variants produced by the relay for an
 * {@link IqQueryPrivacySettingsRequest}.
 *
 * @implNote {@code WAWebQueryPrivacySettingsJob.getPrivacy} parses the
 *           result via {@code privacyParser} and throws
 *           {@code ServerStatusCodeError} on failure; Cobalt routes the
 *           {@code 4xx} / {@code 5xx} split through
 *           {@link IqQueryPrivacySettingsResponse.ClientError} and
 *           {@link IqQueryPrivacySettingsResponse.ServerError}.
 */
@WhatsAppWebModule(moduleName = "WAWebQueryPrivacySettingsJob")
public sealed interface IqQueryPrivacySettingsResponse extends IqOperation.Response
        permits IqQueryPrivacySettingsResponse.Success,
        IqQueryPrivacySettingsResponse.ClientError,
        IqQueryPrivacySettingsResponse.ServerError {

    /**
     * Tries each {@link IqQueryPrivacySettingsResponse} variant in
     * priority order and returns the first that parses cleanly.
     *
     * @param node    the inbound IQ stanza; never {@code null}
     * @param request the original outbound stanza; never {@code null}
     * @return an {@link Optional} carrying the parsed variant, or empty
     *         when no documented variant matched
     * @throws NullPointerException if either argument is {@code null}
     */
    @WhatsAppWebExport(moduleName = "WAWebQueryPrivacySettingsJob",
            exports = "getPrivacy", adaptation = WhatsAppAdaptation.ADAPTED)
    static Optional<? extends IqQueryPrivacySettingsResponse> of(Node node, Node request) {
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
     * The {@code Success} reply variant — the relay returned a
     * {@code <privacy>} envelope listing every privacy category and its
     * current value.
     *
     * @implNote {@code WAWebQueryPrivacySettingsJob.privacyParser}
     *           projects the per-category settings as a flat record;
     *           Cobalt projects them as a
     *           {@link IqQueryPrivacySettingsCategoryName} →
     *           {@link IqQueryPrivacySettingsVisibility} map, dropping
     *           entries whose value failed enum resolution (mirroring
     *           WA Web's skip-on-error behaviour).
     */
    @WhatsAppWebModule(moduleName = "WAWebQueryPrivacySettingsJob")
    final class Success implements IqQueryPrivacySettingsResponse {
        /**
         * The parsed per-category settings — never {@code null}; may be
         * empty when the relay returned no {@code <category/>} children.
         */
        private final Map<IqQueryPrivacySettingsCategoryName, IqQueryPrivacySettingsVisibility> categories;

        /**
         * Constructs a {@code Success} reply.
         *
         * @param categories the per-category settings; never
         *                   {@code null}
         * @throws NullPointerException if {@code categories} is
         *                              {@code null}
         */
        public Success(Map<IqQueryPrivacySettingsCategoryName, IqQueryPrivacySettingsVisibility> categories) {
            Objects.requireNonNull(categories, "categories cannot be null");
            this.categories = Map.copyOf(categories);
        }

        /**
         * Returns the parsed per-category settings.
         *
         * @return an unmodifiable map keyed by category; never
         *         {@code null}
         */
        public Map<IqQueryPrivacySettingsCategoryName, IqQueryPrivacySettingsVisibility> categories() {
            return categories;
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
        @WhatsAppWebExport(moduleName = "WAWebQueryPrivacySettingsJob",
                exports = "privacyParser", adaptation = WhatsAppAdaptation.ADAPTED)
        public static Optional<Success> of(Node node, Node request) {
            if (!SmaxIqResultResponseMixin.validate(node, request)) {
                return Optional.empty();
            }
            var privacy = node.getChild("privacy").orElse(null);
            if (privacy == null) {
                return Optional.empty();
            }
            var map = new EnumMap<IqQueryPrivacySettingsCategoryName, IqQueryPrivacySettingsVisibility>(IqQueryPrivacySettingsCategoryName.class);
            for (var category : privacy.getChildren("category")) {
                var name = category.getAttributeAsString("name")
                        .flatMap(IqQueryPrivacySettingsCategoryName::fromWire)
                        .orElse(null);
                if (name == null) {
                    continue;
                }
                var value = category.getAttributeAsString("value")
                        .flatMap(IqQueryPrivacySettingsVisibility::fromWire)
                        .orElse(null);
                if (value == null) {
                    continue;
                }
                map.put(name, value);
            }
            return Optional.of(new Success(map));
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
            return Objects.equals(this.categories, that.categories);
        }

        @Override
        public int hashCode() {
            return Objects.hash(categories);
        }

        @Override
        public String toString() {
            return "IqQueryPrivacySettingsResponse.Success[categories=" + categories + ']';
        }
    }

    /**
     * The {@code ClientError} reply variant — the relay rejected the
     * request with a {@code 4xx} error code.
     */
    @WhatsAppWebModule(moduleName = "WAWebQueryPrivacySettingsJob")
    final class ClientError implements IqQueryPrivacySettingsResponse {
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
         * @return an {@link Optional} carrying the text, or empty when
         *         omitted
         */
        public Optional<String> errorText() {
            return Optional.ofNullable(errorText);
        }

        /**
         * Tries to parse a {@link ClientError} variant.
         *
         * @param node    the inbound IQ stanza
         * @param request the original outbound request
         * @return an {@link Optional} carrying the parsed variant, or
         *         empty when the stanza does not match the
         *         client-error schema
         */
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
            return "IqQueryPrivacySettingsResponse.ClientError[errorCode=" + errorCode
                    + ", errorText=" + errorText + ']';
        }
    }

    /**
     * The {@code ServerError} reply variant — the relay encountered a
     * transient internal failure ({@code 5xx} error code).
     */
    @WhatsAppWebModule(moduleName = "WAWebQueryPrivacySettingsJob")
    final class ServerError implements IqQueryPrivacySettingsResponse {
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
         * @return an {@link Optional} carrying the text, or empty when
         *         omitted
         */
        public Optional<String> errorText() {
            return Optional.ofNullable(errorText);
        }

        /**
         * Tries to parse a {@link ServerError} variant.
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
            return this.errorCode == that.errorCode
                    && Objects.equals(this.errorText, that.errorText);
        }

        @Override
        public int hashCode() {
            return Objects.hash(errorCode, errorText);
        }

        @Override
        public String toString() {
            return "IqQueryPrivacySettingsResponse.ServerError[errorCode=" + errorCode
                    + ", errorText=" + errorText + ']';
        }
    }
}
