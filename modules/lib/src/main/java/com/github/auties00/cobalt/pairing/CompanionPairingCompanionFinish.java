package com.github.auties00.cobalt.pairing;

/**
 * Carries the output of the companion finish algorithm.
 *
 * <p>The {@link #linkCodePairingWrappedKeyBundle} is the wire-format
 * payload that ships in the {@code companion_finish} IQ, structured as
 * a 32-byte HKDF salt, followed by a 12-byte AES-GCM IV, followed by the
 * AES-GCM ciphertext of the identity bundle. The {@link #advSecret} is
 * the 32-byte HKDF-derived ADV master secret that must be persisted as
 * the store's {@code advSecretKey} once the handshake is acknowledged.
 * The {@link #companionIdentityPublic} is echoed into the IQ alongside
 * the bundle.
 *
 * @param linkCodePairingWrappedKeyBundle the wrapped identity bundle
 *     bytes carried in the IQ
 * @param advSecret                       the HKDF-derived 32-byte ADV
 *     master secret
 * @param companionIdentityPublic         the companion's long-term
 *     identity public key
 */
record CompanionPairingCompanionFinish(
        byte[] linkCodePairingWrappedKeyBundle,
        byte[] advSecret,
        byte[] companionIdentityPublic
) {

}
