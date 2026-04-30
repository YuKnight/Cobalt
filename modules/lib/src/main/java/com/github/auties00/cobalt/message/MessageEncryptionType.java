package com.github.auties00.cobalt.message;

import com.github.auties00.cobalt.meta.annotation.WhatsAppWebExport;
import com.github.auties00.cobalt.meta.annotation.WhatsAppWebModule;
import com.github.auties00.cobalt.meta.model.WhatsAppAdaptation;
import com.github.auties00.libsignal.protocol.SignalCiphertextMessage;

/**
 * Identifies the Signal-protocol ciphertext variant that WhatsApp tags onto
 * an encrypted message payload.
 *
 * <p>Every encrypted stanza that carries a message body uses an {@code <enc>}
 * child node whose {@code type} attribute names one of these variants. The
 * variant tells the receiver which Signal cipher family was used and, for bot
 * messages, that an additional inner AES-GCM layer must be peeled before the
 * plaintext is available.
 *
 * <p>Callers typically never construct values manually. Incoming wire strings
 * are parsed via {@link #fromProtocolValue(String)} and freshly produced
 * Signal ciphertexts are mapped via
 * {@link #fromSignalCiphertext(SignalCiphertextMessage)}.
 */
@WhatsAppWebModule(moduleName = "WAWebBackendJobs.flow")
public enum MessageEncryptionType {
    /**
     * PreKey Signal Message, used for the very first encrypted payload to a
     * device when no Signal session exists yet.
     *
     * <p>Carries the ephemeral keys needed to establish the session. Once the
     * recipient processes it the subsequent messages switch to {@link #MSG}.
     */
    @WhatsAppWebExport(moduleName = "WAWebBackendJobs.flow", exports = "CiphertextType",
            adaptation = WhatsAppAdaptation.DIRECT)
    PKMSG("pkmsg"),

    /**
     * Regular Signal Message, used for every encrypted payload to a device
     * after the Signal session has been established.
     */
    @WhatsAppWebExport(moduleName = "WAWebBackendJobs.flow", exports = "CiphertextType",
            adaptation = WhatsAppAdaptation.DIRECT)
    MSG("msg"),

    /**
     * Sender Key Message, used for group messages encrypted once by the
     * sender with a group key and decrypted by each member using the
     * previously distributed sender key record.
     */
    @WhatsAppWebExport(moduleName = "WAWebBackendJobs.flow", exports = "CiphertextType",
            adaptation = WhatsAppAdaptation.DIRECT)
    SKMSG("skmsg"),

    /**
     * Message Secret Message, used for bot-targeted payloads.
     *
     * <p>The outer Signal envelope wraps a {@code MessageSecretMessage}
     * protobuf whose {@code encPayload} field is an AES-GCM ciphertext keyed
     * by a secret derived from the parent message's {@code messageSecret} via
     * HKDF-SHA256.
     */
    @WhatsAppWebExport(moduleName = "WAWebBackendJobs.flow", exports = "CiphertextType",
            adaptation = WhatsAppAdaptation.DIRECT)
    MSMSG("msmsg");

    /**
     * Wire string identifying this variant in the {@code type} attribute of
     * an {@code <enc>} stanza.
     */
    @WhatsAppWebExport(moduleName = "WAWebBackendJobs.flow", exports = "CiphertextType",
            adaptation = WhatsAppAdaptation.DIRECT)
    private final String protocolValue;

    /**
     * Constructs an encryption-type constant bound to the given wire string.
     *
     * @param protocolValue the wire string for this variant
     */
    MessageEncryptionType(String protocolValue) {
        this.protocolValue = protocolValue;
    }

    /**
     * Returns the wire string used for this variant in the {@code type}
     * attribute of an {@code <enc>} stanza.
     *
     * @return one of {@code "pkmsg"}, {@code "msg"}, {@code "skmsg"}, or
     *         {@code "msmsg"}
     */
    @WhatsAppWebExport(moduleName = "WAWebBackendJobs.flow", exports = "CiphertextType",
            adaptation = WhatsAppAdaptation.DIRECT)
    public String protocolValue() {
        return protocolValue;
    }

    /**
     * Maps a freshly produced {@link SignalCiphertextMessage} to the
     * corresponding wire variant so the outbound encryption pipeline can emit
     * the correct {@code type} attribute on the {@code <enc>} node.
     *
     * @apiNote Does not return {@link #MSMSG}. Bot messages are wrapped
     * separately after the Signal layer and the caller is responsible for
     * overriding the variant to {@link #MSMSG} in that case.
     * @implNote WA Web performs this mapping inline at the emission sites
     * inside {@code WAWebSendGroupSkmsgJob} and sibling send jobs. Cobalt
     * centralises the mapping on the enum.
     * @param ciphertext the Signal ciphertext whose type byte is inspected
     * @return the matching wire variant
     * @throws IllegalArgumentException if the ciphertext reports an unknown
     *         Signal type byte
     */
    public static MessageEncryptionType fromSignalCiphertext(SignalCiphertextMessage ciphertext) {
        return switch (ciphertext.type()) {
            case SignalCiphertextMessage.PRE_KEY_TYPE -> PKMSG;
            case SignalCiphertextMessage.WHISPER_TYPE -> MSG;
            case SignalCiphertextMessage.SENDER_KEY_TYPE -> SKMSG;
            default -> throw new IllegalArgumentException(
                    "Unknown Signal ciphertext type: " + ciphertext.type()
            );
        };
    }

    /**
     * Parses an incoming {@code type} attribute value from an {@code <enc>}
     * node into the matching variant.
     *
     * @param value the wire string read from the stanza
     * @return the matching variant
     * @throws IllegalArgumentException if {@code value} is not one of the
     *         four supported wire strings
     */
    @WhatsAppWebExport(moduleName = "WAWebBackendJobs.flow", exports = "CiphertextType",
            adaptation = WhatsAppAdaptation.DIRECT)
    public static MessageEncryptionType fromProtocolValue(String value) {
        return switch (value) {
            case "pkmsg" -> PKMSG;
            case "msg" -> MSG;
            case "skmsg" -> SKMSG;
            case "msmsg" -> MSMSG;
            default -> throw new IllegalArgumentException("Unknown encryption type: " + value);
        };
    }

    /**
     * Returns whether this variant is a prekey message that establishes a
     * fresh Signal session.
     *
     * @return {@code true} if this is {@link #PKMSG}
     */
    public boolean isPreKeyMessage() {
        return this == PKMSG;
    }

    /**
     * Returns whether this variant is a sender-key message used for group
     * fanout.
     *
     * @return {@code true} if this is {@link #SKMSG}
     */
    public boolean isSenderKeyMessage() {
        return this == SKMSG;
    }
}
