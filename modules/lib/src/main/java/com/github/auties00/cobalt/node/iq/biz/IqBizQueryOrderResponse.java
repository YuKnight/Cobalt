package com.github.auties00.cobalt.node.iq.biz;

import com.github.auties00.cobalt.meta.annotation.WhatsAppWebExport;
import com.github.auties00.cobalt.meta.annotation.WhatsAppWebModule;
import com.github.auties00.cobalt.meta.model.WhatsAppAdaptation;
import com.github.auties00.cobalt.node.Node;
import com.github.auties00.cobalt.node.iq.IqOperation;
import com.github.auties00.cobalt.node.smax.util.SmaxBaseServerErrorMixin;
import com.github.auties00.cobalt.node.smax.util.SmaxIqResultResponseMixin;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/**
 * Sealed family of inbound reply variants produced by the relay in
 * response to an {@link IqBizQueryOrderRequest}. Splits the WA Web
 * {@code queryOrderResponse} parser output into typed
 * {@code Success}/{@code ClientError}/{@code ServerError} variants.
 */
@WhatsAppWebModule(moduleName = "WAWebBizQueryOrderJob")
public sealed interface IqBizQueryOrderResponse extends IqOperation.Response
        permits IqBizQueryOrderResponse.Success, IqBizQueryOrderResponse.ClientError, IqBizQueryOrderResponse.ServerError {

    /**
     * Tries each {@link IqBizQueryOrderResponse} variant in priority order.
     *
     * @param node    the inbound IQ stanza; never {@code null}
     * @param request the original outbound stanza; never {@code null}
     * @return an {@link Optional} carrying the parsed variant
     * @throws NullPointerException if either argument is {@code null}
     */
    static Optional<? extends IqBizQueryOrderResponse> of(Node node, Node request) {
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
     * The {@code Success} reply variant — projects the typed order
     * detail.
     */
    final class Success implements IqBizQueryOrderResponse {
        /**
         * One typed order line-item — id, name, optional price/quantity,
         * optional thumbnail and variant properties.
         */
        public static final class LineItem {
            /**
             * The product id.
             */
            private final String id;

            /**
             * The product display name.
             */
            private final String name;

            /**
             * The unit price (string-encoded major-units integer).
             */
            private final Integer price;

            /**
             * The thumbnail id, when supplied.
             */
            private final String thumbnailId;

            /**
             * The thumbnail URL, when supplied.
             */
            private final String thumbnailUrl;

            /**
             * The currency code, when supplied.
             */
            private final String currency;

            /**
             * The line quantity, when supplied.
             */
            private final Integer quantity;

            /**
             * The list of variant properties (name → value).
             */
            private final List<Map.Entry<String, String>> properties;

            /**
             * Constructs a line item.
             *
             * @param id           the product id; never {@code null}
             * @param name         the display name; never {@code null}
             * @param price        the unit price; may be {@code null}
             * @param thumbnailId  the thumbnail id; may be {@code null}
             * @param thumbnailUrl the thumbnail URL; may be {@code null}
             * @param currency     the currency code; may be {@code null}
             * @param quantity     the line quantity; may be
             *                     {@code null}
             * @param properties   the variant properties; never
             *                     {@code null}
             * @throws NullPointerException if {@code id}, {@code name}
             *                              or {@code properties} is
             *                              {@code null}
             */
            public LineItem(String id,
                            String name,
                            Integer price,
                            String thumbnailId,
                            String thumbnailUrl,
                            String currency,
                            Integer quantity,
                            List<Map.Entry<String, String>> properties) {
                this.id = Objects.requireNonNull(id, "id cannot be null");
                this.name = Objects.requireNonNull(name, "name cannot be null");
                this.price = price;
                this.thumbnailId = thumbnailId;
                this.thumbnailUrl = thumbnailUrl;
                this.currency = currency;
                this.quantity = quantity;
                Objects.requireNonNull(properties, "properties cannot be null");
                this.properties = List.copyOf(properties);
            }

            /**
             * Returns the product id.
             *
             * @return the id; never {@code null}
             */
            public String id() {
                return id;
            }

            /**
             * Returns the display name.
             *
             * @return the name; never {@code null}
             */
            public String name() {
                return name;
            }

            /**
             * Returns the unit price.
             *
             * @return an {@link Optional} carrying the price
             */
            public Optional<Integer> price() {
                return Optional.ofNullable(price);
            }

            /**
             * Returns the thumbnail id.
             *
             * @return an {@link Optional} carrying the id
             */
            public Optional<String> thumbnailId() {
                return Optional.ofNullable(thumbnailId);
            }

            /**
             * Returns the thumbnail URL.
             *
             * @return an {@link Optional} carrying the URL
             */
            public Optional<String> thumbnailUrl() {
                return Optional.ofNullable(thumbnailUrl);
            }

            /**
             * Returns the currency code.
             *
             * @return an {@link Optional} carrying the code
             */
            public Optional<String> currency() {
                return Optional.ofNullable(currency);
            }

            /**
             * Returns the quantity.
             *
             * @return an {@link Optional} carrying the quantity
             */
            public Optional<Integer> quantity() {
                return Optional.ofNullable(quantity);
            }

            /**
             * Returns the variant properties.
             *
             * @return an unmodifiable list; never {@code null}
             */
            public List<Map.Entry<String, String>> properties() {
                return properties;
            }

            @Override
            public boolean equals(Object obj) {
                if (obj == this) {
                    return true;
                }
                if (obj == null || obj.getClass() != this.getClass()) {
                    return false;
                }
                var that = (LineItem) obj;
                return Objects.equals(this.id, that.id)
                        && Objects.equals(this.name, that.name)
                        && Objects.equals(this.price, that.price)
                        && Objects.equals(this.thumbnailId, that.thumbnailId)
                        && Objects.equals(this.thumbnailUrl, that.thumbnailUrl)
                        && Objects.equals(this.currency, that.currency)
                        && Objects.equals(this.quantity, that.quantity)
                        && Objects.equals(this.properties, that.properties);
            }

            @Override
            public int hashCode() {
                return Objects.hash(id, name, price, thumbnailId, thumbnailUrl,
                        currency, quantity, properties);
            }

            @Override
            public String toString() {
                return "IqBizQueryOrderResponse.Success.LineItem[id=" + id
                        + ", name=" + name + ", price=" + price
                        + ", quantity=" + quantity + ", currency=" + currency + ']';
            }
        }

        /**
         * The order creation time, when supplied.
         */
        private final Instant createdAt;

        /**
         * The currency code, when supplied.
         */
        private final String currency;

        /**
         * The pre-tax subtotal in major-units, when supplied.
         */
        private final Integer subtotal;

        /**
         * The tax amount in major-units, when supplied.
         */
        private final Integer tax;

        /**
         * The grand total in major-units, when supplied.
         */
        private final Integer total;

        /**
         * The list of typed line items, in wire order.
         */
        private final List<LineItem> products;

        /**
         * Constructs a successful reply.
         *
         * @param createdAt the creation time; may be {@code null}
         * @param currency  the currency code; may be {@code null}
         * @param subtotal  the subtotal; may be {@code null}
         * @param tax       the tax; may be {@code null}
         * @param total     the total; may be {@code null}
         * @param products  the line items; never {@code null}
         * @throws NullPointerException if {@code products} is
         *                              {@code null}
         */
        public Success(Instant createdAt,
                       String currency,
                       Integer subtotal,
                       Integer tax,
                       Integer total,
                       List<LineItem> products) {
            this.createdAt = createdAt;
            this.currency = currency;
            this.subtotal = subtotal;
            this.tax = tax;
            this.total = total;
            Objects.requireNonNull(products, "products cannot be null");
            this.products = List.copyOf(products);
        }

        /**
         * Returns the creation time.
         *
         * @return an {@link Optional} carrying the time
         */
        public Optional<Instant> createdAt() {
            return Optional.ofNullable(createdAt);
        }

        /**
         * Returns the currency code.
         *
         * @return an {@link Optional} carrying the code
         */
        public Optional<String> currency() {
            return Optional.ofNullable(currency);
        }

        /**
         * Returns the subtotal.
         *
         * @return an {@link Optional} carrying the subtotal
         */
        public Optional<Integer> subtotal() {
            return Optional.ofNullable(subtotal);
        }

        /**
         * Returns the tax.
         *
         * @return an {@link Optional} carrying the tax
         */
        public Optional<Integer> tax() {
            return Optional.ofNullable(tax);
        }

        /**
         * Returns the total.
         *
         * @return an {@link Optional} carrying the total
         */
        public Optional<Integer> total() {
            return Optional.ofNullable(total);
        }

        /**
         * Returns the line items.
         *
         * @return an unmodifiable list; never {@code null}
         */
        public List<LineItem> products() {
            return products;
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
        @WhatsAppWebExport(moduleName = "WAWebBizQueryOrderJob",
                exports = "queryOrderResponse", adaptation = WhatsAppAdaptation.ADAPTED)
        public static Optional<Success> of(Node node, Node request) {
            if (!SmaxIqResultResponseMixin.validate(node, request)) {
                return Optional.empty();
            }
            var orderNode = node.getChild("order").orElse(null);
            if (orderNode == null) {
                return Optional.of(new Success(null, null, null, null, null,
                        Collections.emptyList()));
            }
            var createdAtSeconds = orderNode.getAttributeAsLong("creation_ts").orElse(-1L);
            var createdAt = createdAtSeconds >= 0 ? Instant.ofEpochSecond(createdAtSeconds) : null;
            Integer subtotal = null;
            Integer tax = null;
            Integer total = null;
            String currency = null;
            var priceNode = orderNode.getChild("price").orElse(null);
            if (priceNode != null) {
                subtotal = priceNode.getChild("subtotal")
                        .flatMap(Node::toContentString)
                        .map(Integer::parseInt).orElse(null);
                tax = priceNode.getChild("tax")
                        .flatMap(Node::toContentString)
                        .map(Integer::parseInt).orElse(null);
                total = priceNode.getChild("total")
                        .flatMap(Node::toContentString)
                        .map(Integer::parseInt).orElse(null);
                currency = priceNode.getChild("currency")
                        .flatMap(Node::toContentString).orElse(null);
            }
            var products = new ArrayList<LineItem>();
            for (var productNode : orderNode.getChildren("product")) {
                var idNode = productNode.getChild("id").orElse(null);
                var nameNode = productNode.getChild("name").orElse(null);
                if (idNode == null || nameNode == null) {
                    continue;
                }
                var id = idNode.toContentString().orElse("");
                var name = nameNode.toContentString().orElse("");
                var price = productNode.getChild("price")
                        .flatMap(Node::toContentString)
                        .map(Integer::parseInt).orElse(null);
                var quantity = productNode.getChild("quantity")
                        .flatMap(Node::toContentString)
                        .map(Integer::parseInt).orElse(null);
                var lineCurrency = productNode.getChild("currency")
                        .flatMap(Node::toContentString).orElse(null);
                String thumbnailId = null;
                String thumbnailUrl = null;
                var imageNode = productNode.getChild("image").orElse(null);
                if (imageNode != null) {
                    thumbnailId = imageNode.getChild("id")
                            .flatMap(Node::toContentString).orElse(null);
                    thumbnailUrl = imageNode.getChild("url")
                            .flatMap(Node::toContentString).orElse(null);
                }
                var properties = new ArrayList<Map.Entry<String, String>>();
                productNode.getChild("variant_info").ifPresent(variantInfo ->
                        variantInfo.getChild("properties").ifPresent(props -> {
                            for (var prop : props.getChildren("property")) {
                                var pname = prop.getAttributeAsString("name").orElse(null);
                                var pvalue = prop.getAttributeAsString("value").orElse(null);
                                if (pname != null && pvalue != null) {
                                    properties.add(Map.entry(pname, pvalue));
                                }
                            }
                        }));
                products.add(new LineItem(id, name, price, thumbnailId, thumbnailUrl,
                        lineCurrency, quantity, properties));
            }
            return Optional.of(new Success(createdAt, currency, subtotal, tax, total, products));
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
            return Objects.equals(this.createdAt, that.createdAt)
                    && Objects.equals(this.currency, that.currency)
                    && Objects.equals(this.subtotal, that.subtotal)
                    && Objects.equals(this.tax, that.tax)
                    && Objects.equals(this.total, that.total)
                    && Objects.equals(this.products, that.products);
        }

        @Override
        public int hashCode() {
            return Objects.hash(createdAt, currency, subtotal, tax, total, products);
        }

        @Override
        public String toString() {
            return "IqBizQueryOrderResponse.Success[createdAt=" + createdAt
                    + ", currency=" + currency + ", subtotal=" + subtotal
                    + ", tax=" + tax + ", total=" + total + ", products=" + products + ']';
        }
    }

    /**
     * The {@code ClientError} reply variant.
     */
    final class ClientError implements IqBizQueryOrderResponse {
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
            return this.errorCode == that.errorCode && Objects.equals(this.errorText, that.errorText);
        }

        @Override
        public int hashCode() {
            return Objects.hash(errorCode, errorText);
        }

        @Override
        public String toString() {
            return "IqBizQueryOrderResponse.ClientError[errorCode=" + errorCode
                    + ", errorText=" + errorText + ']';
        }
    }

    /**
     * The {@code ServerError} reply variant.
     */
    final class ServerError implements IqBizQueryOrderResponse {
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
            return "IqBizQueryOrderResponse.ServerError[errorCode=" + errorCode
                    + ", errorText=" + errorText + ']';
        }
    }
}
