package com.github.auties00.cobalt.wam.privatestats;

import com.github.auties00.cobalt.meta.annotation.WhatsAppWebModule;

import java.util.Objects;

/**
 * Outcome of a successful {@code <sign_credential>} IQ round-trip,
 * carrying the secret token nonce and the derived upload-time shared
 * secret.
 *
 * @param token        the 32-byte secret nonce that the client kept
 * @param sharedSecret the 64-byte SHA-512 of
 *                     {@code token || unblindedSignedToken}, used as
 *                     the upload authentication key
 */
@WhatsAppWebModule(moduleName = "WAWebIssuePrivateStatsToken")
public record WamPrivateStatsToken(byte[] token, byte[] sharedSecret) {
    /**
     * Length in bytes of the secret token nonce.
     */
    public static final int TOKEN_BYTES = 32;

    /**
     * Length in bytes of the derived shared secret (SHA-512 output).
     */
    public static final int SHARED_SECRET_BYTES = 64;

    /**
     * Validates the lengths of {@code token} and {@code sharedSecret}
     * and clones each array so external callers cannot mutate the
     * stored state.
     *
     * @throws NullPointerException     if either argument is
     *                                  {@code null}
     * @throws IllegalArgumentException if {@code token} is not
     *                                  {@value #TOKEN_BYTES} bytes or
     *                                  {@code sharedSecret} is not
     *                                  {@value #SHARED_SECRET_BYTES}
     *                                  bytes
     */
    public WamPrivateStatsToken {
        Objects.requireNonNull(token, "token must not be null");
        Objects.requireNonNull(sharedSecret, "sharedSecret must not be null");
        if (token.length != TOKEN_BYTES) {
            throw new IllegalArgumentException(
                    "token must be " + TOKEN_BYTES + " bytes, was " + token.length);
        }
        if (sharedSecret.length != SHARED_SECRET_BYTES) {
            throw new IllegalArgumentException(
                    "sharedSecret must be " + SHARED_SECRET_BYTES + " bytes, was " + sharedSecret.length);
        }
        token = token.clone();
        sharedSecret = sharedSecret.clone();
    }

    /**
     * Returns a fresh copy of the secret token.
     *
     * @return the token bytes
     */
    @Override
    public byte[] token() {
        return token.clone();
    }

    /**
     * Returns a fresh copy of the shared secret.
     *
     * @return the shared-secret bytes
     */
    @Override
    public byte[] sharedSecret() {
        return sharedSecret.clone();
    }
}
