package com.github.auties00.cobalt.node.mex.json.business;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.alibaba.fastjson2.JSONWriter;
import com.github.auties00.cobalt.meta.annotation.WhatsAppWebExport;
import com.github.auties00.cobalt.meta.annotation.WhatsAppWebModule;
import com.github.auties00.cobalt.meta.model.WhatsAppAdaptation;
import com.github.auties00.cobalt.node.Node;
import com.github.auties00.cobalt.node.NodeBuilder;
import com.github.auties00.cobalt.node.mex.json.MexJsonOperation;

import java.io.IOException;
import java.io.StringWriter;
import java.io.UncheckedIOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * Fetches the detail of a business order identified by a message id and a
 * server-issued token.
 *
 * <p>An order is produced when a customer submits an {@code OrderMessage} to
 * a business contact; the business response carries a sensitive token that
 * authenticates subsequent order look-ups. WA Web's
 * {@code WAWebBizQueryOrderJob.queryOrder} routes the request through the
 * {@code WAWebBizQueryOrderJobQuery.graphql} compiled query, dispatched via
 * {@code WAWebRelayClient.fetchQuery} when the GraphQL gate is enabled and
 * falling back to a legacy {@code fb:thrift_iq} IQ otherwise. Cobalt mirrors
 * the GraphQL path and omits the {@code fb:thrift_iq} fallback.
 *
 * <p>This type is a sealed interface that models the two sides of the MEX
 * exchange as sibling variants, matching the pattern used across the rest of
 * the Cobalt {@code mex.json} package.
 *
 * @implNote WAWebBizQueryOrderJob: adapts the
 * {@code WAWebBizQueryOrderJobQuery.graphql} operation used by the GraphQL
 * {@code queryOrder} path. The WA Web request serialises the GraphQL
 * variables under a {@code request.order} envelope containing the order id,
 * a {@code sensitive_string_value} token, the requested image dimensions
 * and an optional {@code direct_connection_encrypted_info} bridge payload;
 * this class reproduces the exact shape directly.
 */
@WhatsAppWebModule(moduleName = "WAWebBizQueryOrderJob")
@WhatsAppWebModule(moduleName = "WAWebBizQueryOrderJobQuery.graphql")
public sealed interface QueryOrderMex extends MexJsonOperation permits QueryOrderMex.Request, QueryOrderMex.Response {
    /**
     * The numeric GraphQL query identifier assigned by the WhatsApp relay
     * to the {@code WAWebBizQueryOrderJobQuery} compiled query.
     *
     * @implNote WAWebBizQueryOrderJobQuery.graphql: corresponds to the
     * {@code params.id} field of the compiled query, extracted from the
     * current snapshot of the WA Web bundle.
     */
    @WhatsAppWebExport(moduleName = "WAWebBizQueryOrderJobQuery.graphql", exports = "params.id",
            adaptation = WhatsAppAdaptation.DIRECT)
    String QUERY_ID = "26593811266898374";

    /**
     * The request variant of {@link QueryOrderMex} that serialises the
     * GraphQL variables and emits the outbound IQ stanza.
     *
     * @implNote WAWebBizQueryOrderJob.queryOrder: adapts the inline
     * {@code variables.request.order} object built by the WA Web job into a
     * dedicated Java class.
     */
    @WhatsAppWebModule(moduleName = "WAWebBizQueryOrderJob")
    final class Request implements QueryOrderMex {
        private final String userJid;
        private final String orderId;
        private final String tokenBase64;
        private final int imageWidth;
        private final int imageHeight;

        /**
         * Creates a new order query request.
         *
         * @param userJid     the logged-in user JID stringified via
         *                    {@code toString()} — matches WA Web
         *                    {@code WAWebUserPrefsMeUser.getMePnUserOrThrow_DO_NOT_USE().toString()}
         * @param orderId     the server-issued order identifier, typically
         *                    the id of the {@code OrderMessage} carrying the
         *                    order
         * @param tokenBase64 the sensitive base64-encoded token returned by
         *                    the business with the order message
         * @param imageWidth  the requested thumbnail width in pixels used
         *                    when the relay rewrites image URLs
         * @param imageHeight the requested thumbnail height in pixels
         */
        public Request(String userJid, String orderId, String tokenBase64, int imageWidth, int imageHeight) {
            this.userJid = userJid;
            this.orderId = orderId;
            this.tokenBase64 = tokenBase64;
            this.imageWidth = imageWidth;
            this.imageHeight = imageHeight;
        }

