package com.github.auties00.cobalt.node.iq.biz;

import com.github.auties00.cobalt.meta.annotation.WhatsAppWebExport;
import com.github.auties00.cobalt.meta.annotation.WhatsAppWebModule;
import com.github.auties00.cobalt.meta.model.WhatsAppAdaptation;
import com.github.auties00.cobalt.node.Node;
import com.github.auties00.cobalt.node.iq.IqOperation;
import com.github.auties00.cobalt.node.smax.util.SmaxBaseServerErrorMixin;
import com.github.auties00.cobalt.node.smax.util.SmaxIqResultResponseMixin;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * Sealed family of inbound reply variants produced by the relay in
 * response to an {@link IqQueryBusinessCategoriesRequest}.
 */
@WhatsAppWebModule(moduleName = "WAWebQueryBusinessCategoriesJob")
public sealed interface IqQueryBusinessCategoriesResponse extends IqOperation.Response
        permits IqQueryBusinessCategoriesResponse.Success, IqQueryBusinessCategoriesResponse.ClientError, IqQueryBusinessCategoriesResponse.ServerError {

    /**
     * Tries each {@link IqQueryBusinessCategoriesResponse} variant in priority order.
     *
     * @param node    the inbound IQ stanza; never {@code null}
     * @param request the original outbound stanza; never {@code null}
     * @return an {@link Optional} carrying the parsed variant, or empty
     *         when no documented variant matched
     * @throws NullPointerException if either argument is {@code null}
     */
    static Optional<? extends IqQueryBusinessCategoriesResponse> of(Node node, Node request) {
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
     * The {@code Success} reply variant — projects the typed category
     * list and the {@code not-a-business} sentinel id.
     */
    final class Success implements IqQueryBusinessCategoriesResponse {
        /**
         * One typed category entry.
         */
        public static final class Category {
            /**
             * The opaque category identifier.
             */
            private final String id;

            /**
             * The localised display name shown in the picker UI.
             */
            private final String localizedDisplayName;

            /**
             * Whether this category is the synthetic
             * {@code not_a_biz} entry — true only when the entry's id
             * matches the id carried by the {@code <not_a_biz/>}
             * sentinel.
             */
            private final boolean notABiz;

            /**
             * Constructs a category entry.
             *
             * @param id                   the category id; never
             *                             {@code null}
             * @param localizedDisplayName the display name; never
             *                             {@code null}
             * @param notABiz              the {@code not_a_biz} flag
             * @throws NullPointerException if either string is
             *                              {@code null}
             */
            public Category(String id, String localizedDisplayName, boolean notABiz) {
                this.id = Objects.requireNonNull(id, "id cannot be null");
                this.localizedDisplayName = Objects.requireNonNull(
                        localizedDisplayName, "localizedDisplayName cannot be null");
                this.notABiz = notABiz;
            }

            /**
             * Returns the category id.
             *
             * @return the id; never {@code null}
             */
            public String id() {
                return id;
            }

            /**
             * Returns the display name.
             *
             * @return the display name; never {@code null}
             */
            public String localizedDisplayName() {
                return localizedDisplayName;
            }

            /**
             * Returns whether this category is the
             * {@code not_a_biz} sentinel.
             *
             * @return {@code true} when this entry corresponds to the
             *         synthetic opt-out category
             */
            public boolean notABiz() {
                return notABiz;
            }

            @Override
            public boolean equals(Object obj) {
                if (obj == this) {
                    return true;
                }
                if (obj == null || obj.getClass() != this.getClass()) {
                    return false;
                }
                var that = (Category) obj;
                return this.notABiz == that.notABiz
                        && Objects.equals(this.id, that.id)
                        && Objects.equals(this.localizedDisplayName, that.localizedDisplayName);
            }

            @Override
            public int hashCode() {
                return Objects.hash(id, localizedDisplayName, notABiz);
            }

            @Override
            public String toString() {
                return "IqQueryBusinessCategoriesResponse.Success.Category[id=" + id
                        + ", localizedDisplayName=" + localizedDisplayName
                        + ", notABiz=" + notABiz + ']';
            }
        }

        /**
         * The typed category entries, in wire order.
         */
        private final List<Category> categories;

        /**
         * The id of the {@code not_a_biz} sentinel category, when
         * the relay supplied one (empty string otherwise — the wire
         * default).
         */
        private final String notABizId;

        /**
         * Constructs a successful reply.
         *
         * @param categories the category list; never {@code null}
         * @param notABizId  the sentinel id; never {@code null}
         *                   (empty string when absent)
         * @throws NullPointerException if either argument is
         *                              {@code null}
         */
        public Success(List<Category> categories, String notABizId) {
            Objects.requireNonNull(categories, "categories cannot be null");
            this.categories = List.copyOf(categories);
            this.notABizId = Objects.requireNonNull(notABizId, "notABizId cannot be null");
        }

        /**
         * Returns the typed category entries.
         *
         * @return an unmodifiable list; never {@code null}
         */
        public List<Category> categories() {
            return categories;
        }

        /**
         * Returns the {@code not_a_biz} sentinel id.
         *
         * @return the sentinel id; empty string when absent
         */
        public String notABizId() {
            return notABizId;
        }

        /**
         * Tries to parse a {@link Success} variant.
         *
         * @param node    the inbound IQ stanza
         * @param request the original outbound request
         * @return an {@link Optional} carrying the parsed variant, or
         *         empty when the stanza does not match the success
         *         schema
         */
        @WhatsAppWebExport(moduleName = "WAWebQueryBusinessCategoriesJob",
                exports = "queryBusinessCategories", adaptation = WhatsAppAdaptation.ADAPTED)
        public static Optional<Success> of(Node node, Node request) {
            if (!SmaxIqResultResponseMixin.validate(node, request)) {
                return Optional.empty();
            }
            var responseNode = node.getChild("response").orElse(null);
            if (responseNode == null) {
                return Optional.of(new Success(Collections.emptyList(), ""));
            }
            var notABizId = responseNode.getChild("not_a_biz")
                    .stream()
                    .flatMap(child -> child.streamChildren("category"))
                    .map(category -> category.getAttributeAsString("id").orElse(""))
                    .reduce("", (acc, id) -> id);
            var categoriesNode = responseNode.getChild("categories").orElse(null);
            var categories = new ArrayList<Category>();
            if (categoriesNode != null) {
                for (var categoryNode : categoriesNode.getChildren("category")) {
                    var id = categoryNode.getAttributeAsString("id").orElse(null);
                    if (id == null) {
                        continue;
                    }
                    var displayName = categoryNode.toContentString().orElse("");
                    var isNotABiz = !notABizId.isEmpty() && id.equals(notABizId);
                    categories.add(new Category(id, displayName, isNotABiz));
                }
            }
            return Optional.of(new Success(categories, notABizId));
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
            return Objects.equals(this.categories, that.categories)
                    && Objects.equals(this.notABizId, that.notABizId);
        }

        @Override
        public int hashCode() {
            return Objects.hash(categories, notABizId);
        }

        @Override
        public String toString() {
            return "IqQueryBusinessCategoriesResponse.Success[categories=" + categories
                    + ", notABizId=" + notABizId + ']';
        }
    }

    /**
     * The {@code ClientError} reply variant.
     */
    final class ClientError implements IqQueryBusinessCategoriesResponse {
        /**
         * The numeric error code.
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
         * Returns the human-readable error text, when supplied.
         *
         * @return an {@link Optional} carrying the error text
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
         *         empty when the stanza does not match the client-error
         *         schema
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
            return this.errorCode == that.errorCode && Objects.equals(this.errorText, that.errorText);
        }

        @Override
        public int hashCode() {
            return Objects.hash(errorCode, errorText);
        }

        @Override
        public String toString() {
            return "IqQueryBusinessCategoriesResponse.ClientError[errorCode=" + errorCode
                    + ", errorText=" + errorText + ']';
        }
    }

    /**
     * The {@code ServerError} reply variant.
     */
    final class ServerError implements IqQueryBusinessCategoriesResponse {
        /**
         * The numeric error code.
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
         * Returns the human-readable error text, when supplied.
         *
         * @return an {@link Optional} carrying the error text
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
         *         empty when the stanza does not match the server-error
         *         schema
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
            return "IqQueryBusinessCategoriesResponse.ServerError[errorCode=" + errorCode
                    + ", errorText=" + errorText + ']';
        }
    }
}
