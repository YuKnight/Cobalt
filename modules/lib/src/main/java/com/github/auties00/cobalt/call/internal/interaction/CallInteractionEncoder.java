package com.github.auties00.cobalt.call.internal.interaction;

import com.github.auties00.cobalt.call.CallInteraction;
import com.github.auties00.cobalt.meta.annotation.WhatsAppWebModule;
import com.github.auties00.cobalt.model.call.datachannel.AppDataMessage;
import com.github.auties00.cobalt.model.call.datachannel.AppDataMessageBuilder;
import com.github.auties00.cobalt.model.call.datachannel.AppDataMessageSpec;
import com.github.auties00.cobalt.model.call.datachannel.AppDataPayloads;
import com.github.auties00.cobalt.model.call.datachannel.AppDataPayloadsBuilder;
import com.github.auties00.cobalt.model.call.datachannel.AppDataPayloadsSpec;
import com.github.auties00.cobalt.model.call.datachannel.ReactionInfo;
import com.github.auties00.cobalt.model.call.datachannel.ReactionInfoBuilder;
import com.github.auties00.cobalt.model.call.datachannel.StreamDescriptor;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Objects;

/**
 * Encodes a {@link CallInteraction} for transport on the call's pre-negotiated DataChannel.
 *
 * <p>For reactions (and any other AppData-stream interaction), the canonical wire shape is a
 * protobuf-serialised {@link AppDataMessage} built via
 * {@link #encodeReactionAsAppData(CallInteraction.Reaction, long)} (single-message) or
 * {@link #encodeAppDataBatch(java.util.List)} (batched). The bytes are sent directly through
 * the SCTP DataChannel (DTLS-encrypted by the transport); no extra SRTP wrap is needed.
 *
 * <p>The {@link #encode(CallInteraction, InteractionStreamState)} path assembles a 12-byte
 * RTP/RTCP-shaped header followed by a per-interaction body. It serves the data-plane
 * interactions only: the key-frame request (an RTCP feedback packet) and the video-upgrade
 * request. The raise-hand, lower-hand, and peer-mute interactions are NOT data-plane packets;
 * they are server-relayed {@code <call>} signaling stanzas built by
 * {@link com.github.auties00.cobalt.call.internal.signaling.CallStanza CallStanza}, so passing
 * one of them to {@link #encode(CallInteraction, InteractionStreamState)} throws.
 *
 * @implNote The raise-hand and peer-mute wire shapes were recovered by live capture: raise-hand is
 * a {@code <user_action action="raise_hand"><raise_hand raise-hand-state="0|1"/></user_action>}
 * stanza and peer-mute is a {@code <mute_v2 request-state="1"/>} stanza, both server-relayed over
 * the websocket rather than the media DataChannel. The key-frame and video-upgrade body layouts
 * remain empirical pending their own capture.
 */
@WhatsAppWebModule(moduleName = "WAWebVoipStackInterfaceWeb")
public final class CallInteractionEncoder {
    /**
     * Encodes the RTP byte-0 bitfield for version 2 with no padding, no extension, and a CSRC count of zero.
     */
    private static final int RTP_V2 = 0x80;

    /**
     * Encodes the RTP byte-1 bitfield for version 2 with the marker bit set and no padding, extension, or CSRC.
     */
    private static final int RTP_V2_MARKER = 0x90;

    /**
     * Encodes the RTP byte-1 bitfield for version 2 with the marker bit set and a CSRC count of one.
     */
    private static final int RTP_V2_MARKER_CC1 = 0x91;

    /**
     * Encodes the RTP payload type for reactions, payload type 119.
     */
    private static final int PT_REACTION = 0x77;

    /**
     * Encodes the RTP payload type for generic requests, payload type 120, shared by key-frame and peer-mute requests.
     */
    private static final int PT_REQUEST = 0x78;

    /**
     * Encodes the RTCP sender-report packet type, 200.
     */
    private static final int RTCP_SR = 0xc8;

    /**
     * Holds the fixed body length, in bytes, of a video-upgrade packet.
     *
     * @implNote This implementation uses 110, which with the 12-byte header yields the 122-byte packet observed in live
     * captures.
     */
    private static final int VIDEO_UPGRADE_BODY_LEN = 110;

    /**
     * Holds the plaintext wrapper size, in bytes, that precedes the UTF-8 emoji in a reaction body.
     *
     * @implNote This implementation uses 12, derived from live captures: the 28-byte thumbs-up packet minus the 12-byte
     * header minus the 4 UTF-8 bytes of the thumbs-up emoji is 12, and the 30-byte heart packet minus 12 minus 6 is also
     * 12.
     */
    private static final int REACTION_WRAPPER_LEN = 12;

    /**
     * Holds the plaintext wrapper size, in bytes, that precedes the target-WID UTF-8 bytes in a peer-mute or key-frame
     * request body.
     *
     * @implNote This implementation uses 16, a best-effort value: live captures saw bodies of 40 to 238 bytes depending
     * on the target-WID encoding, modeled as 16 bytes of wrapper plus the WID UTF-8 bytes, subject to revision after
     * end-to-end validation.
     */
    private static final int REQUEST_WRAPPER_LEN = 16;

