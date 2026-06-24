/*
 * cobalt_sctp_shim.c
 *
 * Implementation of the portable extern-C usrsctp facade declared in
 * cobalt_sctp_shim.h. Each wrapper builds the usrsctp and socket structs
 * (sockaddr_conn, sctp_sndinfo, sctp_sendv_spa, sctp_initmsg, sctp_event) on the
 * C side from the portable scalar arguments, calls the real usrsctp_* functions,
 * and returns only fixed-width scalars and opaque void* handles. The per-socket
 * receive callback usrsctp invokes with by-value union/struct arguments is
 * absorbed by a static C trampoline that reads the metadata C-side and calls a
 * portable scalar Java callback. No usrsctp or socket type ever crosses the FFM
 * boundary, so the jextract-generated Java binding is host-ABI independent.
 *
 * Compiled into the combined cobalt-native shared library by
 * .github/scripts/build-natives.sh (build_usrsctp), linked against the static
 * libusrsctp.a; build_combined forces the cobalt_sctp_* symbols (drawn from
 * generate.sh's --include-function list) into the library's export table.
 */

#include "cobalt_sctp_shim.h"

#include <stdlib.h>
#include <string.h>

#include <usrsctp.h>

#if !defined(_WIN32)
#include <arpa/inet.h>
#endif

/* Per-socket context: the usrsctp socket plus the Java receive callback. */
typedef struct {
    struct socket *sock;
    void *javaRecvCb;
    void *javaUlpInfo;
} cobalt_sctp_socket;

/* Concrete types of the Java upcall stubs passed across the boundary as void*. */
typedef int (*cobalt_sctp_conn_output_fn)(void *addr, void *buffer, size_t length,
                                          uint8_t tos, uint8_t set_df);
typedef int (*cobalt_sctp_recv_fn)(void *data, size_t datalen, int32_t streamId,
                                   int32_t ppid, int32_t flags, int32_t isNotification,
                                   void *ulpInfo);

/*
 * The single static receive callback registered with every usrsctp socket. It
 * absorbs the by-value union sctp_sockstore and struct sctp_rcvinfo (the
 * ABI-sensitive arguments), reads the stream id and PPID C-side (PPID converted
 * to host byte order), and forwards portable scalars to the per-socket Java
 * callback. The data buffer is not freed here, matching the prior raw-binding
 * behavior (the Java callback copies it out synchronously).
 */
static int cobalt_sctp_trampoline(struct socket *sock, union sctp_sockstore addr,
                                  void *data, size_t datalen, struct sctp_rcvinfo rcvinfo,
                                  int flags, void *ulp_info) {
    cobalt_sctp_socket *ctx = (cobalt_sctp_socket *) ulp_info;
    int isNotification;
    int32_t streamId;
    int32_t ppid;
    (void) sock;
    (void) addr;
    if (ctx == NULL || ctx->javaRecvCb == NULL) {
        return 0;
    }
    isNotification = (flags & MSG_NOTIFICATION) ? 1 : 0;
    streamId = (int32_t) rcvinfo.rcv_sid;
    ppid = (int32_t) ntohl(rcvinfo.rcv_ppid);
    return ((cobalt_sctp_recv_fn) ctx->javaRecvCb)(data, datalen, streamId, ppid,
                                                   (int32_t) flags, isNotification,
                                                   ctx->javaUlpInfo);
}

/* Fills a sockaddr_conn C-side, handling the BSD/macOS sconn_len byte. */
static void cobalt_sctp_fill_sconn(struct sockaddr_conn *sconn, int32_t port, void *connId) {
    memset(sconn, 0, sizeof(*sconn));
#if defined(__APPLE__) || defined(__Bitrig__) || defined(__DragonFly__) || \
    defined(__FreeBSD__) || defined(__OpenBSD__) || defined(__NetBSD__)
    sconn->sconn_len = sizeof(struct sockaddr_conn);
#endif
    sconn->sconn_family = AF_CONN;
    sconn->sconn_port = htons((uint16_t) port);
    sconn->sconn_addr = connId;
}

int32_t cobalt_sctp_global_init(int32_t port, void *connOutputCb) {
    if (connOutputCb == NULL) {
        return COBALT_SCTP_BAD_PARAM;
    }
    usrsctp_init_nothreads((uint16_t) port, (cobalt_sctp_conn_output_fn) connOutputCb, NULL);
    return COBALT_SCTP_OK;
}

int32_t cobalt_sctp_register_address(void *connId) {
    if (connId == NULL) {
        return COBALT_SCTP_BAD_PARAM;
    }
    usrsctp_register_address(connId);
    return COBALT_SCTP_OK;
}

