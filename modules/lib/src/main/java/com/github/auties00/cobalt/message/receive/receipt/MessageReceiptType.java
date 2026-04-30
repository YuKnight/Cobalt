package com.github.auties00.cobalt.message.receive.receipt;

import com.github.auties00.cobalt.meta.annotation.WhatsAppWebExport;
import com.github.auties00.cobalt.meta.annotation.WhatsAppWebModule;
import com.github.auties00.cobalt.meta.model.WhatsAppAdaptation;

/**
 * Enumerates the receipt types sent to the server in response to incoming messages or
 * to broadcast outbound state changes.
 *
 * <p>The protocol value is encoded in the {@code type} attribute of the {@code <receipt>}
 * stanza. {@link #DELIVERY} carries a {@code null} protocol value because successful
 * active deliveries omit the attribute entirely.
 */
@WhatsAppWebModule(moduleName = "WAWebSendReceiptJobCommon")
public enum MessageReceiptType {
    /**
     * Standard delivery receipt for a successfully decrypted and processed message.
     *
     * <p>Carries a {@code null} protocol value so the {@code type} attribute is omitted
     * from the stanza, which is how the server identifies a successful active delivery.
     */
    @WhatsAppWebExport(moduleName = "WAWebSendReceiptJobCommon", exports = "RECEIPT_TYPE",
            adaptation = WhatsAppAdaptation.DIRECT)
    DELIVERY(null),

    /**
     * Receipt acknowledging that one of the user's other devices has received the message.
     */
    @WhatsAppWebExport(moduleName = "WAWebSendReceiptJobCommon", exports = "RECEIPT_TYPE",
            adaptation = WhatsAppAdaptation.DIRECT)
    SENDER("sender"),

    /**
     * Receipt for a peer protocol message (for example app state sync from a companion
     * device).
     */
    @WhatsAppWebExport(moduleName = "WAWebSendReceiptJobCommon", exports = "RECEIPT_TYPE",
            adaptation = WhatsAppAdaptation.DIRECT)
    PEER("peer_msg"),

    /**
     * Receipt sent when the recipient chat is considered inactive (muted or archived).
     */
    @WhatsAppWebExport(moduleName = "WAWebSendReceiptJobCommon", exports = "RECEIPT_TYPE",
            adaptation = WhatsAppAdaptation.DIRECT)
    INACTIVE("inactive"),

    /**
     * Retry receipt requesting the sender to re-encrypt and re-send a failed message,
     * optionally including a fresh prekey bundle for session re-establishment.
     */
    @WhatsAppWebExport(moduleName = "WAWebSendRetryReceiptJob", exports = "sendRetryReceipt",
            adaptation = WhatsAppAdaptation.DIRECT)
    RETRY("retry"),

    /**
     * Read receipt indicating that the recipient has read the message.
     */
    @WhatsAppWebExport(moduleName = "WAWebSendReceiptJobCommon", exports = "RECEIPT_TYPE",
            adaptation = WhatsAppAdaptation.DIRECT)
    READ("read"),

    /**
     * Read-self receipt mirroring a read event across the user's companion devices.
     */
    @WhatsAppWebExport(moduleName = "WAWebSendReceiptJobCommon", exports = "RECEIPT_TYPE",
            adaptation = WhatsAppAdaptation.DIRECT)
    READ_SELF("read-self"),

    /**
     * Played receipt indicating that voice or view-once media has been played by the
     * recipient.
     */
    @WhatsAppWebExport(moduleName = "WAWebSendReceiptJobCommon", exports = "RECEIPT_TYPE",
            adaptation = WhatsAppAdaptation.DIRECT)
    PLAYED("played"),

    /**
     * Played-self receipt mirroring a play event across the user's companion devices.
     */
    @WhatsAppWebExport(moduleName = "WAWebSendReceiptJobCommon", exports = "RECEIPT_TYPE",
            adaptation = WhatsAppAdaptation.DIRECT)
    PLAYED_SELF("played-self"),

    /**
     * History sync completion receipt announcing that history sync has finished for a chat.
     */
    @WhatsAppWebExport(moduleName = "WAWebSendReceiptJobCommon", exports = "RECEIPT_TYPE",
            adaptation = WhatsAppAdaptation.DIRECT)
    HISTORY_SYNC_COMPLETION("hist_sync"),

    /**
     * Server error receipt sent when a server-side error occurs for the message.
     */
    @WhatsAppWebExport(moduleName = "WAWebSendReceiptJobCommon", exports = "RECEIPT_TYPE",
            adaptation = WhatsAppAdaptation.DIRECT)
    SERVER_ERROR("server-error");

    /**
     * Protocol-level string placed in the {@code type} attribute of the receipt stanza,
     * or {@code null} to omit the attribute for the default delivery type.
     */
    private final String protocolValue;

    /**
     * Constructs a receipt type with the given protocol value.
     *
     * @param protocolValue the protocol string, or {@code null} to omit the {@code type}
     *                      attribute
     */
    MessageReceiptType(String protocolValue) {
        this.protocolValue = protocolValue;
    }

    /**
     * Returns the protocol-level value used in the {@code type} attribute of the receipt
     * stanza, or {@code null} when the attribute should be omitted for a default delivery
     * receipt.
     *
     * @return the protocol value string, or {@code null}
     */
    public String protocolValue() {
        return protocolValue;
    }
}
