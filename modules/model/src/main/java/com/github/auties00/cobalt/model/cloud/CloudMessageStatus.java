package com.github.auties00.cobalt.model.cloud;

/**
 * The delivery status of an outbound Cloud API message, as reported in a webhook {@code statuses[]}
 * entry.
 *
 * <p>The Cloud API reports the lifecycle of an outbound message through a sequence of status updates;
 * each constant maps to one {@code status} string Meta sends.
 */
public enum CloudMessageStatus {
    /**
     * The message was accepted by the server and sent toward the recipient.
     */
    SENT,
    /**
     * The message was delivered to the recipient's device.
     */
    DELIVERED,
    /**
     * The message was read by the recipient.
     */
    READ,
    /**
     * The message failed to send; the webhook entry carries the failure details.
     */
    FAILED,
    /**
     * The message was deleted.
     */
    DELETED;

    /**
     * Parses a Cloud API {@code status} string into the matching constant.
     *
     * @param value the raw status string, for example {@code "delivered"}
     * @return the matching {@link CloudMessageStatus}, defaulting to {@link #SENT} when the string is
     *         unrecognised
     */
    public static CloudMessageStatus of(String value) {
        if (value == null) {
            return SENT;
        }
        return switch (value.toLowerCase()) {
            case "delivered" -> DELIVERED;
            case "read" -> READ;
            case "failed" -> FAILED;
            case "deleted" -> DELETED;
            default -> SENT;
        };
    }
}
