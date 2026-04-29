package com.github.auties00.cobalt.node.iq.biz;

import java.util.Objects;
import java.util.Optional;

/**
 * The grievance-officer block — a {@link IqSetMerchantComplianceContactDetails} plus an
 * optional name field.
 */
public final class IqSetMerchantComplianceGrievanceOfficerDetails {
    /**
     * The optional officer name.
     */
    private final String name;

    /**
     * The contact triple.
     */
    private final IqSetMerchantComplianceContactDetails contact;

    /**
     * Constructs a block.
     *
     * @param name    the name; may be {@code null}
     * @param contact the contact triple; never {@code null}
     * @throws NullPointerException if {@code contact} is {@code null}
     */
    public IqSetMerchantComplianceGrievanceOfficerDetails(String name, IqSetMerchantComplianceContactDetails contact) {
        this.name = name;
        this.contact = Objects.requireNonNull(contact, "contact cannot be null");
    }

    /**
     * Returns the name.
     *
     * @return an {@link Optional} carrying the name
     */
    public Optional<String> name() {
        return Optional.ofNullable(name);
    }

    /**
     * Returns the contact triple.
     *
     * @return the triple; never {@code null}
     */
    public IqSetMerchantComplianceContactDetails contact() {
        return contact;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (obj == null || obj.getClass() != this.getClass()) {
            return false;
        }
        var that = (IqSetMerchantComplianceGrievanceOfficerDetails) obj;
        return Objects.equals(this.name, that.name)
                && Objects.equals(this.contact, that.contact);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, contact);
    }

    @Override
    public String toString() {
        return "IqSetMerchantComplianceGrievanceOfficerDetails[name=" + name
                + ", contact=" + contact + ']';
    }
}
