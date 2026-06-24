package com.github.auties00.cobalt.calls2.core.control;

import com.github.auties00.cobalt.calls2.core.CallEventType;
import com.github.auties00.cobalt.model.jid.Jid;

import java.util.Objects;
import java.util.Optional;

/**
 * A {@link ControlCallEvent} delivering one fragment of in-call live transcription.
 *
 * <p>The live-transcription feature emits caption fragments over the call's application-data
 * side-channel. The engine surfaces each fragment as this event, carrying the speaking
 * {@link #participant() participant} device JID, the transcribed {@link #text() text}, the ISO
 * {@link #language() language} code of the source speech, and the {@link #sequence() sequence id} the host
 * uses to correlate and order updates for a single utterance.
 *
 * @implNote This implementation models the payload of the wa-voip event {@code 0x9b}
 * ({@link CallEventType#TRANSCRIPT_RECEIVED}) of module {@code ff-tScznZ8P}, whose recovered payload is
 * {@code {jid, text, lang, seq}}, fed by a decoded {@code liveTranscriptionInfo} message on the
 * application-data stream. Cobalt carries those four fields directly; the language is optional because the
 * source message may omit it.
 * @param participant the device JID of the speaking participant; never {@code null}
 * @param text        the transcribed caption text; never {@code null}
 * @param language    the ISO language code of the source speech, or empty when absent; never {@code null}
 * @param sequence    the sequence id correlating and ordering fragments of one utterance
 */
public record TranscriptReceived(Jid participant, String text, Optional<String> language, long sequence)
        implements ControlCallEvent {
    /**
     * Validates the record components.
     *
     * @throws NullPointerException if {@code participant}, {@code text}, or {@code language} is
     *                              {@code null}
     */
    public TranscriptReceived {
        Objects.requireNonNull(participant, "participant cannot be null");
        Objects.requireNonNull(text, "text cannot be null");
        Objects.requireNonNull(language, "language cannot be null");
    }

    /**
     * {@inheritDoc}
     *
     * @return {@link CallEventType#TRANSCRIPT_RECEIVED}
     */
    @Override
    public CallEventType type() {
        return CallEventType.TRANSCRIPT_RECEIVED;
    }
}
