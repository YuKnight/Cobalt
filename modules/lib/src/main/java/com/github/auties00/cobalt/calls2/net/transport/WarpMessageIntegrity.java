package com.github.auties00.cobalt.calls2.net.transport;

import com.github.auties00.cobalt.calls2.platform.VoipCryptoNative;
import com.github.auties00.cobalt.exception.WhatsAppCallException;

import java.util.Arrays;
import java.util.Objects;

/**
 * Computes and appends the hop-by-hop message-integrity tag a WARP control message carries so the relay
 * or SFU can authenticate it without holding the call's end-to-end keys.
 *
 * <p>A {@link WarpMessage} travels toward the relay either piggybacked on an RTP packet or standalone.
 * When hop-by-hop integrity is enabled, the sender appends an HMAC-SHA256 tag computed over the encoded
 * WARP bytes keyed by the per-relay {@value #WARP_AUTH_KEY_LABEL} key, and the relay verifies that tag
 * with the same key before acting on the control message. The tag is a suffix: it is appended after the
 * complete WARP message, so the bytes authenticated are exactly the {@link WarpMessage#encode()} output,
 * and the transmitted packet is the WARP message followed by the tag.
 *
 * <p>The {@value #WARP_AUTH_KEY_LABEL} key is one output of the call's SFU key-derivation schedule (a
 * two-step chained HKDF-SHA256 over the relay hop-by-hop key, the {@code "warp auth salt"} then
 * {@value #WARP_AUTH_KEY_LABEL} steps, derived by
 * {@link com.github.auties00.cobalt.calls2.core.participant.CallE2eKeyDerivation#deriveWarpAuthKey(byte[])});
 * deriving and caching it per relay is the relay transport's responsibility, and this holder only consumes
 * the already-derived key. The tag length the relay expects is a per-relay parameter; the full HMAC-SHA256
 * output is {@value #FULL_TAG_LENGTH} bytes, and {@link #appendTag(byte[], byte[])} appends the full
 * tag, while {@link #appendTag(byte[], byte[], int)} appends a leading-byte truncation when a relay
 * negotiates a shorter tag.
 *
 * @implNote This implementation reproduces {@code add_hbh_warp_mi_tag} (fn5156) of
 * {@code transport/wa_transport_warp.cc} from the wa-voip WASM module {@code ff-tScznZ8P}
 * (tree fn5156 lines 234-264). The native routine checks the WARP seek-buffer has room for the configured
 * tag length ({@code relay_ctx+0x2cc}, where {@code relay_ctx = *(call+0x55558)}), calls the host crypto
 * callback to HMAC-SHA256 the WARP bytes with the {@value #WARP_AUTH_KEY_LABEL} keyed material at
 * {@code relay_ctx+0x2a4} (key length {@code relay_ctx+0x2c4}, derived by {@code wa_sfu_kdf} under the
 * {@value #WARP_AUTH_KEY_LABEL} info label), appends that many leading tag bytes, and increments a
 * counter. The companion {@code check_and_drop_piggybacked_warp_packet} (fn5161 lines 317-322) reads the
 * same pair: a hop-by-hop MI byte is consumed only when the per-relay enable flag {@code relay_ctx+0x2c8}
 * is set, and then by {@code relay_ctx+0x2cc} bytes, so MI is off entirely when the relay does not enable
 * it. The HMAC is computed through {@link VoipCryptoNative#hmacSha256(byte[], byte[])} (the JCA
 * {@code Mac("HmacSHA256")}) rather than a native binding, the same primitive the native engine reaches
 * through statically-linked BoringSSL. The full {@value #FULL_TAG_LENGTH}-byte HMAC-SHA256 output is the
 * unconditional fallback, and {@link #appendTag(byte[], byte[], int)} carries the per-relay truncation as
 * a caller-supplied parameter.
 * <p>
 * The per-relay {@code relay_ctx+0x2cc} MI tag length is not a compiled-in constant: it is the
 * {@code warp_mi_tag_len} attribute of the {@code <relay>} signaling block ({@code relay_ctx+0x2c8} enable
 * and {@code +0x2cc} length are written only from the relay stanza, never from
 * {@code wa_call_get_default_voip_params_internal}). The media bring-up reads it from the relay block and
 * passes it, with the
 * {@link com.github.auties00.cobalt.calls2.core.participant.CallE2eKeyDerivation#deriveWarpAuthKey(byte[]) chained-derived warp auth key},
 * to {@link LiveRelayTransport}, so MI is enabled exactly when and to the length the relay advertises and
 * {@link #appendTag(byte[], byte[], int)} truncates to that many bytes. No relay block in the captures
 * carries {@code warp_mi_tag_len}
 * (re/calls2-spec/captures/transport.json: {@code relayBlock_attrs.warp_mi_tag_len = "ABSENT in this
 * capture"}), so MI was off in every captured call; the {@value #FULL_TAG_LENGTH}-byte full HMAC-SHA256
 * output (and the shorter truncations) is therefore not yet byte-validated against a live MI-enabled relay
 * datagram, which is a validation gap, not an implementation gap.
 */
