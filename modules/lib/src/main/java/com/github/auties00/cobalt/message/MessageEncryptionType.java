package com.github.auties00.cobalt.message;

import com.github.auties00.cobalt.meta.annotation.WhatsAppWebExport;
import com.github.auties00.cobalt.meta.annotation.WhatsAppWebModule;
import com.github.auties00.cobalt.meta.model.WhatsAppAdaptation;
import com.github.auties00.libsignal.protocol.SignalCiphertextMessage;

/**
 * Enumerates the four Signal-protocol ciphertext variants that WhatsApp uses to
 * tag encrypted message payloads on the wire.
 *
 * <p>Every encrypted stanza that carries a message body uses an {@code <enc>}
 * child node whose {@code type} attribute names one of these variants. The
 * variant tells the receiver which Signal cipher family was used and, for bot
 * messages, that an additional inner AES-GCM layer must be peeled before the
 * plaintext is available.
 *
 * <p>The enum is consumed by both the outbound encryption path (which
 * advertises the chosen variant when wrapping a fresh payload) and the inbound
 * decryption path (which switches on the variant to pick the right decryption
 * routine). Callers typically never construct values manually: instead they
 * either parse an incoming string via {@link #fromProtocolValue(String)} or
 * map a freshly produced {@link SignalCiphertextMessage} via
 * {@link #fromSignalCiphertext(SignalCiphertextMessage)}.
 *
 * @implNote WAWebBackendJobs.flow.CiphertextType: defined as
 * {@code $InternalEnum({Skmsg:"skmsg",Pkmsg:"pkmsg",Msg:"msg",Msmsg:"msmsg"})}.
 * The four variants map directly to the four supported values of the
 * {@code type} attribute on {@code <enc>} nodes. The Msmsg variant is
 * dispatched in {@code WAWebMsgProcessingDecryptEnc.decryptEnc} to
 * {@code WAWebBotMessageSecret.decryptMsmsgBotMessage} for the inner AES-GCM
 * stripping pass.
 */
@WhatsAppWebModule(moduleName = "WAWebBackendJobs.flow")
public enum MessageEncryptionType {
    /**
     * PreKey Signal Message, used for the very first encrypted payload to a
     * given device when no Signal session exists yet.
     *
     * <p>Carries the ephemeral keys needed to establish the session; once the
     * recipient processes it the subsequent messages switch to {@link #MSG}.
     *
     * @implNote WAWebBackendJobs.flow.CiphertextType.Pkmsg: string literal
     * {@code "pkmsg"}.
     */
    @WhatsAppWebExport(moduleName = "WAWebBackendJobs.flow", exports = "CiphertextType",
            adaptation = WhatsAppAdaptation.DIRECT)
    PKMSG("pkmsg"),

    /**
     * Regular Signal Message, used for every encrypted payload to a device
     * after the Signal session has been established.
     *
     * @implNote WAWebBackendJobs.flow.CiphertextType.Msg: string literal
     * {@code "msg"}.
     */
    @WhatsAppWebExport(moduleName = "WAWebBackendJobs.flow", exports = "CiphertextType",
            adaptation = WhatsAppAdaptation.DIRECT)
    MSG("msg"),

    /**
     * Sender Key Message, used for group messages that are encrypted once by
     * the sender with a group key and decrypted by each group member using the
     * previously distributed sender key record.
     *
     * @implNote WAWebBackendJobs.flow.CiphertextType.Skmsg: string literal
     * {@code "skmsg"}.
     */
    @WhatsAppWebExport(moduleName = "WAWebBackendJobs.flow", exports = "CiphertextType",
            adaptation = WhatsAppAdaptation.DIRECT)
    SKMSG("skmsg"),

