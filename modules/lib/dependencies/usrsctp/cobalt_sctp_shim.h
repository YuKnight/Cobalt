/*
 * cobalt_sctp_shim.h
 *
 * Portable extern-C facade over usrsctp (sctplab/usrsctp) for the Cobalt calls2
 * WebRTC DataChannel transport. It re-exposes only the usrsctp surface the
 * association uses, through PORTABLE SCALAR TYPES ONLY (the fixed-width
 * <stdint.h> integers, size_t, and opaque void*), so the jextract-generated Java
 * binding is identical on every host ABI.
 *
 * Why this shim exists: jextract bakes the host C ABI into the bindings it
 * emits, and usrsctp's public API is saturated with ABI-sensitive shapes. The
 * per-socket receive callback is declared
 *   int (*)(struct socket*, union sctp_sockstore, void*, size_t,
 *           struct sctp_rcvinfo, int, void*)
 * which passes a union and a struct BY VALUE; sockaddr_conn even changes layout
 * across platforms (an extra sconn_len byte on the BSDs and macOS). bind/connect
 * take a sockaddr; sendv takes sctp_sndinfo / sctp_sendv_spa; setsockopt takes
 * sctp_initmsg / sctp_event. Every one of those struct layouts differs across
 * ABIs, and the old binding leaned on a sysstubs header to coax jextract into a
 * portable-looking layout. By keeping every usrsctp and socket struct entirely
 * C-side and exchanging only fixed-width scalars and opaque handles across the
 * boundary, the generated binding contains no ABI-sensitive layout and is
 * portable as-is, with no sysstubs needed.
 *
 * How the structs are hidden: a socket is an opaque void* handle wrapping a
 * heap-allocated context (the usrsctp struct socket* plus the Java receive
 * callback and its ulp_info). The shim registers its OWN C receive trampoline
 * with usrsctp; that trampoline reads sctp_rcvinfo C-side and calls a PORTABLE
 * Java callback with scalars (data pointer, length, stream id, ppid in host byte
 * order, flags, a notification boolean, ulp_info). bind/connect build the
 * sockaddr_conn C-side from a port and conn id; send builds sctp_sndinfo (and
 * the partial-reliability variant sctp_sendv_spa) C-side; the socket options are
 * applied through typed setters that build sctp_initmsg / sctp_event C-side.
 * Network byte order conversion for the port and the PPID is done C-side, so the
 * Java side deals only in host order.
 *
 * Callbacks crossing the boundary are passed as opaque void* (the Java upcall
 * stub address); the shim casts each to its concrete function-pointer type
 * C-side, so no function-pointer type appears in the bound surface. Their
 * required C signatures are:
 *   conn-output (cobalt_sctp_global_init):
 *     int (*)(void *addr, void *buffer, size_t length, uint8_t tos, uint8_t setDf)
 *   receive (cobalt_sctp_socket_create):
 *     int (*)(void *data, size_t datalen, int32_t streamId, int32_t ppid,
 *             int32_t flags, int32_t isNotification, void *ulpInfo)
 *
 * Symbol naming: every exported symbol is prefixed cobalt_sctp_ so it coexists
 * in the combined cobalt-native library with the statically-linked real
 * usrsctp_* symbols, which these wrappers call internally.
 *
 * Portability rule for this header: it uses ONLY uint8_t/int32_t/int64_t/size_t/
 * void*. It never names a usrsctp or socket type, and never uses bare `long`,
 * `unsigned long` or `long double`.
 */

#ifndef COBALT_SCTP_SHIM_H
#define COBALT_SCTP_SHIM_H

#include <stddef.h>
#include <stdint.h>

