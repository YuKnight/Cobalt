package com.github.auties00.cobalt.call;

/**
 * The four points of a call's life as visible to a Cobalt user.
 * Internal protocol-level events (ICE checks, DTLS handshake, SCTP
 * association, etc.) are folded into {@link #CONNECTING}; users only
 * need to observe {@link #ACTIVE} for "media is flowing" and
 * {@link #ENDED} for "you can clean up".
 */
public enum CallState {
    /**
     * The call has been offered and is awaiting acceptance —
     * either remotely (we're the caller, peer is being notified) or
     * locally (we're the callee, the user must accept or reject).
     */
    RINGING,

    /**
     * The peer has accepted; ICE / DTLS / SRTP setup is in
     * progress. No application-layer media is flowing yet.
     */
    CONNECTING,

    /**
     * Media is flowing. The four media ports on
     * {@link ActiveCall} are live.
     */
    ACTIVE,

    /**
     * The transport layer detected a network change (Wi-Fi ↔
     * cellular IP swap, relay outage, prolonged RTT spike) and is
     * re-establishing the DTLS + SRTP path against a fresh
     * relay/candidate pair. Mic frames written during this window
     * are buffered or dropped at the call's discretion; the call
     * transitions back to {@link #ACTIVE} once the new handshake
     * completes, or to {@link #ENDED} if the recovery fails.
     */
    RECONNECTING,

    /**
     * The call is over for any reason (hangup, reject, timeout,
     * network failure). The call's
     * {@link ActiveCall#endReason()} is populated. No further
     * frames will be exchanged.
     */
    ENDED
}
