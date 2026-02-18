package com.github.auties00.cobalt.model.message;

import com.github.auties00.cobalt.model.chat.ChatMessageInfo;
import com.github.auties00.cobalt.model.newsletter.NewsletterMessageInfo;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

/**
 * A common abstraction over all message info types, providing
 * access to the core metadata that every message carries regardless
 * of whether it originates from an E2E-encrypted chat or a plaintext
 * newsletter.
 *
 * <p>This sealed interface permits {@link ChatMessageInfo} for
 * Signal-encrypted chat messages and {@link NewsletterMessageInfo}
 * for plaintext newsletter messages.
 *
 * <p>The common properties exposed here mirror the base message info
 * fields extracted by WhatsApp Web in
 * {@code WAWebProcessBaseMsgInfo.msgToBaseMsgInfo}:
 * <ul>
 * <li>{@link #key()} — the message key containing id, chat JID, sender,
 *     and direction
 * <li>{@link #message()} — the decoded protobuf message container
 * <li>{@link #timestamp()} — the message timestamp ({@code t})
 * <li>{@link #status()} — the acknowledgment / delivery status ({@code ack})
 * <li>{@link #starred()} — whether the message is starred
 * <li>{@link #receipts()} — the per-recipient delivery and read receipts
 * </ul>
 *
 * @apiNote WAWebProcessBaseMsgInfo: extracts core metadata from
 * message-related objects to create a standardised base info
 * structure containing id, from, to, type, t, ack, author,
 * notifyName, invis, subtype, and viewMode.
 *
 * @since 0.1.0
 */
public sealed interface MessageInfo permits ChatMessageInfo, NewsletterMessageInfo {

    /**
     * Returns the message key that uniquely identifies this message
     * within its conversation.
     *
     * <p>The key contains the stanza-level message id, the
     * conversation (chat or newsletter) JID, the sender, and the
     * {@code fromMe} direction flag.
     *
     * @return the message key, never {@code null}
     */
    MessageKey key();

    /**
     * Returns the decoded protobuf message container.
     *
     * <p>If no message content is available (e.g. stub messages,
     * system notifications, or protobuf decoding failures), returns
     * {@link MessageContainer#empty()}.
     *
     * @return the message container, never {@code null}
     */
    MessageContainer message();

    /**
     * Returns the message timestamp, if available.
     *
     * <p>This is the epoch-second timestamp from the {@code t}
     * stanza attribute or the protobuf {@code messageTimestamp} field.
     *
     * @return an {@link Optional} containing the timestamp,
     *         or empty if not set
     */
    Optional<Instant> timestamp();

    /**
     * Returns the delivery / acknowledgment status of this message,
     * if available.
     *
     * <p>The status progresses through the standard lifecycle:
     * {@code PENDING -> SERVER_ACK -> DELIVERED -> READ -> PLAYED}.
     *
     * @return an {@link Optional} containing the status,
     *         or empty if not set
     */
    Optional<MessageStatus> status();

    /**
     * Returns whether this message has been starred (bookmarked)
     * by the user.
     *
     * @return {@code true} if the message is starred
     */
    boolean starred();

    /**
     * Returns the per-recipient delivery and read receipts for this
     * message.
     *
     * @return an unmodifiable list of receipts, never {@code null}
     */
    List<MessageReceipt> receipts();
}