public final class WarpMessageIntegrity {
    /**
     * Holds the HKDF info label naming the hop-by-hop WARP authentication key in the SFU key schedule.
     */
    public static final String WARP_AUTH_KEY_LABEL = "warp auth key";

    /**
     * Holds the length, in bytes, of the full HMAC-SHA256 tag, the default hop-by-hop WARP integrity
     * tag length.
     */
    public static final int FULL_TAG_LENGTH = 32;

    /**
     * Prevents instantiation of this stateless integrity-primitive holder.
     */
    private WarpMessageIntegrity() {
        throw new AssertionError("WarpMessageIntegrity is not instantiable");
    }

    /**
     * Computes the full HMAC-SHA256 hop-by-hop integrity tag over a WARP message.
     *
     * <p>The {@code warpBytes} are the encoded WARP message exactly as it will be transmitted before the
     * tag is appended, that is the {@link WarpMessage#encode()} output. The tag is keyed by the
     * {@value #WARP_AUTH_KEY_LABEL} key.
     *
     * @param warpBytes   the encoded WARP message bytes the tag authenticates
     * @param warpAuthKey the derived {@value #WARP_AUTH_KEY_LABEL} key, in raw bytes
     * @return the {@value #FULL_TAG_LENGTH}-byte HMAC-SHA256 tag
     * @throws NullPointerException       if {@code warpBytes} or {@code warpAuthKey} is {@code null}
     * @throws WhatsAppCallException.Srtp if the platform cannot compute HMAC-SHA256
     */
    public static byte[] computeTag(byte[] warpBytes, byte[] warpAuthKey) {
        Objects.requireNonNull(warpBytes, "warpBytes cannot be null");
        Objects.requireNonNull(warpAuthKey, "warpAuthKey cannot be null");
        return VoipCryptoNative.hmacSha256(warpAuthKey, warpBytes);
    }

    /**
     * Appends the full HMAC-SHA256 hop-by-hop integrity tag to an encoded WARP message.
     *
     * <p>The returned buffer is the WARP message followed by the {@value #FULL_TAG_LENGTH}-byte tag
     * computed over the message; the input array is not modified.
     *
     * @param warpBytes   the encoded WARP message bytes
     * @param warpAuthKey the derived {@value #WARP_AUTH_KEY_LABEL} key, in raw bytes
     * @return a new buffer holding the WARP message followed by the integrity tag
     * @throws NullPointerException       if {@code warpBytes} or {@code warpAuthKey} is {@code null}
     * @throws WhatsAppCallException.Srtp if the platform cannot compute HMAC-SHA256
     */
    public static byte[] appendTag(byte[] warpBytes, byte[] warpAuthKey) {
        return appendTag(warpBytes, warpAuthKey, FULL_TAG_LENGTH);
    }

