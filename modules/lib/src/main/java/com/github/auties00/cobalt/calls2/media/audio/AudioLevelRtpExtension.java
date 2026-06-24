package com.github.auties00.cobalt.calls2.media.audio;

/**
 * Holds the client-to-mixer audio-level RTP header extension the call sender attaches to every audio
 * packet, carrying the encoder's measured loudness and voice-activity verdict for the frame.
 *
 * <p>The extension lets a selective-forwarding unit or a mixing peer rank speakers and gate silent
 * streams without decoding the audio: each packet advertises how loud its frame is and whether the
 * encoder judged it to contain speech. The level is the negative of the audio level in decibels
 * relative to full scale (-dBov), a magnitude in {@code [0, 127]} where {@code 0} is the loudest
 * possible signal (0 dBov) and {@code 127} is silence or below the floor; the voice-activity flag is a
 * single bit set when the encoder's voice-activity detector classified the frame as active speech.
 *
 * <p>The same level and flag serialize into either of the two standard RTP header-extension framings.
 * The one-byte framing of RFC 6464 packs the extension into a single octet whose top bit is the
 * voice-activity flag and whose low seven bits are the level, prefixed by the RFC 8285 one-byte
 * element header {@code (id << 4) | (len - 1)}; the two-byte framing of RFC 8285 carries an explicit
 * one-byte id and one-byte length ahead of the same value octet. The extmap id is negotiated per call
 * and supplied by the caller rather than fixed here, since the audio-level id WhatsApp negotiates is
 * not recoverable from the static engine.
 *
 * @param level       the audio level as a positive {@code -dBov} magnitude in {@code [0, 127]}, where
 *                    {@code 0} is loudest and {@code 127} is silence
 * @param voiceActive whether the encoder classified the frame as voice-active
 * @implNote This implementation reproduces the audio-level extension the wa-voip WASM module
 * {@code ff-tScznZ8P} attaches through {@code create_rtp_extender(audio_level)} and
 * {@code wa_audio_level_rtp_ext_utils} ({@code rev-media-audio}), feeding the per-stream audio-level
 * history. The value octet follows RFC 6464 section 3: bit {@code 0x80} is the voice-activity flag and
 * bits {@code 0x7f} are the level magnitude in {@code -dBov}, which is the form WebRTC and pjmedia both
 * emit. Whether WhatsApp negotiates the RFC 6464 one-byte or the RFC 8285 two-byte header form, and the
 * extmap id it assigns, are not on the wire in the captured signaling; this record therefore serializes
 * both header forms on demand and takes the id as a parameter, defaulting callers to the one-byte form
 * that the WebRTC audio path uses.
 */
public record AudioLevelRtpExtension(int level, boolean voiceActive) {
    /**
     * The maximum audio-level magnitude, in {@code -dBov}, representing silence.
     *
     * <p>Equal to {@code 127}; a level at this value means the frame is at or below the encoder's
     * silence floor, and a level of {@code 0} means a full-scale signal.
     */
    public static final int SILENCE_LEVEL = 127;

    /**
     * Bit mask in the value octet carrying the voice-activity flag.
     *
     * <p>Set when the frame is voice-active, per RFC 6464 section 3, occupying the most significant bit
     * of the level octet.
     */
    private static final int VOICE_ACTIVE_BIT = 0x80;

    /**
     * Bit mask in the value octet carrying the audio-level magnitude.
     *
     * <p>The low seven bits hold the {@code -dBov} level, per RFC 6464 section 3.
     */
    private static final int LEVEL_MASK = 0x7F;

    /**
     * Validates and clamps the level into the representable range.
     *
     * <p>A negative level is raised to {@code 0} (loudest) and a level above {@link #SILENCE_LEVEL} is
     * lowered to it, so the seven-bit value octet always encodes the level losslessly; the encoder's raw
     * measurement is thereby fitted to the wire form rather than rejected.
     */
    public AudioLevelRtpExtension {
        if (level < 0) {
            level = 0;
        } else if (level > SILENCE_LEVEL) {
            level = SILENCE_LEVEL;
        }
    }

    /**
     * Returns the single value octet encoding the level and voice-activity flag.
     *
     * <p>The octet is shared by both header framings: the top bit is the voice-activity flag and the low
     * seven bits are the level magnitude. This is the payload an RFC 6464 one-byte extension carries in
     * its sole octet and an RFC 8285 two-byte extension carries after its id and length.
     *
     * @return the value octet, in {@code [0, 255]}
     */
    public int valueByte() {
        var value = level & LEVEL_MASK;
        if (voiceActive) {
            value |= VOICE_ACTIVE_BIT;
        }
        return value;
    }

    /**
     * Serializes this extension as a single RFC 8285 one-byte header element.
     *
     * <p>Emits the two octets {@code [(id << 4) | (len - 1), value]}: the one-byte element header whose
     * high nibble is the extmap id and whose low nibble is the data length minus one (here {@code 0},
     * since the value is one octet), followed by the {@linkplain #valueByte() value octet}. This element
     * is appended into the RTP packet's one-byte extension block (profile {@code 0xBEDE}); the caller
     * assembles the block header and any padding.
     *
     * @param extmapId the negotiated extmap id for the audio-level extension, in {@code [1, 14]}
     * @return the two-octet one-byte header element
     * @throws IllegalArgumentException if {@code extmapId} is outside {@code [1, 14]}
     */
    public byte[] toOneByteElement(int extmapId) {
        if (extmapId < 1 || extmapId > 14) {
            throw new IllegalArgumentException("One-byte extmap id must be in [1, 14]: " + extmapId);
        }
        return new byte[]{
                (byte) ((extmapId << 4) | 0x00),
                (byte) valueByte()
        };
    }

    /**
     * Serializes this extension as a single RFC 8285 two-byte header element.
     *
     * <p>Emits the three octets {@code [id, len, value]}: an explicit one-byte extmap id, a one-byte
     * data length (here {@code 1}), and the {@linkplain #valueByte() value octet}. This element is
     * appended into the RTP packet's two-byte extension block (profile {@code 0x1000}); the caller
     * assembles the block header and any padding.
     *
     * @param extmapId the negotiated extmap id for the audio-level extension, in {@code [1, 255]}
     * @return the three-octet two-byte header element
     * @throws IllegalArgumentException if {@code extmapId} is outside {@code [1, 255]}
     */
    public byte[] toTwoByteElement(int extmapId) {
        if (extmapId < 1 || extmapId > 255) {
            throw new IllegalArgumentException("Two-byte extmap id must be in [1, 255]: " + extmapId);
        }
        return new byte[]{
                (byte) extmapId,
                (byte) 0x01,
                (byte) valueByte()
        };
    }

    /**
     * Returns the audio-level extension decoded from one RFC 6464 value octet.
     *
     * <p>Reads the voice-activity flag from the top bit and the level magnitude from the low seven bits.
     * The element header (whether one-byte or two-byte) is stripped by the caller before this is called,
     * so {@code valueByte} is the lone value octet shared by both framings.
     *
     * @param valueByte the value octet, the low eight bits of which are read
     * @return the decoded extension
     */
    public static AudioLevelRtpExtension fromValueByte(int valueByte) {
        var level = valueByte & LEVEL_MASK;
        var voiceActive = (valueByte & VOICE_ACTIVE_BIT) != 0;
        return new AudioLevelRtpExtension(level, voiceActive);
    }
}