        /**
         * Builds the IQ stanza that dispatches this operation to the
         * WhatsApp relay.
         *
         * @implNote WAWebBizQueryOrderJob.queryOrder: mirrors the WA Web
         * {@code request.order} variable shape with {@code jid},
         * {@code token.sensitive_string_value}, {@code id},
         * {@code image_dimensions.height}, {@code image_dimensions.width},
         * and {@code direct_connection_encrypted_info} (omitted here).
         * @return a {@link NodeBuilder} carrying the IQ envelope and the
         *         serialised GraphQL variables
         */
        @WhatsAppWebExport(moduleName = "WAWebBizQueryOrderJob", exports = "queryOrder",
                adaptation = WhatsAppAdaptation.ADAPTED)
        public NodeBuilder toNode() {
            // WAWebBizQueryOrderJob.queryOrder: WAWebRelayClient.fetchQuery(c, {request: {order: {...}}}, {environmentType: "whatsapp_catalog"})
            try (var writer = JSONWriter.ofUTF8()) {
                writer.startObject();
                writer.writeName("variables");
                writer.writeColon();
                writer.startObject();
                writer.writeName("request");
                writer.writeColon();
                writer.startObject();
                writer.writeName("order");
                writer.writeColon();
                writer.startObject();
                writer.writeName("jid");
                writer.writeColon();
                writer.writeString(userJid);
                writer.writeName("token");
                writer.writeColon();
                writer.startObject();
                writer.writeName("sensitive_string_value");
                writer.writeColon();
                writer.writeString(tokenBase64);
                writer.endObject();
                writer.writeName("id");
                writer.writeColon();
                writer.writeString(orderId);
                writer.writeName("image_dimensions");
                writer.writeColon();
                writer.startObject();
                writer.writeName("height");
                writer.writeColon();
                writer.writeInt32(imageHeight);
                writer.writeName("width");
                writer.writeColon();
                writer.writeInt32(imageWidth);
                writer.endObject();
                // WAWebBizQueryOrderJob.queryOrder: direct_connection_encrypted_info defaults to null and Cobalt never supplies it
                writer.endObject();
                writer.endObject();
                writer.endObject();
                writer.endObject();
                try (var output = new StringWriter()) {
                    writer.flushTo(output);
                    return MexJsonOperation.createMexNode(QUERY_ID, output.toString());
                }
            } catch (IOException exception) {
                throw new UncheckedIOException(exception);
            }
        }
    }

    /**
     * The response variant that parses the JSON returned by the relay into
     * a structured order.
     *
     * @implNote WAWebBizQueryOrderJob.queryOrder: adapts the
     * {@code xwa_checkout_get_order_info.order} projection into a Cobalt
     * {@link Order} and its nested {@link OrderItem} list.
     */
    @WhatsAppWebModule(moduleName = "WAWebBizQueryOrderJob")
    final class Response implements QueryOrderMex {
        private final Order order;

        private Response(Order order) {
            this.order = order;
        }

        /**
         * Parses the MEX response carried by an inbound IQ stanza.
         *
         * @implNote WAWebBizQueryOrderJob.queryOrder: WA Web reads
         * {@code data.xwa_checkout_get_order_info.order} and builds the
         * response object in-place; when the projection is missing it raises
         * a {@code ServerStatusCodeError(500)}. Cobalt returns
         * {@link Optional#empty()} instead so callers can branch on absence
         * without try/catch.
         * @param node the inbound IQ stanza carrying the {@code <result>} child
         * @return the parsed response, or {@link Optional#empty()} if the
         *         expected JSON shape is absent
         */
        @WhatsAppWebExport(moduleName = "WAWebBizQueryOrderJob", exports = "queryOrder",
                adaptation = WhatsAppAdaptation.ADAPTED)
        public static Optional<Response> of(Node node) {
            return node.getChild("result")
                    .flatMap(Node::toContentBytes)
                    .flatMap(Response::of);
        }

        /**
         * Returns the parsed order detail.
         *
         * @return the order, never {@code null}
         */
        public Order order() {
            return order;
        }

        private static Optional<Response> of(byte[] json) {
            var root = JSON.parseObject(json);
            if (root == null) {
                return Optional.empty();
            }
            var data = root.getJSONObject("data");
            if (data == null) {
                return Optional.empty();
            }
            var getResult = data.getJSONObject("xwa_checkout_get_order_info");
            if (getResult == null) {
                return Optional.empty();
            }
            var orderObj = getResult.getJSONObject("order");
            if (orderObj == null) {
                return Optional.empty();
            }
            // WAWebBizQueryOrderJob.queryOrder: createdAt: Number(g.creation_time_stamp)
            Long createdAt = null;
            var ts = orderObj.getString("creation_time_stamp");
            if (ts != null && !ts.isEmpty()) {
                try {
                    createdAt = Long.parseLong(ts);
                } catch (NumberFormatException ignored) {
                    createdAt = null;
                }
            }
            // WAWebBizQueryOrderJob.queryOrder: currency / subtotal / total from g.price_details
            String currency = null;
            Long subtotal = null;
            Long total = null;
            var priceDetails = orderObj.getJSONObject("price_details");
            if (priceDetails != null) {
                currency = priceDetails.getString("currency");
                subtotal = parseLong(priceDetails.getString("subtotal_amount"));
                total = parseLong(priceDetails.getString("total_amount"));
            }
            var products = parseProducts(orderObj.getJSONArray("products"));
            return Optional.of(new Response(new Order(createdAt, currency, subtotal, total, products)));
        }

