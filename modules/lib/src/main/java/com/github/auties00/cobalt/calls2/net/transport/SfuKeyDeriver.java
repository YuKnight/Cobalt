package com.github.auties00.cobalt.calls2.net.transport;

import com.github.auties00.cobalt.calls2.platform.VoipCryptoNative;

import java.nio.charset.StandardCharsets;
import java.util.Objects;

/**
 * Derives the per-domain transport keys of a call from a base secret using the WhatsApp SFU key
 * derivation, an HKDF-SHA256 expansion keyed by a fixed per-domain ASCII info label.
 *
 * <p>The wa-voip key-derivation function expands one base secret into several thirty-two-byte keys,
 * each selected by a domain label. Most of those keys (the hop-by-hop SRTP/SRTCP masters and the WARP
 * authentication key) are NOT a flat single-step expansion: they are a two-step chained HKDF over a
 * split of the relay hop-by-hop key, derived by {@code CallE2eKeyDerivation}; the end-to-end SFrame key
 * is derived by the SFrame key schedule. Those are deliberately absent here. This holder carries only the
 * Web-P2P {@link Domain#CERT_FINGERPRINT_HMAC data-channel certificate-fingerprint HMAC} label.
 *
 * <p>The {@link Domain#CERT_FINGERPRINT_HMAC data-channel certificate-fingerprint HMAC} domain the
 * Web-P2P path uses to bind its DTLS certificate is itself NOT a key type of the {@code wa_sfu_kdf} table:
 * it splits the raw end-to-end key material into a sixteen-byte salt and the remaining bytes as input
 * keying material, so it is NOT byte-correct under {@link #derive(byte[], Domain)} (which applies the zero
 * salt). Its label is carried here for the eventual Web-P2P consumer, which must perform the salted
 * derivation rather than calling {@link #derive(byte[], Domain)}.
 *
 * <p>The {@link #derive(byte[], Domain)} derivation is a single RFC 5869 HKDF-SHA256 computation
 * through {@link VoipCryptoNative}: the base secret is the input keying material, the salt is the RFC
 * no-salt default (a thirty-two-byte zero salt), the info is the domain's ASCII label, and the output
 * length is {@value #DERIVED_KEY_LENGTH} bytes. The class is stateless and thread-safe.
 *
 * @implNote This implementation reproduces the single-step form of {@code wa_sfu_kdf} (fn4829) from the
 *           wa-voip WASM module {@code ff-tScznZ8P} ({@code transport/wa_sfu_kdf.cc}), which produces a
 *           {@code 0x20}-byte key by invoking the host HKDF-SHA256 callback with a per-type info label
 *           drawn from the {@code DAT_f0b0c} pointer table. The {@code 'warp auth'}, {@code 'hbh srtp'},
 *           {@code 'hbh srtcp'}, {@code 'uplink hbh srtcp'}, and {@code 'downlink hbh srtcp'} groups of
 *           that table are each a {@code {salt_label, key_label}} pair driving the two-step chain
 *           {@code derive_hbh_srtp_key} (fn4808) runs, NOT this flat helper, so they live on
 *           {@code CallE2eKeyDerivation} (the WARP authentication key being
 *           {@code CallE2eKeyDerivation.deriveWarpAuthKey}, which {@link WarpMessageIntegrity} consumes).
 *           The {@link Domain#CERT_FINGERPRINT_HMAC} label is not in that table either; it is the info
 *           string of the dedicated derivation in {@code system/src/messages/call_signaling_sender.cc}
 *           (fn10891), which calls the HKDF callback with a sixteen-byte salt and the remaining raw
 *           end-to-end bytes as input keying material, so the zero-salt {@link #derive(byte[], Domain)}
 *           does not reproduce it.
 */
public final class SfuKeyDeriver {
    /**
     * The output length, in bytes, of every key this deriver produces.
     */
    public static final int DERIVED_KEY_LENGTH = 32;

    /**
     * The RFC 5869 no-salt default applied to every derivation: a thirty-two-byte all-zero salt.
     */
    private static final byte[] ZERO_SALT = new byte[32];

    /**
     * Enumerates the transport-layer key-derivation domains, each bound to its fixed ASCII info label.
     *
     * @implNote This implementation lists only the {@link #CERT_FINGERPRINT_HMAC} label, which belongs to
     *           the separate salted derivation in {@code call_signaling_sender.cc} (fn10891) rather than to
     *           the {@code wa_sfu_kdf} table. The hop-by-hop SRTP/SRTCP masters, the WARP authentication
     *           key, and the end-to-end SFrame key of the same native function are two-step chained or
     *           SFrame-scheduled derivations carried on {@code CallE2eKeyDerivation}, not flat single-step
     *           expansions of this helper.
     */
    public enum Domain {
        /**
         * The data-channel certificate-fingerprint HMAC domain, label
         * {@code "data-channel cert fingerprint hmac"}.
         *
         * <p>The key it derives binds the Web-P2P DTLS certificate fingerprint, so a peer can verify the
         * fingerprint it learned through signaling against the one presented in the DTLS handshake. The
         * label is the verbatim info string of that derivation, recovered from the {@code ff-tScznZ8P}
         * data segment at {@code 0xbcf7a} (info length {@code 0x22}); the derivation itself (fn10891)
         * uses a sixteen-byte salt taken from the raw end-to-end key material and the remaining bytes as
         * input keying material, so it cannot be produced by the zero-salt {@link #derive(byte[],
         * Domain)} helper and a future Web-P2P consumer must perform the salted HKDF explicitly.
         */
        CERT_FINGERPRINT_HMAC("data-channel cert fingerprint hmac");

        /**
         * Holds the fixed ASCII info label of this domain.
         */
        private final byte[] label;

        /**
         * Constructs a domain bound to its ASCII info label.
         *
         * @param label the ASCII info label, used verbatim as the HKDF info
         */
        Domain(String label) {
            this.label = label.getBytes(StandardCharsets.US_ASCII);
        }

        /**
         * Returns a copy of this domain's ASCII info label bytes.
         *
         * @return the HKDF info bytes for this domain
         */
        byte[] info() {
            return label.clone();
        }
    }

    /**
     * Prevents instantiation of this stateless derivation holder.
     */
    private SfuKeyDeriver() {
        throw new AssertionError("SfuKeyDeriver is not instantiable");
    }

    /**
     * Derives the thirty-two-byte key for a domain from a base secret.
     *
     * <p>The derivation is {@code HKDF-SHA256(ikm = baseSecret, salt = 32 zero bytes, info = domain
     * label, L = 32)}.
     *
     * {@snippet :
     *   key = HKDF-SHA256(baseSecret, new byte[32], domain.info(), 32)
     * }
     *
     * @param baseSecret the input keying material the domain key is expanded from
     * @param domain     the key-derivation domain selecting the info label
     * @return the {@value #DERIVED_KEY_LENGTH}-byte derived key
     * @throws NullPointerException if {@code baseSecret} or {@code domain} is {@code null}
     */
    public static byte[] derive(byte[] baseSecret, Domain domain) {
        Objects.requireNonNull(baseSecret, "baseSecret cannot be null");
        Objects.requireNonNull(domain, "domain cannot be null");
        return VoipCryptoNative.hkdfSha256(baseSecret, ZERO_SALT, domain.info(), DERIVED_KEY_LENGTH);
    }
}