    /**
     * Appends a possibly-truncated HMAC-SHA256 hop-by-hop integrity tag to an encoded WARP message.
     *
     * <p>The tag is the leading {@code tagLength} bytes of the HMAC-SHA256 of the WARP message; a relay
     * that negotiates a shorter tag length receives that many leading tag bytes. The returned buffer is
     * the WARP message followed by the truncated tag; the input array is not modified.
     *
     * @param warpBytes   the encoded WARP message bytes
     * @param warpAuthKey the derived {@value #WARP_AUTH_KEY_LABEL} key, in raw bytes
     * @param tagLength   the number of leading HMAC bytes to append, in {@code 1..}{@value #FULL_TAG_LENGTH}
     * @return a new buffer holding the WARP message followed by the truncated integrity tag
     * @throws NullPointerException       if {@code warpBytes} or {@code warpAuthKey} is {@code null}
     * @throws IllegalArgumentException   if {@code tagLength} is not in {@code 1..}{@value #FULL_TAG_LENGTH}
     * @throws WhatsAppCallException.Srtp if the platform cannot compute HMAC-SHA256
     */
    public static byte[] appendTag(byte[] warpBytes, byte[] warpAuthKey, int tagLength) {
        Objects.requireNonNull(warpBytes, "warpBytes cannot be null");
        Objects.requireNonNull(warpAuthKey, "warpAuthKey cannot be null");
        requireValidTagLength(tagLength);
        var tag = VoipCryptoNative.hmacSha256(warpAuthKey, warpBytes);
        var out = Arrays.copyOf(warpBytes, warpBytes.length + tagLength);
        System.arraycopy(tag, 0, out, warpBytes.length, tagLength);
        return out;
    }

    /**
     * Verifies that an inbound WARP packet's trailing hop-by-hop integrity tag matches the expected
     * HMAC-SHA256 under the {@value #WARP_AUTH_KEY_LABEL} key.
     *
     * <p>The {@code taggedWarpBytes} are the WARP message followed by a {@code tagLength}-byte tag. The
     * expected tag is recomputed over the WARP message prefix (the bytes before the tag) and compared in
     * constant time against the carried tag.
     *
     * @param taggedWarpBytes the WARP message followed by its integrity tag
     * @param warpAuthKey     the derived {@value #WARP_AUTH_KEY_LABEL} key, in raw bytes
     * @param tagLength       the length, in bytes, of the trailing tag, in
     *                        {@code 1..}{@value #FULL_TAG_LENGTH}
     * @return {@code true} if the carried tag equals the leading {@code tagLength} bytes of the
     *         recomputed HMAC, {@code false} otherwise
     * @throws NullPointerException       if {@code taggedWarpBytes} or {@code warpAuthKey} is {@code null}
     * @throws IllegalArgumentException   if {@code tagLength} is not in {@code 1..}{@value #FULL_TAG_LENGTH}
     *                                    or exceeds the packet length
     * @throws WhatsAppCallException.Srtp if the platform cannot compute HMAC-SHA256
     */
    public static boolean verifyTag(byte[] taggedWarpBytes, byte[] warpAuthKey, int tagLength) {
        Objects.requireNonNull(taggedWarpBytes, "taggedWarpBytes cannot be null");
        Objects.requireNonNull(warpAuthKey, "warpAuthKey cannot be null");
        requireValidTagLength(tagLength);
        if (tagLength >= taggedWarpBytes.length) {
            throw new IllegalArgumentException("tagLength " + tagLength
                    + " leaves no WARP message in a " + taggedWarpBytes.length + "-byte packet");
        }
        var prefixLength = taggedWarpBytes.length - tagLength;
        var prefix = Arrays.copyOf(taggedWarpBytes, prefixLength);
        var expected = VoipCryptoNative.hmacSha256(warpAuthKey, prefix);
        var expectedTruncated = Arrays.copyOf(expected, tagLength);
        var actual = Arrays.copyOfRange(taggedWarpBytes, prefixLength, taggedWarpBytes.length);
        return java.security.MessageDigest.isEqual(expectedTruncated, actual);
    }

    /**
     * Validates that a hop-by-hop WARP integrity tag length is within the HMAC-SHA256 output bound.
     *
     * @param tagLength the requested tag length, in bytes
     * @throws IllegalArgumentException if {@code tagLength} is not in {@code 1..}{@value #FULL_TAG_LENGTH}
     */
    private static void requireValidTagLength(int tagLength) {
        if (tagLength < 1 || tagLength > FULL_TAG_LENGTH) {
            throw new IllegalArgumentException("WARP MI tag length must be in 1.." + FULL_TAG_LENGTH
                    + ", got " + tagLength);
        }
    }
}
