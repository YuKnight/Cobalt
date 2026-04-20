package com.github.auties00.cobalt.model.sync.history;

import com.github.auties00.cobalt.model.call.CallLog;
import com.github.auties00.cobalt.model.chat.*;
import com.github.auties00.cobalt.model.chat.group.GroupParticipant;
import com.github.auties00.cobalt.model.chat.group.GroupPastParticipants;
import com.github.auties00.cobalt.model.jid.Jid;
import com.github.auties00.cobalt.model.jid.migration.PhoneNumberToLIDMapping;
import com.github.auties00.cobalt.model.media.MediaVisibility;
import com.github.auties00.cobalt.model.media.StickerMetadata;
import com.github.auties00.cobalt.model.message.PrivacySystemMessage;
import com.github.auties00.cobalt.model.message.system.history.HistorySyncType;
import com.github.auties00.cobalt.model.setting.GlobalSettings;
import com.github.auties00.cobalt.model.setting.WallpaperSettings;
import com.github.auties00.collections.ConcurrentLinkedHashMap;
import it.auties.protobuf.annotation.ProtobufEnum;
import it.auties.protobuf.annotation.ProtobufEnumIndex;
import it.auties.protobuf.annotation.ProtobufMessage;
import it.auties.protobuf.annotation.ProtobufProperty;
import it.auties.protobuf.model.ProtobufType;
import it.auties.protobuf.stream.ProtobufInputStream;
import it.auties.protobuf.stream.ProtobufOutputStream;

import java.io.IOException;
import java.time.Instant;
import java.util.*;

/**
 * Chunk of account history transferred from the primary device to a companion
 * during onboarding or on-demand history fetches.
 *
 * <p>A history sync payload carries everything a freshly-linked companion
 * needs to render the user's account: chats and their messages, push-names,
 * global settings, recent stickers, past group participants, call logs, AI
 * wait-list state, phone-number to LID mappings, companion-meta nonce, the
 * shareable chat identifier encryption key, and linked account descriptors.
 * Payloads come in two shapes: the full variant includes chats and status
 * messages ({@link Full}), while the light variant ({@link Light}) shares
 * the metadata-only subset used for incremental syncs.
 *
 * <p>Instances are produced by decoding a {@code HistorySyncNotification}
 * media blob and are then fanned out to the corresponding store collections.
 */
@ProtobufMessage(name = "HistorySync")
public abstract sealed class HistorySync {
    /**
     * Kind of history transfer this payload represents.
     */
    @ProtobufProperty(index = 1, type = ProtobufType.ENUM)
    HistorySyncType syncType;

    /**
     * Zero-based index of this chunk inside the transfer sequence.
     */
    @ProtobufProperty(index = 5, type = ProtobufType.UINT32)
    Integer chunkOrder;

    /**
     * Progress percentage (0-100) reported for this transfer.
     */
    @ProtobufProperty(index = 6, type = ProtobufType.UINT32)
    Integer progress;

    /**
     * Push-name records reported by the primary device.
     */
    @ProtobufProperty(index = 7, type = ProtobufType.MESSAGE)
    List<Pushname> pushnames;

    /**
     * Global account settings included with the transfer.
     */
    @ProtobufProperty(index = 8, type = ProtobufType.MESSAGE)
    GlobalSettings globalSettings;

    /**
     * Secret used as part of the thread identifier derivation for the user.
     */
    @ProtobufProperty(index = 9, type = ProtobufType.BYTES)
    byte[] threadIdUserSecret;

    /**
     * Offset used when computing disappearing-mode thread windows.
     */
    @ProtobufProperty(index = 10, type = ProtobufType.UINT32)
    Integer threadDsTimeframeOffset;

    /**
     * Recently used stickers transferred for the user.
     */
    @ProtobufProperty(index = 11, type = ProtobufType.MESSAGE)
    List<StickerMetadata> recentStickers;

    /**
     * Participants who have left groups that the user is still a member of.
     */
    @ProtobufProperty(index = 12, type = ProtobufType.MESSAGE)
    List<GroupPastParticipants> pastParticipants;

    /**
     * Call-log records transferred for the user.
     */
    @ProtobufProperty(index = 13, type = ProtobufType.MESSAGE)
    List<CallLog> callLogs;

    /**
     * Wait-list state for AI bot features.
     */
    @ProtobufProperty(index = 14, type = ProtobufType.ENUM)
    BotAIWaitListState aiWaitListState;

    /**
     * Phone-number to LID migration records.
     */
    @ProtobufProperty(index = 15, type = ProtobufType.MESSAGE)
    List<PhoneNumberToLIDMapping> phoneNumberToLidMappings;

    /**
     * Nonce used to authenticate companion metadata updates.
     */
    @ProtobufProperty(index = 16, type = ProtobufType.STRING)
    String companionMetaNonce;

