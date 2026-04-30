package com.github.auties00.cobalt.node.iq.privacy;

import com.github.auties00.cobalt.meta.annotation.WhatsAppWebExport;
import com.github.auties00.cobalt.meta.annotation.WhatsAppWebModule;
import com.github.auties00.cobalt.meta.model.WhatsAppAdaptation;
import com.github.auties00.cobalt.node.Node;
import com.github.auties00.cobalt.node.iq.IqOperation;
import com.github.auties00.cobalt.node.smax.util.SmaxBaseServerErrorMixin;
import com.github.auties00.cobalt.node.smax.util.SmaxIqResultResponseMixin;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * Sealed family of inbound reply variants.
 *
 * @implNote {@code WAWebSetPrivacyJob.setPrivacyParser} maps each
 *           {@code <category/>} child onto either a successful
 *           {@code (name, value, dhash)} projection or a
 *           {@code ServerStatusCodeError} when {@code value="error"}.
 *           Cobalt models the per-category outcomes uniformly via
 *           {@link IqSetPrivacyResponse.Success#categories()} and routes wire-level
 *           {@code 4xx}/{@code 5xx} errors through
 *           {@link IqSetPrivacyResponse.ClientError} / {@link IqSetPrivacyResponse.ServerError}.
 */
public sealed interface IqSetPrivacyResponse extends IqOperation.Response
        permits IqSetPrivacyResponse.Success, IqSetPrivacyResponse.ClientError, IqSetPrivacyResponse.ServerError {

    /**
     * Tries each {@link IqSetPrivacyResponse} variant in priority order.
     *
     * @param node    the inbound IQ stanza. Never {@code null}
     * @param request the original outbound stanza. Never {@code null}
     * @return an {@link Optional} carrying the parsed variant, or
     *         empty when no documented variant matched
     * @throws NullPointerException if either argument is {@code null}
     */
    @WhatsAppWebExport(moduleName = "WAWebSetPrivacyJob",
            exports = "setPrivacy", adaptation = WhatsAppAdaptation.ADAPTED)
    static Optional<? extends IqSetPrivacyResponse> of(Node node, Node request) {
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
     * One per-category outcome inside a {@link Success} response.
     *
     * @implNote {@code WAWebSetPrivacyJob.setPrivacyParser}'s
     *           per-{@code <category>} mapping projects either a
     *           {@code (name, value, dhash)} record or a
     *           {@code ServerStatusCodeError}. {@link CategoryOutcome}
     *           is the typed successful projection. Per-category
     *           rejections are surfaced via the optional
     *           {@link CategoryOutcome#errorCode} / {@link CategoryOutcome#errorText}
     *           fields.
     */
    final class CategoryOutcome {
        /**
         * The category name reported by the relay.
         */
        private final IqQueryPrivacySettingsCategoryName name;

        /**
         * The new value reported by the relay, or {@code null} when
         * the relay rejected the per-category mutation with
         * {@code value="error"}.
         */
        private final IqQueryPrivacySettingsVisibility value;

        /**
         * The optional list digest echoed back for categories with
         * user-list mutations.
         */
        private final String dhash;

        /**
         * The numeric error code carried inside the embedded
         * {@code <error/>} child when the relay rejected this
         * category, or {@code -1} when the category succeeded.
         */
        private final int errorCode;

        /**
         * The optional error text carried inside the embedded
         * {@code <error/>} child.
         */
        private final String errorText;

        /**
         * Constructs a category outcome.
         *
         * @param name      the category. Never {@code null}
         * @param value     the new value, or {@code null} when the
         *                  relay rejected the mutation
         * @param dhash     the optional digest. May be {@code null}
         * @param errorCode the embedded error code, or {@code -1} on
         *                  success
         * @param errorText the optional error text. May be
         *                  {@code null}
         */
        public CategoryOutcome(IqQueryPrivacySettingsCategoryName name,
                               IqQueryPrivacySettingsVisibility value,
                               String dhash,
                               int errorCode,
                               String errorText) {
            this.name = Objects.requireNonNull(name, "name cannot be null");
            this.value = value;
            this.dhash = dhash;
            this.errorCode = errorCode;
            this.errorText = errorText;
        }

        /**
         * Returns the category.
         *
         * @return the category. Never {@code null}
         */
        public IqQueryPrivacySettingsCategoryName name() {
            return name;
        }

        /**
         * Returns the new value, when the per-category mutation
         * succeeded.
         *
         * @return an {@link Optional} carrying the value, or empty
         *         when the relay rejected the mutation with
         *         {@code value="error"}
         */
        public Optional<IqQueryPrivacySettingsVisibility> value() {
            return Optional.ofNullable(value);
        }

        /**
         * Returns the optional list digest.
         *
         * @return an {@link Optional} carrying the digest, or empty
         */
        public Optional<String> dhash() {
            return Optional.ofNullable(dhash);
        }

        /**
         * Returns the embedded error code, when the relay rejected
         * this category.
         *
         * @return the error code, or {@code -1} on success
         */
        public int errorCode() {
            return errorCode;
        }

        /**
         * Returns the optional embedded error text.
         *
         * @return an {@link Optional} carrying the text, or empty
         */
        public Optional<String> errorText() {
            return Optional.ofNullable(errorText);
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == this) {
                return true;
            }
            if (obj == null || obj.getClass() != this.getClass()) {
                return false;
            }
            var that = (CategoryOutcome) obj;
            return this.name == that.name
                    && this.value == that.value
                    && Objects.equals(this.dhash, that.dhash)
                    && this.errorCode == that.errorCode
                    && Objects.equals(this.errorText, that.errorText);
        }

        @Override
        public int hashCode() {
            return Objects.hash(name, value, dhash, errorCode, errorText);
        }

        @Override
        public String toString() {
            return "IqSetPrivacyResponse.CategoryOutcome[name=" + name
                    + ", value=" + value + ", dhash=" + dhash
                    + ", errorCode=" + errorCode + ", errorText=" + errorText + ']';
        }
    }

    /**
     * The {@code Success} reply variant. The relay accepted the
     * envelope and returned a per-category outcome list.
     */
    @WhatsAppWebModule(moduleName = "WAWebSetPrivacyJob")
    final class Success implements IqSetPrivacyResponse {
        /**
         * The parsed per-category outcomes.
         */
        private final List<CategoryOutcome> categories;

        /**
         * Constructs a {@code Success} reply.
         *
         * @param categories the outcomes. Never {@code null}
         * @throws NullPointerException if {@code categories} is
         *                              {@code null}
         */
        public Success(List<CategoryOutcome> categories) {
            Objects.requireNonNull(categories, "categories cannot be null");
            this.categories = List.copyOf(categories);
        }

        /**
         * Returns the parsed per-category outcomes.
         *
         * @return an unmodifiable list. Never {@code null}
         */
        public List<CategoryOutcome> categories() {
            return categories;
        }

        /**
         * Tries to parse a {@link Success} variant.
         *
         * @param node    the inbound IQ stanza
         * @param request the original outbound request
         * @return an {@link Optional} carrying the parsed variant, or
         *         empty when the envelope check fails
         */
        @WhatsAppWebExport(moduleName = "WAWebSetPrivacyJob",
                exports = "setPrivacyParser", adaptation = WhatsAppAdaptation.ADAPTED)
        public static Optional<Success> of(Node node, Node request) {
            if (!SmaxIqResultResponseMixin.validate(node, request)) {
                return Optional.empty();
            }
            var privacy = node.getChild("privacy").orElse(null);
            if (privacy == null) {
                return Optional.empty();
            }
            var outcomes = new ArrayList<CategoryOutcome>();
            for (var category : privacy.getChildren("category")) {
                var name = category.getAttributeAsString("name")
                        .flatMap(IqQueryPrivacySettingsCategoryName::fromWire)
                        .orElse(null);
                if (name == null) {
                    continue;
                }
                var rawValue = category.getAttributeAsString("value").orElse(null);
                if (rawValue == null) {
                    continue;
                }
                if ("error".equals(rawValue)) {
                    var errorChild = category.getChild("error").orElse(null);
                    if (errorChild == null) {
                        continue;
                    }
                    var code = errorChild.getAttributeAsInt("code").orElse(-1);
                    var text = errorChild.getAttributeAsString("text").orElse(null);
                    outcomes.add(new CategoryOutcome(name, null, null, code, text));
                    continue;
                }
                var value = IqQueryPrivacySettingsVisibility.fromWire(rawValue).orElse(null);
                if (value == null) {
                    continue;
                }
                var dhash = category.getAttributeAsString("dhash").orElse(null);
                outcomes.add(new CategoryOutcome(name, value, dhash, -1, null));
            }
            return Optional.of(new Success(outcomes));
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
            return "IqSetPrivacyResponse.Success[categories=" + categories + ']';
        }
    }

    /**
     * The {@code ClientError} reply variant. The relay rejected the
     * envelope with a {@code 4xx} error code.
     */
    @WhatsAppWebModule(moduleName = "WAWebSetPrivacyJob")
    final class ClientError implements IqSetPrivacyResponse {
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
         *         empty when the schema does not match
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
            return "IqSetPrivacyResponse.ClientError[errorCode=" + errorCode
                    + ", errorText=" + errorText + ']';
        }
    }

    /**
     * The {@code ServerError} reply variant. The relay encountered a
     * transient internal failure ({@code 5xx} error code).
     */
    @WhatsAppWebModule(moduleName = "WAWebSetPrivacyJob")
    final class ServerError implements IqSetPrivacyResponse {
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
         *         empty when the schema does not match
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
            return "IqSetPrivacyResponse.ServerError[errorCode=" + errorCode
                    + ", errorText=" + errorText + ']';
        }
    }
}
