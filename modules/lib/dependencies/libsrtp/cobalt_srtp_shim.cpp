/*
 * cobalt_srtp_shim.cpp
 *
 * Implementation of the portable extern-C libsrtp facade declared in
 * cobalt_srtp_shim.h. Each wrapper builds the libsrtp structs (srtp_policy_t,
 * srtp_crypto_policy_t, srtp_ssrc_t) on the C side from the portable scalar
 * arguments, calls the real srtp_* function, and returns only fixed-width
 * scalars and opaque void* handles. No libsrtp type ever crosses the FFM
 * boundary, so the jextract-generated Java binding is host-ABI independent.
 *
 * Compiled into the combined cobalt-native shared library by
 * .github/scripts/build-natives.sh (build_libsrtp), linked against the static
 * libsrtp2.a; the export union in build_combined forces the cobalt_srtp_*
 * symbols (drawn from generate.sh's --include-function list) into the library's
 * export table.
 */

#include "cobalt_srtp_shim.h"

#include <cstring>

#include <srtp2/srtp.h>

/*
 * Fills an srtp_crypto_policy_t for the given suite. The 80-bit suite is
 * libsrtp's RTP/RTCP default policy; the 32-bit suite uses the short-tag setter.
 * forRtcp selects between the RTP and RTCP default setters for the 80-bit suite
 * (libsrtp documents distinct, though presently identical, defaults); the
 * 32-bit setter is the same for both directions. Returns 0 on an unknown suite.
 */
static int cobalt_srtp_fill_crypto(srtp_crypto_policy_t *p, int32_t suite, int forRtcp) {
    switch (suite) {
        case COBALT_SRTP_SUITE_AES_CM_128_HMAC_SHA1_80:
            if (forRtcp) {
                srtp_crypto_policy_set_rtcp_default(p);
            } else {
                srtp_crypto_policy_set_rtp_default(p);
            }
            return 1;
        case COBALT_SRTP_SUITE_AES_CM_128_HMAC_SHA1_32:
            srtp_crypto_policy_set_aes_cm_128_hmac_sha1_32(p);
            return 1;
        default:
            return 0;
    }
}

extern "C" {

int32_t cobalt_srtp_init(void) {
    return (int32_t) srtp_init();
}

int32_t cobalt_srtp_shutdown(void) {
    return (int32_t) srtp_shutdown();
}

int32_t cobalt_srtp_create(int32_t suite,
                           const uint8_t *key,
                           size_t keyLen,
                           int32_t ssrcDirection,
                           void **outCtx) {
    if (outCtx == NULL) {
        return COBALT_SRTP_BAD_PARAM;
    }
    *outCtx = NULL;
    if (key == NULL || keyLen != COBALT_SRTP_MASTER_LEN) {
        return COBALT_SRTP_BAD_PARAM;
    }
    if (ssrcDirection != COBALT_SRTP_DIR_INBOUND && ssrcDirection != COBALT_SRTP_DIR_OUTBOUND) {
        return COBALT_SRTP_BAD_PARAM;
    }

    // Private copy of the master so the policy key pointer stays valid for the
    // duration of srtp_create regardless of the caller's buffer lifetime.
    unsigned char masterKey[COBALT_SRTP_MASTER_LEN];
    memcpy(masterKey, key, COBALT_SRTP_MASTER_LEN);

    srtp_policy_t policy;
    memset(&policy, 0, sizeof(policy));

    if (!cobalt_srtp_fill_crypto(&policy.rtp, suite, 0) ||
        !cobalt_srtp_fill_crypto(&policy.rtcp, suite, 1)) {
        return COBALT_SRTP_BAD_PARAM;
    }

    policy.ssrc.type = (srtp_ssrc_type_t) ssrcDirection;
    policy.ssrc.value = 0;
    policy.key = masterKey;
    policy.num_master_keys = 0;
    policy.window_size = 0;
    policy.allow_repeat_tx = 0;
    policy.next = NULL;

    srtp_t session = NULL;
    srtp_err_status_t status = srtp_create(&session, &policy);
    if (status != srtp_err_status_ok) {
        return (int32_t) status;
    }
    *outCtx = (void *) session;
    return COBALT_SRTP_OK;
}

int32_t cobalt_srtp_protect(void *ctx, uint8_t *buf, int32_t *len) {
    return (int32_t) srtp_protect((srtp_t) ctx, (void *) buf, (int *) len);
}

int32_t cobalt_srtp_unprotect(void *ctx, uint8_t *buf, int32_t *len) {
    return (int32_t) srtp_unprotect((srtp_t) ctx, (void *) buf, (int *) len);
}

int32_t cobalt_srtp_protect_rtcp(void *ctx, uint8_t *buf, int32_t *len) {
    return (int32_t) srtp_protect_rtcp((srtp_t) ctx, (void *) buf, (int *) len);
}

int32_t cobalt_srtp_unprotect_rtcp(void *ctx, uint8_t *buf, int32_t *len) {
    return (int32_t) srtp_unprotect_rtcp((srtp_t) ctx, (void *) buf, (int *) len);
}

int32_t cobalt_srtp_dealloc(void *ctx) {
    if (ctx == NULL) {
        return COBALT_SRTP_OK;
    }
    return (int32_t) srtp_dealloc((srtp_t) ctx);
}

} // extern "C"