int32_t cobalt_sctp_deregister_address(void *connId) {
    if (connId != NULL) {
        usrsctp_deregister_address(connId);
    }
    return COBALT_SCTP_OK;
}

int32_t cobalt_sctp_handle_timers(int32_t elapsedMs) {
    usrsctp_handle_timers((uint32_t) elapsedMs);
    return COBALT_SCTP_OK;
}

int32_t cobalt_sctp_socket_create(void *receiveCb, void *ulpInfo, void **outSock) {
    cobalt_sctp_socket *ctx;
    struct socket *so;
    if (receiveCb == NULL || outSock == NULL) {
        return COBALT_SCTP_BAD_PARAM;
    }
    *outSock = NULL;
    ctx = (cobalt_sctp_socket *) calloc(1, sizeof(*ctx));
    if (ctx == NULL) {
        return COBALT_SCTP_NOMEM;
    }
    ctx->javaRecvCb = receiveCb;
    ctx->javaUlpInfo = ulpInfo;
    so = usrsctp_socket(AF_CONN, SOCK_STREAM, IPPROTO_SCTP, cobalt_sctp_trampoline,
                        NULL, 0, ctx);
    if (so == NULL) {
        free(ctx);
        return COBALT_SCTP_FAIL;
    }
    ctx->sock = so;
    *outSock = ctx;
    return COBALT_SCTP_OK;
}

int32_t cobalt_sctp_bind(void *sock, int32_t port, void *connId) {
    cobalt_sctp_socket *ctx = (cobalt_sctp_socket *) sock;
    struct sockaddr_conn sconn;
    if (ctx == NULL) {
        return COBALT_SCTP_BAD_PARAM;
    }
    cobalt_sctp_fill_sconn(&sconn, port, connId);
    if (usrsctp_bind(ctx->sock, (struct sockaddr *) &sconn, (socklen_t) sizeof(sconn)) < 0) {
        return COBALT_SCTP_FAIL;
    }
    return COBALT_SCTP_OK;
}

int32_t cobalt_sctp_connect(void *sock, int32_t port, void *connId) {
    cobalt_sctp_socket *ctx = (cobalt_sctp_socket *) sock;
    struct sockaddr_conn sconn;
    if (ctx == NULL) {
        return COBALT_SCTP_BAD_PARAM;
    }
    cobalt_sctp_fill_sconn(&sconn, port, connId);
    if (usrsctp_connect(ctx->sock, (struct sockaddr *) &sconn, (socklen_t) sizeof(sconn)) == 0) {
        return COBALT_SCTP_OK;
    }
    /* A non-blocking socket returns -1/EINPROGRESS for the normal case; the
     * association-change notification signals completion or failure later. */
    return COBALT_SCTP_IN_PROGRESS;
}

int32_t cobalt_sctp_conninput(void *connId, const uint8_t *data, size_t len) {
    if (connId == NULL) {
        return COBALT_SCTP_BAD_PARAM;
    }
    usrsctp_conninput(connId, data, len, 0);
    return COBALT_SCTP_OK;
}

int64_t cobalt_sctp_send(void *sock, const uint8_t *data, size_t len,
                         int32_t streamId, int32_t ppid, int32_t unordered) {
    cobalt_sctp_socket *ctx = (cobalt_sctp_socket *) sock;
    struct sctp_sndinfo sndinfo;
    if (ctx == NULL) {
        return COBALT_SCTP_BAD_PARAM;
    }
    memset(&sndinfo, 0, sizeof(sndinfo));
    sndinfo.snd_sid = (uint16_t) streamId;
    sndinfo.snd_flags = unordered ? SCTP_UNORDERED : 0;
    sndinfo.snd_ppid = htonl((uint32_t) ppid);
    return (int64_t) usrsctp_sendv(ctx->sock, data, len, NULL, 0,
                                   &sndinfo, (socklen_t) sizeof(sndinfo),
                                   SCTP_SENDV_SNDINFO, 0);
}

int64_t cobalt_sctp_send_pr(void *sock, const uint8_t *data, size_t len,
                            int32_t streamId, int32_t ppid, int32_t unordered,
                            int32_t prPolicy, int32_t prValue) {
    cobalt_sctp_socket *ctx = (cobalt_sctp_socket *) sock;
    struct sctp_sendv_spa spa;
    uint16_t policy;
    if (ctx == NULL) {
        return COBALT_SCTP_BAD_PARAM;
    }
    switch (prPolicy) {
        case COBALT_SCTP_PR_TTL: policy = SCTP_PR_SCTP_TTL; break;
        case COBALT_SCTP_PR_RTX: policy = SCTP_PR_SCTP_RTX; break;
        default:                 policy = SCTP_PR_SCTP_NONE; break;
    }
    memset(&spa, 0, sizeof(spa));
    spa.sendv_flags = SCTP_SEND_SNDINFO_VALID | SCTP_SEND_PRINFO_VALID;
    spa.sendv_sndinfo.snd_sid = (uint16_t) streamId;
    spa.sendv_sndinfo.snd_flags = unordered ? SCTP_UNORDERED : 0;
    spa.sendv_sndinfo.snd_ppid = htonl((uint32_t) ppid);
    spa.sendv_prinfo.pr_policy = policy;
    spa.sendv_prinfo.pr_value = (uint32_t) prValue;
    return (int64_t) usrsctp_sendv(ctx->sock, data, len, NULL, 0,
                                   &spa, (socklen_t) sizeof(spa),
                                   SCTP_SENDV_SPA, 0);
}

