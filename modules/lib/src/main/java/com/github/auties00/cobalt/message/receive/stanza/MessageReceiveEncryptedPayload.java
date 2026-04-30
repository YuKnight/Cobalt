package com.github.auties00.cobalt.message.receive.stanza;

import com.github.auties00.cobalt.message.MessageEncryptionType;
import com.github.auties00.cobalt.meta.annotation.WhatsAppWebExport;
import com.github.auties00.cobalt.meta.annotation.WhatsAppWebModule;
import com.github.auties00.cobalt.meta.model.WhatsAppAdaptation;

import java.util.Objects;
import java.util.Optional;

/**
 * Represents a single encrypted payload within an incoming {@code <message>} stanza.
 *
 * <p>A stanza may carry multiple {@code <enc>} children at once, typically an
 * {@code skmsg} payload (sender-key group encryption) plus a {@code pkmsg} or {@code msg}
 * payload (per-device Signal encryption). Each is an independent ciphertext routed by
 * the decryption handler to the appropriate Signal cipher.
 */
@WhatsAppWebModule(moduleName = "WAWebHandleMsgParser")
public final class MessageReceiveEncryptedPayload {
    /**
     * Signal encryption type parsed from the {@code type} attribute.
     */
    private final MessageEncryptionType e2eType;

    /**
     * Optional {@code mediatype} attribute identifying the media category of the
     * encrypted payload (for example {@code "image"}, {@code "video"}, {@code "ptt"}).
     */
    private final String encMediaType;

    /**
     * Raw encrypted bytes carried as the content of the {@code <enc>} node.
     */
    private final byte[] ciphertext;

    /**
     * {@code count} attribute indicating how many times the sender has already retried
     * this payload.
     */
    private final int retryCount;

    /**
     * Whether the stanza carries {@code decrypt-fail="hide"}, instructing the receiver
     * to silently drop the message on decryption failure.
     */
    private final boolean hideFail;

    /**
     * Constructs a new encrypted payload record.
     *
     * @param e2eType      the Signal encryption type, never {@code null}
     * @param encMediaType the optional media type, or {@code null}
     * @param ciphertext   the raw encrypted bytes, never {@code null}
     * @param retryCount   the retry count reported by the sender
     * @param hideFail     whether decrypt failures must be silently hidden
     * @throws NullPointerException if {@code e2eType} or {@code ciphertext} is {@code null}
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
     * Returns the Signal encryption type (PKMSG, MSG, SKMSG, or MSMSG).
     *
     * @return the encryption type, never {@code null}
     */
    public MessageEncryptionType e2eType() {
        return e2eType;
    }

    /**
     * Returns the optional media type for the encrypted payload.
     *
     * @return an {@link Optional} wrapping the media type
     */
    public Optional<String> encMediaType() {
        return Optional.ofNullable(encMediaType);
    }

    /**
     * Returns the raw encrypted bytes.
     *
     * @return the ciphertext, never {@code null}
     */
    public byte[] ciphertext() {
        return ciphertext;
    }

    /**
     * Returns the retry count reported by the sender.
     *
     * @return the retry count, or {@code 0} if the attribute was absent
     */
    public int retryCount() {
        return retryCount;
    }

    /**
     * Returns whether the payload carries {@code decrypt-fail="hide"}, requesting the
     * receiver to silently drop the message on decryption failure rather than surface
     * an error placeholder.
     *
     * @return {@code true} when {@code decrypt-fail} is {@code "hide"}
     */
    public boolean hideFail() {
        return hideFail;
    }
}
