package com.github.auties00.cobalt.pairing;

import com.github.auties00.libsignal.key.SignalIdentityKeyPair;

/**
 * Carries the intermediate state that the companion retains between
 * sending {@code companion_hello} and receiving {@code primary_hello}.
 *
 * <p>The {@link #linkCodePairingSecret} is the human-visible code shown
 * to the user. The {@link #linkCodePairingWrappedCompanionEphemeralPub}
 * is the wire-format payload, structured as a 32-byte PBKDF2 salt,
 * followed by a 16-byte AES-CTR initial counter, followed by the
 * AES-CTR ciphertext of the companion ephemeral public key. The
 * {@link #companionEphemeralKeyPair} is held back so its private key
 * can be used during companion finish to derive the ephemeral X25519
 * shared secret.
 *
 * @param linkCodePairingSecret                       the eight-character
 *     pairing code displayed to the user
 * @param linkCodePairingWrappedCompanionEphemeralPub the wrapped
 *     companion ephemeral public key bytes carried in the IQ
 * @param companionEphemeralKeyPair                   the companion ADV
 *     ephemeral Curve25519 keypair generated for this attempt
 */
record CompanionPairingCompanionHello(
        String linkCodePairingSecret,
        byte[] linkCodePairingWrappedCompanionEphemeralPub,
        SignalIdentityKeyPair companionEphemeralKeyPair
) {

}
