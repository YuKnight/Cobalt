package com.github.auties00.cobalt.node.iq.biz;

import java.util.Objects;
import java.util.Optional;

/**
 * Typed customer-care or grievance-officer contact triple
 * (email, landline number, mobile number) carried inside an
 * {@link IqSetMerchantComplianceRequest}. Every field is independently
 * optional.
 */
public final class IqSetMerchantComplianceContactDetails {
    /**
     * The optional contact email.
     */
    private final String email;

    /**
     * The optional landline phone number.
     */
    private final String landlineNumber;

    /**
     * The optional mobile phone number.
     */
    private final String mobileNumber;

    /**
     * Constructs a triple.
     *
     * @param email          the email; may be {@code null}
     * @param landlineNumber the landline; may be {@code null}
     * @param mobileNumber   the mobile; may be {@code null}
     */
    public IqSetMerchantComplianceContactDetails(String email, String landlineNumber, String mobileNumber) {
        this.email = email;
        this.landlineNumber = landlineNumber;
        this.mobileNumber = mobileNumber;
    }

    /**
     * Returns the email.
     *
     * @return an {@link Optional} carrying the email
     */
    public Optional<String> email() {
        return Optional.ofNullable(email);
    }

    /**
     * Returns the landline.
     *
     * @return an {@link Optional} carrying the landline
     */
    public Optional<String> landlineNumber() {
        return Optional.ofNullable(landlineNumber);
    }

    /**
     * Returns the mobile.
     *
     * @return an {@link Optional} carrying the mobile
     */
    public Optional<String> mobileNumber() {
        return Optional.ofNullable(mobileNumber);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (obj == null || obj.getClass() != this.getClass()) {
            return false;
        }
        var that = (IqSetMerchantComplianceContactDetails) obj;
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
        return "IqSetMerchantComplianceContactDetails[email=" + email
                + ", landlineNumber=" + landlineNumber
                + ", mobileNumber=" + mobileNumber + ']';
    }
}
