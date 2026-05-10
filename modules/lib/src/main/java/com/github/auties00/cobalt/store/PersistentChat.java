package com.github.auties00.cobalt.store;

import com.github.auties00.cobalt.model.chat.*;
import com.github.auties00.cobalt.model.chat.group.GroupParticipant;
import com.github.auties00.cobalt.model.jid.Jid;
import com.github.auties00.cobalt.model.media.MediaVisibility;
import com.github.auties00.cobalt.model.message.PrivacySystemMessage;
import com.github.auties00.cobalt.model.setting.WallpaperSettings;
import it.auties.protobuf.annotation.ProtobufMessage;

import java.time.Instant;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;

/**
 * Metadata-only {@link Chat}
 * subclass used as the value type of the {@link PersistentStore#chats}
 * map. Carries no message field of its own — every message-bearing
 * accessor delegates to the parent store's
 * {@link PersistentMessageStore} so chat messages stay off the JVM heap.
 *
 * <p>The {@link #attach(PersistentMessageStore)} hook is invoked by the
 * enclosing {@code PersistentStore} immediately after construction or
 * deserialisation: it wires the LMDB facade and seeds the
 * {@link #messageCount} counter with the current LMDB cursor count
 * so subsequent {@link #messageCount()} calls answer in {@code O(1)}.
 */
@ProtobufMessage
final class PersistentChat extends Chat {
    /**
     * The LMDB facade providing the message storage. Wired by the
     * owning store via {@link #attach(PersistentMessageStore)}; never
     * persisted.
     */
    private volatile PersistentMessageStore messageStore;

    /**
     * Cached message count. Maintained incrementally on every
     * {@link #addMessage} / {@link #removeMessage} call and
     * re-seeded on attachment.
     */
    private final AtomicInteger messageCount;

    /**
     * Constructs a new metadata-only chat; invoked by the generated
     * protobuf builder and deserializer.
     */
    PersistentChat(Jid jid, Jid newJid, Jid oldJid, Instant lastMsgTimestamp, Integer unreadCount, Boolean readOnly, Boolean endOfHistoryTransfer, ChatEphemeralTimer ephemeralExpiration, Instant ephemeralSettingTimestamp, EndOfHistoryTransferType endOfHistoryTransferType, Instant conversationTimestamp, String name, String pHash, Boolean notSpam, Boolean archived, ChatDisappearingMode disappearingMode, Integer unreadMentionCount, Boolean markedAsUnread, List<GroupParticipant> participant, byte[] tcToken, Instant tcTokenTimestamp, byte[] contactPrimaryIdentityKey, Instant pinned, ChatMute mute, WallpaperSettings wallpaper, MediaVisibility mediaVisibility, Instant tcTokenSenderTimestamp, Boolean suspended, Boolean terminated, Instant createdAt, String createdBy, String description, Boolean support, Boolean isParentGroup, String parentGroupId, Boolean isDefaultSubgroup, String displayName, Jid phoneNumberJid, Boolean shareOwnPhoneNumber, Boolean phoneNumberhDuplicateLidThread, Jid lid, String username, String lidOriginType, Integer commentsCount, Boolean locked, PrivacySystemMessage systemMessageToInsert, Boolean capiCreatedGroup, Jid accountLid, Boolean limitSharing, Instant limitSharingSettingTimestamp, ChatLimitSharing.TriggerType limitSharingTrigger, Boolean limitSharingInitiatedByMe, Boolean maibaAiThreadEnabled) {
        super(jid, newJid, oldJid, lastMsgTimestamp, unreadCount, readOnly, endOfHistoryTransfer, ephemeralExpiration, ephemeralSettingTimestamp, endOfHistoryTransferType, conversationTimestamp, name, pHash, notSpam, archived, disappearingMode, unreadMentionCount, markedAsUnread, participant, tcToken, tcTokenTimestamp, contactPrimaryIdentityKey, pinned, mute, wallpaper, mediaVisibility, tcTokenSenderTimestamp, suspended, terminated, createdAt, createdBy, description, support, isParentGroup, parentGroupId, isDefaultSubgroup, displayName, phoneNumberJid, shareOwnPhoneNumber, phoneNumberhDuplicateLidThread, lid, username, lidOriginType, commentsCount, locked, systemMessageToInsert, capiCreatedGroup, accountLid, limitSharing, limitSharingSettingTimestamp, limitSharingTrigger, limitSharingInitiatedByMe, maibaAiThreadEnabled);
        this.messageCount = new AtomicInteger();
    }

    /**
     * Wires the LMDB facade into this chat and seeds the cached
     * message count by walking the underlying cursor once.
     *
     * @param messageStore the LMDB facade owned by the parent store
     */
    void attach(PersistentMessageStore messageStore) {
        this.messageStore = messageStore;
        this.messageCount.set(messageStore.countChatMessages(jid()));
    }

    @Override
    public Stream<ChatMessageInfo> messages() {
        return messageStore.streamChatMessages(jid());
    }

    @Override
    public int messageCount() {
        return messageCount.get();
    }

    @Override
    public void addMessage(ChatMessageInfo info) {
        Objects.requireNonNull(info, "info cannot be null");
        messageStore.putChatMessage(jid(), info);
        messageCount.incrementAndGet();
    }

    @Override
    public boolean removeMessage(String id) {
        if (id == null) {
            return false;
        }
        var removed = messageStore.removeChatMessage(jid(), id);
        if (removed) {
            messageCount.decrementAndGet();
        }
        return removed;
    }

    @Override
    public void removeMessages() {
        messageStore.removeChatMessages(jid());
        messageCount.set(0);
    }

    @Override
    public Optional<ChatMessageInfo> getMessageById(String id) {
        return id == null ? Optional.empty() : messageStore.getChatMessage(jid(), id);
    }

    @Override
    public Optional<ChatMessageInfo> newestMessage() {
        return messageStore.newestChatMessage(jid());
    }

    @Override
    public Optional<ChatMessageInfo> oldestMessage() {
        return messageStore.oldestChatMessage(jid());
    }
}
