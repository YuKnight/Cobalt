package com.github.auties00.cobalt.calls2.signaling;

import com.github.auties00.cobalt.calls2.VideoStreamState;
import com.github.auties00.cobalt.model.jid.Jid;
import com.github.auties00.cobalt.node.Node;
import com.github.auties00.cobalt.node.NodeBuilder;

import java.util.NoSuchElementException;
import java.util.Objects;

/**
 * Represents a {@code <video_state>} in-call action: the sender broadcasts a change in its video
 * stream state.
 *
 * <p>A video-state action reports the sender's current {@link VideoStreamState}: the simple camera
 * on/off lifecycle (enabled, disabled, paused, stopped) as well as every step of the video-upgrade
 * negotiation (request, accept, reject, cancel, and their timeout variants). It carries the universal
 * call header and a numeric {@code state} attribute equal to the
 * {@linkplain VideoStreamState#wireOrdinal() wire ordinal} of the broadcast state.
 *
 * <p>On the wire the element is {@code <video_state call-id="..." call-creator="..." state="N"/>}.
 *
 * @implNote This implementation models the {@code <video_state>} element built by
 * {@code serialize_video_state} in the wa-voip WASM module {@code ff-tScznZ8P}
 * ({@code stanzas/media.cc}), carried in message-container type {@code 15}
 * ({@link Calls2SignalingType#VIDEO_STATE}); the matching ack is taxonomy ordinal {@code 20}. The
 * {@code state} attribute is the engine {@code kVideoState*} ordinal carried by
 * {@link VideoStreamState}, decoded through {@link VideoStreamState#ofWireOrdinal(int)} so an
 * unrecognized ordinal collapses to {@link VideoStreamState#UNKNOWN_PEER}. Attributes are stamped over
 * the common header written by {@code populate_common_call_attr} (fn11591): {@code call-id} (data
 * offset {@code 0x888f9}) and {@code call-creator} (data offset {@code 0x45ea5}).
 *
 * @param callId      the call identifier; never {@code null}
 * @param callCreator the call creator's device JID; never {@code null}
 * @param state       the broadcast video stream state; never {@code null}
 * @see Calls2SignalingType#VIDEO_STATE
 * @see VideoStreamState
 */
public record VideoStateStanza(String callId, Jid callCreator, VideoStreamState state)
        implements InCallActionStanza {
    /**
     * The wire element tag for a video-state action.
     */
    public static final String ELEMENT = "video_state";

    /**
     * The wire attribute naming the video stream state ordinal.
     */
    private static final String STATE_ATTRIBUTE = "state";

    /**
     * Validates the record components.
     *
     * @throws NullPointerException if {@code callId}, {@code callCreator}, or {@code state} is
     *                              {@code null}
     */
    public VideoStateStanza {
        Objects.requireNonNull(callId, "callId cannot be null");
        Objects.requireNonNull(callCreator, "callCreator cannot be null");
        Objects.requireNonNull(state, "state cannot be null");
    }

    /**
     * {@inheritDoc}
     *
     * @return {@link Calls2SignalingType#VIDEO_STATE}
     */
    @Override
    public Calls2SignalingType type() {
        return Calls2SignalingType.VIDEO_STATE;
    }

    /**
     * Builds the {@code <video_state call-id call-creator state/>} action node.
     *
     * <p>The {@code state} attribute is the {@linkplain VideoStreamState#wireOrdinal() wire ordinal}
     * of {@link #state()}, not its Java {@link Enum#ordinal()}.
     *
     * @return the video-state action node
     */
    @Override
    public Node toNode() {
        return CallMessages.stampHeader(new NodeBuilder().description(ELEMENT), callId, callCreator)
                .attribute(STATE_ATTRIBUTE, state.wireOrdinal())
                .build();
    }

    /**
     * Decodes a {@code <video_state>} action node into a {@link VideoStateStanza}.
     *
     * <p>The {@code state} attribute is resolved through {@link VideoStreamState#ofWireOrdinal(int)};
     * an absent attribute decodes to ordinal {@code 0} ({@link VideoStreamState#DISABLED}).
     *
     * @param node the {@code <video_state>} node
     * @return the decoded video-state action
     * @throws NullPointerException   if {@code node} is {@code null}
     * @throws NoSuchElementException if the required {@code call-id} or {@code call-creator} attribute
     *                                is absent
     */
    public static VideoStateStanza of(Node node) {
        Objects.requireNonNull(node, "node cannot be null");
        var callId = node.getRequiredAttributeAsString(CallMessages.CALL_ID_ATTRIBUTE);
        var callCreator = node.getRequiredAttributeAsJid(CallMessages.CALL_CREATOR_ATTRIBUTE);
        var state = VideoStreamState.ofWireOrdinal(node.getAttributeAsInt(STATE_ATTRIBUTE, 0));
        return new VideoStateStanza(callId, callCreator, state);
    }
}
