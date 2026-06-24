package com.github.auties00.cobalt.calls2.net.transport;

import com.github.auties00.cobalt.calls2.net.transport.srtp.bindings.CobaltSrtp;

/**
 * Enumerates the two SRTP crypto suites the hop-by-hop relay context selects between, each pairing
 * AES counter-mode 128-bit confidentiality with HMAC-SHA1 authentication and differing only in the
 * authentication-tag length.
 *
 * <p>The relay leg never negotiates a suite over a handshake the way the Web-P2P DTLS-SRTP path does;
 * the suite is chosen locally by {@code fill_hbh_srtp_crypto}, which picks the 80-bit tag for media
 * RTP and the 32-bit tag where the shorter tag is wanted. Both suites use a 16-byte master key and a
 * 14-byte master salt, so the master the key-derivation hands out is {@value #SUITE_MASTER_LENGTH}
 * bytes for either suite; only the trailing authentication tag on the wire differs.
 *
 * <p>Each constant carries the portable suite selector the libsrtp shim consumes: a context passes
 * {@link #selector()} to {@link CobaltSrtp#cobalt_srtp_create(int, java.lang.foreign.MemorySegment,
 * long, int, java.lang.foreign.MemorySegment)}, and the shim builds the matching libsrtp
 * {@code srtp_crypto_policy_t} for both RTP and RTCP C-side. The selector value equals this constant's
 * {@link #ordinal()}, which is the contract the shim header pins
 * ({@code COBALT_SRTP_SUITE_AES_CM_128_HMAC_SHA1_80 == 0},
 * {@code COBALT_SRTP_SUITE_AES_CM_128_HMAC_SHA1_32 == 1}).
 *
 * @implNote This implementation reproduces the suite selection of {@code fill_hbh_srtp_crypto}
 *           (fn4809) in {@code transport/wa_hbh_srtp_relay.cc}, which chooses between
 *           {@code AES_CM_128_HMAC_SHA1_80} and {@code AES_CM_128_HMAC_SHA1_32}. The suite-to-policy
 *           mapping itself lives C-side in {@code cobalt_srtp_shim.cpp}: the combined
 *           {@code cobalt-native} libsrtp exports the 80-bit suite only as the RTP/RTCP default
 *           ({@code srtp_crypto_policy_set_rtp_default}/{@code _rtcp_default}), not as a standalone
 *           {@code _80} setter, so the shim applies the default policy whose cipher, key length, and
 *           tag length are exactly that suite, and applies
 *           {@code srtp_crypto_policy_set_aes_cm_128_hmac_sha1_32} for the 32-bit suite. Hiding that
 *           mapping behind the shim keeps the {@code srtp_crypto_policy_t} struct (whose
 *           {@code unsigned long} sibling fields in {@code srtp_policy_t} are ABI-sensitive) off the
 *           FFM boundary, so the generated binding is host-ABI independent. The
 *           {@value #SUITE_MASTER_LENGTH}-byte master matches a 16-byte AES-128 key plus a 14-byte
 *           salt.
 */
public enum SrtpCryptoSuite {
    /**
     * The AES counter-mode 128-bit cipher with an 80-bit HMAC-SHA1 authentication tag, the suite the
     * relay context uses for media RTP and the libsrtp RTP/RTCP default.
     */
    AES_CM_128_HMAC_SHA1_80,

    /**
     * The AES counter-mode 128-bit cipher with a 32-bit HMAC-SHA1 authentication tag, the suite the
     * relay context uses where the shorter four-byte tag is preferred over the eight-byte tag.
     */
    AES_CM_128_HMAC_SHA1_32;

    /**
     * Holds the length, in bytes, of the master both suites consume: a 16-byte AES-128 key immediately
     * followed by a 14-byte master salt.
     *
     * <p>The key-derivation hands out a master of exactly this length, and a relay context passes it
     * verbatim to the libsrtp shim, which copies it into the libsrtp policy key buffer it builds, so
     * the value is the common contract between the key-derivation and {@link CobaltSrtp}. It equals the
     * shim's {@code COBALT_SRTP_MASTER_LEN}.
     */
    public static final int SUITE_MASTER_LENGTH = 30;

    /**
     * Returns the portable suite selector the libsrtp shim consumes to build this suite's crypto
     * policy.
     *
     * <p>The selector is this constant's {@link #ordinal()}: {@code 0} for
     * {@link #AES_CM_128_HMAC_SHA1_80} and {@code 1} for {@link #AES_CM_128_HMAC_SHA1_32}, matching the
     * {@code COBALT_SRTP_SUITE_*} macros in {@code cobalt_srtp_shim.h}. A relay context hands this to
     * {@link CobaltSrtp#cobalt_srtp_create(int, java.lang.foreign.MemorySegment, long, int,
     * java.lang.foreign.MemorySegment)}.
     *
     * @return the {@code COBALT_SRTP_SUITE_*} selector for this suite
     */
    int selector() {
        return ordinal();
    }
}
