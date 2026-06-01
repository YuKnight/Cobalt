package com.github.auties00.cobalt.call.internal;

import com.github.auties00.cobalt.call.ActiveCall;
import com.github.auties00.cobalt.call.CallEndReason;
import com.github.auties00.cobalt.call.CallInteraction;
import com.github.auties00.cobalt.call.CallOptions;
import com.github.auties00.cobalt.call.IncomingCall;
import com.github.auties00.cobalt.model.jid.Jid;

/**
 * Test-only {@link CallService} stand-in whose methods are all no-ops.
 *
 * <p>Used by tests that need to construct an {@link ActiveCall} or media session in isolation;
 * the call engine is never exercised beyond the constructor reference, so a fully no-op
 * implementation is the simplest stand-in. Production code must use {@link LiveCallService}
 * with its real collaborators.
 */
public final class NoopCallService implements CallService {
    @Override public ActiveCall placeCall(Jid peer, CallOptions options) { return null; }
    @Override public ActiveCall placeGroupCall(java.util.Set<Jid> peers, Jid groupJid, CallOptions options) { return null; }
    @Override public ActiveCall accept(IncomingCall offer, CallOptions options) { return null; }
    @Override public void reject(IncomingCall offer, CallEndReason reason) {}
    @Override public ActiveCall find(String callId) { return null; }
    @Override public void onPeerAccept(String callId) {}
    @Override public void onPeerCandidates(String callId, java.util.List<java.net.InetSocketAddress> candidates) {}
    @Override public void startLocalVideo(String callId) {}
    @Override public void startScreenShare(String callId) {}
    @Override public void onPeerReject(String callId, String reason) {}
    @Override public void onPeerTerminate(String callId, String reason) {}
    @Override public void unregister(String callId) {}
    @Override public void sendTerminate(Jid peer, Jid creator, String callId, CallEndReason reason) {}
    @Override public void sendMute(Jid peer, Jid creator, String callId, boolean muted) {}
    @Override public void sendVideoState(Jid peer, Jid creator, String callId, boolean enabled) {}
    @Override public void sendVideoUpgradeRequest(Jid peer, Jid creator, String callId) {}
    @Override public void sendVideoUpgradeReject(Jid peer, Jid creator, String callId) {}
    @Override public void sendInteraction(Jid peer, Jid creator, String callId, CallInteraction interaction) {}
    @Override public void onEncRekey(String callId, Jid senderJid, com.github.auties00.cobalt.message.MessageEncryptionType encType, byte[] ciphertext) {}
    @Override public void onGroupRelay(String callId, com.github.auties00.cobalt.ack.CallRelay relay) {}
    @Override public void notifyEnded(String callId, Jid fromJid, String wireReason) {}
}