    /**
     * Key used to encrypt shareable chat identifiers.
     */
    @ProtobufProperty(index = 17, type = ProtobufType.BYTES)
    byte[] shareableChatIdentifierEncryptionKey;

    /**
     * Linked account descriptors (for multi-account users).
     */
    @ProtobufProperty(index = 18, type = ProtobufType.MESSAGE)
    List<Account> accounts;


    /**
     * Constructs the shared state carried by every history-sync variant.
     *
     * @param syncType the history sync kind
     * @param chunkOrder the chunk index within the transfer
     * @param progress the transfer progress percentage
     * @param pushnames the transferred push-names
     * @param globalSettings the transferred global settings
     * @param threadIdUserSecret the thread-id user secret
     * @param threadDsTimeframeOffset the disappearing-mode timeframe offset
     * @param recentStickers the recently used stickers
     * @param pastParticipants the past-participants records
     * @param callLogs the call-log records
     * @param aiWaitListState the AI wait-list state
     * @param phoneNumberToLidMappings the phone-number to LID migration records
     * @param companionMetaNonce the companion metadata nonce
     * @param shareableChatIdentifierEncryptionKey the shareable-identifier key
     * @param accounts the linked account descriptors
     */
    HistorySync(HistorySyncType syncType, Integer chunkOrder, Integer progress, List<Pushname> pushnames, GlobalSettings globalSettings, byte[] threadIdUserSecret, Integer threadDsTimeframeOffset, List<StickerMetadata> recentStickers, List<GroupPastParticipants> pastParticipants, List<CallLog> callLogs, BotAIWaitListState aiWaitListState, List<PhoneNumberToLIDMapping> phoneNumberToLidMappings, String companionMetaNonce, byte[] shareableChatIdentifierEncryptionKey, List<Account> accounts) {
        this.syncType = Objects.requireNonNull(syncType);
        this.chunkOrder = chunkOrder;
        this.progress = progress;
        this.pushnames = pushnames;
        this.globalSettings = globalSettings;
        this.threadIdUserSecret = threadIdUserSecret;
        this.threadDsTimeframeOffset = threadDsTimeframeOffset;
        this.recentStickers = recentStickers;
        this.pastParticipants = pastParticipants;
        this.callLogs = callLogs;
        this.aiWaitListState = aiWaitListState;
        this.phoneNumberToLidMappings = phoneNumberToLidMappings;
        this.companionMetaNonce = companionMetaNonce;
        this.shareableChatIdentifierEncryptionKey = shareableChatIdentifierEncryptionKey;
        this.accounts = accounts;
    }

    /**
     * Decodes a full history-sync payload (chats and status messages included)
     * from the supplied protobuf stream.
     *
     * @param stream the protobuf input stream to decode from
     * @return the decoded full history-sync payload
     */
    public static HistorySync ofFull(ProtobufInputStream stream) {
        return HistorySyncFullSpec.decode(stream);
    }

    /**
     * Decodes a lightweight history-sync payload (metadata only) from the
     * supplied protobuf stream.
     *
     * @param stream the protobuf input stream to decode from
     * @return the decoded light history-sync payload
     */
    public static HistorySync ofLight(ProtobufInputStream stream) {
        return HistorySyncLightSpec.decode(stream);
    }

    /**
     * Returns the kind of history transfer represented by this payload.
     *
     * @return the sync type
     */
    public HistorySyncType syncType() {
        return syncType;
    }

    /**
     * Returns the chats carried by this payload.
     *
     * <p>Light variants always return an empty list; full variants return the
     * transferred chats with their embedded message maps.
     *
     * @return the list of chats, never {@code null}
     */
    public abstract List<? extends Chat> chats();

    /**
     * Returns the status (status-v3) messages carried by this payload.
     *
     * <p>Light variants always return an empty list; full variants return the
     * transferred status messages.
     *
     * @return the list of status messages, never {@code null}
     */
    public abstract List<ChatMessageInfo> statusV3Messages();

    /**
     * Returns the chunk index inside the transfer sequence.
     *
     * @return the chunk order, or empty if absent
     */
    public OptionalInt chunkOrder() {
        return chunkOrder == null ? OptionalInt.empty() : OptionalInt.of(chunkOrder);
    }

    /**
     * Returns the transfer progress percentage.
     *
     * @return the progress value (0-100), or empty if absent
     */
    public OptionalInt progress() {
        return progress == null ? OptionalInt.empty() : OptionalInt.of(progress);
    }

    /**
     * Returns the push-name records transferred with this chunk.
     *
     * @return an unmodifiable list of push-names, never {@code null}
     */
    public List<Pushname> pushnames() {
        return pushnames == null ? List.of() : Collections.unmodifiableList(pushnames);
    }

