package com.github.auties00.cobalt.calls2.core.control;

import com.github.auties00.cobalt.calls2.signaling.CallMessage;
import com.github.auties00.cobalt.calls2.signaling.Calls2CallStanza;

/**
 * The outbound signaling egress an in-call controller dispatches a {@link CallMessage} through.
 *
 * <p>An in-call control operation builds a typed {@link CallMessage} action and hands it here to be put
 * on the wire. This seam hides where the stanza-id assignment, the {@code <call>} envelope wrapping, and
 * the socket write happen: the lifecycle layer that owns a call's dispatcher implements this by wrapping
 * the action through {@link Calls2CallStanza#toCall(CallMessage, com.github.auties00.cobalt.model.jid.Jid, String)}
 * with a fresh dispatcher id and writing it fire-and-forget to the Linked socket. A controller therefore
 * never needs the call's recipient or the dispatcher, only the typed action and this sink.
 *
 * <p>The send is fire-and-forget: it carries no return value and signals no acknowledgement, matching the
 * in-call action plane where the server stops retransmitting on its own {@code <ack class="call">} and the
 * engine does not block a control operation on a reply. Operations that instead need a request-reply IQ
 * (the call-link query and the waiting-room admit and deny, which the relay answers on a typed ack) do not
 * use this seam; they go through their own request senders.
 *
 * @implSpec An implementation MUST address the action to the call's current signaling recipient, assign a
 * fresh dispatcher stanza id, and dispatch it without blocking the caller on a reply. It MUST NOT throw
 * for a transport that is momentarily unavailable; a control action that cannot be sent is dropped the way
 * the native engine drops an action it cannot serialize, since the control plane is best-effort.
 * @implNote This implementation seam stands in for the native per-call signaling dispatcher of module
 * {@code ff-tScznZ8P} ({@code protocol/xmpp/call_signaling_xml.cc}), which wraps each serialized action in
 * the {@code <call to id>} envelope the dispatcher emits and ships it through the socket; Cobalt isolates
 * that wrapping behind this functional seam so the control package depends only on the typed
 * {@link CallMessage} and not on the lifecycle layer that owns the dispatcher.
 * @see CallMessage
 * @see Calls2CallStanza
 */
@FunctionalInterface
public interface CallSignalingSender {
    /**
     * Dispatches an in-call control action onto the wire fire-and-forget.
     *
     * @implSpec An implementation MUST wrap the action in the {@code <call>} envelope with the call's
     * recipient and a fresh dispatcher id and write it without blocking on a reply.
     * @param message the typed in-call control action to send; never {@code null}
     */
    void send(CallMessage message);
}
