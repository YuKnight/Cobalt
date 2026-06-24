package com.github.auties00.cobalt.calls2.net.transport;

import com.github.auties00.cobalt.calls2.platform.VoipCryptoNative;
import com.github.auties00.cobalt.exception.WhatsAppCallException;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Production {@link CallHttpSignaler} that routes the {@code start_session_request} over the injected
 * Cobalt client transport and correlates the {@code start_session_response} by request id.
 *
 * <p>The WhatsApp Web engine never opens a socket for this bootstrap: it formats a request id, sets the
 * literal {@code {"start_session_request":{}}} body, and hands the request to a host send-signaling
 * function pointer ({@code signaler+0x1c}). This implementation is the Cobalt host: the
 * {@link CallSignalingTransport} seam the embedder supplies issues the request over the live client
 * transport (the Linked encrypted socket as a call IQ, or the Cloud REST client) and returns the
 * correlated {@code start_session_response} bytes. There is no separate HTTP endpoint, no JDK
 * {@link java.net.http.HttpClient}, and no synthetic request-id header on the wire; the transport seam owns
 * how the request id rides the real WhatsApp transport.
 *
 * <p>The signaler generates a {@value CallHttpSignaler#REQUEST_ID_LENGTH}-character request id, records it
 * in an open-request table that mirrors the native correlation hashtable, dispatches the request through
 * the transport seam, and blocks the calling virtual thread on the response. An entry is removed once its
 * response returns or its request fails. The signaler is safe to share across the threads that bootstrap
 * concurrent calls: the open-request table is concurrent and each request blocks on its own response.
 *
 * @implNote This implementation reproduces {@code http_signaler_send_start_session_request} (fn11769)
 *           from the wa-voip WASM module {@code ff-tScznZ8P}
 *           ({@code system/transport/call_http_signaler.cc}): it formats a
 *           {@value CallHttpSignaler#REQUEST_ID_LENGTH}-character request id, sets the JSON body
 *           {@value CallHttpSignaler#START_SESSION_REQUEST_BODY}, invokes the host send-signaling-HTTP
 *           callback (here the {@link CallSignalingTransport} seam, which the Cobalt RE design maps to the
 *           existing client transport: the {@code LinkedWhatsAppClient} socket or the
 *           {@code CloudApiClient}), and keeps an open-request table keyed by request id. The native code
 *           logs and returns an error when the callback pointer is unset; here a transport failure surfaces
 *           as {@link WhatsAppCallException.DataChannel}, the non-fatal media-plane bring-up failure.
 */
public final class LiveCallHttpSignaler implements CallHttpSignaler {
    /**
     * The number of random bytes drawn to derive a request id; base64url of these bytes is truncated to
     * {@value CallHttpSignaler#REQUEST_ID_LENGTH} characters.
     */
    private static final int REQUEST_ID_ENTROPY_BYTES = 48;

    /**
     * Holds the client-transport seam the bootstrap request is issued over.
     */
    private final CallSignalingTransport transport;

    /**
     * Holds the open requests keyed by request id, mirroring the native correlation hashtable.
     */
    private final Map<String, Long> openRequests = new ConcurrentHashMap<>();

    /**
     * Constructs a signaler routing the bootstrap request over the given client-transport seam.
     *
     * @param transport the client-transport seam the {@code start_session_request} is issued over
     * @throws NullPointerException if {@code transport} is {@code null}
     */
    public LiveCallHttpSignaler(CallSignalingTransport transport) {
        this.transport = Objects.requireNonNull(transport, "transport cannot be null");
    }

    @Override
    public StartSessionResult sendStartSessionRequest() {
        var requestId = newRequestId();
        openRequests.put(requestId, System.nanoTime());
        try {
            byte[] responseBody;
            try {
                responseBody = transport.sendStartSessionRequest(
                        requestId, START_SESSION_REQUEST_BODY.getBytes(StandardCharsets.UTF_8));
            } catch (WhatsAppCallException exception) {
                throw exception;
            } catch (RuntimeException exception) {
                throw new WhatsAppCallException.DataChannel(
                        "start_session_request failed for request " + requestId, exception);
            }
            if (responseBody == null) {
                throw new WhatsAppCallException.DataChannel(
                        "start_session_request returned no response for request " + requestId);
            }
            return new StartSessionResult(requestId, responseBody);
        } finally {
            openRequests.remove(requestId);
        }
    }

    /**
     * Returns the number of requests currently awaiting a response.
     *
     * @return the size of the open-request table
     */
    public int openRequestCount() {
        return openRequests.size();
    }

    /**
     * Generates a fresh {@value CallHttpSignaler#REQUEST_ID_LENGTH}-character request id.
     *
     * <p>The id is the URL-safe, unpadded base64 of {@value #REQUEST_ID_ENTROPY_BYTES} cryptographically
     * strong random bytes, truncated to the fixed request-id length.
     *
     * @return a new request id of exactly {@value CallHttpSignaler#REQUEST_ID_LENGTH} characters
     */
    private static String newRequestId() {
        var entropy = VoipCryptoNative.randomBytes(REQUEST_ID_ENTROPY_BYTES);
        var encoded = Base64.getUrlEncoder().withoutPadding().encodeToString(entropy);
        if (encoded.length() >= REQUEST_ID_LENGTH) {
            return encoded.substring(0, REQUEST_ID_LENGTH);
        }
        var padded = new StringBuilder(encoded);
        while (padded.length() < REQUEST_ID_LENGTH) {
            padded.append('0');
        }
        return padded.toString();
    }

    /**
     * The seam the call-bootstrap request is issued over, the Cobalt analogue of the WhatsApp Web host
     * send-signaling-HTTP function pointer.
     *
     * <p>The WhatsApp Web engine hands the {@code start_session_request} to a host callback rather than
     * opening a socket; in Cobalt that callback is this seam, supplied by the embedder and backed by the
     * live client transport. An implementation issues the request over the existing transport (the
     * {@code LinkedWhatsAppClient} encrypted socket as a call IQ, or the {@code CloudApiClient} REST call),
     * carries the request id so the asynchronous {@code start_session_response} can be correlated to it,
     * and returns the response body bytes. The call blocks the calling virtual thread for the round-trip,
     * in keeping with the project's blocking-on-a-virtual-thread model.
     *
     * @apiNote This is the integration seam an embedder wires the call transport to: the media plane builds
     *          a {@link LiveCallHttpSignaler} over this seam, and the seam routes the bootstrap onto the
     *          real WhatsApp transport. It is not itself an embedder-facing API surface.
     * @implSpec An implementation MUST issue the request body over its client transport, MUST correlate the
     *           response to the supplied request id, and MUST surface a transport failure by throwing
     *           (a {@link WhatsAppCallException} is propagated unchanged; any other runtime failure is
     *           wrapped as a {@link WhatsAppCallException.DataChannel}) rather than returning {@code null}
     *           on error; returning {@code null} is reserved for a transport that completed but yielded no
     *           response body and is itself treated as a bring-up failure.
     */
    @FunctionalInterface
    public interface CallSignalingTransport {
        /**
         * Issues the {@code start_session_request} over the client transport and returns the correlated
         * response body.
         *
         * @param requestId   the {@value CallHttpSignaler#REQUEST_ID_LENGTH}-character request id to
         *                    correlate the exchange by; never {@code null}
         * @param requestBody the literal {@code start_session_request} body bytes to issue; never
         *                    {@code null}
         * @return the correlated {@code start_session_response} body bytes, or {@code null} when the
         *         transport completed without a response body
         * @throws WhatsAppCallException if the request cannot be issued or the response does not return
         */
        byte[] sendStartSessionRequest(String requestId, byte[] requestBody);
    }
}