    /**
     * Message Secret Message, used for bot-targeted payloads. The outer Signal
     * envelope wraps a {@code MessageSecretMessage} protobuf whose
     * {@code encPayload} field is an AES-GCM ciphertext keyed by a secret
     * derived from the parent message's {@code messageSecret} via HKDF-SHA256.
     *
     * @implNote WAWebBackendJobs.flow.CiphertextType.Msmsg: string literal
     * {@code "msmsg"}. Dispatched in {@code WAWebMsgProcessingDecryptEnc.decryptEnc}
     * to {@code WAWebBotMessageSecret.decryptMsmsgBotMessage} for the inner AES-GCM
     * stripping pass.
     */
    @WhatsAppWebExport(moduleName = "WAWebBackendJobs.flow", exports = "CiphertextType",
            adaptation = WhatsAppAdaptation.DIRECT)
    MSMSG("msmsg");

    /**
     * The wire string that identifies this variant in the {@code type}
     * attribute of an {@code <enc>} stanza.
     *
     * @implNote WAWebBackendJobs.flow.CiphertextType: the string literal
     * associated with each enum variant in the {@code $InternalEnum} call.
     */
    @WhatsAppWebExport(moduleName = "WAWebBackendJobs.flow", exports = "CiphertextType",
            adaptation = WhatsAppAdaptation.DIRECT)
    private final String protocolValue;

    /**
     * Constructs an encryption-type constant bound to the given wire string.
     *
     * @param protocolValue the wire string for this variant
     * @implNote WAWebBackendJobs.flow.CiphertextType: each variant is bound
     * to a string literal ("pkmsg", "msg", "skmsg", "msmsg").
     */
    MessageEncryptionType(String protocolValue) {
        this.protocolValue = protocolValue;
    }

    /**
     * Returns the wire string used for this variant in the {@code type}
     * attribute of an {@code <enc>} stanza.
     *
     * @return the wire string (one of {@code "pkmsg"}, {@code "msg"},
     *         {@code "skmsg"}, {@code "msmsg"})
     * @implNote WAWebBackendJobs.flow.CiphertextType: direct string
     * representation of the enum variant.
     */
    @WhatsAppWebExport(moduleName = "WAWebBackendJobs.flow", exports = "CiphertextType",
            adaptation = WhatsAppAdaptation.DIRECT)
    public String protocolValue() {
        return protocolValue;
    }

    /**
     * Maps a freshly produced {@link SignalCiphertextMessage} to the
     * corresponding wire variant so that the outbound encryption pipeline can
     * emit the correct {@code type} attribute on the {@code <enc>} node.
     *
     * <p>Does not return {@link #MSMSG}: bot messages are wrapped separately
     * after the Signal layer and the caller is responsible for overriding the
     * variant to {@link #MSMSG} in that case.
     *
     * @param ciphertext the Signal ciphertext whose type byte is inspected
     * @return the matching wire variant
     * @throws IllegalArgumentException if the ciphertext reports an unknown
     *         Signal type byte
     * @implNote ADAPTED: WA Web performs this mapping inline at the emission
     * sites inside {@code WAWebSendGroupSkmsgJob} and sibling send jobs by
     * testing {@code WAWebSignal.Signal.Type} constants against the local
     * variable holding the ciphertext. Cobalt centralises the mapping on the
     * enum so the send pipeline can call a single helper.
     */
    public static MessageEncryptionType fromSignalCiphertext(SignalCiphertextMessage ciphertext) {
        // ADAPTED: WAWebBackendJobs.flow.CiphertextType
        // Maps the numeric Signal ciphertext type byte onto the matching wire variant

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
     * @implNote WAWebBackendJobs.flow.CiphertextType: reverse lookup from
     * the string literal to the enum variant.
     */
    @WhatsAppWebExport(moduleName = "WAWebBackendJobs.flow", exports = "CiphertextType",
            adaptation = WhatsAppAdaptation.DIRECT)
    public static MessageEncryptionType fromProtocolValue(String value) {
        // WAWebBackendJobs.flow.CiphertextType
        // Reverse lookup of the $InternalEnum string literal to the enum variant

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
