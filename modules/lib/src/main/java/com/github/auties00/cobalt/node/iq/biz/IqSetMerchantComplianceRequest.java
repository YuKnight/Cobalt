package com.github.auties00.cobalt.node.iq.biz;

import com.github.auties00.cobalt.meta.annotation.WhatsAppWebExport;
import com.github.auties00.cobalt.meta.model.WhatsAppAdaptation;
import com.github.auties00.cobalt.model.jid.JidServer;
import com.github.auties00.cobalt.node.Node;
import com.github.auties00.cobalt.node.NodeBuilder;
import com.github.auties00.cobalt.node.iq.IqOperation;
import java.util.ArrayList;
import java.util.Objects;
import java.util.Optional;

/**
 * The outbound stanza variant.
 */
public final class IqSetMerchantComplianceRequest implements IqOperation.Request {
    /**
     * Whether the merchant is registered.
     */
    private final boolean registered;

    /**
     * The optional legal entity name.
     */
    private final String entityName;

    /**
     * The optional entity type marker.
     */
    private final String entityType;

    /**
     * The optional custom entity-type label.
     */
    private final String entityTypeCustom;

    /**
     * The optional customer-care contact triple.
     */
    private final IqSetMerchantComplianceContactDetails customerCareDetails;

    /**
     * The optional grievance-officer block.
     */
    private final IqSetMerchantComplianceGrievanceOfficerDetails grievanceOfficerDetails;

    /**
     * Constructs a request.
     *
     * @param registered              the registered flag
     * @param entityName              the entity name; may be
     *                                {@code null}
     * @param entityType              the entity type; may be
     *                                {@code null}
     * @param entityTypeCustom        the custom-type label; may be
     *                                {@code null}
     * @param customerCareDetails     the customer-care triple; may be
     *                                {@code null}
     * @param grievanceOfficerDetails the grievance-officer block; may
     *                                be {@code null}
     */
    public IqSetMerchantComplianceRequest(boolean registered,
                   String entityName,
                   String entityType,
                   String entityTypeCustom,
                   IqSetMerchantComplianceContactDetails customerCareDetails,
                   IqSetMerchantComplianceGrievanceOfficerDetails grievanceOfficerDetails) {
        this.registered = registered;
        this.entityName = entityName;
        this.entityType = entityType;
        this.entityTypeCustom = entityTypeCustom;
        this.customerCareDetails = customerCareDetails;
        this.grievanceOfficerDetails = grievanceOfficerDetails;
    }

    /**
     * Returns the registered flag.
     *
     * @return the flag
     */
    public boolean registered() {
        return registered;
    }

    /**
     * Returns the entity name.
     *
     * @return an {@link Optional} carrying the name
     */
    public Optional<String> entityName() {
        return Optional.ofNullable(entityName);
    }

