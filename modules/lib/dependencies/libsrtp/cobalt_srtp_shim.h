/*
 * cobalt_srtp_shim.h
 *
 * Portable extern-C facade over libsrtp (cisco/libsrtp v2.8.0) for the Cobalt
 * calls2 hop-by-hop SRTP relay. It re-exposes only the libsrtp surface the
 * relay uses, through PORTABLE SCALAR TYPES ONLY (the fixed-width <stdint.h>
 * integers, size_t, and opaque void*), so the jextract-generated Java binding
 * is identical on every host ABI.
 *
 * Why this shim exists: jextract bakes the host C ABI into the bindings it
 * emits. libsrtp's srtp_policy_t / srtp_crypto_policy_t carry `int` and
 * `unsigned long` members, and `unsigned long` is 64-bit on LP64 (Linux,
 * macOS) but 32-bit on LLP64 (Windows). A binding generated for one ABI
 * (ValueLayout.OfLong vs OfInt for those fields) ClassCastExceptions on the
 * other. By keeping every libsrtp struct entirely C-side and exchanging only
 * fixed-width scalars and opaque handles across the boundary, the generated
 * binding contains no ABI-sensitive layout and is portable as-is.
 *
 * Symbol naming: every exported symbol is prefixed cobalt_srtp_ so it coexists
 * in the combined cobalt-native library with the statically-linked real srtp_*
 * symbols, which these wrappers call internally.
 *
 * Portability rule for this header: it uses ONLY uint8_t/uint32_t/int32_t/
 * size_t/void*. It never names a libsrtp type, and never uses bare `long`,
 * `unsigned long`, or `long double`.
 */

#ifndef COBALT_SRTP_SHIM_H
#define COBALT_SRTP_SHIM_H

#include <stddef.h>
#include <stdint.h>

