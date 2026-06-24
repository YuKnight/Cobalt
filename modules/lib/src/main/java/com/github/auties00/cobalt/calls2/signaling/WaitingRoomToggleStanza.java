package com.github.auties00.cobalt.calls2.signaling;

import com.github.auties00.cobalt.model.jid.Jid;
import com.github.auties00.cobalt.node.Node;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Optional;

/**
 * Represents a {@code <waiting_room_toggle>} signal: the host flipping the waiting-room gate in-call.
 *
 * <p>A waiting-room toggle is sent by the call host to enable or disable the lobby gate of an active
 * call. It carries the universal call header, the desired {@link #enabled() gate state}, and, when the
 * gate is keyed by a call link, the {@link #linkToken() link token}. The relay answers on a
 * waiting-room toggle receipt.
 *
 * <p>This is the in-call signaling-plane toggle distinct from the call-link admin toggle issued out of
 * call as an SMAX operation; both share the {@code waiting_room_toggle} element tag.
 *
 * <p>On the wire the element is {@code <waiting_room_toggle call-id="..." call-creator="..." enabled="1"
 * link-token="..."/>}.
 *
 * @implNote This implementation models the waiting-room toggle element of the wa-voip WASM module
 * {@code ff-tScznZ8P} ({@code features/waiting_room.cc}, message type {@code 69}, toggles the
 * {@code enabled} attribute), sharing the {@code <waiting_room>} grammar (element data offset
 * {@code 0x5a594}) centralized in {@link WaitingRoomStanzas}. The element tag is taken from
 * {@link Calls2SignalingType#WAITING_ROOM_TOGGLE} and the {@code enabled} flag serializes as the
 * {@code '1'}/{@code '0'} boolean literal.
 *
 * @param callId      the call identifier; never {@code null}
 * @param callCreator the call creator's device JID; never {@code null}
 * @param enabled     the desired gate state
 * @param linkToken   the targeted call-link token, if present
 * @see Calls2SignalingType#WAITING_ROOM_TOGGLE
 */
public record WaitingRoomToggleStanza(String callId, Jid callCreator, boolean enabled, Optional<String> linkToken)
        implements CallMessage {
    /**
     * Validates the record components.
     *
     * @throws NullPointerException if {@code callId}, {@code callCreator}, or {@code linkToken} is
     *                              {@code null}
     */
    public WaitingRoomToggleStanza {
        Objects.requireNonNull(callId, "callId cannot be null");
        Objects.requireNonNull(callCreator, "callCreator cannot be null");
        Objects.requireNonNull(linkToken, "linkToken cannot be null");
    }

    /**
     * Returns a toggle signal that flips the gate of an active call without naming a call-link token.
     *
     * @param callId      the call identifier
     * @param callCreator the call creator's device JID
     * @param enabled     the desired gate state
     * @return the toggle signal
     * @throws NullPointerException if {@code callId} or {@code callCreator} is {@code null}
     */
    public static WaitingRoomToggleStanza of(String callId, Jid callCreator, boolean enabled) {
        return new WaitingRoomToggleStanza(callId, callCreator, enabled, Optional.empty());
    }

    /**
     * {@inheritDoc}
     *
     * @return {@link Calls2SignalingType#WAITING_ROOM_TOGGLE}
     */
    @Override
    public Calls2SignalingType type() {
        return Calls2SignalingType.WAITING_ROOM_TOGGLE;
    }

    /**
     * Builds the {@code <waiting_room_toggle call-id call-creator enabled link-token/>} action node.
     *
     * <p>The {@code enabled} attribute is always written as {@code '1'} or {@code '0'}; an absent link
     * token is omitted and the element carries no {@code <user>} children.
     *
     * @return the waiting-room toggle action node
     */
    @Override
    public Node toNode() {
        return WaitingRoomStanzas.build(type().wireTag().orElseThrow(), callId, callCreator,
                Optional.of(enabled), linkToken, Optional.empty(), List.of());
    }

    /**
     * Decodes a {@code <waiting_room_toggle>} action node into a {@link WaitingRoomToggleStanza}.
     *
     * <p>An absent {@code enabled} attribute classifies to {@code false}.
     *
     * @param node the {@code <waiting_room_toggle>} node
     * @return the decoded waiting-room toggle signal
     * @throws NullPointerException   if {@code node} is {@code null}
     * @throws NoSuchElementException if the required {@code call-id} or {@code call-creator} attribute
     *                                is absent
     */
    public static WaitingRoomToggleStanza of(Node node) {
        Objects.requireNonNull(node, "node cannot be null");
        var callId = node.getRequiredAttributeAsString(CallMessages.CALL_ID_ATTRIBUTE);
        var callCreator = node.getRequiredAttributeAsJid(CallMessages.CALL_CREATOR_ATTRIBUTE);
        var enabled = "1".equals(node.getAttributeAsString(WaitingRoomStanzas.ENABLED_ATTRIBUTE).orElse("0"));
        var linkToken = WaitingRoomStanzas.linkToken(node);
        return new WaitingRoomToggleStanza(callId, callCreator, enabled, linkToken);
    }
}
