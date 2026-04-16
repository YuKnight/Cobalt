package com.github.auties00.cobalt.message.receive.receipt;

import com.github.auties00.cobalt.meta.annotation.WhatsAppWebExport;
import com.github.auties00.cobalt.meta.annotation.WhatsAppWebModule;
import com.github.auties00.cobalt.meta.model.WhatsAppAdaptation;

/**
 * Enumerates the receipt types that can be sent to the WhatsApp server
 * in response to incoming messages or to announce outbound state changes.
 *
 * <p>After decrypting and processing an incoming message, Cobalt sends
 * a receipt stanza back to the server to communicate the outcome. The
 * receipt type is encoded in the {@code type} attribute and informs
 * both the server and the sending client how to treat the acknowledgment.
 *
 * <p>The first four values ({@link #DELIVERY}, {@link #SENDER},
 * {@link #PEER}, {@link #INACTIVE}) are used on inbound acknowledgments;
 * {@link #RETRY} is used when decryption needs to be retried; the
 * remaining values ({@link #READ}, {@link #READ_SELF}, {@link #PLAYED},
 * {@link #PLAYED_SELF}, {@link #HISTORY_SYNC_COMPLETION},
 * {@link #SERVER_ERROR}) are used for outbound state changes or
 * aggregate receipts.
 *
 * @implNote WAWebSendReceiptJobCommon.RECEIPT_TYPE: frozen object with
 * INACTIVE, SENDER, DELIVERY, READ, READ_SELF, PLAYED, PLAYED_SELF,
 * HISTORY_SYNC_COMPLETION, SERVER_ERROR, PEER_MSG.
 * WAWebHandleMsgSendReceipt.sendReceipt: selects the receipt type based
 * on the E2E processing result and message metadata.
 */
@WhatsAppWebModule(moduleName = "WAWebSendReceiptJobCommon")
public enum MessageReceiptType {
    /**
     * The standard delivery receipt confirming that a message was
     * successfully decrypted and processed.
     *
     * <p>No {@code type} attribute is set on the receipt stanza (the
     * value is dropped/omitted), which is how WhatsApp servers identify
     * a successful active delivery.
     *
     * @implNote WAWebSendReceiptJobCommon.RECEIPT_TYPE.DELIVERY:
     * nominal value is {@code "delivery"} but when used in
     * WAWebSendDeliveryReceiptJob the {@code type} attribute is set to
     * {@code DROP_ATTR} (omitted) for active delivery receipts.
     */
    @WhatsAppWebExport(moduleName = "WAWebSendReceiptJobCommon", exports = "RECEIPT_TYPE",
            adaptation = WhatsAppAdaptation.DIRECT)
    DELIVERY(null),

    /**
     * A sender receipt acknowledging that the message has been received
     * by our own companion device.
     *
     * <p>This tells the sender (the logged-in user) that one of their
     * other devices knows the message was delivered.
     *
     * @implNote WAWebSendReceiptJobCommon.RECEIPT_TYPE.SENDER
     */
    @WhatsAppWebExport(moduleName = "WAWebSendReceiptJobCommon", exports = "RECEIPT_TYPE",
            adaptation = WhatsAppAdaptation.DIRECT)
    SENDER("sender"),

    /**
     * A peer message receipt for peer protocol messages such as app
     * state sync from our own device.
     *
     * @implNote WAWebSendReceiptJobCommon.RECEIPT_TYPE.PEER_MSG
     */
    @WhatsAppWebExport(moduleName = "WAWebSendReceiptJobCommon", exports = "RECEIPT_TYPE",
            adaptation = WhatsAppAdaptation.DIRECT)
    PEER("peer_msg"),

    /**
     * An inactive receipt sent when the recipient chat is considered
     * inactive (for example muted or archived).
     *
     * @implNote WAWebSendReceiptJobCommon.RECEIPT_TYPE.INACTIVE
     */
    @WhatsAppWebExport(moduleName = "WAWebSendReceiptJobCommon", exports = "RECEIPT_TYPE",
            adaptation = WhatsAppAdaptation.DIRECT)
    INACTIVE("inactive"),