    /**
     * Prevents instantiation of this stateless utility class.
     */
    private CallInteractionEncoder() {
    }

    /**
     * Encodes a {@link CallInteraction.Reaction} as a single
     * {@link AppDataMessage} carrying a {@link ReactionInfo}, ready to send on the call's
     * AppData {@link com.github.auties00.cobalt.call.internal.transport.sctp.datachannel.DataChannel
     * DataChannel} ({@link StreamDescriptor.StreamLayer#APP_DATA_STREAM0}).
     *
     * <p>This is the canonical wire shape for reactions verified against
     * {@code WAWebVoipSendSignalingXmpp}'s plaintext extractor. It supersedes the empirical
     * RTP-shaped {@link #encode(CallInteraction, InteractionStreamState)} path for the
     * reaction case, which assembled a 12-byte RTP/RTCP-shaped header followed by a
     * zero-filled wrapper before the emoji bytes; the wasm-side decoder expects a
     * protobuf-serialised {@link AppDataMessage} instead, with no RTP framing.
     *
     * <p>The bytes are sent directly through the DataChannel (which itself rides DTLS-encrypted
     * SCTP) without an extra SRTP wrap: the data plane is encrypted by the transport, not by
     * SRTP.
     *
     * @apiNote The {@code transactionId} is a sender-side monotonic identifier. Callers typically
     * source it from an {@link java.util.concurrent.atomic.AtomicLong} per call or from the
     * call's {@link InteractionStreamState}; the receiver displays the reaction as a transient
     * UI overlay and uses the id to deduplicate retransmissions.
     *
     * @param reaction      the reaction whose emoji is encoded; must not be {@code null}
     * @param transactionId the sender-side transaction id for this reaction
     * @return the protobuf-serialised bytes of the {@link AppDataMessage}, ready for the
     *         DataChannel
     * @throws NullPointerException if {@code reaction} is {@code null}
     */
    public static byte[] encodeReactionAsAppData(CallInteraction.Reaction reaction, long transactionId) {
        Objects.requireNonNull(reaction, "reaction cannot be null");
        var info = new ReactionInfoBuilder()
                .transactionId(transactionId)
                .reaction(reaction.emoji())
                .build();
        var message = new AppDataMessageBuilder()
                .reactionInfo(info)
                .build();
        return AppDataMessageSpec.encode(message);
    }

    /**
     * Wraps one or more {@link AppDataMessage} payloads in an {@link AppDataPayloads} batch
     * envelope and serialises the batch for the AppData {@link com.github.auties00.cobalt.call.internal.transport.sctp.datachannel.DataChannel
     * DataChannel}.
     *
     * <p>The runtime can coalesce multiple application-data messages into a single batched send
     * so the receiver applies them atomically; a producer with a single message still wraps it
     * in a one-entry {@link AppDataPayloads}.
     *
     * @param messages the batched payloads; must not be {@code null} or empty
     * @return the protobuf-serialised bytes of the {@link AppDataPayloads} batch
     * @throws NullPointerException     if {@code messages} is {@code null}
     * @throws IllegalArgumentException if {@code messages} is empty
     */
    public static byte[] encodeAppDataBatch(List<AppDataMessage> messages) {
        Objects.requireNonNull(messages, "messages cannot be null");
        if (messages.isEmpty()) {
            throw new IllegalArgumentException("messages cannot be empty");
        }
        var payloads = new AppDataPayloadsBuilder()
                .messages(messages)
                .build();
        return AppDataPayloadsSpec.encode(payloads);
    }

    /**
     * Encodes one interaction into a plaintext packet comprising the 12-byte RTP/RTCP-shaped header and its body.
     *
     * <p>The interaction kind selects the header tag pair, the logical stream, and the body layout. The sequence,
     * timestamp, and SSRC written into the header are drawn from {@code state}, which is mutated as a side effect: the
     * sequence and timestamp counters of the selected stream advance by one packet.
     *
     * @param interaction the interaction to encode
     * @param state       the per-call stream state from which to draw the SSRC, sequence, and timestamp
     * @return the plaintext packet, header followed by body, ready for SRTP encryption
     * @throws NullPointerException if {@code interaction} or {@code state} is {@code null}
     */
    public static byte[] encode(CallInteraction interaction, InteractionStreamState state) {
        Objects.requireNonNull(interaction, "interaction cannot be null");
        Objects.requireNonNull(state, "state cannot be null");
        return switch (interaction) {
            case CallInteraction.Reaction r -> encodeReaction(r, state);
            case CallInteraction.KeyFrameRequest _ -> encodeKeyFrameRequest(state);
            case CallInteraction.VideoUpgradeRequest _ -> encodeVideoUpgrade(state);
            case CallInteraction.RaiseHand _, CallInteraction.LowerHand _,
                 CallInteraction.PeerMuteRequest _ -> throw new IllegalArgumentException(
                    "raise-hand, lower-hand, and peer-mute are sent as server-relayed <call> signaling "
                            + "stanzas, not data-plane packets; route them through CallStanza instead");
        };
    }