#ifdef __cplusplus
extern "C" {
#endif

/*
 * SRTP crypto suite selector for cobalt_srtp_create. The values match the
 * ordinals of the Java SrtpCryptoSuite enum, so the Java side passes
 * suite.ordinal() with no lookup table:
 *
 *   0 = AES_CM_128_HMAC_SHA1_80  (libsrtp RTP/RTCP default policy)
 *   1 = AES_CM_128_HMAC_SHA1_32  (short 32-bit authentication tag)
 *
 * Both suites use a 16-byte AES-128 key followed by a 14-byte salt, i.e. a
 * 30-byte master (COBALT_SRTP_MASTER_LEN).
 */
#define COBALT_SRTP_SUITE_AES_CM_128_HMAC_SHA1_80 0
#define COBALT_SRTP_SUITE_AES_CM_128_HMAC_SHA1_32 1

/*
 * Wildcard SSRC direction selector for cobalt_srtp_create. The values match
 * libsrtp's srtp_ssrc_type_t enumerators so the meaning is unambiguous, but the
 * srtp_ssrc_t struct is built C-side:
 *
 *   2 = inbound  (unprotect; libsrtp ssrc_any_inbound)
 *   3 = outbound (protect;   libsrtp ssrc_any_outbound)
 */
#define COBALT_SRTP_DIR_INBOUND  2
#define COBALT_SRTP_DIR_OUTBOUND 3

/*
 * Length, in bytes, of the SRTP master both suites consume: a 16-byte AES-128
 * key immediately followed by a 14-byte salt.
 */
#define COBALT_SRTP_MASTER_LEN 30

/*
 * Maximum number of octets cobalt_srtp_protect / cobalt_srtp_protect_rtcp may
 * append to a packet (libsrtp SRTP_MAX_TRAILER_LEN / SRTP_MAX_SRTCP_TRAILER_LEN
 * worst case). Exposed so the Java side can size its scratch buffer without
 * binding any libsrtp constant.
 */
#define COBALT_SRTP_MAX_TRAILER_LEN 148

/*
 * Status codes returned by the protect/unprotect/create/init entry points.
 * Zero is success; non-zero values forward libsrtp's srtp_err_status_t verbatim
 * so the Java side can report the exact libsrtp failure. These are the codes
 * the relay path can observe.
 */
#define COBALT_SRTP_OK            0
#define COBALT_SRTP_FAIL          1
#define COBALT_SRTP_BAD_PARAM     2
#define COBALT_SRTP_ALLOC_FAIL    3
#define COBALT_SRTP_INIT_FAIL     5
#define COBALT_SRTP_AUTH_FAIL     7
#define COBALT_SRTP_CIPHER_FAIL   8
#define COBALT_SRTP_REPLAY_FAIL   9
#define COBALT_SRTP_REPLAY_OLD    10

/**
 * Initializes the libsrtp library once for the process.
 *
 * Forwards to srtp_init, which sets up libsrtp's global cipher and
 * authentication tables. Must be called before cobalt_srtp_create. Idempotency
 * is the caller's responsibility (the Java side guards a one-time init).
 *
 * @return COBALT_SRTP_OK on success, otherwise the libsrtp status code.
 */
int32_t cobalt_srtp_init(void);

/**
 * De-initializes the libsrtp library.
 *
 * Forwards to srtp_shutdown. No cobalt_srtp_* function may be called after
 * this, save a fresh cobalt_srtp_init.
 *
 * @return COBALT_SRTP_OK on success, otherwise the libsrtp status code.
 */
int32_t cobalt_srtp_shutdown(void);

/**
 * Creates one libsrtp session over a master key for a single wildcard SSRC
 * direction, building the srtp_policy_t and its RTP/RTCP crypto policies C-side
 * from portable scalar arguments.
 *
 * The wrapper zero-initializes an srtp_policy_t, stamps its RTP and RTCP crypto
 * policies from suite (the libsrtp default setters for the 80-bit suite, the
 * srtp_crypto_policy_set_aes_cm_128_hmac_sha1_32 setter for the 32-bit suite),
 * points the policy key at a private copy of the master, sets the wildcard SSRC
 * from ssrcDirection, clears num_master_keys and the next link, then calls
 * srtp_create. The created session is returned through outCtx as an opaque
 * handle. The master is copied into the session, so the caller's key buffer
 * need not outlive this call.
 *
 * @param suite         one of COBALT_SRTP_SUITE_* selecting the crypto suite.
 * @param key           pointer to the SRTP master (key followed by salt).
 * @param keyLen        length of key in bytes; must be COBALT_SRTP_MASTER_LEN.
 * @param ssrcDirection one of COBALT_SRTP_DIR_* selecting the wildcard SSRC.
 * @param outCtx        receives the created session handle on success, or is
 *                      left holding NULL on failure; must not be NULL.
 * @return COBALT_SRTP_OK on success, COBALT_SRTP_BAD_PARAM for an invalid
 *         argument, otherwise the libsrtp srtp_create status code.
 */
int32_t cobalt_srtp_create(int32_t suite,
                           const uint8_t *key,
                           size_t keyLen,
                           int32_t ssrcDirection,
                           void **outCtx);

/**
 * Applies SRTP protection to an RTP packet in place.
 *
 * Forwards to srtp_protect. On entry *len holds the cleartext RTP packet
 * length; on success the packet at buf is replaced by the SRTP packet and *len
 * is updated to its length (grown by up to COBALT_SRTP_MAX_TRAILER_LEN). The
 * caller must provide that much writable room past the packet.
 *
 * @param ctx the session handle from cobalt_srtp_create.
 * @param buf the in-place packet buffer.
 * @param len in/out packet length cell, in bytes.
 * @return COBALT_SRTP_OK on success, otherwise the libsrtp status code.
 */
int32_t cobalt_srtp_protect(void *ctx, uint8_t *buf, int32_t *len);

/**
 * Verifies and removes SRTP protection from an SRTP packet in place.
 *
 * Forwards to srtp_unprotect. On entry *len holds the SRTP packet length; on
 * success the packet at buf is replaced by the RTP packet and *len is updated
 * to its (smaller) length.
 *
 * @param ctx the session handle from cobalt_srtp_create.
 * @param buf the in-place packet buffer.
 * @param len in/out packet length cell, in bytes.
 * @return COBALT_SRTP_OK on success, COBALT_SRTP_AUTH_FAIL on a failed
 *         authentication check, COBALT_SRTP_REPLAY_FAIL on a replay, otherwise
 *         the libsrtp status code.
 */
int32_t cobalt_srtp_unprotect(void *ctx, uint8_t *buf, int32_t *len);

/**
 * Applies SRTCP protection to an RTCP packet in place.
 *
 * Forwards to srtp_protect_rtcp. On entry *len holds the cleartext RTCP packet
 * length; on success the packet at buf is replaced by the SRTCP packet and *len
 * is updated to its length (grown by up to COBALT_SRTP_MAX_TRAILER_LEN).
 *
 * @param ctx the session handle from cobalt_srtp_create.
 * @param buf the in-place packet buffer.
 * @param len in/out packet length cell, in bytes.
 * @return COBALT_SRTP_OK on success, otherwise the libsrtp status code.
 */
int32_t cobalt_srtp_protect_rtcp(void *ctx, uint8_t *buf, int32_t *len);

/**
 * Verifies and removes SRTCP protection from an SRTCP packet in place.
 *
 * Forwards to srtp_unprotect_rtcp. On entry *len holds the SRTCP packet length;
 * on success the packet at buf is replaced by the RTCP packet and *len is
 * updated to its (smaller) length.
 *
 * @param ctx the session handle from cobalt_srtp_create.
 * @param buf the in-place packet buffer.
 * @param len in/out packet length cell, in bytes.
 * @return COBALT_SRTP_OK on success, COBALT_SRTP_AUTH_FAIL on a failed
 *         authentication check, COBALT_SRTP_REPLAY_FAIL on a replay, otherwise
 *         the libsrtp status code.
 */
int32_t cobalt_srtp_unprotect_rtcp(void *ctx, uint8_t *buf, int32_t *len);

/**
 * Deallocates a libsrtp session created by cobalt_srtp_create.
 *
 * Forwards to srtp_dealloc. Must be called at most once per handle. A NULL
 * handle is ignored and reported as success.
 *
 * @param ctx the session handle, or NULL.
 * @return COBALT_SRTP_OK on success, otherwise the libsrtp status code.
 */
int32_t cobalt_srtp_dealloc(void *ctx);

#ifdef __cplusplus
}
#endif

#endif /* COBALT_SRTP_SHIM_H */