    /**
     * Returns the entity type.
     *
     * @return an {@link Optional} carrying the type
     */
    public Optional<String> entityType() {
        return Optional.ofNullable(entityType);
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
     * Returns the customer-care triple.
     *
     * @return an {@link Optional} carrying the triple
     */
    public Optional<IqSetMerchantComplianceContactDetails> customerCareDetails() {
        return Optional.ofNullable(customerCareDetails);
    }

    /**
     * Returns the grievance-officer block.
     *
     * @return an {@link Optional} carrying the block
     */
    public Optional<IqSetMerchantComplianceGrievanceOfficerDetails> grievanceOfficerDetails() {
        return Optional.ofNullable(grievanceOfficerDetails);
    }

    /**
     * Returns a fresh builder.
     *
     * @return a new {@link Builder}; never {@code null}
     */
    public static Builder builder() {
        return new Builder();
    }

    /**
     * Builds the outbound IQ stanza ready for dispatch.
     *
     * @return a {@link NodeBuilder} carrying the IQ envelope
     */
    @Override
    @WhatsAppWebExport(moduleName = "WAWebMerchantComplianceJob",
            exports = "setMerchantCompliance", adaptation = WhatsAppAdaptation.DIRECT)
    public NodeBuilder toNode() {
        var children = new ArrayList<Node>();
        if (entityName != null) {
            children.add(new NodeBuilder().description("entity_name").content(entityName).build());
        }
        if (entityType != null) {
            children.add(new NodeBuilder().description("entity_type").content(entityType).build());
        }
        if (entityTypeCustom != null) {
            children.add(new NodeBuilder().description("entity_type_custom")
                    .content(entityTypeCustom).build());
        }
        if (customerCareDetails != null) {
            children.add(buildContactNode("customer_care_details", customerCareDetails));
        }
        if (grievanceOfficerDetails != null) {
            children.add(buildGrievanceNode(grievanceOfficerDetails));
        }
        var merchantInfoNode = new NodeBuilder()
                .description("merchant_info")
                .attribute("is_registered", registered ? "true" : "false")
                .content(children)
                .build();
        return new NodeBuilder()
                .description("iq")
                .attribute("xmlns", "w:biz:merchant_info")
                .attribute("to", JidServer.user())
                .attribute("type", "set")
                .content(merchantInfoNode);
    }

    /**
     * Builds a contact-triple child carrying the supplied tag.
     *
     * @param description the tag (e.g.
     *                    {@code "customer_care_details"})
     * @param contact     the triple
     * @return the built node; never {@code null}
     */
    private static Node buildContactNode(String description, IqSetMerchantComplianceContactDetails contact) {
        var contactChildren = new ArrayList<Node>();
        if (contact.email().isPresent()) {
            contactChildren.add(new NodeBuilder()
                    .description("email")
                    .content(contact.email().get())
                    .build());
        }
        if (contact.landlineNumber().isPresent()) {
            contactChildren.add(new NodeBuilder()
                    .description("landline_number")
                    .content(contact.landlineNumber().get())
                    .build());
        }
        if (contact.mobileNumber().isPresent()) {
            contactChildren.add(new NodeBuilder()
                    .description("mobile_number")
                    .content(contact.mobileNumber().get())
                    .build());
        }
        return new NodeBuilder()
                .description(description)
                .content(contactChildren)
                .build();
    }

    /**
     * Builds the {@code <grievance_officer_details/>} child.
     *
     * @param block the block; never {@code null}
     * @return the built node; never {@code null}
     */
    private static Node buildGrievanceNode(IqSetMerchantComplianceGrievanceOfficerDetails block) {
        var grievanceChildren = new ArrayList<Node>();
        if (block.name().isPresent()) {
            grievanceChildren.add(new NodeBuilder()
                    .description("name")
                    .content(block.name().get())
                    .build());
        }
        var contact = block.contact();
        if (contact.email().isPresent()) {
            grievanceChildren.add(new NodeBuilder()
                    .description("email")
                    .content(contact.email().get())
                    .build());
        }
        if (contact.landlineNumber().isPresent()) {
            grievanceChildren.add(new NodeBuilder()
                    .description("landline_number")
                    .content(contact.landlineNumber().get())
                    .build());
        }
        if (contact.mobileNumber().isPresent()) {
            grievanceChildren.add(new NodeBuilder()
                    .description("mobile_number")
                    .content(contact.mobileNumber().get())
                    .build());
        }
        return new NodeBuilder()
                .description("grievance_officer_details")
                .content(grievanceChildren)
                .build();
    }

    /**
     * Fluent builder for {@link IqSetMerchantComplianceRequest}.
     */
    public static final class Builder {
        /**
         * The registered flag (default {@code false}).
         */
        private boolean registered;

        /**
         * The optional entity name.
         */
        private String entityName;

        /**
         * The optional entity type.
         */
        private String entityType;

        /**
         * The optional custom-type label.
         */
        private String entityTypeCustom;

        /**
         * The optional customer-care triple.
         */
        private IqSetMerchantComplianceContactDetails customerCareDetails;

        /**
         * The optional grievance-officer block.
         */
        private IqSetMerchantComplianceGrievanceOfficerDetails grievanceOfficerDetails;

        /**
         * Package-private — use {@link IqSetMerchantComplianceRequest#builder()}.
         */
        Builder() {
        }

        /**
         * Sets the registered flag.
         *
         * @param registered the flag
         * @return this builder; never {@code null}
         */
        public Builder registered(boolean registered) {
            this.registered = registered;
            return this;
        }

        /**
         * Sets the entity name.
         *
         * @param entityName the name; may be {@code null}
         * @return this builder; never {@code null}
         */
        public Builder entityName(String entityName) {
            this.entityName = entityName;
            return this;
        }

        /**
         * Sets the entity type.
         *
         * @param entityType the type; may be {@code null}
         * @return this builder; never {@code null}
         */
        public Builder entityType(String entityType) {
            this.entityType = entityType;
            return this;
        }

        /**
         * Sets the custom-type label.
         *
         * @param entityTypeCustom the label; may be {@code null}
         * @return this builder; never {@code null}
         */
        public Builder entityTypeCustom(String entityTypeCustom) {
            this.entityTypeCustom = entityTypeCustom;
            return this;
        }

        /**
         * Sets the customer-care triple.
         *
         * @param customerCareDetails the triple; may be {@code null}
         * @return this builder; never {@code null}
         */
        public Builder customerCareDetails(IqSetMerchantComplianceContactDetails customerCareDetails) {
            this.customerCareDetails = customerCareDetails;
            return this;
        }

        /**
         * Sets the grievance-officer block.
         *
         * @param grievanceOfficerDetails the block; may be {@code null}
         * @return this builder; never {@code null}
         */
        public Builder grievanceOfficerDetails(IqSetMerchantComplianceGrievanceOfficerDetails grievanceOfficerDetails) {
            this.grievanceOfficerDetails = grievanceOfficerDetails;
            return this;
        }

        /**
         * Builds a new {@link IqSetMerchantComplianceRequest}.
         *
         * @return the built request; never {@code null}
         */
        public IqSetMerchantComplianceRequest build() {
            return new IqSetMerchantComplianceRequest(registered, entityName, entityType, entityTypeCustom,
                    customerCareDetails, grievanceOfficerDetails);
        }
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (obj == null || obj.getClass() != this.getClass()) {
            return false;
        }
        var that = (IqSetMerchantComplianceRequest) obj;
        return this.registered == that.registered
                && Objects.equals(this.entityName, that.entityName)
                && Objects.equals(this.entityType, that.entityType)
                && Objects.equals(this.entityTypeCustom, that.entityTypeCustom)
                && Objects.equals(this.customerCareDetails, that.customerCareDetails)
                && Objects.equals(this.grievanceOfficerDetails, that.grievanceOfficerDetails);
    }

    @Override
    public int hashCode() {
        return Objects.hash(registered, entityName, entityType, entityTypeCustom,
                customerCareDetails, grievanceOfficerDetails);
    }

    @Override
    public String toString() {
        return "IqSetMerchantComplianceRequest[registered=" + registered
                + ", entityName=" + entityName + ", entityType=" + entityType + ']';
    }
}
