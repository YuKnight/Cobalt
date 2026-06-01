package com.github.auties00.cobalt.model.business.ads;

import it.auties.protobuf.annotation.ProtobufMessage;
import it.auties.protobuf.annotation.ProtobufProperty;
import it.auties.protobuf.model.ProtobufType;

import java.util.Objects;
import java.util.Optional;

/**
 * Input model for the email confirmation step of Click-to-WhatsApp ad
 * account onboarding.
 *
 * <p>While onboarding a Click-to-WhatsApp advertising account (an account
 * that funds paid promotions which open a chat with the business when
 * tapped), the merchant supplies a contact email and pastes the
 * verification code they received in their inbox. This input bundles the
 * advertising-account identifier the confirmation is scoped to, the email
 * being verified, and the verification code the merchant pasted from the
 * email.
 *
 * <p>Field shape inferred from WhatsApp Business email-onboarding
 * conventions; additional optional fields will be added as they surface in
 * live captures.
 */
@ProtobufMessage(name = "BusinessAdEmailOnboardingConfirmation")
public final class BusinessAdEmailOnboardingConfirmation {
    /**
     * Advertising-account identifier the email confirmation is scoped to.
     * Required by the onboarding backend; unset omits the field.
     */
    @ProtobufProperty(index = 1, type = ProtobufType.STRING)
    final String adAccountId;

    /**
     * Email address being verified. Carries the contact email the merchant
     * supplied earlier in the onboarding flow so the server can match the
     * code against the right address. Unset omits the field.
     */
    @ProtobufProperty(index = 2, type = ProtobufType.STRING)
    final String email;

    /**
     * Verification code the merchant pasted from the confirmation email
     * the server sent. Required by the onboarding backend; unset omits the
     * field.
     */
    @ProtobufProperty(index = 3, type = ProtobufType.STRING)
    final String verificationCode;

    /**
     * Constructs a new {@code BusinessAdEmailOnboardingConfirmation}. Every
     * argument may be {@code null} to omit the corresponding field from
     * the request.
     *
     * @param adAccountId      the advertising-account identifier, or
     *                         {@code null}
     * @param email            the email address being verified, or
     *                         {@code null}
     * @param verificationCode the verification code from the email, or
     *                         {@code null}
     */
    public BusinessAdEmailOnboardingConfirmation(String adAccountId, String email, String verificationCode) {
        this.adAccountId = adAccountId;
        this.email = email;
        this.verificationCode = verificationCode;
    }

    /**
     * Returns the advertising-account identifier.
     *
     * @return an {@link Optional} carrying the identifier, or empty when
     *         unset
     */
    public Optional<String> adAccountId() {
        return Optional.ofNullable(adAccountId);
    }

    /**
     * Returns the email address being verified.
     *
     * @return an {@link Optional} carrying the email, or empty when unset
     */
    public Optional<String> email() {
        return Optional.ofNullable(email);
    }

    /**
     * Returns the verification code the merchant pasted from the email.
     *
     * @return an {@link Optional} carrying the code, or empty when unset
     */
    public Optional<String> verificationCode() {
        return Optional.ofNullable(verificationCode);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        var that = (BusinessAdEmailOnboardingConfirmation) obj;
        return Objects.equals(adAccountId, that.adAccountId)
                && Objects.equals(email, that.email)
                && Objects.equals(verificationCode, that.verificationCode);
    }

    @Override
    public int hashCode() {
        return Objects.hash(adAccountId, email, verificationCode);
    }

    @Override
    public String toString() {
        return "BusinessAdEmailOnboardingConfirmation[" +
                "adAccountId=" + adAccountId + ", " +
                "email=" + email + ", " +
                "verificationCode=" + verificationCode + ']';
    }
}
