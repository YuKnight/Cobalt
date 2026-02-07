package com.github.auties00.cobalt.socket.message;

import com.github.auties00.cobalt.client.WhatsAppClient;
import com.github.auties00.cobalt.exception.WhatsAppMediaException;
import com.github.auties00.cobalt.exception.WhatsAppMessageException;
import com.github.auties00.cobalt.exception.WhatsAppLidMigrationException;
import com.github.auties00.cobalt.message.receive.receipt.MessageReceiptHandler;
import com.github.auties00.cobalt.message.receive.MessageReceivingService;
import com.github.auties00.cobalt.message.receive.MessageReceiveStanzaParser;
import com.github.auties00.cobalt.migration.LidMigrationService;
import com.github.auties00.cobalt.model.action.ContactActionBuilder;
import com.github.auties00.cobalt.model.chat.Chat;
import com.github.auties00.cobalt.model.chat.ChatEphemeralTimer;
import com.github.auties00.cobalt.model.contact.Contact;
import com.github.auties00.cobalt.model.contact.ContactStatus;
import com.github.auties00.cobalt.model.info.ChatMessageInfo;
import com.github.auties00.cobalt.model.info.MessageIndexInfoBuilder;
import com.github.auties00.cobalt.model.info.MessageInfo;
import com.github.auties00.cobalt.model.jid.Jid;
import com.github.auties00.cobalt.model.message.common.Message;
import com.github.auties00.cobalt.model.message.common.MessageContainerSpec;
import com.github.auties00.cobalt.model.message.server.ProtocolMessage;
import com.github.auties00.cobalt.model.setting.EphemeralSettingsBuilder;
import com.github.auties00.cobalt.model.sync.*;
import com.github.auties00.cobalt.node.Node;
import com.github.auties00.cobalt.socket.SocketStream;
import com.github.auties00.cobalt.util.Clock;
import it.auties.protobuf.stream.ProtobufInputStream;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.time.ZonedDateTime;
import java.util.BitSet;
import java.util.HashSet;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.IntStream;
import java.util.zip.GZIPInputStream;
import java.util.zip.InflaterInputStream;

import com.github.auties00.cobalt.exception.WhatsAppHistorySyncException;

public final class MessageStreamNodeHandler extends SocketStream.Handler {
    private static final System.Logger LOGGER = System.getLogger("MessageStreamNodeHandler");
    private static final int HISTORY_SYNC_MAX_TIMEOUT = 25;
    private static final Set<HistorySync.Type> REQUIRED_HISTORY_SYNC_TYPES = Set.of(HistorySync.Type.INITIAL_BOOTSTRAP, HistorySync.Type.PUSH_NAME, HistorySync.Type.NON_BLOCKING_DATA);

    private final LidMigrationService lidMigrationService;
    private final MessageReceivingService messageReceivingService;
    private final MessageReceiptHandler messageReceiptHandler;
    private final Set<Jid> historyCache;
    private final HistorySyncProgressTracker recentHistorySyncTracker;
    private final HistorySyncProgressTracker fullHistorySyncTracker;
    private final Set<HistorySync.Type> historySyncTypes;
    private CompletableFuture<Void> historySyncTask;

    public MessageStreamNodeHandler(
            WhatsAppClient whatsapp,
            LidMigrationService lidMigrationService,
            MessageReceivingService messageReceivingService,
            MessageReceiptHandler messageReceiptHandler
    ) {
        super(whatsapp, "message");
        this.lidMigrationService = lidMigrationService;
        this.historyCache = new HashSet<>();
        this.historySyncTypes = new HashSet<>();
        this.recentHistorySyncTracker = new HistorySyncProgressTracker();
        this.fullHistorySyncTracker = new HistorySyncProgressTracker();
        this.messageReceivingService = messageReceivingService;
        this.messageReceiptHandler = messageReceiptHandler;
    }

