package com.github.auties00.cobalt.model.cloud;

import java.time.Instant;
import java.util.Objects;
import java.util.Optional;

/**
 * A WhatsApp Cloud API calling event, decoded from a {@code calls} webhook change.
 *
 * <p>The Calling API delivers call lifecycle transitions over the webhook: an inbound offer carrying
 * an SDP description, a connect, or a terminate. This model projects the common envelope so listeners
 * can route on {@link #event()} and answer an inbound offer with the carried {@link #sdp()}.
 */
public final class CloudCallEvent {
    /**
     * The call identifier assigned by the server.
     */
    private final String callId;

    /**
     * The event kind, for example {@code "connect"}, {@code "terminate"}, or {@code "offer"}.
     */
    private final String event;

    /**
     * The phone number of the other party in E.164 form.
     */
    private final String from;

    /**
     * The SDP description carried on an offer or answer, or {@code null} when the event carries none.
     */
    private final String sdp;

    /**
     * The event timestamp, or {@code null} when absent.
     */
    private final Instant timestamp;

    /**
     * Constructs a new call event.
     *
     * @param callId    the call identifier
     * @param event     the event kind
     * @param from      the other party's phone number in E.164 form
     * @param sdp       the SDP description, or {@code null} when none
     * @param timestamp the event timestamp, or {@code null} when absent
     * @throws NullPointerException if {@code callId}, {@code event}, or {@code from} is {@code null}
     */
    public CloudCallEvent(String callId, String event, String from, String sdp, Instant timestamp) {
        this.callId = Objects.requireNonNull(callId, "callId must not be null");
        this.event = Objects.requireNonNull(event, "event must not be null");
        this.from = Objects.requireNonNull(from, "from must not be null");
        this.sdp = sdp;
        this.timestamp = timestamp;
    }

    /**
     * Returns the call identifier.
     *
     * @return the call id
     */
    public String callId() {
        return callId;
    }

    /**
     * Returns the event kind.
     *
     * @return the event kind, for example {@code "connect"}, {@code "terminate"}, or {@code "offer"}
     */
    public String event() {
        return event;
    }

    /**
     * Returns the other party's phone number.
     *
     * @return the phone number in E.164 form
     */
    public String from() {
        return from;
    }

    /**
     * Returns the SDP description carried on the event.
     *
     * @return an {@link Optional} carrying the SDP, or empty when the event carried none
     */
    public Optional<String> sdp() {
        return Optional.ofNullable(sdp);
    }

    /**
     * Returns the event timestamp.
     *
     * @return an {@link Optional} carrying the timestamp, or empty when absent
     */
    public Optional<Instant> timestamp() {
        return Optional.ofNullable(timestamp);
    }
}
