package com.github.auties00.cobalt.message.send;

import com.github.auties00.cobalt.model.info.MessageInfo;
import com.github.auties00.cobalt.model.jid.Jid;

import java.util.List;
import java.util.Objects;

/**
 * Sealed interface representing the result of a message send operation.
 * The result can indicate success or various types of failures that
 * may require different recovery actions.
 */
public sealed interface MessageSendResult {

    /**
     * Indicates the message was successfully sent and acknowledged by the server.
     *
     * @param info      the message info with updated status
     */
    record Success(MessageInfo info) implements MessageSendResult {
        public Success {
            Objects.requireNonNull(info, "info cannot be null");
        }
    }

    /**
     * Indicates the server rejected the message due to a phash mismatch.
     * This occurs when the client's device list is stale.
     * Recovery: refresh device list and retry.
     *
     * @param expectedPhash the phash the server expected
     * @param actualPhash   the phash the client sent
     */
    record PhashMismatch(String expectedPhash, String actualPhash) implements MessageSendResult {
    }

    /**
     * Indicates the client doesn't have Signal sessions with some devices.
     * Recovery: fetch prekey bundles, establish sessions, and retry.
     *
     * @param devices the device JIDs missing sessions
     */
    record MissingPreKeys(List<Jid> devices) implements MessageSendResult {
        public MissingPreKeys {
            Objects.requireNonNull(devices, "devices cannot be null");
            devices = List.copyOf(devices);
        }
    }

    /**
     * Indicates some devices have changed their identity keys.
     * This may indicate the user has reinstalled the app.
     * Recovery: notifyStr listeners, delete old sessions, fetch new prekeys, retry.
     *
     * @param devices the device JIDs with changed identities
     */
    record IdentityChanged(List<Jid> devices) implements MessageSendResult {
        public IdentityChanged {
            Objects.requireNonNull(devices, "devices cannot be null");
            devices = List.copyOf(devices);
        }
    }

    /**
     * Indicates a network error occurred during sending.
     * Recovery: retry with exponential backoff.
     *
     * @param cause the underlying network exception
     */
    record NetworkError(Exception cause) implements MessageSendResult {
        public NetworkError {
            Objects.requireNonNull(cause, "cause cannot be null");
        }
    }

    /**
     * Indicates the server returned a protocol-level error.
     * Recovery depends on the error code.
     *
     * @param code        the error code from the server
     * @param description human-readable error description
     */
    record ProtocolError(String code, String description) implements MessageSendResult {
        public ProtocolError {
            Objects.requireNonNull(code, "code cannot be null");
        }
    }

    /**
     * Indicates sender key distribution is needed for new group members.
     * Recovery: generate and distribute sender keys, then retry the message.
     *
     * @param devices the device JIDs that need sender key distribution
     */
    record SenderKeyDistributionNeeded(List<Jid> devices) implements MessageSendResult {
        public SenderKeyDistributionNeeded {
            Objects.requireNonNull(devices, "devices cannot be null");
            devices = List.copyOf(devices);
        }
    }

    /**
     * Indicates the send was retried after an error.
     * This is used internally to track retry state.
     *
     * @param retryCount    the number of retries attempted
     * @param lastError     the last error encountered
     * @param finalResult   the final result after retries
     */
    record Retried(int retryCount, MessageSendResult lastError, MessageSendResult finalResult) implements MessageSendResult {
        public Retried {
            Objects.requireNonNull(lastError, "lastError cannot be null");
            Objects.requireNonNull(finalResult, "finalResult cannot be null");
        }
    }

    /**
     * Returns whether this result indicates a successful send.
     *
     * @return true if the message was sent successfully
     */
    default boolean isSuccess() {
        return this instanceof Success;
    }

    /**
     * Returns whether this result indicates a retryable error.
     *
     * @return true if the error can be retried
     */
    default boolean isRetryable() {
        return this instanceof PhashMismatch
                || this instanceof MissingPreKeys
                || this instanceof IdentityChanged
                || this instanceof NetworkError
                || this instanceof SenderKeyDistributionNeeded;
    }
}
