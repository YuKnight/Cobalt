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
 * response to an {@link IqSetMerchantComplianceRequest}.
 */
@WhatsAppWebModule(moduleName = "WAWebMerchantComplianceJob")
public sealed interface IqSetMerchantComplianceResponse extends IqOperation.Response
        permits IqSetMerchantComplianceResponse.Success, IqSetMerchantComplianceResponse.ClientError, IqSetMerchantComplianceResponse.ServerError {

    /**
     * Tries each {@link IqSetMerchantComplianceResponse} variant in priority order.
     *
     * @param node    the inbound IQ stanza; never {@code null}
     * @param request the original outbound stanza; never {@code null}
     * @return an {@link Optional} carrying the parsed variant
     * @throws NullPointerException if either argument is {@code null}
     */
    static Optional<? extends IqSetMerchantComplianceResponse> of(Node node, Node request) {
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
     * The {@code Success} reply variant — projects the post-mutation
     * compliance entries (the relay echoes the same shape as
     * {@link IqGetMerchantComplianceResponse.Success}).
     */
    final class Success implements IqSetMerchantComplianceResponse {
        /**
         * The post-mutation compliance entries.
         */
        private final List<IqGetMerchantComplianceResponse.Success.MerchantInfo> entries;

        /**
         * Constructs a successful reply.
         *
         * @param entries the entries; never {@code null}
         * @throws NullPointerException if {@code entries} is
         *                              {@code null}
         */
        public Success(List<IqGetMerchantComplianceResponse.Success.MerchantInfo> entries) {
            Objects.requireNonNull(entries, "entries cannot be null");
            this.entries = List.copyOf(entries);
        }

        /**
         * Returns the post-mutation entries.
         *
         * @return an unmodifiable list; never {@code null}
         */
        public List<IqGetMerchantComplianceResponse.Success.MerchantInfo> entries() {
            return entries;
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
        @WhatsAppWebExport(moduleName = "WAWebMerchantComplianceJob",
                exports = "merchantComplianceResponse", adaptation = WhatsAppAdaptation.ADAPTED)
        public static Optional<Success> of(Node node, Node request) {
            if (!SmaxIqResultResponseMixin.validate(node, request)) {
                return Optional.empty();
            }
            var entries = new ArrayList<IqGetMerchantComplianceResponse.Success.MerchantInfo>();
            for (var miNode : node.getChildren("merchant_info")) {
                var entityName = miNode.getChild("entity_name")
                        .flatMap(Node::toContentString).orElse("");
                var entityType = miNode.getChild("entity_type")
                        .flatMap(Node::toContentString).orElse("");
                var entityTypeCustom = miNode.getChild("entity_type_custom")
                        .flatMap(Node::toContentString).orElse(null);
                var registered = miNode.getAttributeAsString("is_registered")
                        .map("true"::equals).orElse(false);
                var ccdNode = miNode.getChild("customer_care_details").orElse(null);
                var ccdEmail = ccdNode == null
                        ? ""
                        : ccdNode.getChild("email").flatMap(Node::toContentString).orElse("");
                var ccdLandline = ccdNode == null
                        ? ""
                        : ccdNode.getChild("landline_number").flatMap(Node::toContentString).orElse("");
                var ccdMobile = ccdNode == null
                        ? ""
                        : ccdNode.getChild("mobile_number").flatMap(Node::toContentString).orElse("");
                var ccd = new IqGetMerchantComplianceResponse.Success.BusinessContactDetails(
                        ccdEmail, ccdLandline, ccdMobile);
                var godNode = miNode.getChild("grievance_officer_details").orElse(null);
                var godName = godNode == null
                        ? ""
                        : godNode.getChild("name").flatMap(Node::toContentString).orElse("");
                var godEmail = godNode == null
                        ? ""
                        : godNode.getChild("email").flatMap(Node::toContentString).orElse("");
                var godLandline = godNode == null
                        ? ""
                        : godNode.getChild("landline_number").flatMap(Node::toContentString).orElse("");
                var godMobile = godNode == null
                        ? ""
                        : godNode.getChild("mobile_number").flatMap(Node::toContentString).orElse("");
                var god = new IqGetMerchantComplianceResponse.Success.BusinessGrievanceOfficerDetails(
                        godName, godEmail, godLandline, godMobile);
                entries.add(new IqGetMerchantComplianceResponse.Success.MerchantInfo(
                        entityName, entityType, registered, entityTypeCustom, ccd, god));
            }
            if (entries.isEmpty()) {
                return Optional.of(new Success(Collections.emptyList()));
            }
            return Optional.of(new Success(entries));
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
            return Objects.equals(this.entries, that.entries);
        }

        @Override
        public int hashCode() {
            return Objects.hash(entries);
        }

        @Override
        public String toString() {
            return "IqSetMerchantComplianceResponse.Success[entries=" + entries + ']';
        }
    }

    /**
     * The {@code ClientError} reply variant.
     */
    final class ClientError implements IqSetMerchantComplianceResponse {
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
            return "IqSetMerchantComplianceResponse.ClientError[errorCode=" + errorCode
                    + ", errorText=" + errorText + ']';
        }
    }

    /**
     * The {@code ServerError} reply variant.
     */
    final class ServerError implements IqSetMerchantComplianceResponse {
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
            return "IqSetMerchantComplianceResponse.ServerError[errorCode=" + errorCode
                    + ", errorText=" + errorText + ']';
        }
    }
}
