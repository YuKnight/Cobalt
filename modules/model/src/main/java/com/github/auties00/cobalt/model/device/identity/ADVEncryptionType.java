package com.github.auties00.cobalt.model.device.identity;

import it.auties.protobuf.annotation.ProtobufEnum;
import it.auties.protobuf.annotation.ProtobufEnumIndex;

/**
 * Describes how messages destined for a specific account or companion device are encrypted.
 *
 * <p>Every WhatsApp account and every linked companion device is tagged with an
 * {@code ADVEncryptionType} inside its {@link ADVDeviceIdentity}. The value determines
 * which cryptographic layer protects messages sent to that account or device and which
 * signature prefix is used when verifying identity signatures.
 *
 * <p>The type is embedded in identity payloads exchanged during device pairing and when
 * identity metadata is attached to outgoing messages, so that both sides of a conversation
 * agree on the encryption scheme in use for each recipient.
 */
@ProtobufEnum(name = "ADVEncryptionType")
public enum ADVEncryptionType {
    /**
     * Standard end-to-end encryption using the Signal protocol.
     *
     * <p>This is the default and by far the most common value. Personal accounts and
     * companion devices linked to personal accounts use {@code E2EE}, meaning message
     * payloads are encrypted with a Signal session negotiated between the sender's and
     * recipient's device identity keys. Only the participating devices can decrypt the
     * content.
     */
    E2EE(0),

    /**
     * Messages are relayed through a trusted server-hosted layer rather than being
     * encrypted directly between user devices.
     *
     * <p>Used exclusively for business accounts that opt into the WhatsApp Business
     * hosted infrastructure (business coexistence). A hosted recipient receives messages
     * via a different signature prefix so that clients can distinguish hosted endpoints
     * from regular end-to-end encrypted ones and apply the appropriate verification
     * rules.
     */
    HOSTED(1);

    /**
     * Constructs an encryption type with the given protobuf index.
     *
     * @param index the protobuf wire index assigned to this enum constant
     */
    ADVEncryptionType(@ProtobufEnumIndex int index) {
        this.index = index;
    }

    /**
     * The protobuf wire index of this encryption type.
     */
    final int index;

    /**
     * Returns the protobuf wire index of this encryption type.
     *
     * @return the integer index used on the wire and in serialized ADV payloads
     */
    public int index() {
        return this.index;
    }
}