    /**
     * Returns the global account settings transferred with this chunk.
     *
     * @return the global settings, or empty if absent
     */
    public Optional<GlobalSettings> globalSettings() {
        return Optional.ofNullable(globalSettings);
    }

    /**
     * Returns the user secret used during thread-id derivation.
     *
     * @return the secret bytes, or empty if absent
     */
    public Optional<byte[]> threadIdUserSecret() {
        return Optional.ofNullable(threadIdUserSecret);
    }

    /**
     * Returns the offset used when computing disappearing-mode windows.
     *
     * @return the offset, or empty if absent
     */
    public OptionalInt threadDsTimeframeOffset() {
        return threadDsTimeframeOffset == null ? OptionalInt.empty() : OptionalInt.of(threadDsTimeframeOffset);
    }

    /**
     * Returns the recently used stickers transferred with this chunk.
     *
     * @return an unmodifiable list of sticker metadata, never {@code null}
     */
    public List<StickerMetadata> recentStickers() {
        return recentStickers == null ? List.of() : Collections.unmodifiableList(recentStickers);
    }

    /**
     * Returns the past-participants records transferred with this chunk.
     *
     * @return an unmodifiable list of past-participants entries, never {@code null}
     */
    public List<GroupPastParticipants> pastParticipants() {
        return pastParticipants == null ? List.of() : Collections.unmodifiableList(pastParticipants);
    }

    /**
     * Returns the call-log records transferred with this chunk.
     *
     * @return an unmodifiable list of call logs, never {@code null}
     */
    public List<CallLog> callLogRecords() {
        return callLogs == null ? List.of() : Collections.unmodifiableList(callLogs);
    }

    /**
     * Returns the AI wait-list state of the account.
     *
     * @return the wait-list state, or empty if absent
     */
    public Optional<BotAIWaitListState> aiWaitListState() {
        return Optional.ofNullable(aiWaitListState);
    }

    /**
     * Returns the phone-number to LID migration records transferred with this
     * chunk.
     *
     * @return an unmodifiable list of mappings, never {@code null}
     */
    public List<PhoneNumberToLIDMapping> phoneNumberToLidMappings() {
        return phoneNumberToLidMappings == null ? List.of() : Collections.unmodifiableList(phoneNumberToLidMappings);
    }

    /**
     * Returns the companion metadata nonce.
     *
     * @return the nonce, or empty if absent
     */
    public Optional<String> companionMetaNonce() {
        return Optional.ofNullable(companionMetaNonce);
    }

    /**
     * Returns the encryption key used for shareable chat identifiers.
     *
     * @return the key bytes, or empty if absent
     */
    public Optional<byte[]> shareableChatIdentifierEncryptionKey() {
        return Optional.ofNullable(shareableChatIdentifierEncryptionKey);
    }

    /**
     * Returns the linked account descriptors transferred with this chunk.
     *
     * @return an unmodifiable list of account descriptors, never {@code null}
     */
    public List<Account> accounts() {
        return accounts == null ? List.of() : Collections.unmodifiableList(accounts);
    }

    /**
     * Sets the sync type.
     *
     * @param syncType the sync type
     */
    public void setSyncType(HistorySyncType syncType) {
        this.syncType = syncType;
    }

    /**
     * Sets the chunk index inside the transfer sequence.
     *
     * @param chunkOrder the chunk order
     */
    public void setChunkOrder(Integer chunkOrder) {
        this.chunkOrder = chunkOrder;
    }

    /**
     * Sets the transfer progress percentage.
     *
     * @param progress the progress value
     */
    public void setProgress(Integer progress) {
        this.progress = progress;
    }

    /**
     * Sets the push-name records.
     *
     * @param pushnames the push-names
     */
    public void setPushnames(List<Pushname> pushnames) {
        this.pushnames = pushnames;
    }

    /**
     * Sets the global account settings.
     *
     * @param globalSettings the settings
     */
    public void setGlobalSettings(GlobalSettings globalSettings) {
        this.globalSettings = globalSettings;
    }

    /**
     * Sets the thread-id user secret.
     *
     * @param threadIdUserSecret the secret bytes
     */
    public void setThreadIdUserSecret(byte[] threadIdUserSecret) {
        this.threadIdUserSecret = threadIdUserSecret;
    }

    /**
     * Sets the disappearing-mode timeframe offset.
     *
     * @param threadDsTimeframeOffset the offset
     */
    public void setThreadDsTimeframeOffset(Integer threadDsTimeframeOffset) {
        this.threadDsTimeframeOffset = threadDsTimeframeOffset;
    }

    /**
     * Sets the recently used stickers.
     *
     * @param recentStickers the stickers
     */
    public void setRecentStickers(List<StickerMetadata> recentStickers) {
        this.recentStickers = recentStickers;
    }

    /**
     * Sets the past-participants records.
     *
     * @param pastParticipants the past participants
     */
    public void setPastParticipants(List<GroupPastParticipants> pastParticipants) {
        this.pastParticipants = pastParticipants;
    }

