package com.github.auties00.cobalt.registration.push.fcm.mcs;

import com.github.auties00.cobalt.registration.push.fcm.FcmClient;
import it.auties.protobuf.annotation.ProtobufMessage;
import it.auties.protobuf.annotation.ProtobufProperty;
import it.auties.protobuf.model.ProtobufType;

import java.util.List;

/**
 * Wire shape of an incoming push (frame tag {@code 8}). Decoded by
 * {@link FcmClient}, which scans the {@code app_data} entries for the
 * WhatsApp verification code and surfaces it via
 * {@link FcmClient#getPushCode()}.
 */
@ProtobufMessage(name = "FcmMcsDataMessageStanza")
public final class FcmMcsDataMessageStanza {
    /**
     * Application-level message id, often the same value the original
     * sender supplied to the FCM HTTP API.
     */
    @ProtobufProperty(index = 2, type = ProtobufType.STRING)
    String id;

    /**
     * Sender id (typically the project's GCM sender number).
     */
    @ProtobufProperty(index = 3, type = ProtobufType.STRING)
    String from;

    /**
     * Push category. Typically the receiving app package or the topic
     * name on topic-style pushes.
     */
    @ProtobufProperty(index = 5, type = ProtobufType.STRING)
    String category;

    /**
     * FCM collapse key.
     */
    @ProtobufProperty(index = 6, type = ProtobufType.STRING)
    String token;

    /**
     * The application-level key/value payload. The FCM HTTP API's
     * {@code data} JSON object lands here.
     */
    @ProtobufProperty(index = 7, type = ProtobufType.MESSAGE)
    List<AppData> appData;

    /**
     * Per-message persistent id used by the at-least-once delivery
     * mechanism. Tracked by {@link FcmClient} so it can be replayed on
     * the next login.
     */
    @ProtobufProperty(index = 9, type = ProtobufType.STRING)
    String persistentId;

    /**
     * Time-to-live the sender attached, in seconds.
     */
    @ProtobufProperty(index = 17, type = ProtobufType.INT64)
    long ttl;

    /**
     * Server-side send timestamp, in milliseconds since epoch.
     */
    @ProtobufProperty(index = 18, type = ProtobufType.INT64)
    long sent;

    /**
     * Optional binary payload, used by FCM-driven SMS/silent
     * verification flows where the verification code is shipped as
     * raw bytes rather than as an {@code app_data} entry.
     */
    @ProtobufProperty(index = 21, type = ProtobufType.BYTES)
    byte[] rawData;

    FcmMcsDataMessageStanza(String id, String from, String category, String token,
                            List<AppData> appData, String persistentId, long ttl,
                            long sent, byte[] rawData) {
        this.id = id;
        this.from = from;
        this.category = category;
        this.token = token;
        this.appData = appData;
        this.persistentId = persistentId;
        this.ttl = ttl;
        this.sent = sent;
        this.rawData = rawData;
    }

    public String id() {
        return id;
    }

    public String from() {
        return from;
    }

    public String category() {
        return category;
    }

    public String token() {
        return token;
    }

    public List<AppData> appData() {
        return appData;
    }

    public String persistentId() {
        return persistentId;
    }

    public long ttl() {
        return ttl;
    }

    public long sent() {
        return sent;
    }

    public byte[] rawData() {
        return rawData;
    }

    /**
     * One key/value entry in the {@code app_data} list.
     */
    @ProtobufMessage(name = "FcmMcsDataMessageStanza.AppData")
    public static final class AppData {
        @ProtobufProperty(index = 1, type = ProtobufType.STRING)
        String key;

        @ProtobufProperty(index = 2, type = ProtobufType.STRING)
        String value;

        AppData(String key, String value) {
            this.key = key;
            this.value = value;
        }

        public String key() {
            return key;
        }

        public String value() {
            return value;
        }
    }
}
