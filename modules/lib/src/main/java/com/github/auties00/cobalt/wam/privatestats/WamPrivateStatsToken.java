package com.github.auties00.cobalt.wam.privatestats;

import java.util.Objects;

/**
 * A successful {@code <sign_credential>} round-trip.
 *
 * @param token        the 32-byte secret nonce that the client kept
 * @param sharedSecret the 64-byte SHA-512 of {@code token ||
 *                     unblindedSignedToken}, used as the upload
 *                     authentication key
 */
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
     * Defensive-copy compact constructor that prevents external
     * mutation of the held byte arrays.
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
