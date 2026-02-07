package com.github.auties00.cobalt.message.receive.receipt;

/**
 * Receipt types supported by WhatsApp.
 */
public enum MessageReceiptType {
    /**
     * Delivery receipt - message was delivered to device.
     * Shows as double gray checkmark.
     */
    DELIVERY(null),

    /**
     * Read receipt - message was viewed.
     * Shows as double blue checkmark.
     */
    READ("read"),

    /**
     * Played receipt - voice message was played.
     * Shows as blue checkmarks with audio played indicator.
     */
    PLAYED("played"),

    /**
     * Inactive receipt - used for presence updates.
     */
    INACTIVE("inactive"),

    /**
     * Retry receipt - sent when message decryption fails.
     * Requests the sender to re-transmit the message.
     */
    RETRY("retry"),

    /**
     * Nack receipt - sent when message parsing or protocol errors occur.
     * Indicates the message cannot be processed.
     */
    NACK("nack");

    private final String value;

    MessageReceiptType(String value) {
        this.value = value;
    }

    /**
     * Returns the protocol value for the receipt type.
     *
     * @return the receipt type value, or null for delivery receipts
     */
    public String value() {
        return value;
    }
}