    @Override
    public void handle(Node node) {
        // Check if this is a newsletter message (unencrypted)
        var fromJid = node.getRequiredAttributeAsJid("from");
        if (fromJid.hasNewsletterServer()) {
            handleNewsletterMessage(node);
            return;
        }

        var parsedStanza = MessageReceiveStanzaParser.parse(node);

        try {
            var info = messageReceivingService.process(parsedStanza);

            if (info == null) {
                return;
            }

            var content = info.message().content();
            if (content instanceof ProtocolMessage pm) {
                handleProtocolMessage(info, pm);
            }

            saveMessage(info);
            notifyListeners(info);
            messageReceiptHandler.sendDeliveryReceipt(info);

        } catch (WhatsAppMessageException.Receive.InvalidProtobuf e) {
            LOGGER.log(System.Logger.Level.WARNING,
                    "Parse error for message {0}: {1}",
                    parsedStanza.id(), e.getMessage());
            messageReceiptHandler.sendNackReceipt(
                    parsedStanza.id(),
                    parsedStanza.chatJid(),
                    parsedStanza.participant(),
                    e.errorCode().orElse("400")
            );

        } catch (WhatsAppMessageException.Receive.AdvFailure e) {
            LOGGER.log(System.Logger.Level.WARNING,
                    "ADV validation failed for message {0}: {1}",
                    parsedStanza.id(), e.getMessage());
            messageReceiptHandler.sendRetryReceipt(
                    parsedStanza.id(),
                    parsedStanza.chatJid(),
                    parsedStanza.senderJid(),
                    WhatsAppMessageException.Receive.RetryReason.ADV_FAILURE,
                    1
            );

        } catch (WhatsAppMessageException.Receive e) {
            var retryReason = e.retryReason();
            if (retryReason.shouldSendRetryReceipt()) {
                var retryCount = parsedStanza.retryCount().orElse(0) + 1;
                LOGGER.log(System.Logger.Level.WARNING,
                        "Decryption failed for message {0} ({1}), sending retry receipt (count={2})",
                        parsedStanza.id(), retryReason, retryCount);
                messageReceiptHandler.sendRetryReceipt(
                        parsedStanza.id(),
                        parsedStanza.chatJid(),
                        parsedStanza.senderJid(),
                        retryReason,
                        retryCount
                );
            } else {
                LOGGER.log(System.Logger.Level.DEBUG,
                        "Decryption issue for message {0} ({1}), not sending retry",
                        parsedStanza.id(), retryReason);
            }
        }
    }

    /**
     * Handles unencrypted newsletter messages.
     */
    private void handleNewsletterMessage(Node node) {
        var bodyNode = node.getChild("body");
        if (bodyNode.isEmpty()) {
            LOGGER.log(System.Logger.Level.DEBUG, "Newsletter message missing body node");
            return;
        }

        var plaintext = bodyNode.get().toContentBytes().orElse(null);
        if (plaintext == null || plaintext.length == 0) {
            LOGGER.log(System.Logger.Level.DEBUG, "Newsletter message has empty body");
            return;
        }

        try {
            var messageContainer = MessageContainerSpec.decode(ProtobufInputStream.fromBytes(plaintext));
            // TODO: Build NewsletterMessageInfo and save
            LOGGER.log(System.Logger.Level.DEBUG, "Received newsletter message");
        } catch (Exception e) {
            LOGGER.log(System.Logger.Level.WARNING, "Failed to parse newsletter message: {0}", e.getMessage());
        }
    }

    private void saveMessage(MessageInfo messageInfo) {
        if(messageInfo instanceof ChatMessageInfo chatMessageInfo && chatMessageInfo.parentJid().equals(Jid.statusBroadcastAccount())) {
            whatsapp.store().addStatus(chatMessageInfo);
        }else if (messageInfo instanceof ChatMessageInfo chatMessageInfo && !chatMessageInfo.message().hasCategory(Message.Category.SERVER)) {
            var chat = chatMessageInfo.chat()
                    .orElseGet(() -> whatsapp.store().addNewChat(chatMessageInfo.chatJid()));
            chat.addMessage(chatMessageInfo);
            if (chatMessageInfo.timestampSeconds().orElse(0L) > whatsapp.store().initializationTimeStamp()) {
                if (chat.archived() && whatsapp.store().unarchiveChats()) {
                    chat.setArchived(false);
                }
                chatMessageInfo.sender()
                        .filter(this::isTyping)
                        .ifPresent(sender -> {
                            var contact = whatsapp.store()
                                    .findContactByJid(sender);
                            if (contact.isPresent()) {
                                contact.get().setLastKnownPresence(ContactStatus.AVAILABLE);
                                contact.get().setLastSeen(ZonedDateTime.now());
                            }

                            var provider = contact.orElse(sender);
                            chat.addPresence(sender, ContactStatus.AVAILABLE);
                            for (var listener : whatsapp.store().listeners()) {
                                Thread.startVirtualThread(() -> listener.onContactPresence(whatsapp, chatMessageInfo.chatJid(), provider.jid()));
                            }
                        });
                if (!chatMessageInfo.ignore() && !chatMessageInfo.fromMe()) {
                    chat.setUnreadMessagesCount(chat.unreadMessagesCount() + 1);
                }
            }
        }
    }

