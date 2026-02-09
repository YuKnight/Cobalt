package com.github.auties00.cobalt.model.message.standard;

import com.github.auties00.cobalt.message.addon.MessageAddonEncryption;
import com.github.auties00.cobalt.message.addon.MessageAddonType;
import com.github.auties00.cobalt.model.info.ChatMessageInfo;
import com.github.auties00.cobalt.model.jid.Jid;
import com.github.auties00.cobalt.model.message.common.ChatMessageKey;
import com.github.auties00.cobalt.model.message.common.EncryptedMessage;
import com.github.auties00.cobalt.model.message.common.Message;
import com.github.auties00.cobalt.model.poll.*;
import com.github.auties00.cobalt.util.Clock;
import it.auties.protobuf.annotation.ProtobufBuilder;
import it.auties.protobuf.annotation.ProtobufMessage;
import it.auties.protobuf.annotation.ProtobufProperty;
import it.auties.protobuf.model.ProtobufType;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.ZonedDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * A model class that represents a message holding a vote for a poll inside.
 * <p>
 * Use {@link PollUpdateMessageSimpleBuilder} to create outgoing poll votes.
 * It encrypts the selected options automatically.
 *
 * @apiNote WAWebPollVoteEncryptMsgData.encryptPollVoteMsgData: encrypts
 * vote options as PollVoteMessage protobuf, then wraps in PollEncValue.
 */
@ProtobufMessage(name = "Message.PollUpdateMessage")
public final class PollUpdateMessage implements Message, EncryptedMessage {
    @ProtobufProperty(index = 1, type = ProtobufType.MESSAGE)
    final ChatMessageKey pollCreationMessageKey;

    @ProtobufProperty(index = 2, type = ProtobufType.MESSAGE)
    PollUpdateEncryptedMetadata encryptedMetadata;

    @ProtobufProperty(index = 3, type = ProtobufType.MESSAGE)
    final PollUpdateMessageMetadata metadata;

    @ProtobufProperty(index = 4, type = ProtobufType.INT64)
    final long senderTimestampMilliseconds;

    /**
     * Plaintext vote option names, retained for newsletter poll votes
     * where the SMAX stanza sends option names as {@code <vote>} children
     * instead of encrypted hashes.
     *
     * <p>This field is local-only and not serialized on the wire.
     *
     * @apiNote WASmaxOutMessagePublishNewsletterPollVoteMixin: sends
     * plaintext option names, not the encrypted PollEncValue.
     */
    List<String> voteOptionNames;

    PollUpdateMessage(ChatMessageKey pollCreationMessageKey, PollUpdateEncryptedMetadata encryptedMetadata, PollUpdateMessageMetadata metadata, long senderTimestampMilliseconds) {
        this.pollCreationMessageKey = pollCreationMessageKey;
        this.encryptedMetadata = encryptedMetadata;
        this.metadata = metadata;
        this.senderTimestampMilliseconds = senderTimestampMilliseconds;
    }

    /**
     * Constructs a new poll vote message with encrypted vote data.
     *
     * <p>The selected options are SHA-256 hashed, serialized as a
     * {@code PollVoteMessage} protobuf, and AES-GCM encrypted using the
     * parent poll's messageSecret.
     *
     * @param poll       the parent poll creation message (must contain messageSecret)
     * @param votes      the options to vote for (empty list to revoke all votes)
     * @param selfJid    the sender's user JID (used for HKDF info and AAD)
     * @return the encrypted poll update message
     * @throws IllegalArgumentException if poll is not a POLL_CREATION type or has no messageSecret
     *
     * @apiNote WAWebPollVoteEncryptMsgData: hashes option names with SHA-256,
     * encodes as PollVoteMessage, encrypts via WAWebAddonEncryption.encryptAddOn.
     */
    @ProtobufBuilder(className = "PollUpdateMessageSimpleBuilder")
    static PollUpdateMessage simpleBuilder(ChatMessageInfo poll, List<PollOption> votes, Jid selfJid) {
        if (poll.message().type() != Type.POLL_CREATION) {
            throw new IllegalArgumentException("Expected a poll, got %s".formatted(poll.message().type()));
        }

        var parentSecret = poll.messageSecret()
                .orElseThrow(() -> new IllegalArgumentException("Parent poll has no messageSecret"));
        var parentKey = poll.key();
        var originalSender = parentKey.senderJid()
                .orElse(parentKey.fromMe() ? selfJid : parentKey.chatJid())
                .toUserJid();

        // WAWebPollsProtobufConversion: SHA-256 hash each selected option name
        var selectedHashes = votes.stream()
                .map(option -> sha256(option.name().getBytes(StandardCharsets.UTF_8)))
                .toList();

        // WAWebPollVoteEncryptMsgData: encode as PollVoteMessage protobuf
        var voteMessage = new PollUpdateEncryptedOptionsBuilder()
                .selectedOptions(selectedHashes)
                .build();
        var plaintext = PollUpdateEncryptedOptionsSpec.encode(voteMessage);

        // WAWebAddonEncryption.encryptAddOn: dual encrypt with parent's messageSecret
        var encrypted = MessageAddonEncryption.encrypt(
                plaintext, parentSecret, parentKey.id(),
                originalSender, selfJid.toUserJid(),
                MessageAddonType.POLL_VOTE);

        var metadata = new PollUpdateEncryptedMetadataBuilder()
                .payload(encrypted.ciphertext())
                .iv(encrypted.iv())
                .build();
        var result = new PollUpdateMessageBuilder()
                .pollCreationMessageKey(parentKey)
                .senderTimestampMilliseconds(Clock.nowMilliseconds())
                .encryptedMetadata(metadata)
                .build();
        result.voteOptionNames = votes.stream().map(PollOption::name).toList();
        return result;
    }

    private static byte[] sha256(byte[] input) {
        try {
            return MessageDigest.getInstance("SHA-256").digest(input);
        } catch (NoSuchAlgorithmException e) {
            throw new UnsupportedOperationException("SHA-256 not available", e);
        }
    }

    public ChatMessageKey pollCreationMessageKey() {
        return pollCreationMessageKey;
    }

    public Optional<PollUpdateEncryptedMetadata> encryptedMetadata() {
        return Optional.ofNullable(encryptedMetadata);
    }

    public void setEncryptedMetadata(PollUpdateEncryptedMetadata encryptedMetadata) {
        this.encryptedMetadata = encryptedMetadata;
    }

    public Optional<PollUpdateMessageMetadata> metadata() {
        return Optional.ofNullable(metadata);
    }

    public long senderTimestampMilliseconds() {
        return senderTimestampMilliseconds;
    }

    public Optional<ZonedDateTime> senderTimestamp() {
        return Clock.parseSeconds(senderTimestampMilliseconds);
    }

    /**
     * Returns the plaintext vote option names, if available.
     *
     * <p>Only populated when created via {@link PollUpdateMessageSimpleBuilder}.
     * Used by the newsletter send path which sends option names as
     * plaintext {@code <vote>} nodes.
     *
     * @return unmodifiable list of option names, or empty list
     */
    public List<String> voteOptionNames() {
        return voteOptionNames != null ? Collections.unmodifiableList(voteOptionNames) : List.of();
    }

    @Override
    public Type type() {
        return Type.POLL_UPDATE;
    }

    @Override
    public Category category() {
        return Category.STANDARD;
    }
}
