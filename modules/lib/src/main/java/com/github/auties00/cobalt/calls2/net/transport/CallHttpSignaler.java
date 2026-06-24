package com.github.auties00.cobalt.calls2.net.transport;

/**
 * Bootstraps a call's media session by posting a {@code start_session_request} and correlating its
 * {@code start_session_response}.
 *
 * <p>Before a call's media plane can come up, the engine asks a host-provided signaler to post a small
 * JSON bootstrap request, {@code {"start_session_request":{}}}, and waits for the matching
 * {@code start_session_response}. On WhatsApp Web that signaler is a host function pointer that ships
 * the request over the browser's HTTP stack; in Cobalt it is this interface, implemented over the
 * Cobalt client transport. Each request carries a freshly generated request id so the asynchronous
 * response can be matched to it; an implementation tracks its open requests by that id.
 *
 * <p>The single method {@link #sendStartSessionRequest()} blocks the calling virtual thread until the
 * response returns or the request fails, in keeping with the project's virtual-thread blocking model
 * (no {@code CompletableFuture}). The interface is sealed and permits only its production
 * implementation, so a call site dispatching over a {@link CallHttpSignaler} can pattern-match
 * exhaustively.
 *
 * @apiNote This is engine-internal plumbing, not an embedder API: the transport controller calls it
 * during call bring-up, and an embedder instead supplies the HTTP transport the implementation posts
 * over. Treat it as the contract documenting the call-bootstrap round-trip.
 *
 * @implSpec An implementation MUST format a unique request id, post the literal body
 * {@code {"start_session_request":{}}} over its transport, retain the open request keyed by id until
 * the response arrives, and return the response correlated to the request. It MUST surface a transport
 * failure as a {@link com.github.auties00.cobalt.exception.WhatsAppCallException} rather than returning a
 * partial result.
 *
 * @implNote This contract reproduces {@code http_signaler_send_start_session_request} (fn11769) from
 *           the wa-voip WASM module {@code ff-tScznZ8P} ({@code system/transport/call_http_signaler.cc}),
 *           which formats a sixty-three-byte request id, sets the JSON body
 *           {@code {"start_session_request":{}}}, invokes the host send-signaling-HTTP callback, and
 *           keeps an open-request table keyed by request id so the {@code start_session_response} can be
 *           matched. In Cobalt the host callback is replaced by the client transport.
 */
public sealed interface CallHttpSignaler permits LiveCallHttpSignaler {
    /**
     * The length, in bytes, of a generated session-bootstrap request id.
     */
    int REQUEST_ID_LENGTH = 63;

    /**
     * The literal JSON body posted to bootstrap a call's media session.
     */
    String START_SESSION_REQUEST_BODY = "{\"start_session_request\":{}}";

    /**
     * Posts the {@code start_session_request} and blocks until its {@code start_session_response}
     * returns.
     *
     * <p>The implementation generates a unique request id, posts the bootstrap body over its transport,
     * and waits for the correlated response, returning it as a {@link StartSessionResult}. The calling
     * virtual thread blocks for the round-trip.
     *
     * @return the correlated bootstrap result
     * @throws com.github.auties00.cobalt.exception.WhatsAppCallException if the request cannot be sent or
     *                                                                    the response does not return
     */
    StartSessionResult sendStartSessionRequest();

    /**
     * Holds the outcome of a {@code start_session} bootstrap round-trip.
     *
     * <p>It carries the request id that correlated the exchange and the raw response body the transport
     * returned, which the transport controller parses for any session parameters the response conveys.
     *
     * @param requestId    the sixty-three-byte request id that correlated the request and response
     * @param responseBody the raw {@code start_session_response} body bytes, or an empty array when the
     *                     response carried no body
     */
    record StartSessionResult(String requestId, byte[] responseBody) {
        /**
         * Canonicalizes the record components, defensively copying the response body.
         *
         * @throws NullPointerException if {@code requestId} or {@code responseBody} is {@code null}
         */
        public StartSessionResult {
            java.util.Objects.requireNonNull(requestId, "requestId cannot be null");
            java.util.Objects.requireNonNull(responseBody, "responseBody cannot be null");
            responseBody = responseBody.clone();
        }

        /**
         * Returns a defensive copy of the raw response body bytes.
         *
         * @return a copy of the response body; never {@code null}, possibly empty
         */
        @Override
        public byte[] responseBody() {
            return responseBody.clone();
        }
    }
}