    /**
     * A retry receipt requesting that the sender re-encrypt and re-send
     * a failed message, optionally with a new prekey bundle for session
     * re-establishment.
     *
     * @implNote WAWebSendRetryReceiptJob.sendRetryReceipt: builds a
     * retry receipt with registration ID, retry count, and optional
     * key bundle.
     */
    @WhatsAppWebExport(moduleName = "WAWebSendRetryReceiptJob", exports = "sendRetryReceipt",
            adaptation = WhatsAppAdaptation.DIRECT)
    RETRY("retry"),

    /**
     * A read receipt indicating that the recipient has read the message.
     *
     * @implNote WAWebSendReceiptJobCommon.RECEIPT_TYPE.READ
     */
    @WhatsAppWebExport(moduleName = "WAWebSendReceiptJobCommon", exports = "RECEIPT_TYPE",
            adaptation = WhatsAppAdaptation.DIRECT)
    READ("read"),

    /**
     * A read-self receipt mirroring a read event across the user's
     * companion devices.
     *
     * @implNote WAWebSendReceiptJobCommon.RECEIPT_TYPE.READ_SELF
     */
    @WhatsAppWebExport(moduleName = "WAWebSendReceiptJobCommon", exports = "RECEIPT_TYPE",
            adaptation = WhatsAppAdaptation.DIRECT)
    READ_SELF("read-self"),

    /**
     * A played receipt indicating the voice message or view-once media
     * has been played or viewed by the recipient.
     *
     * @implNote WAWebSendReceiptJobCommon.RECEIPT_TYPE.PLAYED
     */
    @WhatsAppWebExport(moduleName = "WAWebSendReceiptJobCommon", exports = "RECEIPT_TYPE",
            adaptation = WhatsAppAdaptation.DIRECT)
    PLAYED("played"),

    /**
     * A played-self receipt mirroring a play/view event across the
     * user's companion devices.
     *
     * @implNote WAWebSendReceiptJobCommon.RECEIPT_TYPE.PLAYED_SELF
     */
    @WhatsAppWebExport(moduleName = "WAWebSendReceiptJobCommon", exports = "RECEIPT_TYPE",
            adaptation = WhatsAppAdaptation.DIRECT)
    PLAYED_SELF("played-self"),

    /**
     * A history sync completion receipt announcing that history sync
     * has finished for a chat.
     *
     * @implNote WAWebSendReceiptJobCommon.RECEIPT_TYPE.HISTORY_SYNC_COMPLETION
     */
    @WhatsAppWebExport(moduleName = "WAWebSendReceiptJobCommon", exports = "RECEIPT_TYPE",
            adaptation = WhatsAppAdaptation.DIRECT)
    HISTORY_SYNC_COMPLETION("hist_sync"),

    /**
     * A server error receipt sent when a server-side error occurs for
     * the message.
     *
     * @implNote WAWebSendReceiptJobCommon.RECEIPT_TYPE.SERVER_ERROR
     */
    @WhatsAppWebExport(moduleName = "WAWebSendReceiptJobCommon", exports = "RECEIPT_TYPE",
            adaptation = WhatsAppAdaptation.DIRECT)
    SERVER_ERROR("server-error");

    /**
     * The protocol-level string value placed in the {@code type}
     * attribute of the receipt stanza, or {@code null} to omit the
     * attribute for the default delivery type.
     *
     * @implNote WAWebSendReceiptJobCommon.RECEIPT_TYPE: maps each type
     * name to its protocol string value.
     */
    private final String protocolValue;

    /**
     * Constructs a receipt type with the given protocol value.
     *
     * @param protocolValue the protocol string value, or {@code null}
     *                      to omit the {@code type} attribute
     * @implNote WAWebSendReceiptJobCommon.RECEIPT_TYPE
     */
    MessageReceiptType(String protocolValue) {
        this.protocolValue = protocolValue;
    }

    /**
     * Returns the protocol-level value used in the {@code type}
     * attribute of the receipt stanza, or {@code null} when the
     * attribute should be omitted for a default delivery receipt.
     *
     * @return the protocol value string, or {@code null}
     * @implNote WAWebSendReceiptJobCommon.RECEIPT_TYPE
     */
    public String protocolValue() {
        return protocolValue;
    }
}
