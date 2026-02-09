package com.github.auties00.cobalt.message.receive.receipt;

/**
 * Types of receipt stanzas sent in response to incoming messages.
 *
 * <p>After decrypting and processing an incoming message, the client sends
 * a receipt back to the server to indicate the outcome.  The receipt type
 * determines how the server and sending client handle the acknowledgment.
 *
 * @apiNote WAWebSendReceiptJobCommon: RECEIPT_TYPE constants used when
 * building receipt stanzas.
 * WAWebHandleMsgSendReceipt.sendReceipt: selects the receipt type based
 * on the E2E processing result and message metadata.
 */
public enum MessageReceiptType {
    /**
     * Standard delivery receipt — message was successfully decrypted and
     * processed.  No {@code type} attribute is set on the receipt stanza.
     *
     * @apiNote WAWebSendDeliveryReceiptJob: omits the type attribute
     * for normal delivery receipts.
     */
    DELIVERY(null),

    /**
     * Sender receipt — the message was received by our own companion
     * device, acknowledging that we (as sender) know it was delivered.
     *
     * @apiNote WAWebSendReceiptJobCommon.RECEIPT_TYPE.SENDER
     */
    SENDER("sender"),

    /**
     * Peer message receipt — the message was a peer protocol message
     * (e.g. app state sync) from our own device.
     *
     * @apiNote WAWebSendReceiptJobCommon.RECEIPT_TYPE.PEER_MSG
     */
    PEER("peer_msg"),

    /**
     * Inactive receipt — the message was processed but the recipient
     * chat is considered inactive (e.g. muted or archived).
     *
     * @apiNote WAWebSendReceiptJobCommon.RECEIPT_TYPE.INACTIVE
     */
    INACTIVE("inactive"),

    /**
     * Retry receipt — decryption failed and we are requesting the sender
     * to re-send the message, optionally with a new prekey bundle.
     *
     * @apiNote WAWebSendRetryReceiptJob.sendRetryReceipt: builds a retry
     * receipt with registration ID, retry count, and optional key bundle.
     */
    RETRY("retry");

    private final String protocolValue;

    MessageReceiptType(String protocolValue) {
        this.protocolValue = protocolValue;
    }

    /**
     * Returns the protocol-level value used in the {@code type} attribute
     * of the receipt stanza, or {@code null} for the default delivery type.
     */
    public String protocolValue() {
        return protocolValue;
    }
}