    private boolean isTyping(Contact sender) {
        return sender.lastKnownPresence() == ContactStatus.COMPOSING
               || sender.lastKnownPresence() == ContactStatus.RECORDING;
    }

    private void notifyListeners(MessageInfo messageInfo) {
        if(messageInfo instanceof ChatMessageInfo chatInfo && chatInfo.chatJid().equals(Jid.statusBroadcastAccount())) {
            for (var listener : whatsapp.store().listeners()) {
                Thread.startVirtualThread(() -> listener.onNewStatus(whatsapp, chatInfo));
            }
        }else {
            for (var listener : whatsapp.store().listeners()) {
                Thread.startVirtualThread(() -> listener.onNewMessage(whatsapp, messageInfo));
            }

            var quotedMessageInfo = messageInfo.quotedMessage()
                    .orElse(null);
            if(quotedMessageInfo == null) {
                return;
            }

            for (var listener : whatsapp.store().listeners()) {
                Thread.startVirtualThread(() -> listener.onMessageReply(whatsapp, messageInfo, quotedMessageInfo));
            }
        }
    }

    private void handleProtocolMessage(ChatMessageInfo info, ProtocolMessage protocolMessage) {
        switch (protocolMessage.protocolType()) {
            case HISTORY_SYNC_NOTIFICATION -> onHistorySyncNotification(info, protocolMessage);

            case APP_STATE_SYNC_KEY_SHARE -> onAppStateSyncKeyShare(protocolMessage);

            case REVOKE -> onMessageRevoked(info, protocolMessage);

            case EPHEMERAL_SETTING -> onEphemeralSettings(info, protocolMessage);

            case EPHEMERAL_SYNC_RESPONSE -> {
                // TODO
            }

            case APP_STATE_SYNC_KEY_REQUEST -> {
                // TODO
            }

            case MSG_FANOUT_BACKFILL_REQUEST -> {
                // TODO
            }

            case INITIAL_SECURITY_NOTIFICATION_SETTING_SYNC -> {
                // TODO
            }

            case APP_STATE_FATAL_EXCEPTION_NOTIFICATION -> {
                // TODO
            }

            case SHARE_PHONE_NUMBER -> {
                // TODO
            }

            case MESSAGE_EDIT -> {
                // TODO
            }

            case PEER_DATA_OPERATION_REQUEST_MESSAGE -> {
                // TODO
            }

            case PEER_DATA_OPERATION_REQUEST_RESPONSE_MESSAGE -> {
                // TODO
            }

            case REQUEST_WELCOME_MESSAGE -> {
                // TODO
            }

            case BOT_FEEDBACK_MESSAGE -> {
                // TODO
            }

            case MEDIA_NOTIFY_MESSAGE -> {
                // TODO
            }

            case CLOUD_API_THREAD_CONTROL_NOTIFICATION -> {
                // TODO
            }

            case LID_MIGRATION_MAPPING_SYNC -> onLidMigrationMappingSync(protocolMessage);

            case REMINDER_MESSAGE -> {
                // TODO
            }

            case BOT_MEMU_ONBOARDING_MESSAGE -> {
                // TODO
            }

            case STATUS_MENTION_MESSAGE -> {
                // TODO
            }

            case STOP_GENERATION_MESSAGE -> {
                // TODO
            }

            case LIMIT_SHARING -> {
                // TODO
            }

            case AI_PSI_METADATA -> {
                // TODO
            }

            case AI_QUERY_FANOUT -> {
                // TODO
            }

            case GROUP_MEMBER_LABEL_CHANGE -> {
                // TODO
            }
        }
    }

