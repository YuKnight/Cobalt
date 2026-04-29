package com.github.auties00.cobalt.node.iq.biz;

import com.github.auties00.cobalt.meta.annotation.WhatsAppWebExport;
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
 * Sealed family of inbound reply variants.
 */
public sealed interface IqGetMerchantComplianceResponse extends IqOperation.Response
        permits IqGetMerchantComplianceResponse.Success, IqGetMerchantComplianceResponse.ClientError, IqGetMerchantComplianceResponse.ServerError {

    /**
     * Tries each {@link IqGetMerchantComplianceResponse} variant in priority order.
     *
     * @param node    the inbound IQ stanza; never {@code null}
     * @param request the original outbound stanza; never {@code null}
     * @return an {@link Optional} carrying the parsed variant
     * @throws NullPointerException if either argument is {@code null}
     */
    static Optional<? extends IqGetMerchantComplianceResponse> of(Node node, Node request) {
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
     * The {@code Success} reply variant — projects the typed merchant
     * compliance bundles.
     */
    final class Success implements IqGetMerchantComplianceResponse {
        /**
         * One merchant-info entry, decoded from a
         * {@code <merchant_info/>} child of the inbound IQ.
         */
        public static final class MerchantInfo {
            /**
             * The legal entity name.
             */
            private final String entityName;

            /**
             * The entity type (e.g.
             * {@code "LIMITED_LIABILITY_PARTNERSHIP"},
             * {@code "PRIVATE_COMPANY"}).
             */
            private final String entityType;

            /**
             * Whether the merchant is registered.
             */
            private final boolean registered;

            /**
             * The optional custom entity-type label (free text).
             */
            private final String entityTypeCustom;

            /**
             * The customer-care details block.
             */
            private final BusinessContactDetails customerCareDetails;

            /**
             * The grievance-officer details block.
             */
            private final BusinessGrievanceOfficerDetails grievanceOfficerDetails;

            /**
             * Constructs a merchant-info entry.
             *
             * @param entityName              the legal entity name;
             *                                never {@code null}
             * @param entityType              the entity type; never
             *                                {@code null}
             * @param registered              the registered flag
             * @param entityTypeCustom        the custom-type label; may
             *                                be {@code null}
             * @param customerCareDetails     the customer-care block;
             *                                never {@code null}
             * @param grievanceOfficerDetails the grievance-officer
             *                                block; never {@code null}
             * @throws NullPointerException if any non-optional argument
             *                              is {@code null}
             */
            public MerchantInfo(String entityName,
                                String entityType,
                                boolean registered,
                                String entityTypeCustom,
                                BusinessContactDetails customerCareDetails,
                                BusinessGrievanceOfficerDetails grievanceOfficerDetails) {
                this.entityName = Objects.requireNonNull(entityName, "entityName cannot be null");
                this.entityType = Objects.requireNonNull(entityType, "entityType cannot be null");
                this.registered = registered;
                this.entityTypeCustom = entityTypeCustom;
                this.customerCareDetails = Objects.requireNonNull(
                        customerCareDetails, "customerCareDetails cannot be null");
                this.grievanceOfficerDetails = Objects.requireNonNull(
                        grievanceOfficerDetails, "grievanceOfficerDetails cannot be null");
            }

            /**
             * Returns the entity name.
             *
             * @return the name; never {@code null}
             */
            public String entityName() {
                return entityName;
            }

            /**
             * Returns the entity type.
             *
             * @return the type; never {@code null}
             */
            public String entityType() {
                return entityType;
            }

            /**
             * Returns the registered flag.
             *
             * @return {@code true} when registered
             */
            public boolean registered() {
                return registered;
            }

            /**
             * Returns the custom-type label.
             *
             * @return an {@link Optional} carrying the label
             */
            public Optional<String> entityTypeCustom() {
                return Optional.ofNullable(entityTypeCustom);
            }

            /**
             * Returns the customer-care block.
             *
             * @return the block; never {@code null}
             */
            public BusinessContactDetails customerCareDetails() {
                return customerCareDetails;
            }

            /**
             * Returns the grievance-officer block.
             *
             * @return the block; never {@code null}
             */
            public BusinessGrievanceOfficerDetails grievanceOfficerDetails() {
                return grievanceOfficerDetails;
            }

            @Override
            public boolean equals(Object obj) {
                if (obj == this) {
                    return true;
                }
                if (obj == null || obj.getClass() != this.getClass()) {
                    return false;
                }
                var that = (MerchantInfo) obj;
                return this.registered == that.registered
                        && Objects.equals(this.entityName, that.entityName)
                        && Objects.equals(this.entityType, that.entityType)
                        && Objects.equals(this.entityTypeCustom, that.entityTypeCustom)
                        && Objects.equals(this.customerCareDetails, that.customerCareDetails)
                        && Objects.equals(this.grievanceOfficerDetails, that.grievanceOfficerDetails);
            }

            @Override
            public int hashCode() {
                return Objects.hash(entityName, entityType, registered, entityTypeCustom,
                        customerCareDetails, grievanceOfficerDetails);
            }

            @Override
            public String toString() {
                return "IqGetMerchantComplianceResponse.Success.MerchantInfo[entityName="
                        + entityName + ", entityType=" + entityType
                        + ", registered=" + registered + ']';
            }
        }

        /**
         * The customer-care contact triple — email, landline number,
         * mobile number.
         */
        public static final class BusinessContactDetails {
            /**
             * The contact email.
             */
            private final String email;

            /**
             * The landline phone number.
             */
            private final String landlineNumber;

            /**
             * The mobile phone number.
             */
            private final String mobileNumber;

            /**
             * Constructs a triple.
             *
             * @param email          the email; never {@code null}
             * @param landlineNumber the landline; never {@code null}
             * @param mobileNumber   the mobile; never {@code null}
             * @throws NullPointerException if any argument is
             *                              {@code null}
             */
            public BusinessContactDetails(String email, String landlineNumber, String mobileNumber) {
                this.email = Objects.requireNonNull(email, "email cannot be null");
                this.landlineNumber = Objects.requireNonNull(landlineNumber, "landlineNumber cannot be null");
                this.mobileNumber = Objects.requireNonNull(mobileNumber, "mobileNumber cannot be null");
            }

            /**
             * Returns the email.
             *
             * @return the email; never {@code null}
             */
            public String email() {
                return email;
            }

            /**
             * Returns the landline number.
             *
             * @return the number; never {@code null}
             */
            public String landlineNumber() {
                return landlineNumber;
            }

            /**
             * Returns the mobile number.
             *
             * @return the number; never {@code null}
             */
            public String mobileNumber() {
                return mobileNumber;
            }

            @Override
            public boolean equals(Object obj) {
                if (obj == this) {
                    return true;
                }
                if (obj == null || obj.getClass() != this.getClass()) {
                    return false;
                }
                var that = (BusinessContactDetails) obj;
                return Objects.equals(this.email, that.email)
                        && Objects.equals(this.landlineNumber, that.landlineNumber)
                        && Objects.equals(this.mobileNumber, that.mobileNumber);
            }

            @Override
            public int hashCode() {
                return Objects.hash(email, landlineNumber, mobileNumber);
            }

            @Override
            public String toString() {
                return "IqGetMerchantComplianceResponse.Success.BusinessContactDetails[email="
                        + email + ", landlineNumber=" + landlineNumber
                        + ", mobileNumber=" + mobileNumber + ']';
            }
        }

        /**
         * The grievance-officer block — a {@link BusinessContactDetails} plus
         * a name field.
         */
        public static final class BusinessGrievanceOfficerDetails {
            /**
             * The officer's full name.
             */
            private final String name;

            /**
             * The officer's email.
             */
            private final String email;

            /**
             * The officer's landline number.
             */
            private final String landlineNumber;

            /**
             * The officer's mobile number.
             */
            private final String mobileNumber;

            /**
             * Constructs a block.
             *
             * @param name           the name; never {@code null}
             * @param email          the email; never {@code null}
             * @param landlineNumber the landline; never {@code null}
             * @param mobileNumber   the mobile; never {@code null}
             * @throws NullPointerException if any argument is
             *                              {@code null}
             */
            public BusinessGrievanceOfficerDetails(String name, String email,
                                           String landlineNumber, String mobileNumber) {
                this.name = Objects.requireNonNull(name, "name cannot be null");
                this.email = Objects.requireNonNull(email, "email cannot be null");
                this.landlineNumber = Objects.requireNonNull(landlineNumber, "landlineNumber cannot be null");
                this.mobileNumber = Objects.requireNonNull(mobileNumber, "mobileNumber cannot be null");
            }

            /**
             * Returns the name.
             *
             * @return the name; never {@code null}
             */
            public String name() {
                return name;
            }

            /**
             * Returns the email.
             *
             * @return the email; never {@code null}
             */
            public String email() {
                return email;
            }

            /**
             * Returns the landline number.
             *
             * @return the number; never {@code null}
             */
            public String landlineNumber() {
                return landlineNumber;
            }

            /**
             * Returns the mobile number.
             *
             * @return the number; never {@code null}
             */
            public String mobileNumber() {
                return mobileNumber;
            }

            @Override
            public boolean equals(Object obj) {
                if (obj == this) {
                    return true;
                }
                if (obj == null || obj.getClass() != this.getClass()) {
                    return false;
                }
                var that = (BusinessGrievanceOfficerDetails) obj;
                return Objects.equals(this.name, that.name)
                        && Objects.equals(this.email, that.email)
                        && Objects.equals(this.landlineNumber, that.landlineNumber)
                        && Objects.equals(this.mobileNumber, that.mobileNumber);
            }

            @Override
            public int hashCode() {
                return Objects.hash(name, email, landlineNumber, mobileNumber);
            }

            @Override
            public String toString() {
                return "IqGetMerchantComplianceResponse.Success.BusinessGrievanceOfficerDetails[name="
                        + name + ", email=" + email + ']';
            }
        }

        /**
         * The decoded compliance entries, in wire order.
         */
        private final List<MerchantInfo> entries;

        /**
         * Constructs a successful reply.
         *
         * @param entries the compliance entries; never {@code null}
         * @throws NullPointerException if {@code entries} is
         *                              {@code null}
         */
        public Success(List<MerchantInfo> entries) {
            Objects.requireNonNull(entries, "entries cannot be null");
            this.entries = List.copyOf(entries);
        }

        /**
         * Returns the compliance entries.
         *
         * @return an unmodifiable list; never {@code null}
         */
        public List<MerchantInfo> entries() {
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
            var entries = new ArrayList<MerchantInfo>();
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
                var ccd = parseContactDetails(ccdNode);
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
                var god = new BusinessGrievanceOfficerDetails(godName, godEmail, godLandline, godMobile);
                entries.add(new MerchantInfo(entityName, entityType, registered,
                        entityTypeCustom, ccd, god));
            }
            if (entries.isEmpty()) {
                return Optional.of(new Success(Collections.emptyList()));
            }
            return Optional.of(new Success(entries));
        }

        /**
         * Decodes a {@code <customer_care_details/>} (or any
         * {@code email}/{@code landline_number}/{@code mobile_number}
         * carrier) into a {@link BusinessContactDetails} triple, defaulting
         * each missing field to the empty string per the WA Web parser.
         *
         * @param contactNode the optional carrier node
         * @return a {@link BusinessContactDetails}; never {@code null}
         */
        private static BusinessContactDetails parseContactDetails(Node contactNode) {
            var email = contactNode == null
                    ? ""
                    : contactNode.getChild("email").flatMap(Node::toContentString).orElse("");
            var landline = contactNode == null
                    ? ""
                    : contactNode.getChild("landline_number").flatMap(Node::toContentString).orElse("");
            var mobile = contactNode == null
                    ? ""
                    : contactNode.getChild("mobile_number").flatMap(Node::toContentString).orElse("");
            return new BusinessContactDetails(email, landline, mobile);
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
            return "IqGetMerchantComplianceResponse.Success[entries=" + entries + ']';
        }
    }

    /**
     * The {@code ClientError} reply variant.
     */
    final class ClientError implements IqGetMerchantComplianceResponse {
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
            return "IqGetMerchantComplianceResponse.ClientError[errorCode=" + errorCode
                    + ", errorText=" + errorText + ']';
        }
    }

    /**
     * The {@code ServerError} reply variant.
     */
    final class ServerError implements IqGetMerchantComplianceResponse {
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
            return "IqGetMerchantComplianceResponse.ServerError[errorCode=" + errorCode
                    + ", errorText=" + errorText + ']';
        }
    }
}
