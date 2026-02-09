package com.github.auties00.cobalt.message.receive.stanza;

/**
 * Classifies the addressing type of an incoming message based on the
 * {@code from} JID and the presence/absence of a {@code participant}
 * attribute.
 *
 * <p>This enum determines how the message is processed downstream:
 * DSM unwrapping rules, receipt type, sender key distribution, and
 * placeholder generation all depend on the message type.
 *
 * @apiNote WAWebHandleMsgTypes.flow.MESSAGE_TYPE: the exact set of
 * message types used in WA Web's message handling pipeline.
 */
public enum MessageType {
    /**
     * 1:1 chat message — {@code from} is a user/LID/bot JID.
     */
    CHAT,

    /**
     * Group message — {@code from} is a group JID with a
     * {@code participant} attribute identifying the sender.
     */
    GROUP,

    /**
     * Peer broadcast message — broadcast from our own device to multiple
     * recipients, with a {@code <participants>} child listing the BCL.
     */
    PEER_BROADCAST,

    /**
     * Broadcast message from another user — broadcast JID with
     * participant identifying the sender.
     */
    OTHER_BROADCAST,

    /**
     * Direct peer status — status from our own device with direct
     * (non-skmsg) encryption, optionally with a participant list.
     */
    DIRECT_PEER_STATUS,

    /**
     * Status from another user — status broadcast JID with participant.
     */
    OTHER_STATUS,

    /**
     * Peer message from the companion device
     */
    PEER_CHAT,
}