/* Applies one integer-valued SCTP-level socket option C-side. */
static int32_t cobalt_sctp_set_int_opt(cobalt_sctp_socket *ctx, int option, int value) {
    int v = value;
    if (usrsctp_setsockopt(ctx->sock, IPPROTO_SCTP, option, &v, (socklen_t) sizeof(v)) < 0) {
        return COBALT_SCTP_FAIL;
    }
    return COBALT_SCTP_OK;
}

int32_t cobalt_sctp_set_nodelay(void *sock, int32_t on) {
    cobalt_sctp_socket *ctx = (cobalt_sctp_socket *) sock;
    if (ctx == NULL) {
        return COBALT_SCTP_BAD_PARAM;
    }
    return cobalt_sctp_set_int_opt(ctx, SCTP_NODELAY, on);
}

int32_t cobalt_sctp_set_disable_fragments(void *sock, int32_t on) {
    cobalt_sctp_socket *ctx = (cobalt_sctp_socket *) sock;
    if (ctx == NULL) {
        return COBALT_SCTP_BAD_PARAM;
    }
    return cobalt_sctp_set_int_opt(ctx, SCTP_DISABLE_FRAGMENTS, on);
}

int32_t cobalt_sctp_set_initmsg(void *sock, int32_t numOstreams, int32_t maxInstreams,
                                int32_t maxAttempts, int32_t maxInitTimeo) {
    cobalt_sctp_socket *ctx = (cobalt_sctp_socket *) sock;
    struct sctp_initmsg initmsg;
    if (ctx == NULL) {
        return COBALT_SCTP_BAD_PARAM;
    }
    memset(&initmsg, 0, sizeof(initmsg));
    initmsg.sinit_num_ostreams = (uint16_t) numOstreams;
    initmsg.sinit_max_instreams = (uint16_t) maxInstreams;
    initmsg.sinit_max_attempts = (uint16_t) maxAttempts;
    initmsg.sinit_max_init_timeo = (uint16_t) maxInitTimeo;
    if (usrsctp_setsockopt(ctx->sock, IPPROTO_SCTP, SCTP_INITMSG, &initmsg,
                           (socklen_t) sizeof(initmsg)) < 0) {
        return COBALT_SCTP_FAIL;
    }
    return COBALT_SCTP_OK;
}

int32_t cobalt_sctp_subscribe_assoc_change(void *sock, int32_t on) {
    cobalt_sctp_socket *ctx = (cobalt_sctp_socket *) sock;
    struct sctp_event ev;
    if (ctx == NULL) {
        return COBALT_SCTP_BAD_PARAM;
    }
    memset(&ev, 0, sizeof(ev));
    ev.se_assoc_id = 0;
    ev.se_type = SCTP_ASSOC_CHANGE;
    ev.se_on = (uint8_t) (on ? 1 : 0);
    if (usrsctp_setsockopt(ctx->sock, IPPROTO_SCTP, SCTP_EVENT, &ev,
                           (socklen_t) sizeof(ev)) < 0) {
        return COBALT_SCTP_FAIL;
    }
    return COBALT_SCTP_OK;
}

int32_t cobalt_sctp_set_non_blocking(void *sock, int32_t on) {
    cobalt_sctp_socket *ctx = (cobalt_sctp_socket *) sock;
    if (ctx == NULL) {
        return COBALT_SCTP_BAD_PARAM;
    }
    if (usrsctp_set_non_blocking(ctx->sock, on) < 0) {
        return COBALT_SCTP_FAIL;
    }
    return COBALT_SCTP_OK;
}

int32_t cobalt_sctp_close(void *sock) {
    cobalt_sctp_socket *ctx = (cobalt_sctp_socket *) sock;
    if (ctx == NULL) {
        return COBALT_SCTP_OK;
    }
    if (ctx->sock != NULL) {
        usrsctp_close(ctx->sock);
    }
    free(ctx);
    return COBALT_SCTP_OK;
}
