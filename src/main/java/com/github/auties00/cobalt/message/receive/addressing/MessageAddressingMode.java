package com.github.auties00.cobalt.message.receive.addressing;

import com.github.auties00.cobalt.model.jid.Jid;

/**
 * Represents the addressing mode used for message routing.
 * <p>
 * WhatsApp is transitioning from phone number (PN) addressing to LID addressing
 * for improved privacy. Both message sending and receiving need to understand
 * and handle these addressing modes.
 */
public sealed interface MessageAddressingMode permits LidMessageAddressingMode, PhoneNumberMessageAddressingMode, MixedMessageAddressingMode {
    /**
     * The protocol value for phone number addressing mode.
     */
    String PHONE_NUMBER_VALUE = "1";

    /**
     * The protocol value for LID addressing mode.
     */
    String LID_VALUE = "2";

    /**
     * Returns the protocol value to use in the stanza addressing_mode attribute.
     *
     * @return "1" for phone number, "2" for LID
     */
    String protocolValue();

    /**
     * Returns the primary JID for this addressing mode.
     *
     * @return the JID to use for addressing
     */
    default Jid primaryJid() {
        return switch (this) {
            case PhoneNumberMessageAddressingMode pn -> pn.phoneJid();
            case LidMessageAddressingMode lid -> lid.lidJid();
            case MixedMessageAddressingMode mixed -> mixed.preferLid() ? mixed.lidJid() : mixed.phoneJid();
        };
    }

    /**
     * Returns whether this is LID addressing mode.
     *
     * @return true if LID mode (protocol value "2")
     */
    default boolean isLidMode() {
        return LID_VALUE.equals(protocolValue());
    }

    /**
     * Returns whether this is phone number addressing mode.
     *
     * @return true if phone number mode (protocol value "1")
     */
    default boolean isPhoneNumberMode() {
        return PHONE_NUMBER_VALUE.equals(protocolValue());
    }
}