    /**
     * Sets the call-log records.
     *
     * @param callLogs the call logs
     */
    public void setCallLogRecords(List<CallLog> callLogs) {
        this.callLogs = callLogs;
    }

    /**
     * Sets the AI wait-list state.
     *
     * @param aiWaitListState the state
     */
    public void setAiWaitListState(BotAIWaitListState aiWaitListState) {
        this.aiWaitListState = aiWaitListState;
    }

    /**
     * Sets the phone-number to LID migration records.
     *
     * @param phoneNumberToLidMappings the mappings
     */
    public void setPhoneNumberToLidMappings(List<PhoneNumberToLIDMapping> phoneNumberToLidMappings) {
        this.phoneNumberToLidMappings = phoneNumberToLidMappings;
    }

    /**
     * Sets the companion metadata nonce.
     *
     * @param companionMetaNonce the nonce
     */
    public void setCompanionMetaNonce(String companionMetaNonce) {
        this.companionMetaNonce = companionMetaNonce;
    }

    /**
     * Sets the shareable chat identifier encryption key.
     *
     * @param shareableChatIdentifierEncryptionKey the key bytes
     */
    public void setShareableChatIdentifierEncryptionKey(byte[] shareableChatIdentifierEncryptionKey) {
        this.shareableChatIdentifierEncryptionKey = shareableChatIdentifierEncryptionKey;
    }

    /**
     * Sets the linked account descriptors.
     *
     * @param accounts the accounts
     */
    public void setAccounts(List<Account> accounts) {
        this.accounts = accounts;
    }

    /**
     * Serialises this payload to the supplied protobuf output stream,
     * choosing the encoding matching the concrete variant.
     *
     * @param out the output stream to write to
     * @throws IOException if writing fails
     */
    public abstract void writeTo(ProtobufOutputStream<?> out) throws IOException;

    /**
     * Full history-sync variant: carries chats and their messages in addition
     * to the shared metadata.
     */
    @ProtobufMessage
    static final class Full extends HistorySync {
        /**
         * Chats transferred in this chunk, complete with embedded messages.
         */
        @ProtobufProperty(index = 2, type = ProtobufType.MESSAGE)
        List<Chat> chats;

        /**
         * Status (status-v3) messages transferred in this chunk.
         */
        @ProtobufProperty(index = 3, type = ProtobufType.MESSAGE)
        List<ChatMessageInfo> statusV3Messages;

        /**
         * Constructs a full history-sync payload.
         *
         * @param syncType the sync type
         * @param chunkOrder the chunk index
         * @param progress the progress value
         * @param pushnames the push-names
         * @param globalSettings the global settings
         * @param threadIdUserSecret the thread-id user secret
         * @param threadDsTimeframeOffset the timeframe offset
         * @param recentStickers the recent stickers
         * @param pastParticipants the past participants
         * @param callLogs the call logs
         * @param aiWaitListState the AI wait-list state
         * @param phoneNumberToLidMappings the PN to LID mappings
         * @param companionMetaNonce the companion nonce
         * @param shareableChatIdentifierEncryptionKey the shareable-id key
         * @param accounts the linked accounts
         * @param chats the transferred chats
         * @param statusV3Messages the transferred status messages
         */
        Full(HistorySyncType syncType, Integer chunkOrder, Integer progress, List<Pushname> pushnames, GlobalSettings globalSettings, byte[] threadIdUserSecret, Integer threadDsTimeframeOffset, List<StickerMetadata> recentStickers, List<GroupPastParticipants> pastParticipants, List<CallLog> callLogs, BotAIWaitListState aiWaitListState, List<PhoneNumberToLIDMapping> phoneNumberToLidMappings, String companionMetaNonce, byte[] shareableChatIdentifierEncryptionKey, List<Account> accounts, List<Chat> chats, List<ChatMessageInfo> statusV3Messages) {
            super(syncType, chunkOrder, progress, pushnames, globalSettings, threadIdUserSecret, threadDsTimeframeOffset, recentStickers, pastParticipants, callLogs, aiWaitListState, phoneNumberToLidMappings, companionMetaNonce, shareableChatIdentifierEncryptionKey, accounts);
            this.chats = chats;
            this.statusV3Messages = statusV3Messages;
        }

        /**
         * Returns the chats transferred in this chunk.
         *
         * @return an unmodifiable list of chats, never {@code null}
         */
        @Override
        public List<Chat> chats() {
            return chats == null ? List.of() : Collections.unmodifiableList(chats);
        }

        /**
         * Returns the status (status-v3) messages transferred in this chunk.
         *
         * @return an unmodifiable list of status messages, never {@code null}
         */
        @Override
        public List<ChatMessageInfo> statusV3Messages() {
            return statusV3Messages == null ? List.of() : Collections.unmodifiableList(statusV3Messages);
        }

