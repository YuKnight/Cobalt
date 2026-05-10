package com.github.auties00.cobalt.wam.event;

import com.github.auties00.cobalt.meta.annotation.WhatsAppWebModule;

import com.github.auties00.cobalt.wam.annotation.WamEvent;
import com.github.auties00.cobalt.wam.annotation.WamProperty;
import com.github.auties00.cobalt.wam.model.WamEventSpec;
import com.github.auties00.cobalt.wam.model.WamType;
import com.github.auties00.cobalt.wam.type.CallResultType;
import com.github.auties00.cobalt.wam.type.CallSide;

import java.time.Instant;
import java.util.Optional;
import java.util.OptionalInt;

/**
 * End-of-call telemetry event (WAM id {@code 462}, internally
 * referred to as the "fieldstats-ready" event because WhatsApp Web
 * fires it once the per-call field stats have been finalised).
 *
 * <p>The full WhatsApp Web definition carries 700+ fields covering
 * every facet of the call (codec parameters, jitter/loss histograms,
 * relay election, hardware fingerprints, AEC/AGC counters, …).
 * Cobalt populates the subset its engine actually observes — the
 * fields below — and leaves the rest unset; the WAM event format
 * makes this clean since every property is {@link Optional}.
 *
 * <p>As subsequent tasks land they fill more fields:
 *
 * <ul>
 *   <li>#76 (ICE) → {@code activeRelayProtocol},
 *       {@code callRelayServers},
 *       {@code callTransport}.</li>
 *   <li>#77 (DTLS-SRTP) → handshake-related timers.</li>
 *   <li>#78 (RTP) → loss/jitter histograms.</li>
 *   <li>#61 / #62 (audio/video pipelines) → codec, bitrate, frame
 *       rate, encoder/decoder counters.</li>
 * </ul>
 */
@WhatsAppWebModule(moduleName = "WAWebCallWamEvent")
@WamEvent(id = 462)
public interface CallEvent extends WamEventSpec {
    /**
     * Whether the local user placed or received the call.
     *
     * @return the call side
     */
    @WamProperty(index = 1, type = WamType.ENUM)
    Optional<CallSide> callSide();

    /**
     * The terminal classification of the call.
     *
     * @return the call result
     */
    @WamProperty(index = 63, type = WamType.ENUM)
    Optional<CallResultType> callResult();

    /**
     * Number of state-machine transitions the call went through.
     *
     * @return the transition count
     */
    @WamProperty(index = 78, type = WamType.INTEGER)
    OptionalInt callTransitionCount();

    /**
     * Total elapsed time from {@code <offer>} send/receive to the
     * terminal event — used as the "call duration" in user-facing
     * call-history entries.
     *
     * @return the call's start timestamp (the stop is implicit at
     *         emit time)
     */
    @WamProperty(index = 102, type = WamType.TIMER)
    Optional<Instant> callOfferElapsedT();

    /**
     * The locally-generated 32-character call identifier — used to
     * correlate this event with logs and the matching
     * {@code <call><offer>} stanza.
     *
     * @return the call id
     */
    @WamProperty(index = 529, type = WamType.STRING)
    Optional<String> callRandomId();

    /**
     * Whether video was enabled at any point during the call.
     *
     * @return {@code true} for an audio + video call, {@code false}
     *         for audio-only
     */
    @WamProperty(index = 163, type = WamType.BOOLEAN)
    Optional<Boolean> videoEnabled();

    /**
     * Whether video was enabled at the moment the call started —
     * distinguishes "started as video" from "started as audio,
     * upgraded later".
     *
     * @return {@code true} if the call started in video mode
     */
    @WamProperty(index = 270, type = WamType.BOOLEAN)
    Optional<Boolean> videoEnabledAtCallStart();

    /**
     * Whether the call was placed/received via a shared call link.
     *
     * @return {@code true} for link-initiated calls
     */
    @WamProperty(index = 1335, type = WamType.BOOLEAN)
    Optional<Boolean> isLinkJoin();
}
