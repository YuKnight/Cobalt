package com.github.auties00.cobalt.registration.push.fcm.mcs;

import it.auties.protobuf.annotation.ProtobufMessage;
import it.auties.protobuf.annotation.ProtobufProperty;
import it.auties.protobuf.model.ProtobufType;

/**
 * Wire shape for both heartbeat directions: tag {@code 0} (ping)
 * outbound on the 10-minute timer, and tag {@code 0} inbound from the
 * server. The server may also send tag {@code 1} (ack), which uses the
 * same field shape.
 */
@ProtobufMessage(name = "FcmMcsHeartbeatPing")
public final class FcmMcsHeartbeatPing {
    /**
     * Last stream id the sender has observed; lets the peer infer the
     * cursor without parsing any stanza payload.
     */
    @ProtobufProperty(index = 2, type = ProtobufType.INT64)
    long lastStreamIdReceived;

    FcmMcsHeartbeatPing(long lastStreamIdReceived) {
        this.lastStreamIdReceived = lastStreamIdReceived;
    }

    public long lastStreamIdReceived() {
        return lastStreamIdReceived;
    }
}