        /**
         * Serialises this full payload using the corresponding protobuf spec.
         *
         * @param out the output stream to write to
         */
        @Override
        public void writeTo(ProtobufOutputStream<?> out) {
            HistorySyncFullSpec.encode(this, out);
        }

        /**
         * History-sync representation of a chat.
         *
         * <p>Extends the core {@link com.github.auties00.cobalt.model.chat.Chat}
         * with a map of messages keyed by message id and wrapped inside
         * {@link HistorySyncMsg} envelopes so that the transfer preserves the
         * monotonic order imposed by {@code msgOrderId}. The chat exposes the
         * message collection through the usual chat API by projecting the map
         * values on demand.
         */
        @ProtobufMessage
        static final class Chat extends com.github.auties00.cobalt.model.chat.Chat {
            /**
             * Messages keyed by id, ordered by insertion to preserve history
             * ordering.
             */
            @ProtobufProperty(index = 2, type = ProtobufType.MAP, mapKeyType = ProtobufType.STRING, mapValueType = ProtobufType.MESSAGE)
            final ConcurrentLinkedHashMap<String, HistorySyncMsg> messages;

            /**
             * Constructs a history-sync chat with the inherited chat fields
             * plus the message map.
             *
             * @param jid the chat JID
             * @param newJid the migrated-to JID, if any
             * @param oldJid the previous JID, if any
             * @param lastMsgTimestamp the timestamp of the last known message
             * @param unreadCount the unread-message counter
             * @param readOnly whether the chat is read-only
             * @param endOfHistoryTransfer whether this is the end-of-history marker
             * @param ephemeralExpiration the ephemeral timer
             * @param ephemeralSettingTimestamp the ephemeral setting timestamp
             * @param endOfHistoryTransferType the end-of-history transfer type
             * @param conversationTimestamp the conversation activity timestamp
             * @param name the chat name
             * @param pHash the peer hash
             * @param notSpam whether the chat was marked not-spam
             * @param archived whether the chat is archived
             * @param disappearingMode the disappearing-mode setting
             * @param unreadMentionCount the number of unread mentions
             * @param markedAsUnread whether the chat was manually marked unread
             * @param participant the group participants
             * @param tcToken the trusted-contact token
             * @param tcTokenTimestamp the trusted-contact token timestamp
             * @param contactPrimaryIdentityKey the contact primary identity key
             * @param pinnedTimestamp the pin timestamp
             * @param mute the mute configuration
             * @param wallpaper the wallpaper settings
             * @param mediaVisibility the media visibility setting
             * @param tcTokenSenderTimestamp the sender-side TC token timestamp
             * @param suspended whether the chat is suspended
             * @param terminated whether the chat is terminated
             * @param createdAt the creation timestamp
             * @param createdBy the creator descriptor
             * @param description the chat description
             * @param support whether this is a support chat
             * @param isParentGroup whether this is a parent community group
             * @param parentGroupId the parent community group id, if any
             * @param isDefaultSubgroup whether this is the default subgroup
             * @param displayName the display name
             * @param phoneNumberJid the phone-number JID
             * @param shareOwnPhoneNumber whether to share own phone number
             * @param phoneNumberhDuplicateLidThread flag used during PN/LID deduplication
             * @param lid the chat LID
             * @param username the username
             * @param lidOriginType the LID origin type
             * @param commentsCount the comments counter
             * @param locked whether the chat is locked
             * @param systemMessageToInsert the pending privacy system message
             * @param capiCreatedGroup whether the group was created via CAPI
             * @param accountLid the account LID
             * @param limitSharing whether sharing is limited
             * @param limitSharingSettingTimestamp the limit-sharing setting timestamp
             * @param limitSharingTrigger the limit-sharing trigger type
             * @param limitSharingInitiatedByMe whether the user initiated limit-sharing
             * @param maibaAiThreadEnabled whether the Maiba AI thread is enabled
             * @param messages the ordered message map
             */
            Chat(Jid jid, Jid newJid, Jid oldJid, Instant lastMsgTimestamp, Integer unreadCount, Boolean readOnly, Boolean endOfHistoryTransfer, ChatEphemeralTimer ephemeralExpiration, Instant ephemeralSettingTimestamp, EndOfHistoryTransferType endOfHistoryTransferType, Instant conversationTimestamp, String name, String pHash, Boolean notSpam, Boolean archived, ChatDisappearingMode disappearingMode, Integer unreadMentionCount, Boolean markedAsUnread, List<GroupParticipant> participant, byte[] tcToken, Instant tcTokenTimestamp, byte[] contactPrimaryIdentityKey, Instant pinnedTimestamp, ChatMute mute, WallpaperSettings wallpaper, MediaVisibility mediaVisibility, Instant tcTokenSenderTimestamp, Boolean suspended, Boolean terminated, Long createdAt, String createdBy, String description, Boolean support, Boolean isParentGroup, String parentGroupId, Boolean isDefaultSubgroup, String displayName, Jid phoneNumberJid, Boolean shareOwnPhoneNumber, Boolean phoneNumberhDuplicateLidThread, Jid lid, String username, String lidOriginType, Integer commentsCount, Boolean locked, PrivacySystemMessage systemMessageToInsert, Boolean capiCreatedGroup, Jid accountLid, Boolean limitSharing, Instant limitSharingSettingTimestamp, ChatLimitSharing.TriggerType limitSharingTrigger, Boolean limitSharingInitiatedByMe, Boolean maibaAiThreadEnabled, ConcurrentLinkedHashMap<String, HistorySyncMsg> messages) {
                super(jid, newJid, oldJid, lastMsgTimestamp, unreadCount, readOnly, endOfHistoryTransfer, ephemeralExpiration, ephemeralSettingTimestamp, endOfHistoryTransferType, conversationTimestamp, name, pHash, notSpam, archived, disappearingMode, unreadMentionCount, markedAsUnread, participant, tcToken, tcTokenTimestamp, contactPrimaryIdentityKey, pinnedTimestamp, mute, wallpaper, mediaVisibility, tcTokenSenderTimestamp, suspended, terminated, createdAt, createdBy, description, support, isParentGroup, parentGroupId, isDefaultSubgroup, displayName, phoneNumberJid, shareOwnPhoneNumber, phoneNumberhDuplicateLidThread, lid, username, lidOriginType, commentsCount, locked, systemMessageToInsert, capiCreatedGroup, accountLid, limitSharing, limitSharingSettingTimestamp, limitSharingTrigger, limitSharingInitiatedByMe, maibaAiThreadEnabled);
                this.messages = messages;
            }

