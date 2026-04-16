package com.github.auties00.cobalt.message.receive.stanza;

import com.github.auties00.cobalt.message.MessageEncryptionType;
import com.github.auties00.cobalt.meta.annotation.WhatsAppWebExport;
import com.github.auties00.cobalt.meta.annotation.WhatsAppWebModule;
import com.github.auties00.cobalt.meta.model.WhatsAppAdaptation;

import java.util.Objects;
import java.util.Optional;

/**
 * Represents a single encrypted payload within an incoming
 * {@code <message>} stanza.
 *
 * <p>A message stanza may carry multiple {@code <enc>} child nodes at
 * once: typically an {@code skmsg} payload (sender-key group
 * encryption) and a {@code pkmsg} or {@code msg} payload (per-device
 * Signal Protocol encryption). Each is an independent ciphertext that
 * will be decrypted separately by the receive pipeline. Cobalt captures
 * the parsed attributes and raw ciphertext of one such node so the
 * decryption handler can route each one to the appropriate Signal
 * cipher.
 *
 * @implNote WAWebHandleMsgParser.incomingMsgParser: maps each
 * {@code <enc>} child to an object with e2eType, encMediaType,
 * ciphertext, retryCount, and hideFail fields.
 */
@WhatsAppWebModule(moduleName = "WAWebHandleMsgParser")
public final class MessageReceiveEncryptedPayload {
    /**
     * The Signal encryption type of this payload.
     *
     * @implNote WAWebHandleMsgParser.incomingMsgParser: {@code e2eType}
     * parsed from the {@code type} attribute of the {@code <enc>} node.
     */
    private final MessageEncryptionType e2eType;

    /**
     * The optional {@code mediatype} attribute that indicates the media
     * category of the encrypted payload (for example {@code "image"},
     * {@code "video"}, {@code "ptt"}).
     *
     * @implNote WAWebHandleMsgParser.incomingMsgParser: {@code encMediaType}.
     */
    private final String encMediaType;

    /**
     * The raw encrypted bytes carried as the content of the {@code <enc>}
     * node.
     *
     * @implNote WAWebHandleMsgParser.incomingMsgParser: {@code ciphertext}.
     */
    private final byte[] ciphertext;

    /**
     * The {@code count} attribute of the enc node, indicating how many
     * times the sender has already retried sending this payload.
     *
     * @implNote WAWebHandleMsgParser.incomingMsgParser: {@code retryCount}.
     */
    private final int retryCount;

    /**
     * Whether the stanza carries {@code decrypt-fail="hide"}, which
     * instructs the receiver to silently drop the message on decryption
     * failure instead of displaying a placeholder.
     *
     * @implNote WAWebHandleMsgParser.incomingMsgParser: {@code hideFail}.
     */
    private final boolean hideFail;

    /**
     * Constructs a new encrypted payload record with all parsed fields.
     *
     * @param e2eType      the Signal encryption type, never {@code null}
     * @param encMediaType the optional media type, or {@code null}
     * @param ciphertext   the raw encrypted bytes, never {@code null}
     * @param retryCount   the retry count reported by the sender
     * @param hideFail     whether decrypt failures must be silently hidden
     *
     * @throws NullPointerException if {@code e2eType} or {@code ciphertext}
     *                              is {@code null}
     *
     * @implNote WAWebHandleMsgParser.incomingMsgParser: builds the enc
     * payload object from the {@code <enc>} node attributes and content.
     */
    @WhatsAppWebExport(moduleName = "WAWebHandleMsgParser", exports = "incomingMsgParser",
            adaptation = WhatsAppAdaptation.DIRECT)
    public MessageReceiveEncryptedPayload(
            MessageEncryptionType e2eType,
            String encMediaType,
            byte[] ciphertext,
            int retryCount,
            boolean hideFail
    ) {
        this.e2eType = Objects.requireNonNull(e2eType, "e2eType cannot be null");
        this.encMediaType = encMediaType;
        this.ciphertext = Objects.requireNonNull(ciphertext, "ciphertext cannot be null");
        this.retryCount = retryCount;
        this.hideFail = hideFail;
    }

    /**
     * Returns the Signal encryption type (one of PKMSG, MSG, SKMSG, or
     * MSMSG).
     *
     * @return the encryption type, never {@code null}
     * @implNote WAWebHandleMsgParser.incomingMsgParser: {@code e2eType}.
     */
    public MessageEncryptionType e2eType() {
        return e2eType;
    }

    /**
     * Returns the optional media type for the encrypted payload.
     *
     * @return an {@link Optional} wrapping the media type string
     * @implNote WAWebHandleMsgParser.incomingMsgParser: {@code encMediaType}.
     */
    public Optional<String> encMediaType() {
        return Optional.ofNullable(encMediaType);
    }

    /**
     * Returns the raw encrypted bytes of the {@code <enc>} node.
     *
     * @return the ciphertext, never {@code null}
     * @implNote WAWebHandleMsgParser.incomingMsgParser: {@code ciphertext}.
     */
    public byte[] ciphertext() {
        return ciphertext;
    }

    /**
     * Returns the number of retries already performed by the sender.
     *
     * @return the retry count, or {@code 0} if the attribute was absent
     * @implNote WAWebHandleMsgParser.incomingMsgParser: {@code retryCount}.
     */
    public int retryCount() {
        return retryCount;
    }

    /**
     * Returns whether the payload is marked with
     * {@code decrypt-fail="hide"}, instructing the receiver to silently
     * drop the message on decryption failure rather than surface an
     * error placeholder.
     *
     * @return {@code true} when {@code decrypt-fail} is {@code "hide"}
     * @implNote WAWebHandleMsgParser.incomingMsgParser: {@code hideFail}.
     */
    public boolean hideFail() {
        return hideFail;
    }
}