    private void onLidMigrationMappingSync(ProtocolMessage protocolMessage) {
        var lidMigrationMappingSyncMessage = protocolMessage.lidMigrationMappingSyncMessage();
        if(lidMigrationMappingSyncMessage.isEmpty()) {
            whatsapp.handleFailure(new WhatsAppLidMigrationException.FailedToParseMappings("missing mapping sync message"));
            return;
        }

        var lidMigrationMappingPayload =  lidMigrationMappingSyncMessage.get()
                .encodedMappingPayload();
        if(lidMigrationMappingPayload.isEmpty()) {
            whatsapp.handleFailure(new WhatsAppLidMigrationException.FailedToParseMappings("missing encoded mapping payload"));
            return;
        }

        try(var stream = new GZIPInputStream(new ByteArrayInputStream(lidMigrationMappingPayload.get()))) {
            var lidMigrationMapping = LIDMigrationMappingSyncPayloadSpec.decode(ProtobufInputStream.fromStream(stream));
            lidMigrationService.processProtocolMessage(lidMigrationMapping);
        } catch (Throwable throwable) {
            whatsapp.handleFailure(new WhatsAppLidMigrationException.FailedToParseMappings("cannot parse protobuf message", throwable));
        }
    }

    private void onEphemeralSettings(ChatMessageInfo info, ProtocolMessage protocolMessage) {
        var chat = info.chat().orElse(null);
        var timestampSeconds = info.timestampSeconds().orElse(0L);
        if (chat != null) {
            chat.setEphemeralMessagesToggleTimeSeconds(timestampSeconds);
            chat.setEphemeralMessageDuration(ChatEphemeralTimer.of((int) protocolMessage.ephemeralExpirationSeconds()));
        }
        var setting = new EphemeralSettingsBuilder()
                .timestampSeconds((int) protocolMessage.ephemeralExpirationSeconds())
                .timestampSeconds(timestampSeconds)
                .build();
        for (var listener : whatsapp.store().listeners()) {
            Thread.startVirtualThread(() -> listener.onWebAppStateSetting(whatsapp, setting));
        }
    }

    private void onMessageRevoked(ChatMessageInfo info, ProtocolMessage protocolMessage) {
        var id = protocolMessage.key().orElseThrow().id();
        info.chat()
                .flatMap(chat -> whatsapp.store().findMessageById(chat, id))
                .ifPresent(message -> onMessageDeleted(info, message));
    }

    private void onAppStateSyncKeyShare(ProtocolMessage protocolMessage) {
        var data = protocolMessage.appStateSyncKeyShare()
                .orElseThrow(() -> new NoSuchElementException("Missing app state keys"));
        whatsapp.store()
                .addWebAppStateKeys(data.keys());
        whatsapp.pullWebAppState(PatchType.values());
    }

    private void onHistorySyncNotification(ChatMessageInfo info, ProtocolMessage protocolMessage) {
        scheduleHistorySyncTimeout();
        try {
            var historySync = downloadHistorySync(protocolMessage);
            onHistoryNotification(historySync);
        } catch (Throwable throwable) {
            whatsapp.handleFailure(new WhatsAppHistorySyncException(throwable));
        } finally {
            whatsapp.sendReceipt(info.id(), info.chatJid(), "hist_sync");
        }
    }

    private HistorySync downloadHistorySync(ProtocolMessage protocolMessage) {
        if (historySyncTypes.containsAll(REQUIRED_HISTORY_SYNC_TYPES) &&
             (whatsapp.store().webHistoryPolicy().isEmpty() || whatsapp.store().webHistoryPolicy().get().isZero())) {
            return null;
        }

        protocolMessage.historySyncNotification()
                .ifPresent(historySyncNotification -> historySyncTypes.add(historySyncNotification.syncType()));
        return protocolMessage.historySyncNotification()
                .map(this::downloadHistorySyncNotification)
                .orElse(null);
    }

    private HistorySync downloadHistorySyncNotification(HistorySyncNotification notification) {
        try {
            var initialPayload = notification.initialHistBootstrapInlinePayload();
            if (initialPayload.isPresent()) {
                var initialPayloadStream = new InflaterInputStream(new InputStream() {
                    @Override
                    public int read() {
                        return initialPayload.get().get() & 0xFF;
                    }

                    @Override
                    public int read(byte[] b, int off, int len) {
                        var length = Math.min(len, available());
                        initialPayload.get().get(b, off, length);
                        return length;
                    }

                    @Override
                    public int available() {
                        return initialPayload.get().remaining();
                    }
                });
                try(var mediaStream = ProtobufInputStream.fromStream(initialPayloadStream)) {
                    return HistorySyncSpec.decode(mediaStream);
                }
            }else {
                var mediaConnection = whatsapp.store()
                        .waitForMediaConnection();
                try(var mediaStream = ProtobufInputStream.fromStream(mediaConnection.download(notification))) {
                    return HistorySyncSpec.decode(mediaStream);
                }
            }
        } catch (Throwable throwable) {
            throw new WhatsAppMediaException.Download("Cannot download history sync", throwable);
        }
    }