            /**
             * Returns the messages of this chat as a sequenced collection of
             * {@link ChatMessageInfo}, projected on the fly from the
             * underlying {@link HistorySyncMsg} envelopes.
             *
             * @return the sequenced collection of messages
             */
            @Override
            public SequencedCollection<ChatMessageInfo> messages() {
                return createMessagesView(messages.sequencedValues());
            }

            /**
             * Creates an unmodifiable, lazily-projecting view of the given
             * envelope sequence as a sequenced collection of messages.
             *
             * @param data the source sequence of history-sync envelopes
             * @return a read-only view that yields each envelope's message
             */
            private SequencedCollection<ChatMessageInfo> createMessagesView(SequencedCollection<HistorySyncMsg> data) {
                return new SequencedCollection<>() {
                    @Override
                    public SequencedCollection<ChatMessageInfo> reversed() {
                        return createMessagesView(data.reversed());
                    }

                    @Override
                    public int size() {
                        return data.size();
                    }

                    @Override
                    public boolean isEmpty() {
                        return data.isEmpty();
                    }

                    @Override
                    public boolean contains(Object o) {
                        if(!(o instanceof ChatMessageInfo chatMessageInfo)) {
                            return false;
                        }

                        var id = chatMessageInfo.key().id();
                        return id.isPresent()
                               && messages.containsKey(id.get());
                    }

                    @Override
                    public Iterator<ChatMessageInfo> iterator() {
                        var delegate = data.iterator();
                        return new Iterator<>() {
                            @Override
                            public boolean hasNext() {
                                return delegate.hasNext();
                            }

                            @Override
                            public ChatMessageInfo next() {
                                var result = delegate.next();
                                return result != null ? result.message : null;
                            }
                        };
                    }

                    @Override
                    public Object[] toArray() {
                        return data.toArray();
                    }

                    @Override
                    public <T> T[] toArray(T[] a) {
                        return data.toArray(a);
                    }

                    @Override
                    public boolean add(ChatMessageInfo info) {
                        throw new UnsupportedOperationException();
                    }

                    @Override
                    public boolean remove(Object o) {
                        throw new UnsupportedOperationException();
                    }

                    @Override
                    public boolean containsAll(Collection<?> c) {
                        for (var entry : c) {
                            if (!contains(entry)) {
                                return false;
                            }
                        }
                        return true;
                    }

                    @Override
                    public boolean addAll(Collection<? extends ChatMessageInfo> c) {
                        throw new UnsupportedOperationException();
                    }

                    @Override
                    public boolean removeAll(Collection<?> c) {
                        throw new UnsupportedOperationException();
                    }

                    @Override
                    public boolean retainAll(Collection<?> c) {
                        throw new UnsupportedOperationException();
                    }

                    @Override
                    public void clear() {
                        throw new UnsupportedOperationException();
                    }
                };
            }

