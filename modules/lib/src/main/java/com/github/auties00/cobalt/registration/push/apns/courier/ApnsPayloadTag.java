package com.github.auties00.cobalt.registration.push.apns.courier;

/**
 * One-byte type code that prefixes every APNS frame on the wire.
 * Names match the labels Apple's push daemon ({@code apsd}) uses
 * internally. Values are the actual bytes observed on the v3
 * protocol.
 */
public enum ApnsPayloadTag {
    /**
     * Outbound: client login (after TLS handshake completes).
     */
    CONNECT(0x07),
    /**
     * Inbound: response to {@link #CONNECT}. Carries the auth token.
     */
    READY(0x08),
    /**
     * Outbound: subscribes to topic hashes so we receive their
     * pushes.
     */
    FILTER(0x09),
    /**
     * Inbound: a delivered push. Must be acked.
     */
    NOTIFICATION(0x0A),
    /**
     * Outbound: ack for a {@link #NOTIFICATION}.
     */
    ACK(0x0B),
    /**
     * Outbound: 5 s keep-alive ping.
     */
    KEEP_ALIVE_SEND(0x0C),
    /**
     * Inbound: keep-alive ack.
     */
    KEEP_ALIVE_ACK(0x0D),
    /**
     * Inbound: the server has no spare storage.
     */
    NO_STORAGE(0x0E),
    /**
     * Outbound: fetch a push token for a bundle id.
     */
    GET_TOKEN(0x11),
    /**
     * Inbound: response to {@link #GET_TOKEN}, carries the token.
     */
    TOKEN_RESPONSE(0x12),
    /**
     * Outbound: announces presence/idle state to the courier.
     */
    STATE(0x14),
    /**
     * Outbound: pub/sub control.
     */
    PUB_SUB(0x1D),
    /**
     * Inbound: pub/sub response.
     */
    PUB_SUB_RESPONSE(0x20);

    /**
     * Reverse lookup table from wire byte to tag, populated once on
     * class load. Entries past the last known tag value are
     * {@code null} so unknown bytes return {@code null} from
     * {@link #of(int)}.
     */
    private static final ApnsPayloadTag[] BY_VALUE = buildLookup();

    /**
     * The wire byte that identifies this tag.
     */
    private final int value;

    /**
     * Constructs a tag with the given wire byte value.
     *
     * @param value the wire byte
     */
    ApnsPayloadTag(int value) {
        this.value = value;
    }

    /**
     * @return the wire byte for this tag
     */
    public int value() {
        return value;
    }

    /**
     * Resolves the wire byte to a tag, or {@code null} when the byte
     * is not one we recognise. Returning {@code null} (rather than
     * throwing) lets the read pump log-and-skip an unknown packet
     * instead of tearing down the connection.
     *
     * @param value the wire tag byte
     * @return the matching tag, or {@code null} if unknown
     */
    public static ApnsPayloadTag of(int value) {
        if (value < 0 || value >= BY_VALUE.length) {
            return null;
        }
        return BY_VALUE[value];
    }

    /**
     * Builds the reverse lookup table indexed by wire byte. Sized to
     * the largest value across all enum constants.
     *
     * @return the populated lookup array
     */
    private static ApnsPayloadTag[] buildLookup() {
        var max = 0;
        for (var tag : values()) {
            if (tag.value > max) {
                max = tag.value;
            }
        }
        var table = new ApnsPayloadTag[max + 1];
        for (var tag : values()) {
            table[tag.value] = tag;
        }
        return table;
    }
}
