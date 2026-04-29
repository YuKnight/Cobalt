package com.github.auties00.cobalt.registration.push.fcm.mcs;

import it.auties.protobuf.annotation.ProtobufMessage;
import it.auties.protobuf.annotation.ProtobufProperty;
import it.auties.protobuf.model.ProtobufType;

/**
 * Wire shape for an outbound heartbeat ack (tag {@code 1}), sent in
 * response to an incoming server ping. Carries the same stream cursor
 * as {@link FcmMcsHeartbeatPing} plus a status field the native client
 * always sets to {@code 0}.
 */
@ProtobufMessage(name = "FcmMcsHeartbeatAck")
public final class FcmMcsHeartbeatAck {
    @ProtobufProperty(index = 2, type = ProtobufType.INT64)
    long lastStreamIdReceived;

    @ProtobufProperty(index = 3, type = ProtobufType.INT64)
    long status;

    FcmMcsHeartbeatAck(long lastStreamIdReceived, long status) {
        this.lastStreamIdReceived = lastStreamIdReceived;
        this.status = status;
    }
}
