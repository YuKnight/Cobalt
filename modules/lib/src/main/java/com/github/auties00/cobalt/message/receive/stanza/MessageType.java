package com.github.auties00.cobalt.message.receive.stanza;

import com.github.auties00.cobalt.meta.annotation.WhatsAppWebExport;
import com.github.auties00.cobalt.meta.annotation.WhatsAppWebModule;
import com.github.auties00.cobalt.meta.model.WhatsAppAdaptation;

/**
 * Classifies the addressing shape of an incoming message based on its {@code from} JID,
 * its {@code participant} attribute, and the logged-in user's identity.
 *
 * <p>The classification is computed once during stanza parsing and drives every
 * subsequent decision in the receive pipeline: {@code DeviceSentMessage} unwrapping
 * rules, receipt type selection, sender key distribution processing, and debug
 * placeholder generation all branch on this value.
 *
 * @implNote The first six values mirror WA Web's {@code MESSAGE_TYPE} enum directly.
 * {@link #PEER_CHAT} is a Cobalt-specific adaptation that fuses the {@code CHAT} type
 * with the {@code MSG_CATEGORY.peer} check that WA Web performs separately downstream.
 */
@WhatsAppWebModule(moduleName = "WAWebHandleMsgTypes.flow")
@WhatsAppWebModule(moduleName = "WAWebHandleMsgCommon")
public enum MessageType {
    /**
     * A 1:1 chat message.
     *
     * <p>The {@code from} attribute is a user, LID, or bot JID, and the stanza's
     * {@code category} attribute is not {@code "peer"}.
     */
    @WhatsAppWebExport(moduleName = "WAWebHandleMsgTypes.flow", exports = "MESSAGE_TYPE",
            adaptation = WhatsAppAdaptation.DIRECT)
    CHAT,

    /**
     * A group message.
     *
     * <p>The {@code from} attribute is a group or community JID and the sender's device
     * is identified by the {@code participant} attribute.
     */
    @WhatsAppWebExport(moduleName = "WAWebHandleMsgTypes.flow", exports = "MESSAGE_TYPE",
            adaptation = WhatsAppAdaptation.DIRECT)
    GROUP,

    /**
     * A broadcast message sent from one of the user's own devices.
     *
     * <p>The {@code from} attribute is a broadcast list JID, the participant identifies
     * a companion device of ours, and a {@code <participants>} child lists the broadcast
     * contact list.
     */
    @WhatsAppWebExport(moduleName = "WAWebHandleMsgTypes.flow", exports = "MESSAGE_TYPE",
            adaptation = WhatsAppAdaptation.DIRECT)
    PEER_BROADCAST,

    /**
     * A broadcast message from another user addressed to us.
     *
     * <p>The {@code from} attribute is a broadcast list JID with a participant
     * identifying the remote sender.
     */
    @WhatsAppWebExport(moduleName = "WAWebHandleMsgTypes.flow", exports = "MESSAGE_TYPE",
            adaptation = WhatsAppAdaptation.DIRECT)
    OTHER_BROADCAST,

    /**
     * A status update from one of the user's own devices, using direct (non-SKMSG)
     * encryption.
     *
     * <p>Status updates posted from the same account but a different device are
     * delivered as direct-peer status so the current device can mirror them locally.
     */
    @WhatsAppWebExport(moduleName = "WAWebHandleMsgTypes.flow", exports = "MESSAGE_TYPE",
            adaptation = WhatsAppAdaptation.DIRECT)
    DIRECT_PEER_STATUS,

    /**
     * A status update from another user.
     *
     * <p>Uses the status broadcast JID with a participant attribute identifying the
     * remote poster.
     */
    @WhatsAppWebExport(moduleName = "WAWebHandleMsgTypes.flow", exports = "MESSAGE_TYPE",
            adaptation = WhatsAppAdaptation.DIRECT)
    OTHER_STATUS,

    /**
     * A peer protocol message from one of the user's own companion devices.
     *
     * <p>This is a 1:1 chat message whose {@code category} attribute is {@code "peer"}.
     * WA Web represents this as {@code MESSAGE_TYPE.CHAT} combined with
     * {@code MSG_CATEGORY.peer}; Cobalt fuses the two checks into a single enum value.
     */
    @WhatsAppWebExport(moduleName = "WAWebHandleMsgTypes.flow", exports = "MESSAGE_TYPE",
            adaptation = WhatsAppAdaptation.ADAPTED)
    @WhatsAppWebExport(moduleName = "WAWebHandleMsgCommon", exports = "MSG_CATEGORY",
            adaptation = WhatsAppAdaptation.ADAPTED)
    PEER_CHAT,
}