            /**
             * Appends a message to the history-sync message map, wrapping it
             * in a {@link HistorySyncMsg} envelope with a sentinel order id.
             *
             * @param info the message to add
             * @throws NullPointerException if {@code info} or its key id is {@code null}
             */
            @Override
            public void addMessage(ChatMessageInfo info) {
                Objects.requireNonNull(info, "info cannot be null");
                var id = info.key()
                        .id()
                        .orElseThrow(() -> new NullPointerException("id cannot be null"));
                var msg = new HistorySyncMsgBuilder()
                        .msgOrderId(-1L)
                        .message(info)
                        .build();
                messages.put(id, msg);
            }

            /**
             * Checks whether a message with the given id currently exists in
             * the history-sync chat.
             *
             * @param id the message id to probe
             * @return {@code true} if an entry with that id is present
             */
            @Override
            public boolean removeMessage(String id) {
                return messages.get(id) != null;
            }

            /**
             * Clears all messages from the history-sync chat.
             */
            @Override
            public void removeMessages() {
                messages.clear();
            }

            /**
             * Looks up a message by its id.
             *
             * @param id the message id
             * @return the matching message, or empty if absent
             */
            @Override
            public Optional<ChatMessageInfo> getMessageById(String id) {
                var msg = messages.get(id);
                if(msg == null) {
                    return Optional.empty();
                } else {
                    return msg.message();
                }
            }

            /**
             * Returns the newest (most recently inserted) message of the chat.
             *
             * @return the newest message, or empty if the chat has no messages
             */
            @Override
            public Optional<ChatMessageInfo> newestMessage() {
                var entry = messages.lastEntry();
                if(entry == null) {
                    return Optional.empty();
                } else {
                    return entry.getValue().message();
                }
            }

            /**
             * Returns the oldest message of the chat.
             *
             * @return the oldest message, or empty if the chat has no messages
             */
            @Override
            public Optional<ChatMessageInfo> oldestMessage() {
                var entry = messages.firstEntry();
                if(entry == null) {
                    return Optional.empty();
                } else {
                    return entry.getValue().message();
                }
            }
        }
    }

    /**
     * Lightweight history-sync variant: carries only the shared metadata and
     * omits chats and status messages. Used when the server pushes metadata
     * updates without re-sending message history.
     */
    @ProtobufMessage
    static final class Light extends HistorySync {
        /**
         * Constructs a light history-sync payload. All chat-bearing fields
         * inherited from {@link HistorySync} are left to default.
         *
         * @param syncType the sync type
         * @param chunkOrder the chunk index
         * @param progress the progress value
         * @param pushnames the push-names
         * @param globalSettings the global settings
         * @param threadIdUserSecret the thread-id user secret
         * @param threadDsTimeframeOffset the timeframe offset
         * @param recentStickers the recent stickers
         * @param pastParticipants the past participants
         * @param callLogs the call logs
         * @param aiWaitListState the AI wait-list state
         * @param phoneNumberToLidMappings the PN to LID mappings
         * @param companionMetaNonce the companion nonce
         * @param shareableChatIdentifierEncryptionKey the shareable-id key
         * @param accounts the linked accounts
         */
        Light(HistorySyncType syncType, Integer chunkOrder, Integer progress, List<Pushname> pushnames, GlobalSettings globalSettings, byte[] threadIdUserSecret, Integer threadDsTimeframeOffset, List<StickerMetadata> recentStickers, List<GroupPastParticipants> pastParticipants, List<CallLog> callLogs, BotAIWaitListState aiWaitListState, List<PhoneNumberToLIDMapping> phoneNumberToLidMappings, String companionMetaNonce, byte[] shareableChatIdentifierEncryptionKey, List<Account> accounts) {
            super(syncType, chunkOrder, progress, pushnames, globalSettings, threadIdUserSecret, threadDsTimeframeOffset, recentStickers, pastParticipants, callLogs, aiWaitListState, phoneNumberToLidMappings, companionMetaNonce, shareableChatIdentifierEncryptionKey, accounts);
        }

        /**
         * Returns an empty list: the light variant never carries chats.
         *
         * @return an empty chat list
         */
        @Override
        public List<Chat> chats() {
            return List.of();
        }

        /**
         * Returns an empty list: the light variant never carries status
         * messages.
         *
         * @return an empty status-message list
         */
        @Override
        public List<ChatMessageInfo> statusV3Messages() {
            return List.of();
        }

        /**
         * Serialises this light payload using the corresponding protobuf spec.
         *
         * @param out the output stream to write to
         */
        @Override
        public void writeTo(ProtobufOutputStream<?> out) {
            HistorySyncLightSpec.encode(this, out);
        }
    }

    /**
     * Wait-list state for AI bot features on the account.
     */
    @ProtobufEnum(name = "HistorySync.BotAIWaitListState")
    public enum BotAIWaitListState {
        /**
         * Account is on the wait-list for AI features.
         */
        IN_WAITLIST(0),
        /**
         * AI features are available to the account.
         */
        AI_AVAILABLE(1);

