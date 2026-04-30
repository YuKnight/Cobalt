package com.github.auties00.cobalt.registration.push.fcm.mcs;

import it.auties.protobuf.annotation.ProtobufMessage;
import it.auties.protobuf.annotation.ProtobufProperty;
import it.auties.protobuf.model.ProtobufType;

/**
 * Server reply to {@link FcmMcsLoginRequest} (tag {@code 3} on the wire).
 * On success, {@link #error} is {@code null} and {@link #serverTimestamp}
 * carries the server's clock. On failure, {@link #error} carries the
 * code/message pair.
 */
@ProtobufMessage(name = "FcmMcsLoginResponse")
public final class FcmMcsLoginResponse {
    /**
     * Optional error block. {@code null} iff the login succeeded.
     */
    @ProtobufProperty(index = 3, type = ProtobufType.MESSAGE)
    ErrorInfo error;

    /**
     * Server's wall-clock at login time, in milliseconds since epoch.
     */
    @ProtobufProperty(index = 8, type = ProtobufType.INT64)
    long serverTimestamp;

    FcmMcsLoginResponse(ErrorInfo error, long serverTimestamp) {
        this.error = error;
        this.serverTimestamp = serverTimestamp;
    }

    public ErrorInfo error() {
        return error;
    }

    public long serverTimestamp() {
        return serverTimestamp;
    }

    /**
     * Non-zero error code + human-readable message returned when the
     * login is rejected (e.g. Expired security token).
     */
    @ProtobufMessage(name = "FcmMcsLoginResponse.ErrorInfo")
    public static final class ErrorInfo {
        @ProtobufProperty(index = 1, type = ProtobufType.INT64)
        long code;

        @ProtobufProperty(index = 2, type = ProtobufType.STRING)
        String message;

        ErrorInfo(long code, String message) {
            this.code = code;
            this.message = message;
        }

        public long code() {
            return code;
        }

        public String message() {
            return message;
        }
    }
}