        private static List<OrderItem> parseProducts(JSONArray array) {
            if (array == null || array.isEmpty()) {
                return List.of();
            }
            var out = new ArrayList<OrderItem>(array.size());
            for (var i = 0; i < array.size(); i++) {
                parseProduct(array.getJSONObject(i)).ifPresent(out::add);
            }
            return List.copyOf(out);
        }

        private static Optional<OrderItem> parseProduct(JSONObject obj) {
            if (obj == null) {
                return Optional.empty();
            }
            // WAWebBizQueryOrderJob.queryOrder: product mapping
            var id = obj.getString("id");
            var name = obj.getString("name");
            var price = parseLong(obj.getString("price"));
            var currency = obj.getString("currency");
            var quantity = parseInt(obj.getString("quantity"));
            var properties = new ArrayList<String[]>();
            var variantInfo = obj.getJSONObject("variant_info");
            if (variantInfo != null) {
                var variantProperties = variantInfo.getJSONArray("variant_properties");
                if (variantProperties != null) {
                    for (var i = 0; i < variantProperties.size(); i++) {
                        var prop = variantProperties.getJSONObject(i);
                        if (prop == null) {
                            continue;
                        }
                        var pName = prop.getString("name");
                        var pValue = prop.getString("value");
                        if (pName != null && pValue != null) {
                            properties.add(new String[]{pName, pValue});
                        }
                    }
                }
            }
            String thumbnailId = null;
            String thumbnailUrl = null;
            var media = obj.getJSONObject("media");
            if (media != null) {
                var images = media.getJSONArray("images");
                if (images != null && !images.isEmpty()) {
                    var first = images.getJSONObject(0);
                    if (first != null) {
                        thumbnailId = first.getString("id");
                        thumbnailUrl = first.getString("request_image_url");
                    }
                }
            }
            return Optional.of(new OrderItem(id, name, price, currency, quantity, thumbnailId, thumbnailUrl, List.copyOf(properties)));
        }

        private static Long parseLong(String value) {
            if (value == null || value.isEmpty()) {
                return null;
            }
            try {
                return Long.parseLong(value);
            } catch (NumberFormatException ignored) {
                return null;
            }
        }

        private static Integer parseInt(String value) {
            if (value == null || value.isEmpty()) {
                return null;
            }
            try {
                return Integer.parseInt(value);
            } catch (NumberFormatException ignored) {
                return null;
            }
        }
    }

    /**
     * Represents the detail of a business order returned by the
     * {@code queryOrder} MEX operation.
     *
     * <p>All fields are nullable — WA Web constructs the object with
     * {@code null}-safe extraction from the GraphQL projection and Cobalt
     * preserves that semantic so callers can branch on missing values.
     *
     * @param createdAt the order creation timestamp in seconds since epoch,
     *                  or {@code null} when absent
     * @param currency  the ISO 4217 currency code, or {@code null}
     * @param subtotal  the subtotal amount in thousandths of the currency
     *                  unit, or {@code null}
     * @param total     the total amount in thousandths of the currency unit,
     *                  or {@code null}
     * @param items     the parsed order items, never {@code null}
     */
    record Order(Long createdAt, String currency, Long subtotal, Long total, List<OrderItem> items) {
        /**
         * Normalises the item list so callers observe an unmodifiable view
         * even when callers pass a mutable list.
         *
         * @param createdAt order creation timestamp
         * @param currency  ISO 4217 currency
         * @param subtotal  subtotal in thousandths
         * @param total     total in thousandths
         * @param items     order items; {@code null} is coerced to empty
         */
        public Order {
            items = items == null ? List.of() : List.copyOf(items);
        }
    }

    /**
     * Represents a single line item attached to an {@link Order}.
     *
     * <p>Each item carries the server-issued product id, the product name,
     * the per-unit price expressed in thousandths of the currency unit, the
     * ISO 4217 currency code, the ordered quantity, a thumbnail id/url pair
     * when the relay published one, and the list of variant properties as
     * {@code [name, value]} pairs.
     *
     * @param id           the server-issued product id
     * @param name         the product name
     * @param price        per-unit price in thousandths, or {@code null}
     * @param currency     ISO 4217 currency code, or {@code null}
     * @param quantity     ordered quantity, or {@code null}
     * @param thumbnailId  thumbnail image id, or {@code null}
     * @param thumbnailUrl thumbnail image url, or {@code null}
     * @param properties   variant {@code [name, value]} pairs
     */
    record OrderItem(
            String id,
            String name,
            Long price,
            String currency,
            Integer quantity,
            String thumbnailId,
            String thumbnailUrl,
            List<String[]> properties
    ) {
        /**
         * Normalises the variant property list to an unmodifiable view.
         *
         * @param id           product id
         * @param name         product name
         * @param price        per-unit price
         * @param currency     currency code
         * @param quantity     ordered quantity
         * @param thumbnailId  thumbnail image id
         * @param thumbnailUrl thumbnail image url
         * @param properties   variant properties; {@code null} is coerced to empty
         */
        public OrderItem {
            properties = properties == null ? List.of() : Collections.unmodifiableList(properties);
        }
    }
}