        /**
         * Constructs a wait-list state constant.
         *
         * @param index the protobuf wire index
         */
        BotAIWaitListState(@ProtobufEnumIndex int index) {
            this.index = index;
        }

        /**
         * Protobuf wire index for this state.
         */
        final int index;

        /**
         * Returns the protobuf wire index of this state.
         *
         * @return the protobuf enum index
         */
        public int index() {
            return this.index;
        }
    }

    /**
     * Descriptor of an account linked to the main WhatsApp identity, carried
     * alongside the main history sync when a user has multiple accounts.
     */
    @ProtobufMessage(name = "Account")
    public static final class Account {
        /**
         * LID of the linked account.
         */
        @ProtobufProperty(index = 1, type = ProtobufType.STRING)
        String lid;

        /**
         * Username of the linked account, if any.
         */
        @ProtobufProperty(index = 2, type = ProtobufType.STRING)
        String username;

        /**
         * Country code associated with the linked account.
         */
        @ProtobufProperty(index = 3, type = ProtobufType.STRING)
        String countryCode;

        /**
         * Whether the username has been deleted.
         */
        @ProtobufProperty(index = 4, type = ProtobufType.BOOL)
        Boolean isUsernameDeleted;


        /**
         * Constructs a linked-account descriptor.
         *
         * @param lid the account LID
         * @param username the account username
         * @param countryCode the account country code
         * @param isUsernameDeleted whether the username has been deleted
         */
        Account(String lid, String username, String countryCode, Boolean isUsernameDeleted) {
            this.lid = lid;
            this.username = username;
            this.countryCode = countryCode;
            this.isUsernameDeleted = isUsernameDeleted;
        }

        /**
         * Returns the LID of the linked account.
         *
         * @return the LID, or empty if absent
         */
        public Optional<String> lid() {
            return Optional.ofNullable(lid);
        }

        /**
         * Returns the username of the linked account.
         *
         * @return the username, or empty if absent
         */
        public Optional<String> username() {
            return Optional.ofNullable(username);
        }

        /**
         * Returns the country code of the linked account.
         *
         * @return the country code, or empty if absent
         */
        public Optional<String> countryCode() {
            return Optional.ofNullable(countryCode);
        }

        /**
         * Returns whether the username has been deleted. Absent flags are
         * treated as {@code false}.
         *
         * @return {@code true} if the username is marked deleted
         */
        public boolean isUsernameDeleted() {
            return isUsernameDeleted != null && isUsernameDeleted;
        }

        /**
         * Sets the LID of the linked account.
         *
         * @param lid the LID
         */
        public void setLid(String lid) {
            this.lid = lid;
    }

        /**
         * Sets the username of the linked account.
         *
         * @param username the username
         */
        public void setUsername(String username) {
            this.username = username;
    }

        /**
         * Sets the country code of the linked account.
         *
         * @param countryCode the country code
         */
        public void setCountryCode(String countryCode) {
            this.countryCode = countryCode;
    }

        /**
         * Sets whether the username has been deleted.
         *
         * @param isUsernameDeleted the deletion flag
         */
        public void setUsernameDeleted(Boolean isUsernameDeleted) {
            this.isUsernameDeleted = isUsernameDeleted;
    }
    }

    /**
     * Mapping from a contact identifier to its user-visible push name.
     *
     * <p>Delivered as part of a history sync so that the receiving device
     * can display correct sender names for messages whose senders are not in
     * the local contact list.
     */
    @ProtobufMessage(name = "Pushname")
    public static final class Pushname {
        /**
         * Identifier of the contact (usually a JID string).
         */
        @ProtobufProperty(index = 1, type = ProtobufType.STRING)
        String id;

        /**
         * Push name advertised by that contact.
         */
        @ProtobufProperty(index = 2, type = ProtobufType.STRING)
        String pushname;


        /**
         * Constructs a push-name mapping.
         *
         * @param id the contact identifier
         * @param pushname the advertised push name
         */
        Pushname(String id, String pushname) {
            this.id = id;
            this.pushname = pushname;
        }

        /**
         * Returns the contact identifier.
         *
         * @return the identifier, or empty if absent
         */
        public Optional<String> id() {
            return Optional.ofNullable(id);
        }

        /**
         * Returns the advertised push name.
         *
         * @return the push name, or empty if absent
         */
        public Optional<String> pushname() {
            return Optional.ofNullable(pushname);
        }

        /**
         * Sets the contact identifier.
         *
         * @param id the identifier
         */
        public void setId(String id) {
            this.id = id;
    }

        /**
         * Sets the advertised push name.
         *
         * @param pushname the push name
         */
        public void setPushname(String pushname) {
            this.pushname = pushname;
    }
    }
}