#ifdef __cplusplus
extern "C" {
#endif

/*
 * Status codes returned by the entry points. COBALT_SCTP_OK is success.
 * COBALT_SCTP_IN_PROGRESS is returned by cobalt_sctp_connect when the
 * non-blocking handshake has started (usrsctp's EINPROGRESS), which the caller
 * treats as success and then awaits the association-change notification. The
 * negatives are shim-level failures (invalid argument, allocation failure, a
 * usrsctp call returning an error). The send entry points instead return the
 * byte count, or a negative value on failure.
 */
#define COBALT_SCTP_OK          0
#define COBALT_SCTP_IN_PROGRESS 1
#define COBALT_SCTP_BAD_PARAM   (-1)
#define COBALT_SCTP_NOMEM       (-2)
#define COBALT_SCTP_FAIL        (-3)

/*
 * Partial-reliability policy selectors for cobalt_sctp_send_pr. The wrapper maps
 * each to usrsctp's SCTP_PR_SCTP_* policy C-side:
 *
 *   0 = none (fully reliable; prValue ignored)
 *   1 = timed (prValue is the lifetime in milliseconds)
 *   2 = retransmit-limited (prValue is the maximum retransmission count)
 */
#define COBALT_SCTP_PR_NONE 0
#define COBALT_SCTP_PR_TTL  1
#define COBALT_SCTP_PR_RTX  2

/**
 * Initializes the usrsctp stack in no-threads mode with a conn-output callback.
 *
 * Forwards to usrsctp_init_nothreads with the given UDP-encapsulation port (0
 * for the AF_CONN path that routes through the callback) and the conn-output
 * callback, passing a NULL debug printf. Must be called once for the process
 * before any socket is created; the stack is never finished. The callback is the
 * single global function usrsctp invokes for every outbound packet.
 *
 * @param port           the UDP encapsulation port, or 0 for the AF_CONN path.
 * @param connOutputCb   the conn-output upcall stub (see the header preamble for
 *                       its required signature); must not be NULL.
 * @return COBALT_SCTP_OK on success, COBALT_SCTP_BAD_PARAM for a NULL callback.
 */
int32_t cobalt_sctp_global_init(int32_t port, void *connOutputCb);

/**
 * Registers a conn id so usrsctp routes its outbound packets to the conn-output
 * callback.
 *
 * Forwards to usrsctp_register_address. The conn id is an opaque pointer the
 * caller owns and uses as a routing key; usrsctp never dereferences it.
 *
 * @param connId the conn id pointer.
 * @return COBALT_SCTP_OK.
 */
int32_t cobalt_sctp_register_address(void *connId);

/**
 * Deregisters a conn id previously registered with cobalt_sctp_register_address.
 *
 * Forwards to usrsctp_deregister_address. After this returns usrsctp routes no
 * further outbound packets to the conn id. A NULL conn id is ignored.
 *
 * @param connId the conn id pointer, or NULL.
 * @return COBALT_SCTP_OK.
 */
int32_t cobalt_sctp_deregister_address(void *connId);

/**
 * Advances usrsctp's timer wheel by the given elapsed time.
 *
 * Forwards to usrsctp_handle_timers. Driven periodically by the caller's ticker
 * thread because the stack runs in no-threads mode.
 *
 * @param elapsedMs the elapsed time since the previous tick, in milliseconds.
 * @return COBALT_SCTP_OK.
 */
int32_t cobalt_sctp_handle_timers(int32_t elapsedMs);

/**
 * Creates one AF_CONN SCTP socket bound to a receive callback.
 *
 * The wrapper allocates a context holding the receive callback and its ulp_info,
 * calls usrsctp_socket(AF_CONN, SOCK_STREAM, IPPROTO_SCTP, trampoline, NULL, 0,
 * ctx) where trampoline is the shim's static C receive callback, and returns the
 * context through outSock as an opaque handle. When usrsctp later decodes a
 * message the trampoline reads its sctp_rcvinfo C-side and calls receiveCb with
 * portable scalars; for a notification the isNotification argument is set and the
 * stream id and ppid are unspecified.
 *
 * @param receiveCb the receive upcall stub (see the header preamble for its
 *                  required signature); must not be NULL.
 * @param ulpInfo   an opaque pointer passed back to receiveCb on each firing
 *                  (the caller's conn id); may be NULL.
 * @param outSock   receives the created socket handle on success, or is left
 *                  holding NULL on failure; must not be NULL.
 * @return COBALT_SCTP_OK on success, COBALT_SCTP_BAD_PARAM for a NULL receiveCb
 *         or outSock, COBALT_SCTP_NOMEM if the context could not be allocated,
 *         COBALT_SCTP_FAIL if usrsctp_socket returned NULL.
 */
int32_t cobalt_sctp_socket_create(void *receiveCb, void *ulpInfo, void **outSock);

/**
 * Binds a socket to a local SCTP port on the AF_CONN address of a conn id.
 *
 * The wrapper builds a sockaddr_conn C-side (AF_CONN family, the port in network
 * byte order, the conn id as the address) and calls usrsctp_bind.
 *
 * @param sock   the socket handle from cobalt_sctp_socket_create.
 * @param port   the local SCTP port in host byte order.
 * @param connId the conn id whose AF_CONN address to bind.
 * @return COBALT_SCTP_OK on success, COBALT_SCTP_BAD_PARAM for a NULL socket,
 *         COBALT_SCTP_FAIL if usrsctp_bind returned an error.
 */
int32_t cobalt_sctp_bind(void *sock, int32_t port, void *connId);

/**
 * Starts the SCTP handshake toward a peer port on the AF_CONN address.
 *
 * The wrapper builds a sockaddr_conn C-side and calls usrsctp_connect. On a
 * non-blocking socket usrsctp returns -1 with EINPROGRESS for the normal case;
 * the wrapper maps that to COBALT_SCTP_IN_PROGRESS, an immediate connect to
 * COBALT_SCTP_OK. Handshake completion is signalled later through the
 * association-change notification delivered to the receive callback.
 *
 * @param sock   the socket handle from cobalt_sctp_socket_create.
 * @param port   the peer SCTP port in host byte order.
 * @param connId the conn id whose AF_CONN address to connect to.
 * @return COBALT_SCTP_OK on an immediate connect, COBALT_SCTP_IN_PROGRESS when
 *         the handshake has started, COBALT_SCTP_BAD_PARAM for a NULL socket.
 */
int32_t cobalt_sctp_connect(void *sock, int32_t port, void *connId);

/**
 * Feeds one inbound SCTP packet into the stack for the given conn id.
 *
 * Forwards to usrsctp_conninput with a type-of-service of 0. The call may drive
 * the SCTP state machine and synchronously invoke the receive callback of the
 * socket bound to this conn id.
 *
 * @param connId the conn id the packet arrived for.
 * @param data   the SCTP packet bytes.
 * @param len    the packet length in bytes.
 * @return COBALT_SCTP_OK on success, COBALT_SCTP_BAD_PARAM for a NULL conn id.
 */
int32_t cobalt_sctp_conninput(void *connId, const uint8_t *data, size_t len);

/**
 * Sends one application message on a stream with a fully reliable policy.
 *
 * The wrapper builds an sctp_sndinfo C-side (the stream id, the unordered flag,
 * the PPID converted to network byte order, zero context and association id) and
 * calls usrsctp_sendv with SCTP_SENDV_SNDINFO.
 *
 * @param sock      the socket handle from cobalt_sctp_socket_create.
 * @param data      the message bytes, or NULL for an empty message.
 * @param len       the message length in bytes.
 * @param streamId  the SCTP stream index.
 * @param ppid      the SCTP Payload Protocol Identifier in host byte order.
 * @param unordered non-zero for unordered delivery, zero for ordered.
 * @return the number of bytes accepted (equal to len on success), or a negative
 *         value (the usrsctp error, or COBALT_SCTP_BAD_PARAM for a NULL socket).
 */
int64_t cobalt_sctp_send(void *sock, const uint8_t *data, size_t len,
                         int32_t streamId, int32_t ppid, int32_t unordered);

/**
 * Sends one application message under a partial-reliability policy.
 *
 * The wrapper builds an sctp_sendv_spa C-side carrying both an sctp_sndinfo (as
 * cobalt_sctp_send builds) and an sctp_prinfo (the policy mapped from prPolicy,
 * the operand prValue), with the SCTP_SEND_SNDINFO_VALID and
 * SCTP_SEND_PRINFO_VALID flags, and calls usrsctp_sendv with SCTP_SENDV_SPA.
 *
 * @param sock      the socket handle from cobalt_sctp_socket_create.
 * @param data      the message bytes, or NULL for an empty message.
 * @param len       the message length in bytes.
 * @param streamId  the SCTP stream index.
 * @param ppid      the SCTP PPID in host byte order.
 * @param unordered non-zero for unordered delivery, zero for ordered.
 * @param prPolicy  one of COBALT_SCTP_PR_NONE / _TTL / _RTX.
 * @param prValue   the policy operand: lifetime milliseconds for _TTL, maximum
 *                  retransmissions for _RTX, ignored for _NONE.
 * @return the number of bytes accepted (equal to len on success), or a negative
 *         value (the usrsctp error, or COBALT_SCTP_BAD_PARAM for a NULL socket).
 */
int64_t cobalt_sctp_send_pr(void *sock, const uint8_t *data, size_t len,
                            int32_t streamId, int32_t ppid, int32_t unordered,
                            int32_t prPolicy, int32_t prValue);

/**
 * Enables or disables Nagle batching on the socket (the SCTP_NODELAY option).
 *
 * @param sock the socket handle from cobalt_sctp_socket_create.
 * @param on   non-zero to disable batching (enable NODELAY), zero to allow it.
 * @return COBALT_SCTP_OK on success, COBALT_SCTP_BAD_PARAM for a NULL socket,
 *         COBALT_SCTP_FAIL if usrsctp_setsockopt returned an error.
 */
int32_t cobalt_sctp_set_nodelay(void *sock, int32_t on);

/**
 * Enables or disables message fragmentation on the socket (the
 * SCTP_DISABLE_FRAGMENTS option).
 *
 * @param sock the socket handle from cobalt_sctp_socket_create.
 * @param on   non-zero to disable fragmentation, zero to allow it.
 * @return COBALT_SCTP_OK on success, COBALT_SCTP_BAD_PARAM for a NULL socket,
 *         COBALT_SCTP_FAIL if usrsctp_setsockopt returned an error.
 */
int32_t cobalt_sctp_set_disable_fragments(void *sock, int32_t on);

/**
 * Sets the initial stream and handshake parameters (the SCTP_INITMSG option).
 *
 * The wrapper builds an sctp_initmsg C-side from the four counts and applies it.
 * A value of 0 for attempts or timeout leaves usrsctp's default in force.
 *
 * @param sock         the socket handle from cobalt_sctp_socket_create.
 * @param numOstreams  the requested number of outbound streams.
 * @param maxInstreams the maximum number of inbound streams.
 * @param maxAttempts  the maximum INIT retransmission attempts, or 0 for default.
 * @param maxInitTimeo the maximum INIT timeout in milliseconds, or 0 for default.
 * @return COBALT_SCTP_OK on success, COBALT_SCTP_BAD_PARAM for a NULL socket,
 *         COBALT_SCTP_FAIL if usrsctp_setsockopt returned an error.
 */
int32_t cobalt_sctp_set_initmsg(void *sock, int32_t numOstreams, int32_t maxInstreams,
                                int32_t maxAttempts, int32_t maxInitTimeo);

/**
 * Subscribes to or unsubscribes from association-change notifications (an
 * SCTP_EVENT subscription for SCTP_ASSOC_CHANGE).
 *
 * The wrapper builds an sctp_event C-side for the SCTP_ASSOC_CHANGE type and
 * applies it. The subscribed notification is delivered to the receive callback
 * with its isNotification argument set, carrying the handshake completion or
 * failure state.
 *
 * @param sock the socket handle from cobalt_sctp_socket_create.
 * @param on   non-zero to subscribe, zero to unsubscribe.
 * @return COBALT_SCTP_OK on success, COBALT_SCTP_BAD_PARAM for a NULL socket,
 *         COBALT_SCTP_FAIL if usrsctp_setsockopt returned an error.
 */
int32_t cobalt_sctp_subscribe_assoc_change(void *sock, int32_t on);

/**
 * Switches the socket between non-blocking and blocking mode.
 *
 * Forwards to usrsctp_set_non_blocking. The association path requires
 * non-blocking mode so the single-threaded lock model does not deadlock on the
 * WebRTC simultaneous-open handshake.
 *
 * @param sock the socket handle from cobalt_sctp_socket_create.
 * @param on   non-zero for non-blocking, zero for blocking.
 * @return COBALT_SCTP_OK on success, COBALT_SCTP_BAD_PARAM for a NULL socket,
 *         COBALT_SCTP_FAIL if usrsctp_set_non_blocking returned an error.
 */
int32_t cobalt_sctp_set_non_blocking(void *sock, int32_t on);

/**
 * Closes a socket created by cobalt_sctp_socket_create and frees its handle.
 *
 * Forwards to usrsctp_close on the wrapped socket and frees the context. Must be
 * called at most once per handle. A NULL handle is ignored and reported as
 * success. The caller must deregister the conn id before this call so any
 * in-flight conn-output callbacks drain first.
 *
 * @param sock the socket handle, or NULL.
 * @return COBALT_SCTP_OK.
 */
int32_t cobalt_sctp_close(void *sock);

#ifdef __cplusplus
}
#endif

#endif /* COBALT_SCTP_SHIM_H */
