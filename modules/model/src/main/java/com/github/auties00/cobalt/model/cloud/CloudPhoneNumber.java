package com.github.auties00.cobalt.model.cloud;

import java.util.Objects;
import java.util.Optional;

/**
 * The Cloud API view of a WhatsApp Business phone number.
 *
 * <p>Projects the fields of a phone-number node: the server id used to address the messaging and
 * profile edges, the human-readable display number, the verified business name, the messaging quality
 * rating, and the registration and code-verification status.
 */
public final class CloudPhoneNumber {
    /**
     * The phone number id used to address the messaging and profile edges.
     */
    private final String id;

    /**
     * The display phone number in international form, or {@code null} when absent.
     */
    private final String displayPhoneNumber;

    /**
     * The verified business name, or {@code null} when not yet verified.
     */
    private final String verifiedName;

    /**
     * The messaging quality rating ({@code GREEN}, {@code YELLOW}, {@code RED}), or {@code null} when
     * absent.
     */
    private final String qualityRating;

    /**
     * The code-verification status ({@code VERIFIED}, {@code NOT_VERIFIED}), or {@code null} when
     * absent.
     */
    private final String codeVerificationStatus;

    /**
     * The platform registration status ({@code CONNECTED}, {@code PENDING}), or {@code null} when
     * absent.
     */
    private final String status;

    /**
     * Constructs a new phone-number view.
     *
     * @param id                     the phone number id
     * @param displayPhoneNumber     the display number, or {@code null}
     * @param verifiedName           the verified business name, or {@code null}
     * @param qualityRating          the messaging quality rating, or {@code null}
     * @param codeVerificationStatus the code-verification status, or {@code null}
     * @param status                 the platform registration status, or {@code null}
     * @throws NullPointerException if {@code id} is {@code null}
     */
    public CloudPhoneNumber(String id, String displayPhoneNumber, String verifiedName, String qualityRating,
                            String codeVerificationStatus, String status) {
        this.id = Objects.requireNonNull(id, "id must not be null");
        this.displayPhoneNumber = displayPhoneNumber;
        this.verifiedName = verifiedName;
        this.qualityRating = qualityRating;
        this.codeVerificationStatus = codeVerificationStatus;
        this.status = status;
    }

    /**
     * Returns the phone number id.
     *
     * @return the phone number id
     */
    public String id() {
        return id;
    }

    /**
     * Returns the display phone number.
     *
     * @return an {@link Optional} carrying the display number, or empty when absent
     */
    public Optional<String> displayPhoneNumber() {
        return Optional.ofNullable(displayPhoneNumber);
    }

    /**
     * Returns the verified business name.
     *
     * @return an {@link Optional} carrying the verified name, or empty when not yet verified
     */
    public Optional<String> verifiedName() {
        return Optional.ofNullable(verifiedName);
    }

    /**
     * Returns the messaging quality rating.
     *
     * @return an {@link Optional} carrying the quality rating, or empty when absent
     */
    public Optional<String> qualityRating() {
        return Optional.ofNullable(qualityRating);
    }

    /**
     * Returns the code-verification status.
     *
     * @return an {@link Optional} carrying the code-verification status, or empty when absent
     */
    public Optional<String> codeVerificationStatus() {
        return Optional.ofNullable(codeVerificationStatus);
    }

    /**
     * Returns the platform registration status.
     *
     * @return an {@link Optional} carrying the status, or empty when absent
     */
    public Optional<String> status() {
        return Optional.ofNullable(status);
    }
}