    private void onHistoryNotification(HistorySync history) {
        if (history == null) {
            return;
        }

        handleHistorySync(history);
        if (history.progress() == null) {
            return;
        }

        var recent = history.syncType() == HistorySync.Type.RECENT;
        if (recent) {
            recentHistorySyncTracker.commit(history.chunkOrder(), history.progress() == 100);
            if (recentHistorySyncTracker.isDone()) {
                for (var listener : whatsapp.store().listeners()) {
                    Thread.startVirtualThread(() -> listener.onWebHistorySyncProgress(whatsapp, history.progress(), true));
                }
            }
        } else {
            fullHistorySyncTracker.commit(history.chunkOrder(), history.progress() == 100);
            if (fullHistorySyncTracker.isDone()) {
                for (var listener : whatsapp.store().listeners()) {
                    Thread.startVirtualThread(() -> listener.onWebHistorySyncProgress(whatsapp, history.progress(), false));
                }
            }
        }
    }

    private void onMessageDeleted(ChatMessageInfo info, ChatMessageInfo message) {
        info.chat().ifPresent(chat -> chat.removeMessage(message.id()));
        message.setRevokeTimestampSeconds(Clock.nowSeconds());
        for (var listener : whatsapp.store().listeners()) {
            Thread.startVirtualThread(() -> listener.onMessageDeleted(whatsapp, message, true));
        }
    }

    private void handleHistorySync(HistorySync history) {
        // Process LID mappings from all history sync types that may contain them
        // This must happen before conversation processing to ensure mappings are available
        lidMigrationService.processHistorySync(history);

        switch (history.syncType()) {
            case INITIAL_STATUS_V3 -> handleInitialStatus(history);
            case PUSH_NAME -> handlePushNames(history);
            case INITIAL_BOOTSTRAP -> handleInitialBootstrap(history);
            case FULL -> handleChatsSync(history, false);
            case RECENT -> handleChatsSync(history, true);
            case NON_BLOCKING_DATA -> handleNonBlockingData(history);
            case ON_DEMAND -> {} // No specific handling needed
        }
    }

    private void handleInitialStatus(HistorySync history) {
        for (var messageInfo : history.statusV3Messages()) {
            whatsapp.store().addStatus(messageInfo);
        }
        whatsapp.store()
                .setSyncedStatus(true);
        var status = whatsapp.store()
                .status();
        for(var listener : whatsapp.store().listeners()) {
            Thread.startVirtualThread(() -> listener.onStatus(whatsapp, status));
        }
    }

    private void handlePushNames(HistorySync history) {
        for (var pushName : history.pushNames()) {
            handNewPushName(pushName);
        }
        whatsapp.store()
                .setSyncedContacts(true);
        var contacts = whatsapp.store()
                .contacts();
        for (var listener : whatsapp.store().listeners()) {
            Thread.startVirtualThread(() -> listener.onContacts(whatsapp, contacts));
        }
    }

    private void handNewPushName(PushName pushName) {
        var jid = Jid.of(pushName.id());
        var contact = whatsapp.store()
                .findContactByJid(jid)
                .orElseGet(() -> createNewContact(jid));
        pushName.name()
                .ifPresent(contact::setChosenName);
        var action = new ContactActionBuilder()
                .firstName(pushName.name().orElse(null))
                .build();
        var index = new MessageIndexInfoBuilder()
                .type("contact")
                .targetId(pushName.id())
                .fromMe(true)
                .build();
        for (var listener : whatsapp.store().listeners()) {
            Thread.startVirtualThread(() -> listener.onWebAppStateAction(whatsapp, action, index));
        }
    }

    private Contact createNewContact(Jid jid) {
        var contact = whatsapp.store().addNewContact(jid);
        for(var listener : whatsapp.store().listeners()) {
            Thread.startVirtualThread(() -> listener.onNewContact(whatsapp, contact));
        }
        return contact;
    }

