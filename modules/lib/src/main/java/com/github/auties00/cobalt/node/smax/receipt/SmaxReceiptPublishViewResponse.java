package com.github.auties00.cobalt.node.smax.receipt;

import com.github.auties00.cobalt.meta.annotation.WhatsAppWebExport;
import com.github.auties00.cobalt.meta.annotation.WhatsAppWebModule;
import com.github.auties00.cobalt.meta.model.WhatsAppAdaptation;
import com.github.auties00.cobalt.model.jid.Jid;
import com.github.auties00.cobalt.node.Node;
import com.github.auties00.cobalt.node.NodeBuilder;
import com.github.auties00.cobalt.node.smax.SmaxOperation;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * Sealed family of inbound reply variants. Only the {@code Success}
 * shape is documented for this RPC.
 */
public sealed interface SmaxReceiptPublishViewResponse extends SmaxOperation.Response
        permits SmaxReceiptPublishViewResponse.Success {

    /**
     * Tries the single {@link Success} variant.
     *
     * @param node    the inbound ack stanza. Never {@code null}
     * @param request the original outbound stanza. Used to validate
     *                echoed identifiers. Never {@code null}
     * @return an {@link Optional} carrying the parsed variant, or
     *         empty when the stanza does not match the documented
     *         shape
     * @throws NullPointerException if either argument is {@code null}
     */
    @WhatsAppWebExport(moduleName = "WASmaxReceiptPublishViewRPC",
            exports = "sendPublishViewRPC", adaptation = WhatsAppAdaptation.ADAPTED)
    static Optional<? extends SmaxReceiptPublishViewResponse> of(Node node, Node request) {
        Objects.requireNonNull(node, "node cannot be null");
        Objects.requireNonNull(request, "request cannot be null");
        return Success.of(node, request);
    }

    /**
     * The {@code Success} reply variant. The relay produced an
     * {@code <ack class="receipt" type=ECHO from=ECHO id=ECHO/>}
     * envelope, optionally carrying the timestamp and read-receipts
     * echo plus a deprecated edit marker.
     */
    @WhatsAppWebModule(moduleName = "WASmaxInReceiptPublishViewResponseSuccess")
    @WhatsAppWebModule(moduleName = "WASmaxInReceiptPublishSuccessMixin")
    @WhatsAppWebModule(moduleName = "WASmaxInReceiptDeprecatedEditMixin")
    final class Success implements SmaxReceiptPublishViewResponse {
        /**
         * The optional Unix-epoch timestamp echo (the relay's
         * processing time).
         */
        private final Long timestamp;

        /**
         * The optional {@code readreceipts} echo. One of
         * {@code "all"} / {@code "none"}.
         */
        private final String readReceipts;

        /**
         * The optional deprecated {@code edit} marker. One of
         * {@code "0"} / {@code "1"} / {@code "7"}.
         */
        private final String deprecatedEdit;

        /**
         * Constructs a new success reply.
         *
         * @param timestamp      the optional timestamp. May be
         *                       {@code null}
         * @param readReceipts   the optional read-receipts echo. May
         *                       be {@code null}
         * @param deprecatedEdit the optional deprecated edit marker;
         *                       may be {@code null}
         */
        public Success(Long timestamp, String readReceipts, String deprecatedEdit) {
            this.timestamp = timestamp;
            this.readReceipts = readReceipts;
            this.deprecatedEdit = deprecatedEdit;
        }

        /**
         * Returns the optional timestamp echo.
         *
         * @return an {@link Optional} carrying the timestamp
         */
        public Optional<Long> timestamp() {
            return Optional.ofNullable(timestamp);
        }

        /**
         * Returns the optional read-receipts echo.
         *
         * @return an {@link Optional} carrying the value
         */
        public Optional<String> readReceipts() {
            return Optional.ofNullable(readReceipts);
        }

        /**
         * Returns the optional deprecated {@code edit} marker.
         *
         * @return an {@link Optional} carrying the marker
         */
        public Optional<String> deprecatedEdit() {
            return Optional.ofNullable(deprecatedEdit);
        }

        /**
         * Tries to parse a {@link Success} variant.
         *
         * @param node    the inbound ack stanza
         * @param request the original outbound request
         * @return an {@link Optional} carrying the parsed variant
         */
        @WhatsAppWebExport(moduleName = "WASmaxInReceiptPublishViewResponseSuccess",
                exports = "parsePublishViewResponseSuccess",
                adaptation = WhatsAppAdaptation.ADAPTED)
        @WhatsAppWebExport(moduleName = "WASmaxInReceiptPublishSuccessMixin",
                exports = "parsePublishSuccessMixin",
                adaptation = WhatsAppAdaptation.ADAPTED)
        public static Optional<Success> of(Node node, Node request) {
            Objects.requireNonNull(node, "node cannot be null");
            Objects.requireNonNull(request, "request cannot be null");
            if (!node.hasDescription("ack")) {
                return Optional.empty();
            }
            if (!node.hasAttribute("class", "receipt")) {
                return Optional.empty();
            }
            var requestId = request.getAttributeAsString("id").orElse(null);
            if (requestId == null || !node.hasAttribute("id", requestId)) {
                return Optional.empty();
            }
            var requestTo = request.getAttributeAsString("to").orElse(null);
            if (requestTo == null || !node.hasAttribute("from", requestTo)) {
                return Optional.empty();
            }
            var requestType = request.getAttributeAsString("type").orElse(null);
            if (requestType == null || !node.hasAttribute("type", requestType)) {
                return Optional.empty();
            }
            var timestamp = node.getAttributeAsLong("t");
            var timestampValue = timestamp.isPresent() ? Long.valueOf(timestamp.getAsLong()) : null;
            var readReceipts = node.getAttributeAsString("readreceipts").orElse(null);
            if (readReceipts != null
                    && !"all".equals(readReceipts)
                    && !"none".equals(readReceipts)) {
                return Optional.empty();
            }
            var deprecatedEdit = node.getAttributeAsString("edit").orElse(null);
            if (deprecatedEdit != null
                    && !"0".equals(deprecatedEdit)
                    && !"1".equals(deprecatedEdit)
                    && !"7".equals(deprecatedEdit)) {
                return Optional.empty();
            }
            return Optional.of(new Success(timestampValue, readReceipts, deprecatedEdit));
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
            return Objects.equals(this.timestamp, that.timestamp)
                    && Objects.equals(this.readReceipts, that.readReceipts)
                    && Objects.equals(this.deprecatedEdit, that.deprecatedEdit);
        }

        @Override
        public int hashCode() {
            return Objects.hash(timestamp, readReceipts, deprecatedEdit);
        }

        @Override
        public String toString() {
            return "SmaxReceiptPublishViewResponse.Success[timestamp=" + timestamp
                    + ", readReceipts=" + readReceipts
                    + ", deprecatedEdit=" + deprecatedEdit + ']';
        }
    }
}
