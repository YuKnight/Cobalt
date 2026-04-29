package com.github.auties00.cobalt.exception;

/**
 * Exception thrown when the private-stats token issuance flow fails.
 *
 * <p>The flow is the {@code <sign_credential>} IQ round-trip against
 * {@code s.whatsapp.net} that produces the single-use authentication token
 * used by the WAM private-stats upload backend (mirrors
 * {@code WAWebIssuePrivateStatsToken.getToken}). It can fail for several
 * reasons:
 *
 * <ul>
 *   <li><b>Server error:</b> the IQ response carries a {@code type="error"}
 *       attribute, indicating the server rejected the request (transient
 *       overload, missing capability gate, or banned account).</li>
 *   <li><b>Malformed response:</b> a required child element
 *       ({@code <signed_credential>}, {@code <acs_public_key>}) is missing
 *       or carries content of the wrong length.</li>
 *   <li><b>Invalid credential:</b> the {@code <signed_credential>} or
 *       {@code <acs_public_key>} fails to decode as a valid Ed25519 point,
 *       so the unblinding step cannot proceed.</li>
 * </ul>
 *
 * <h2>Fatality</h2>
 * Token-issuance failures are non-fatal. They affect only the specific
 * upload buffer that needed authentication; the client session remains
 * usable and a subsequent retry may succeed against the same server.
 */
public final class WhatsAppPrivateStatsTokenIssuerException extends WhatsAppException {

    /**
     * Constructs a new token-issuance exception with the specified detail
     * message.
     *
     * @param message the detail message describing the failure
     */
    public WhatsAppPrivateStatsTokenIssuerException(String message) {
        super(message);
    }

    /**
     * Constructs a new token-issuance exception with the specified detail
     * message and cause.
     *
     * @param message the detail message describing the failure
     * @param cause   the underlying cause
     */
    public WhatsAppPrivateStatsTokenIssuerException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * Returns whether this exception represents a fatal error.
     *
     * <p>Token-issuance failures are non-fatal: the client session remains
     * usable and a retry may succeed.
     *
     * @return {@code false}
     */
    @Override
    public boolean isFatal() {
        return false;
    }
}
