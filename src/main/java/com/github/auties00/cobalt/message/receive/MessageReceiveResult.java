package com.github.auties00.cobalt.message.receive;

import com.github.auties00.cobalt.exception.MessageDecryptionException;
import com.github.auties00.cobalt.model.info.ChatMessageInfo;

import java.util.Objects;

/**
 * Sealed interface representing the result of a message receive operation.
 * The result determines what receipt should be sent back to the sender.
 */
public sealed interface MessageReceiveResult {

    /**
     * Indicates the message was successfully decrypted, parsed, and stored.
     * A delivery receipt should be sent.
     *
     * @param info the successfully processed message info
     */
    record Success(ChatMessageInfo info) implements MessageReceiveResult {
        public Success {
            Objects.requireNonNull(info, "info cannot be null");
        }
    }

    /**
     * Indicates decryption failed due to Signal protocol issues.
     * A retry receipt should be sent so the sender can re-send.
     *
     * @param reason    the retry reason to send in the receipt
     * @param messageId the message ID from the stanza
     * @param cause     the underlying exception, if available
     */
    record DecryptionFailure(
            MessageDecryptionException.Reason reason,
            String messageId,
            Exception cause
    ) implements MessageReceiveResult {
        public DecryptionFailure {
            Objects.requireNonNull(reason, "reason cannot be null");
            Objects.requireNonNull(messageId, "messageId cannot be null");
        }

        public DecryptionFailure(MessageDecryptionException.Reason reason, String messageId) {
            this(reason, messageId, null);
        }
    }

    /**
     * Indicates the protobuf could not be parsed or validated.
     * A nack receipt should be sent indicating a protocol error.
     *
     * @param errorCode   the error code to send in the nack receipt
     * @param messageId   the message ID from the stanza
     * @param description human-readable error description
     */
    record ParseError(
            String errorCode,
            String messageId,
            String description
    ) implements MessageReceiveResult {
        public ParseError {
            Objects.requireNonNull(errorCode, "errorCode cannot be null");
            Objects.requireNonNull(messageId, "messageId cannot be null");
        }

        public ParseError(String messageId, String description) {
            this("400", messageId, description);
        }
    }

    /**
     * Indicates this is a duplicate message that was already processed.
     * No receipt should be sent (or a delivery receipt if configured).
     * <p>
     * Edge case 58: If suppressNotification is true (hideFail flag set or reaction/poll vote),
     * the duplicate should be silently ignored without logging a warning.
     *
     * @param messageId            the duplicate message ID
     * @param suppressNotification true if notification should be suppressed (hideFail or reaction/poll)
     */
    record Duplicate(String messageId, boolean suppressNotification) implements MessageReceiveResult {
        public Duplicate {
            Objects.requireNonNull(messageId, "messageId cannot be null");
        }

        /**
         * Constructor for duplicates that should show a warning.
         */
        public Duplicate(String messageId) {
            this(messageId, false);
        }
    }

    /**
     * Indicates ADV signature validation failed for a companion device message.
     * A retry receipt with ADV_FAILURE reason should be sent.
     *
     * @param messageId the message ID from the stanza
     * @param reason    human-readable reason for the failure
     */
    record AdvValidationFailure(
            String messageId,
            String reason
    ) implements MessageReceiveResult {
        public AdvValidationFailure {
            Objects.requireNonNull(messageId, "messageId cannot be null");
        }
    }

    /**
     * Indicates the message stanza was malformed or missing required fields.
     * A nack receipt should be sent.
     *
     * @param messageId   the message ID, if parseable
     * @param description what was wrong with the stanza
     */
    record InvalidStanza(
            String messageId,
            String description
    ) implements MessageReceiveResult {
    }

    /**
     * Indicates the message is not for this device.
     * No action needed - just ignore.
     *
     * @param messageId the message ID
     * @param reason    why the message was skipped
     */
    record NotForThisDevice(
            String messageId,
            String reason
    ) implements MessageReceiveResult {
    }

    /**
     * Returns whether a retry receipt should be sent for this result.
     * <p>
     * Note: Some decryption failures (like DUPLICATE_MESSAGE) should not
     * trigger retry receipts - use {@link MessageDecryptionException.Reason#shouldSendRetryReceipt()}.
     *
     * @return true if a retry receipt is needed
     */
    default boolean requiresRetryReceipt() {
        return switch (this) {
            case DecryptionFailure df -> df.reason().shouldSendRetryReceipt();
            case AdvValidationFailure _ -> true;
            default -> false;
        };
    }

    /**
     * Returns whether a nack receipt should be sent for this result.
     *
     * @return true if a nack receipt is needed
     */
    default boolean requiresNackReceipt() {
        return this instanceof ParseError
                || this instanceof InvalidStanza;
    }

    /**
     * Returns whether a delivery receipt should be sent for this result.
     *
     * @return true if a delivery receipt should be sent
     */
    default boolean requiresDeliveryReceipt() {
        return this instanceof Success;
    }

    /**
     * Returns whether this result indicates successful processing.
     *
     * @return true if the message was processed successfully
     */
    default boolean isSuccess() {
        return this instanceof Success;
    }

    /**
     * Returns the message ID from this result, if available.
     *
     * @return the message ID, or null if not available
     */
    default String messageId() {
        return switch (this) {
            case Success s -> s.info().id();
            case DecryptionFailure df -> df.messageId();
            case ParseError pe -> pe.messageId();
            case Duplicate d -> d.messageId();
            case AdvValidationFailure avf -> avf.messageId();
            case InvalidStanza is -> is.messageId();
            case NotForThisDevice nf -> nf.messageId();
        };
    }
}
