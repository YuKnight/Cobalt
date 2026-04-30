package com.github.auties00.cobalt.registration.push.fcm.checkin;

import it.auties.protobuf.annotation.ProtobufMessage;
import it.auties.protobuf.annotation.ProtobufProperty;
import it.auties.protobuf.model.ProtobufType;

/**
 * Wire body for the gzipped POST against
 * {@code android.clients.google.com/checkin}, the first step of the
 * Android device-registration handshake.
 *
 * <p>Field numbers come from Google's internal AndroidCheckin protobuf
 * (the public reference is the leaked {@code checkin.proto} schema).
 * Only the subset Cobalt actually needs is encoded.
 *
 * <p>The corresponding response is decoded by
 * {@link FcmCheckinResponse}.
 */
@ProtobufMessage(name = "FcmCheckinRequest")
public final class FcmCheckinRequest {
    /**
     * Existing Android id. {@code 0} for a fresh registration.
     */
    @ProtobufProperty(index = 2, type = ProtobufType.INT64)
    long id;

    /**
     * Nested device description.
     */
    @ProtobufProperty(index = 4, type = ProtobufType.MESSAGE)
    Checkin checkin;

    /**
     * UI locale, e.g. {@code "en_US"}.
     */
    @ProtobufProperty(index = 6, type = ProtobufType.STRING)
    String locale;

    /**
     * Random per-checkin logging id, ~{@code SecureRandom.nextLong() & MAX_LONG}.
     */
    @ProtobufProperty(index = 7, type = ProtobufType.INT64)
    long loggingId;

    /**
     * Time zone, always {@code "UTC"}.
     */
    @ProtobufProperty(index = 12, type = ProtobufType.STRING)
    String timeZone;

    /**
     * Checkin version. The native Android client sends {@code 3}.
     */
    @ProtobufProperty(index = 14, type = ProtobufType.INT32)
    int version;

    /**
     * Numeric flags Cobalt sets to {@code 0}. Carried so the wire body
     * matches what the server expects.
     */
    @ProtobufProperty(index = 20, type = ProtobufType.INT64)
    long fragment;

    /**
     * Numeric flag Cobalt sets to {@code 0}. Same rationale as
     * {@link #fragment}.
     */
    @ProtobufProperty(index = 22, type = ProtobufType.INT64)
    long userSerialNumber;

    FcmCheckinRequest(long id, Checkin checkin, String locale, long loggingId,
                      String timeZone, int version, long fragment, long userSerialNumber) {
        this.id = id;
        this.checkin = checkin;
        this.locale = locale;
        this.loggingId = loggingId;
        this.timeZone = timeZone;
        this.version = version;
        this.fragment = fragment;
        this.userSerialNumber = userSerialNumber;
    }

    /**
     * Nested device-and-event payload (field 4 of the outer request).
     */
    @ProtobufMessage(name = "FcmCheckinRequest.Checkin")
    public static final class Checkin {
        /**
         * Build description of the device being impersonated.
         */
        @ProtobufProperty(index = 1, type = ProtobufType.MESSAGE)
        Build build;

        /**
         * Last-checkin timestamp, {@code 0} for a fresh registration.
         */
        @ProtobufProperty(index = 2, type = ProtobufType.INT64)
        long lastCheckinMs;

        /**
         * One synthetic {@code event_log_start} entry. The server expects
         * at least one event to be present.
         */
        @ProtobufProperty(index = 3, type = ProtobufType.MESSAGE)
        Event event;

        /**
         * Multi-user serial number, {@code 0} on stock Android.
         */
        @ProtobufProperty(index = 9, type = ProtobufType.INT64)
        long userNumber;

        Checkin(Build build, long lastCheckinMs, Event event, long userNumber) {
            this.build = build;
            this.lastCheckinMs = lastCheckinMs;
            this.event = event;
            this.userNumber = userNumber;
        }
    }

    /**
     * Build description (field 1 of {@link Checkin}).
     */
    @ProtobufMessage(name = "FcmCheckinRequest.Build")
    public static final class Build {
        /**
         * Build fingerprint, e.g. {@code "google/razor/flo:5.0.1/LRX22C/1602158:user/release-keys"}.
         */
        @ProtobufProperty(index = 1, type = ProtobufType.STRING)
        String fingerprint;

        /**
         * Hardware id, e.g. {@code "flo"}.
         */
        @ProtobufProperty(index = 2, type = ProtobufType.STRING)
        String hardware;

        /**
         * Brand, e.g. {@code "google"}.
         */
        @ProtobufProperty(index = 3, type = ProtobufType.STRING)
        String brand;

        /**
         * Client identifier, always {@code "android-google"}.
         */
        @ProtobufProperty(index = 6, type = ProtobufType.STRING)
        String clientId;

        /**
         * Build timestamp, in epoch seconds.
         */
        @ProtobufProperty(index = 7, type = ProtobufType.INT64)
        long timeMs;

        /**
         * SDK version (Android API level), e.g. {@code 30}.
         */
        @ProtobufProperty(index = 10, type = ProtobufType.INT32)
        int sdkVersion;

        /**
         * Marketing model, e.g. {@code "Nexus 7"}.
         */
        @ProtobufProperty(index = 11, type = ProtobufType.STRING)
        String model;

        /**
         * OEM, e.g. {@code "asus"}.
         */
        @ProtobufProperty(index = 12, type = ProtobufType.STRING)
        String manufacturer;

        /**
         * Internal product code, e.g. {@code "razor"}.
         */
        @ProtobufProperty(index = 13, type = ProtobufType.STRING)
        String product;

        /**
         * Whether an OTA update is installed; {@code false} for a fresh
         * synthetic device.
         */
        @ProtobufProperty(index = 14, type = ProtobufType.BOOL)
        boolean otaInstalled;

        Build(String fingerprint, String hardware, String brand, String clientId,
              long timeMs, int sdkVersion, String model, String manufacturer,
              String product, boolean otaInstalled) {
            this.fingerprint = fingerprint;
            this.hardware = hardware;
            this.brand = brand;
            this.clientId = clientId;
            this.timeMs = timeMs;
            this.sdkVersion = sdkVersion;
            this.model = model;
            this.manufacturer = manufacturer;
            this.product = product;
            this.otaInstalled = otaInstalled;
        }
    }

    /**
     * Single event entry (field 3 of {@link Checkin}).
     */
    @ProtobufMessage(name = "FcmCheckinRequest.Event")
    public static final class Event {
        /**
         * Event tag, always {@code "event_log_start"} for a synthetic
         * checkin.
         */
        @ProtobufProperty(index = 1, type = ProtobufType.STRING)
        String tag;

        /**
         * Event timestamp in epoch milliseconds.
         */
        @ProtobufProperty(index = 3, type = ProtobufType.INT64)
        long timeMs;

        Event(String tag, long timeMs) {
            this.tag = tag;
            this.timeMs = timeMs;
        }
    }
}
