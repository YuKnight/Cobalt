package com.github.auties00.cobalt.registration.push.fcm.checkin;

import it.auties.protobuf.annotation.ProtobufMessage;
import it.auties.protobuf.annotation.ProtobufProperty;
import it.auties.protobuf.model.ProtobufType;

/**
 * Wire shape of the response to {@code POST /checkin}, gunzipped before
 * decoding. Only the two fields Cobalt cares about. The assigned
 * Android id and its security token. Are mapped. All other fields the
 * server returns are ignored.
 */
@ProtobufMessage(name = "FcmCheckinResponse")
public final class FcmCheckinResponse {
    /**
     * The 64-bit Android device id the server has assigned. Becomes
     * the username on the MCS login.
     */
    @ProtobufProperty(index = 7, type = ProtobufType.FIXED64)
    long androidId;

    /**
     * The 64-bit security token paired with {@link #androidId}. Becomes
     * the password on the MCS login.
     */
    @ProtobufProperty(index = 8, type = ProtobufType.FIXED64)
    long securityToken;

    FcmCheckinResponse(long androidId, long securityToken) {
        this.androidId = androidId;
        this.securityToken = securityToken;
    }

    public long androidId() {
        return androidId;
    }

    public long securityToken() {
        return securityToken;
    }
}