    /**
     * Encodes a {@link CallInteraction.Reaction} into a reaction packet.
     *
     * <p>The packet carries the {@link #RTP_V2} byte-0 tag and the {@link #PT_REACTION} payload type, a
     * {@link #REACTION_WRAPPER_LEN}-byte zero wrapper, and the UTF-8 bytes of the reaction emoji. It is framed on the
     * {@link InteractionStreamState.Stream#REACTION} stream.
     *
     * @param reaction the reaction whose emoji is encoded
     * @param state    the per-call stream state
     * @return the encoded packet
     */
    private static byte[] encodeReaction(CallInteraction.Reaction reaction, InteractionStreamState state) {
        var emojiBytes = reaction.emoji().getBytes(StandardCharsets.UTF_8);
        var body = new byte[REACTION_WRAPPER_LEN + emojiBytes.length];
        System.arraycopy(emojiBytes, 0, body, REACTION_WRAPPER_LEN, emojiBytes.length);
        return frame(RTP_V2, PT_REACTION,
                state, InteractionStreamState.Stream.REACTION, body);
    }

    /**
     * Encodes a key-frame request into a request packet.
     *
     * <p>The packet carries the {@link #RTP_V2_MARKER} byte-0 tag and the {@link #PT_REQUEST} payload type and a
     * {@link #REQUEST_WRAPPER_LEN}-byte zero wrapper body. It is framed on the
     * {@link InteractionStreamState.Stream#CONTROL} stream.
     *
     * @param state the per-call stream state
     * @return the encoded packet
     */
    private static byte[] encodeKeyFrameRequest(InteractionStreamState state) {
        var body = new byte[REQUEST_WRAPPER_LEN];
        return frame(RTP_V2_MARKER, PT_REQUEST,
                state, InteractionStreamState.Stream.CONTROL, body);
    }

    /**
     * Encodes a video-upgrade request into a video-upgrade packet.
     *
     * <p>The packet carries the {@link #RTP_V2_MARKER_CC1} byte-0 tag and the {@link #RTCP_SR} packet type and a fixed
     * {@link #VIDEO_UPGRADE_BODY_LEN}-byte zero body. It is framed on the
     * {@link InteractionStreamState.Stream#VIDEO_UPGRADE} stream.
     *
     * @param state the per-call stream state
     * @return the encoded packet
     */
    private static byte[] encodeVideoUpgrade(InteractionStreamState state) {
        var body = new byte[VIDEO_UPGRADE_BODY_LEN];
        return frame(RTP_V2_MARKER_CC1, RTCP_SR,
                state, InteractionStreamState.Stream.VIDEO_UPGRADE, body);
    }

    /**
     * Assembles a 12-byte RTP/RTCP-shaped header and the given body into one packet.
     *
     * <p>The header is laid out big-endian as byte 0, byte 1, a 16-bit sequence number, a 32-bit timestamp, and a 32-bit
     * SSRC. The sequence and timestamp are pulled fresh from the stream state via {@link InteractionStreamState#nextSequence}
     * and {@link InteractionStreamState#nextTimestamp}, advancing those counters; the SSRC is read with
     * {@link InteractionStreamState#ssrc}. The body is copied verbatim after the header.
     *
     * @param byte0  the header byte 0, the version/padding/extension/CSRC bitfield
     * @param byte1  the header byte 1, the marker/payload-type bitfield
     * @param state  the per-call stream state
     * @param stream the logical stream whose counters and SSRC are used
     * @param body   the body bytes appended after the header
     * @return the assembled packet
     */
    private static byte[] frame(int byte0, int byte1,
                                InteractionStreamState state,
                                InteractionStreamState.Stream stream,
                                byte[] body) {
        var seq = state.nextSequence(stream);
        var ts = state.nextTimestamp(stream);
        var ssrc = state.ssrc(stream);
        var packet = new byte[12 + body.length];
        packet[0] = (byte) byte0;
        packet[1] = (byte) byte1;
        packet[2] = (byte) ((seq >>> 8) & 0xff);
        packet[3] = (byte) (seq & 0xff);
        packet[4] = (byte) ((ts >>> 24) & 0xff);
        packet[5] = (byte) ((ts >>> 16) & 0xff);
        packet[6] = (byte) ((ts >>> 8) & 0xff);
        packet[7] = (byte) (ts & 0xff);
        packet[8] = (byte) ((ssrc >>> 24) & 0xff);
        packet[9] = (byte) ((ssrc >>> 16) & 0xff);
        packet[10] = (byte) ((ssrc >>> 8) & 0xff);
        packet[11] = (byte) (ssrc & 0xff);
        System.arraycopy(body, 0, packet, 12, body.length);
        return packet;
    }
}