    private void handleInitialBootstrap(HistorySync history) {
        var historyPolicy = whatsapp.store().webHistoryPolicy();
        if (historyPolicy.isEmpty() || !historyPolicy.get().isZero()) {
            var jids = history.conversations()
                    .stream()
                    .map(Chat::jid)
                    .toList();
            historyCache.addAll(jids);
        }

        handleConversations(history);
        whatsapp.store()
                .setSyncedChats(true);
        var chats = whatsapp.store().chats();
        for (var listener : whatsapp.store().listeners()) {
            Thread.startVirtualThread(() -> listener.onChats(whatsapp, chats));
        }
    }

    private void handleChatsSync(HistorySync history, boolean recent) {
        var historyPolicy = whatsapp.store().webHistoryPolicy();
        if (historyPolicy.isPresent() && historyPolicy.get().isZero()) {
            return;
        }

        handleConversations(history);
        handleConversationsNotifications(history, recent);
        scheduleHistorySyncTimeout();
    }

    private void handleConversationsNotifications(HistorySync history, boolean recent) {
        var toRemove = new HashSet<Jid>();
        for (var cachedJid : historyCache) {
            var chat = whatsapp.store()
                    .findChatByJid(cachedJid)
                    .orElse(null);
            if (chat == null) {
                continue;
            }

            var done = !recent && !history.conversations().contains(chat);
            if (done) {
                chat.setEndOfHistoryTransfer(true);
                chat.setEndOfHistoryTransferType(Chat.EndOfHistoryTransferType.COMPLETE_AND_NO_MORE_MESSAGE_REMAIN_ON_PRIMARY);
                toRemove.add(cachedJid);
            }

            for(var listener : whatsapp.store().listeners()) {
                Thread.startVirtualThread(() -> listener.onWebHistorySyncMessages(whatsapp, chat, done));
            }
        }

        historyCache.removeAll(toRemove);
    }

    private void scheduleHistorySyncTimeout() {
        if (historySyncTask != null && !historySyncTask.isDone()) {
            historySyncTask.cancel(true);
        }

        this.historySyncTask =  CompletableFuture.runAsync(this::onForcedHistorySyncCompletion,
                CompletableFuture.delayedExecutor(HISTORY_SYNC_MAX_TIMEOUT, TimeUnit.SECONDS));
    }

    private void onForcedHistorySyncCompletion() {
        for (var cachedJid : historyCache) {
            var chat = whatsapp.store()
                    .findChatByJid(cachedJid)
                    .orElse(null);
            if (chat == null) {
                continue;
            }

            for (var listener : whatsapp.store().listeners()) {
                Thread.startVirtualThread(() -> listener.onWebHistorySyncMessages(whatsapp, chat, true));
            }
        }

        historyCache.clear();
    }

    private void handleConversations(HistorySync history) {
        for (var chat : history.conversations()) {
            whatsapp.store().addChat(chat);
        }
    }

    private void handleNonBlockingData(HistorySync history) {
        handlePastParticipants(history);
        // LID mappings are processed at the top of handleHistorySync() via lidMigrationService.onHistorySyncReceived()
    }

    private void handlePastParticipants(HistorySync history) {
        for (var pastParticipants : history.pastParticipants()) {
            for (var listener : whatsapp.store().listeners()) {
                Thread.startVirtualThread(() -> listener.onWebHistorySyncPastParticipants(whatsapp, pastParticipants.groupJid(), pastParticipants.pastParticipants()));
            }
        }
    }

    @Override
    public void reset() {
        historyCache.clear();
        if (historySyncTask != null) {
            historySyncTask.cancel(true);
            historySyncTask = null;
        }
        recentHistorySyncTracker.clear();
        fullHistorySyncTracker.clear();
        historySyncTypes.clear();
    }

    private static final class HistorySyncProgressTracker {
        private final BitSet chunksMarker;
        private final AtomicInteger chunkEnd;

        private HistorySyncProgressTracker() {
            this.chunksMarker = new BitSet();
            this.chunkEnd = new AtomicInteger(0);
        }

        private boolean isDone() {
            var chunkEnd = this.chunkEnd.get();
            return chunkEnd > 0 && IntStream.range(0, chunkEnd)
                    .allMatch(chunksMarker::get);
        }

        private void commit(int chunk, boolean finished) {
            if (finished) {
                chunkEnd.set(chunk);
            }

            chunksMarker.set(chunk);
        }

        private void clear() {
            chunksMarker.clear();
            chunkEnd.set(0);
        }
    }
}
