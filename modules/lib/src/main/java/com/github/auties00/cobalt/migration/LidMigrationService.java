package com.github.auties00.cobalt.migration;

import com.github.auties00.cobalt.client.WhatsAppClient;
import com.github.auties00.cobalt.exception.WhatsAppLidMigrationException;
import com.github.auties00.cobalt.model.chat.Chat;
import com.github.auties00.cobalt.model.chat.ChatDisappearingMode;
import com.github.auties00.cobalt.model.chat.ChatMessageInfo;
import com.github.auties00.cobalt.model.chat.ChatMetadata;
import com.github.auties00.cobalt.model.chat.ChatMute;
import com.github.auties00.cobalt.model.sync.history.HistorySync;
import com.github.auties00.cobalt.model.jid.Jid;
import com.github.auties00.cobalt.model.jid.migration.LIDMigrationMapping;
import com.github.auties00.cobalt.model.jid.migration.LIDMigrationMappingSyncPayload;
import com.github.auties00.cobalt.model.jid.migration.PhoneNumberToLIDMapping;
import com.github.auties00.cobalt.model.message.MessageKey;
import com.github.auties00.cobalt.model.message.MessageKeyBuilder;
import com.github.auties00.cobalt.model.setting.GlobalSettings;
import com.github.auties00.cobalt.props.ABProp;
import com.github.auties00.cobalt.props.ABPropsService;
import com.github.auties00.cobalt.store.WhatsAppStore;

import com.github.auties00.cobalt.util.SchedulerUtils;

import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;

/**
 * Service responsible for orchestrating LID (Long ID) migration.
 *
 * <p>LID migration is a process where WhatsApp transitions from phone number-based
 * addressing to LID-based addressing for improved privacy. This service replaces WA Web's
 * {@code Lid1X1MigrationUtils} singleton with a state-machine approach using
 * {@link LidMigrationState}, where WA Web uses UserPrefs keys
 * ({@code WAIsAccountLidFieldMigrated}, {@code WALidOneOnOneMigrationSource},
 * {@code WAIsPureLidSyncDSession}).
 *
 * @implNote WAWebLid1X1MigrationGating.Lid1X1MigrationUtils,
 *           WAWebLid1x1MigrationPrimaryCache.lidPnMigrationPrimaryCache,
 *           WAWebLid1x1MigrationManager.ThreadMigrationManager
 */
public final class LidMigrationService {
    /**
     * Logger instance for migration lifecycle events.
     *
     * @implNote ADAPTED: WAWebLid1X1MigrationGating — WA Web uses WALogger.LOG tagged templates
     */
    private static final System.Logger LOGGER = System.getLogger(LidMigrationService.class.getName());

    /**
     * LID origin type value for phone-number-hiding click-to-WhatsApp chats.
     * Matches WA Web's {@code LidOriginType.PNH_CTWA} ({@code "ctwa"}).
     *
     * @implNote WAWebUsernameTypes.LidOriginType.PNH_CTWA
     */
    private static final String LID_ORIGIN_TYPE_PNH_CTWA = "ctwa";

    /**
     * LID origin type value for general (non-PNH) chats.
     * Matches WA Web's {@code LidOriginType.GENERAL} ({@code "general"}).
     *
     * @implNote WAWebUsernameTypes.LidOriginType.GENERAL
     */
    private static final String LID_ORIGIN_TYPE_GENERAL = "general";

    /**
     * Stub types that are considered safe to ignore during LID migration deletability
     * checks, matching WA Web's {@code X()} function.
     *
     * <p>WA Web's {@code X()} matches exactly two conditions:
     * <ul>
     * <li>{@code getIsInitialE2ENotification}: type === "e2e_notification" AND subtype === "encrypt"
     * <li>{@code getIsDisappearingModeSystemMessage}: type === "notification_template" AND subtype === "disappearing_mode"
     * </ul>
     *
     * @implNote WAWebLid1X1ThreadAccountMigrations.flow.X
     */
    private static final Set<ChatMessageInfo.StubType> MIGRATION_SAFE_STUB_TYPES = EnumSet.of(
            // Maps to getIsInitialE2ENotification (e2e_notification + encrypt)
            ChatMessageInfo.StubType.E2E_ENCRYPTED,
            ChatMessageInfo.StubType.E2E_ENCRYPTED_NOW,
            // Maps to getIsDisappearingModeSystemMessage (notification_template + disappearing_mode)
            ChatMessageInfo.StubType.DISAPPEARING_MODE
    );

    /**
     * Stub types that represent call log entries, matching WA Web's
     * {@code type === MSG_TYPE.CALL_LOG} check in the {@code ee()} function.
     *
     * @implNote WAWebLid1X1ThreadAccountMigrations.flow.ee
     */
    private static final Set<ChatMessageInfo.StubType> CALL_LOG_STUB_TYPES = EnumSet.of(
            ChatMessageInfo.StubType.CALL_MISSED_VOICE,
            ChatMessageInfo.StubType.CALL_MISSED_VIDEO,
            ChatMessageInfo.StubType.CALL_MISSED_GROUP_VOICE,
            ChatMessageInfo.StubType.CALL_MISSED_GROUP_VIDEO,
            ChatMessageInfo.StubType.SILENCED_UNKNOWN_CALLER_AUDIO,
            ChatMessageInfo.StubType.SILENCED_UNKNOWN_CALLER_VIDEO
    );

    /**
     * The WhatsApp client instance for error handling delegation.
     *
     * @implNote NO_WA_BASIS — Cobalt DI pattern
     */
    private final WhatsAppClient whatsapp;

    /**
     * The store for chat, contact, and LID mapping persistence.
     *
     * @implNote ADAPTED: WAWebLid1X1MigrationGating — WA Web uses UserPrefsIdb, Cobalt uses WhatsAppStore
     */
    private final WhatsAppStore store;

    /**
     * The AB props service for reading feature flags.
     *
     * @implNote NO_WA_BASIS — Cobalt DI pattern
     */
    private final ABPropsService abPropsService;

    /**
     * Current migration state, replacing WA Web's {@code WAIsAccountLidFieldMigrated}
     * UserPrefs key with a richer state machine.
     *
     * @implNote ADAPTED: WAWebLid1X1MigrationGating.isLidMigrated — maps {@code COMPLETE} to {@code true}
     */
    private final AtomicReference<LidMigrationState> state;

    /**
     * Primary device's PN to assigned LID mappings cache.
     * Maps phone number (as numeric string) to the LID assigned by the primary
     * device at migration time.
     *
     * @implNote WAWebLid1x1MigrationPrimaryCache.$2 — populated from LIDMigrationMappingSyncPayload
     */
    private final ConcurrentHashMap<String, Jid> primaryPnToAssignedLidCache;

    /**
     * Primary device's PN to latest LID mappings cache.
     * Maps phone number (as numeric string) to the most recent LID known to
     * the primary device, which may differ from the assigned LID.
     *
     * @implNote WAWebLid1x1MigrationPrimaryCache.$3 — populated from LIDMigrationMappingSyncPayload
     */
    private final ConcurrentHashMap<String, Jid> primaryPnToLatestLidCache;

    /**
     * Cache of original LIDs for locally-created chats, keyed by the chat's
     * phone number JID user part.
     *
     * <p>Matches WA Web's {@code chat.originalLid} field, which is set in
     * {@code WAWebCreateChat} when a LID mapping is known at chat creation
     * time. Used as a last-resort fallback in {@code resolveThread()}
     * when no other LID mapping is available.
     *
     * @implNote WAWebCreateChat — chat.originalLid field
     */
    private final ConcurrentHashMap<String, Jid> originalLidCache;

    /**
     * Chat DB migration timestamp from primary device.
     *
     * @implNote WAWebLid1x1MigrationPrimaryCache.$4 — set from parsed payload's primaryMigrationTsSec
     */
    private volatile Instant chatDbMigrationTimestamp;

    /**
     * Timestamp when the protocol message was received, used as a fallback
     * when {@link #chatDbMigrationTimestamp} is {@code null}.
     *
     * @implNote NO_WA_BASIS — Cobalt-specific fallback for timestamp comparison
     */
    private volatile Instant receiveTimestamp;

    /**
     * Future for the mapping timeout task, cancelled when mappings are received.
     * Corresponds to the {@code _} module-level variable in WA Web's
     * {@code WAWebLid1x1MigrationTimeout}, which holds the {@code setTimeout} handle.
     *
     * @implNote ADAPTED: WAWebLid1x1MigrationTimeout._ — WA Web uses a raw setTimeout
     *           handle; Cobalt uses a {@link CompletableFuture} from
     *           {@link SchedulerUtils#scheduleDelayed(Duration, Runnable)}
     */
    private volatile CompletableFuture<Void> mappingTimeoutFuture;

    /**
     * Creates a new LID migration service.
     *
     * @implNote ADAPTED: WAWebLid1X1MigrationGating — WA Web uses a singleton class {@code new c()},
     *           Cobalt uses constructor DI
     * @param whatsapp       the WhatsApp client instance
     * @param abPropsService the AB props service for reading feature flags
     */
    public LidMigrationService(WhatsAppClient whatsapp, ABPropsService abPropsService) {
        this.whatsapp = whatsapp;
        this.store = whatsapp.store();
        this.abPropsService = abPropsService;
        this.state = new AtomicReference<>(LidMigrationState.NOT_STARTED);
        this.primaryPnToAssignedLidCache = new ConcurrentHashMap<>();
        this.primaryPnToLatestLidCache = new ConcurrentHashMap<>();
        this.originalLidCache = new ConcurrentHashMap<>();
    }

    /**
     * Returns whether the LID migration has completed.
     *
     * <p>This is the Cobalt equivalent of WA Web's
     * {@code Lid1X1MigrationUtils.isLidMigrated()}, which reads
     * {@code WAIsAccountLidFieldMigrated === true} from UserPrefsIdb.
     * In Cobalt, migration completion is tracked by the state machine
     * reaching {@link LidMigrationState#COMPLETE}.
     *
     * @implNote ADAPTED: WAWebLid1X1MigrationGating.Lid1X1MigrationUtils.isLidMigrated
     * @return {@code true} if the migration state is {@link LidMigrationState#COMPLETE}
     */
    public boolean isLidMigrated() {
        return state.get() == LidMigrationState.COMPLETE; // WAWebLid1X1MigrationGating.isLidMigrated
    }

    /**
     * Returns whether the syncd session has been migrated.
     *
     * <p>This is a stub that always returns {@code false}, matching WA Web's
     * {@code Lid1X1MigrationUtils.isSyncdSessionMigrated()} which also
     * unconditionally returns {@code false}.
     *
     * @implNote WAWebLid1X1MigrationGating.Lid1X1MigrationUtils.isSyncdSessionMigrated
     * @return {@code false} always
     */
    public boolean isSyncdSessionMigrated() {
        return false; // WAWebLid1X1MigrationGating.isSyncdSessionMigrated
    }

    /**
     * Returns whether a PN (phone number) chat should be created.
     *
     * <p>This is a stub that always returns {@code false}, matching WA Web's
     * {@code Lid1X1MigrationUtils.shouldCreatePnChat()} which also
     * unconditionally returns {@code false}.
     *
     * @implNote WAWebLid1X1MigrationGating.Lid1X1MigrationUtils.shouldCreatePnChat
     * @return {@code false} always
     */
    public boolean shouldCreatePnChat() {
        return false; // WAWebLid1X1MigrationGating.shouldCreatePnChat
    }

    /**
     * Returns whether there is a state discrepancy in the LID migration status.
     *
     * <p>In WA Web, this checks {@code !isLidMigrated() && rawValue === true},
     * which is effectively always {@code false} since both read the same UserPrefs key.
     * Cobalt preserves this behavior by always returning {@code false}.
     *
     * @implNote WAWebLid1X1MigrationGating.Lid1X1MigrationUtils.hasStateDiscrepancy
     * @return {@code false} always (dead code in WA Web)
     */
    public boolean hasStateDiscrepancy() {
        return false; // WAWebLid1X1MigrationGating.hasStateDiscrepancy
    }

    /**
     * Initializes the migration service.
     * Should be called after connection is established.
     *
     * @implNote NO_WA_BASIS — Cobalt-specific state machine initialization
     */
    public void initialize() {
        if (state.compareAndSet(LidMigrationState.NOT_STARTED, LidMigrationState.WAITING_PROP)) {
            LOGGER.log(System.Logger.Level.INFO, "LID migration initialized, waiting for AB prop");
        }
    }

    /**
     * Called when the AB prop indicates LID migration is enabled.
     * Transitions from WAITING_PROP to WAITING_MAPPINGS state and schedules
     * a timeout for receiving peer mappings.
     *
     * <p>The timeout duration is read from the
     * {@link ABProp#LID_ONE_ON_ONE_MIGRATION_PEER_SYNC_TIMEOUT_IN_SECONDS} AB prop.
     * If the AB prop value is {@code 0}, no timeout is scheduled, matching WA Web's
     * early return when {@code getABPropConfigValue("lid_one_on_one_migration_peer_sync_timeout_in_seconds") === 0}.
     *
     * <p>The double-scheduling guard uses a state machine CAS instead of WA Web's
     * nullable timeout handle ({@code _ != null} check in
     * {@code WAWebLid1x1MigrationTimeout.scheduleLogoutIfNeeded}).
     *
     * @implNote WAWebLid1X1ThreadAccountMigrations.checkIfMigrationEnabled (function w),
     *           ADAPTED: WAWebLid1x1MigrationTimeout.scheduleLogoutIfNeeded,
     *           WAWebLid1x1MigrationTimeoutUtils.shouldScheduleTimeoutForMissingPeerMessage —
     *           WA Web computes timeout relative to primary migration timestamp via
     *           {@code timeoutForAt(now, primaryMigrationTime, timeoutSeconds)}; Cobalt schedules a flat
     *           timeout from the moment migration is enabled because the primary migration timestamp
     *           is not yet available (it arrives with the mapping sync message).
     *           The timeout callback re-check uses {@code state == WAITING_MAPPINGS} instead of
     *           re-calling {@code shouldScheduleTimeoutForMissingPeerMessage()}.
     *           The {@code hasStateDiscrepancy()} branch is dead code (always false) and is omitted.
     *           WAM events and the 5-second flush delay are telemetry and are skipped.
     */
    public void enableMigration() {
        if (state.compareAndSet(LidMigrationState.WAITING_PROP, LidMigrationState.WAITING_MAPPINGS)) {
            LOGGER.log(System.Logger.Level.INFO, "LID migration enabled, waiting for mappings from primary");
            // WAWebLid1x1MigrationTimeoutUtils.shouldScheduleTimeoutForMissingPeerMessage:
            //   var t = getABPropConfigValue("lid_one_on_one_migration_peer_sync_timeout_in_seconds");
            //   if (t === 0) return {shouldScheduleTimeout: false};
            var timeoutSeconds = abPropsService.getInt(ABProp.LID_ONE_ON_ONE_MIGRATION_PEER_SYNC_TIMEOUT_IN_SECONDS); // WAWebLid1x1MigrationTimeoutUtils.shouldScheduleTimeoutForMissingPeerMessage
            if (timeoutSeconds == 0) {
                LOGGER.log(System.Logger.Level.INFO, "LID migration peer sync timeout disabled by AB prop");
                return; // WAWebLid1x1MigrationTimeoutUtils.shouldScheduleTimeoutForMissingPeerMessage
            }
            mappingTimeoutFuture = SchedulerUtils.scheduleDelayed(
                    Duration.ofSeconds(timeoutSeconds),
                    () -> {
                        if (state.get() == LidMigrationState.WAITING_MAPPINGS) {
                            LOGGER.log(System.Logger.Level.WARNING,
                                    "LID migration timed out after {0}s waiting for mappings", timeoutSeconds);
                            handleError(new WhatsAppLidMigrationException.FailedToParseMappings(
                                    "Timed out waiting for peer migration mappings"));
                        }
                    });
        }
    }

    /**
     * Called when the AB prop indicates LID migration is disabled.
     * Transitions from WAITING_PROP to DISABLED.
     *
     * @implNote NO_WA_BASIS — Cobalt-specific state machine transition
     */
    public void disableMigration() {
        if (state.compareAndSet(LidMigrationState.WAITING_PROP, LidMigrationState.DISABLED)) {
            LOGGER.log(System.Logger.Level.INFO, "LID migration disabled");
        }
    }

    /**
     * Processes LID migration mappings received from the primary device.
     *
     * <p>This method handles the decoded {@link LIDMigrationMappingSyncPayload}
     * by populating the primary mapping caches ({@link #primaryPnToAssignedLidCache}
     * and {@link #primaryPnToLatestLidCache}), storing the
     * {@linkplain LIDMigrationMappingSyncPayload#chatDbMigrationTimestamp() migration timestamp},
     * and transitioning the state machine to {@link LidMigrationState#READY}.
     *
     * <p>If the payload is {@code null}, the method treats it as a malformed
     * peer message and delegates to {@link #handleError(WhatsAppLidMigrationException)}.
     * If the payload's mapping list is empty, the method proceeds normally
     * with an empty cache, matching WA Web's behavior where the parser returns
     * {@code {mappings: [], primaryMigrationTsSec: null}} and the caller
     * stores the result without error. Per-chat errors ({@code NoLidAvailable},
     * etc.) will arise naturally during the subsequent migration execution.
     *
     * @implNote WAWebLid1X1ThreadAccountMigrations.setLidMigrationMappings (function A/F),
     *           WAWebLid1x1MigrationPrimaryCache.$5,
     *           WAWebLid1x1MigrationMsgParser.parseLidMigrationMappingSyncMsg
     * @param payload the decoded mapping payload from primary device
     */
    public void processProtocolMessage(LIDMigrationMappingSyncPayload payload) {
        // WAWebLid1X1ThreadAccountMigrations.setLidMigrationMappings: if(e==null) -> logout(LidMigrationPeerMappingsMalformed)
        if (payload == null) {
            handleError(new WhatsAppLidMigrationException.FailedToParseMappings("null payload"));
            return;
        }

        var currentState = state.get();
        if (currentState != LidMigrationState.WAITING_MAPPINGS && currentState != LidMigrationState.WAITING_PROP) {
            LOGGER.log(System.Logger.Level.DEBUG, "Ignoring mappings in state: {0}", currentState);
            return;
        }

        try {
            // Cancel mapping timeout since we received mappings
            var timeout = mappingTimeoutFuture;
            if (timeout != null) {
                timeout.cancel(false);
                mappingTimeoutFuture = null;
            }

            // WAWebLid1X1ThreadAccountMigrations.setLidMigrationMappings: ts = unixTime()
            this.receiveTimestamp = Instant.now(); // ADAPTED: WAWebLid1X1ThreadAccountMigrations.setLidMigrationMappings

            // Process mappings — WA Web's parseLidMigrationMappingSyncMsg returns
            // {mappings: [], primaryMigrationTsSec: null} for empty payloads (not an error).
            // The caller (WAWebLid1x1MigrationPrimaryCache.$5) stores the result normally.
            var mappings = payload.pnToLidMappings();

            // WAWebLid1x1MigrationPrimaryCache.$5: t.$4 = e.primaryMigrationTsSec
            // For non-empty mappings this is a.chatDbMigrationTimestamp; for empty it is null
            if (mappings.isEmpty()) {
                // WAWebLid1x1MigrationMsgParser.parseLidMigrationMappingSyncMsg: returns primaryMigrationTsSec: null
                this.chatDbMigrationTimestamp = null;
            } else {
                // WAWebLid1x1MigrationMsgParser.parseLidMigrationMappingSyncMsg: returns primaryMigrationTsSec: a.chatDbMigrationTimestamp
                this.chatDbMigrationTimestamp = payload.chatDbMigrationTimestamp()
                        .orElse(null);
            }

            LOGGER.log(System.Logger.Level.INFO, "Processing {0} LID mappings from primary", mappings.size());

            // WAWebLid1x1MigrationPrimaryCache.$5: e.mappings.forEach(...)
            // Populate primary caches and update contacts
            for (var mapping : mappings) {
                processSingleMapping(mapping);
            }

            // Transition to READY state
            // WAWebLid1X1ThreadAccountMigrations.setLidMigrationMappings: state = READY
            state.set(LidMigrationState.READY);
            LOGGER.log(System.Logger.Level.INFO, "LID migration ready with {0} assigned mappings, {1} latest mappings",
                    primaryPnToAssignedLidCache.size(), primaryPnToLatestLidCache.size());

            // WAWebLid1x1MigrationManager.executeMigrationIfNeeded -> shouldMigrateNow -> migrate1x1Chats
            if (shouldAutoStartMigration()) {
                executeMigration();
            }

        } catch (Throwable throwable) {
            // WAWebLid1x1MigrationMsgParser.parseLidMigrationMappingSyncMsg: catch -> socketLogout(LidMigrationFailedToParseMapping)
            handleError(new WhatsAppLidMigrationException.FailedToParseMappings("error processing mappings", throwable));
        }
    }

    /**
     * Observes a chat DB migration timestamp and updates the internal timestamp
     * if the provided value is more recent.
     *
     * @implNote WAWebLid1X1ThreadAccountMigrations.flow — chatDbMigrationTimestamp tracking
     * @param timestamp the timestamp to observe, may be {@code null}
     */
    public void observeChatDbMigrationTimestamp(Instant timestamp) {
        if (timestamp == null) {
            return;
        }

        if (chatDbMigrationTimestamp == null || timestamp.isAfter(chatDbMigrationTimestamp)) {
            chatDbMigrationTimestamp = timestamp;
        }
    }

    /**
     * Processes LID mappings from a HistorySync message.
     *
     * <p>This method extracts LID mappings from two sources:
     * <ol>
     *     <li>The top-level {@code phoneNumberToLidMappings} field</li>
     *     <li>Individual conversation entries containing {@code pnJid} or {@code lidJid} fields</li>
     * </ol>
     *
     * <p>History sync data is only stored in the general store (via
     * {@code store.registerLidMapping()} and contact updates), not in the
     * primary mapping caches. This matches WhatsApp Web's behavior where
     * history sync mappings do not feed into the migration decision caches.
     *
     * <p>Additionally, if GlobalSettings contains a {@code chatDbLidMigrationTimestamp},
     * that timestamp is recorded for use during migration timing decisions.
     *
     * @implNote WAWebLid1X1ThreadAccountMigrations.flow
     * @param historySync the decoded HistorySync protobuf
     */
    public void processHistorySync(HistorySync historySync) {
        if (historySync == null) {
            return;
        }

        var mappingsProcessed = 0;

        // 1. Process top-level phoneNumberToLidMappings
        var topLevelMappings = historySync.phoneNumberToLidMappings();
        if (topLevelMappings != null && !topLevelMappings.isEmpty()) {
            for (var mapping : topLevelMappings) {
                if (processPhoneNumberToLidMapping(mapping)) {
                    mappingsProcessed++;
                }
            }
        }

        // 2. Process conversation-level LID fields
        var conversations = historySync.chats();
        if (conversations != null) {
            for (var conversation : conversations) {
                if (processConversationLidData(conversation)) {
                    mappingsProcessed++;
                }
            }
        }

        // 3. Extract chatDbMigrationTimestamp from GlobalSettings if present
        var chatDbLidMigrationTimestamp = historySync.globalSettings()
                .flatMap(GlobalSettings::chatDbLidMigrationTimestamp);
        if (chatDbLidMigrationTimestamp.isPresent()) {
            if (chatDbMigrationTimestamp == null || chatDbLidMigrationTimestamp.get().isAfter(chatDbMigrationTimestamp)) {
                this.chatDbMigrationTimestamp = chatDbLidMigrationTimestamp.get();
                LOGGER.log(System.Logger.Level.DEBUG,
                        "Updated chatDbMigrationTimestamp from GlobalSettings: {0}", chatDbLidMigrationTimestamp.get());
            }
        }

        if (mappingsProcessed > 0) {
            LOGGER.log(System.Logger.Level.INFO,
                    "Processed {0} LID mappings from history sync (type={1})",
                    mappingsProcessed, historySync.syncType());
        }
    }

    /**
     * Processes a single PhoneNumberToLidMapping from the top-level history sync field.
     *
     * <p>History sync mappings are stored only in the general store, not in the
     * primary migration caches.
     *
     * @implNote WAWebLid1X1ThreadAccountMigrations.flow
     * @param mapping the mapping to process
     * @return {@code true} if a valid mapping was processed
     */
    private boolean processPhoneNumberToLidMapping(PhoneNumberToLIDMapping mapping) {
        if (mapping == null) {
            return false;
        }

        var pnJid = mapping.pnJid().orElse(null);
        var lidJid = mapping.lidJid().orElse(null);

        if (pnJid == null || lidJid == null) {
            return false;
        }

        // Register bidirectional mapping in store (not in primary cache)
        store.registerLidMapping(pnJid, lidJid);

        // Update contact if exists
        store.findContactByJid(pnJid).ifPresent(contact -> contact.setLid(lidJid));

        return true;
    }

    /**
     * Extracts LID mapping from a conversation entry.
     *
     * <p>For LID chats (jid has lid server): extracts phone number from pnJid field.
     * For PN chats (jid has user server): extracts LID from lidJid field.
     *
     * <p>History sync conversation data is stored only in the general store, not
     * in the primary migration caches.
     *
     * @implNote WAWebLid1X1ThreadAccountMigrations.flow
     * @param conversation the conversation to process
     * @return {@code true} if a valid mapping was extracted
     */
    private boolean processConversationLidData(Chat conversation) {
        if (conversation == null) {
            return false;
        }

        var chatJid = conversation.jid();

        // Only process 1:1 chats (user or lid server)
        if (!chatJid.hasUserServer() && !chatJid.hasLidServer()) {
            return false;
        }

        final Jid phoneJid;
        final Jid lidJid;

        if (chatJid.hasLidServer()) {
            // LID chat: extract phone number from pnJid field
            phoneJid = conversation.phoneNumberJid().orElse(null);
            lidJid = chatJid;
        } else if (chatJid.hasUserServer()) {
            // PN chat: extract LID from lidJid field (the 'lid' field in Chat)
            phoneJid = chatJid;
            lidJid = conversation.lid().orElse(null);
        } else {
            phoneJid = null;
            lidJid = null;
        }

        if (phoneJid == null || lidJid == null) {
            return false;
        }

        // Register bidirectional mapping in store (not in primary cache)
        store.registerLidMapping(phoneJid, lidJid);

        // Update contact if exists
        store.findContactByJid(phoneJid).ifPresent(contact -> contact.setLid(lidJid));

        // Update the chat itself to ensure it has the mapping
        conversation.setLid(lidJid);
        if (!phoneJid.equals(chatJid)) {
            conversation.setPhoneNumberJid(phoneJid);
        }

        return true;
    }

    /**
     * Processes a single LID mapping entry from the primary device's protocol message.
     *
     * <p>Stores {@code assignedLid} into {@link #primaryPnToAssignedLidCache} (keyed
     * by the phone number's user part) and {@code latestLid} into
     * {@link #primaryPnToLatestLidCache} if present.  This matches WA Web's
     * {@code WAWebLid1x1MigrationPrimaryCache.$5} forEach callback:
     * <pre>{@code
     * t.$2.set(e.pnUser, e.assignedLid);
     * e.latestLid != null && t.$3.set(e.pnUser, e.latestLid);
     * }</pre>
     *
     * <p>Additionally performs an early contact update and store mapping
     * registration if the contact already exists.  WA Web defers this to
     * {@code learnMappingsInBulk()} after migration completes, but Cobalt
     * eagerly registers the mapping for any known contact as a proactive
     * optimization.
     *
     * @implNote WAWebLid1x1MigrationPrimaryCache.$5 (forEach callback),
     *           WAWebLid1x1MigrationMsgParser.parseLidMigrationMappingSyncMsg (mapping transform)
     * @param mapping the mapping entry to process
     */
    private void processSingleMapping(LIDMigrationMapping mapping) {
        if (mapping == null) {
            return;
        }

        // WAWebLid1x1MigrationMsgParser.parseLidMigrationMappingSyncMsg:
        //   n = asUserWidOrThrow(createUserWidOrThrow(e.pn.toString()))
        var jid = mapping.pn();
        var user = jid.user();

        // WAWebLid1x1MigrationPrimaryCache.$5: t.$2.set(e.pnUser, e.assignedLid)
        var assignedLid = mapping.assignedLid();
        primaryPnToAssignedLidCache.put(user, assignedLid);

        // WAWebLid1x1MigrationPrimaryCache.$5: e.latestLid != null && t.$3.set(e.pnUser, e.latestLid)
        mapping.latestLid().ifPresent(latest ->
                primaryPnToLatestLidCache.put(user, latest)
        );

        // ADAPTED: WAWebLid1x1MigrationPrimaryCache.$5 — WA Web defers contact/store
        // updates to learnMappingsInBulk; Cobalt eagerly updates for known contacts
        store.findContactByJid(jid).ifPresent(contact -> {
            contact.setLid(assignedLid);
            store.registerLidMapping(jid, assignedLid);
        });
    }

    /**
     * Executes the LID migration for all eligible chat threads.
     *
     * <p>After successful execution, sets state to {@link LidMigrationState#COMPLETE},
     * which is the equivalent of WA Web's {@code setIsLidMigrated(true, PEER, false)}.
     *
     * @implNote WAWebLid1X1ThreadAccountMigrations.migrate1x1Chats (function B/W),
     *           ADAPTED: WAWebLid1X1MigrationGating.setIsLidMigrated — state transition replaces UserPrefs write
     */
    public void executeMigration() {
        if (!state.compareAndSet(LidMigrationState.READY, LidMigrationState.IN_PROGRESS)) {
            var currentState = state.get();
            LOGGER.log(System.Logger.Level.WARNING, "Cannot start migration in state: {0}", currentState);
            return;
        }

        // Check compatibility AB prop before proceeding
        if (!abPropsService.getBool(ABProp.LID_ONE_ON_ONE_MIGRATION_COMPATIBLE)) {
            handleError(new WhatsAppLidMigrationException.IncompatibleClient());
            return;
        }

        LOGGER.log(System.Logger.Level.INFO, "Starting LID migration execution, waiting for offline delivery");
        store.waitForOfflineDeliveryEnd();
        LOGGER.log(System.Logger.Level.INFO, "Offline delivery complete, proceeding with migration");

        try {
            var resolutions = new ArrayList<LidMigrationResolution>();
            var chatsToProcess = new ArrayList<>(store.chats());

            // Pre-compute set of existing LID thread JIDs for inline split thread detection
            var existingLidThreads = new HashSet<Jid>();
            for (var chat : chatsToProcess) {
                if (chat.jid().hasLidServer()) {
                    existingLidThreads.add(chat.jid().toUserJid());
                }
            }

            // Phase 1: Resolve all threads (split thread detection is inline)
            for (var chat : chatsToProcess) {
                var resolution = resolveThread(chat, existingLidThreads);
                resolutions.add(resolution);
            }

            // Phase 2: Execute migrations
            executeResolutions(resolutions);

            // Phase 3: Mark complete — WA Web sets COMPLETE and setIsLidMigrated(true)
            // inside the DB lock, BEFORE learnMappingsInBulk() runs
            // WAWebLid1X1ThreadAccountMigrations.migrate1x1Chats: V(COMPLETE), setIsLidMigrated(true)
            state.set(LidMigrationState.COMPLETE);
            LOGGER.log(System.Logger.Level.INFO, "LID migration completed");

            // Phase 4: Bulk-register all primary mappings in the store
            // WAWebLid1X1ThreadAccountMigrations.migrate1x1Chats: learnMappingsInBulk() after lock
            learnMappingsInBulk();

        } catch (WhatsAppLidMigrationException e) {
            handleError(e);
        } catch (Throwable throwable) {
            handleError(new WhatsAppLidMigrationException.FailedToParseMappings("migration execution failed", throwable));
        }
    }

    /**
     * Resolves a single chat thread to determine its migration action.
     *
     * @param chat               the chat to resolve
     * @param existingLidThreads the set of JIDs of existing LID threads, used for
     *                           inline split thread detection
     * @return the resolution for this thread
     * @throws WhatsAppLidMigrationException.PrimaryMappingsObsolete if a LID mismatch
     *         indicates stale primary mappings
     * @throws WhatsAppLidMigrationException.NoLidAvailable if a non-deletable chat
     *         has no LID mapping
     * @throws WhatsAppLidMigrationException.SplitThreadMismatch if a local LID would
     *         collide with an existing LID thread
     * @implNote WAWebLid1X1ThreadAccountMigrations.getResolvedThreadAccountLid (function q/U),
     *           WAWebLid1X1ThreadAccountMigrations.migrate1x1Chats (LID chat handling in W)
     */
    private LidMigrationResolution resolveThread(Chat chat, Set<Jid> existingLidThreads) {
        var jid = chat.jid();

        // Rule 1: Already LID -> KEEP
        // Also handle LidOriginType promotion: PNH_CTWA -> GENERAL when primaryProvidedLatestLid matches
        if (jid.hasLidServer()) {
            if (LID_ORIGIN_TYPE_PNH_CTWA.equals(chat.lidOriginType().orElse(null))) {
                var matchesPrimary = primaryPnToLatestLidCache.values().stream()
                        .anyMatch(latestLid -> latestLid.toUserJid().equals(jid.toUserJid()));
                if (matchesPrimary) {
                    chat.setLidOriginType(LID_ORIGIN_TYPE_GENERAL);
                }
            }
            return new LidMigrationResolution.Keep(jid, LidMigrationResolution.KeepReason.ALREADY_LID);
        }

        // Rule 2: Groups and communities -> KEEP (not subject to 1:1 migration)
        if (jid.hasGroupOrCommunityServer()) {
            return new LidMigrationResolution.Keep(jid, LidMigrationResolution.KeepReason.GROUP_OR_COMMUNITY);
        }

        // Rule 3: Newsletters -> KEEP
        if (jid.hasNewsletterServer()) {
            return new LidMigrationResolution.Keep(jid, LidMigrationResolution.KeepReason.NEWSLETTER);
        }

        // Rule 4: Broadcast lists -> KEEP
        if (jid.hasBroadcastServer()) {
            if (jid.isStatusBroadcastAccount()) {
                return new LidMigrationResolution.Keep(jid, LidMigrationResolution.KeepReason.STATUS_BROADCAST);
            }
            return new LidMigrationResolution.Keep(jid, LidMigrationResolution.KeepReason.BROADCAST);
        }

        // Rule 5: Bot accounts -> KEEP
        if (jid.hasBotServer()) {
            return new LidMigrationResolution.Keep(jid, LidMigrationResolution.KeepReason.BOT);
        }

        // Rule 6: Check for split thread flag
        // phoneDuplicateLidThread indicates this PN chat has a duplicate LID thread
        if (chat.phoneNumberhDuplicateLidThread()) {
            return new LidMigrationResolution.Keep(jid, LidMigrationResolution.KeepReason.DUPLICATE_WILL_MERGE);
        }

        // Rule 7: Determine local LID (from chat or store) and primary LID (from assigned cache)
        var chatLid = chat.lid().orElse(null);
        var user = jid.user();
        // Use assignedLid cache (not merged) - matches WA Web's getLidForPn()
        var primaryLid = user != null
                ? primaryPnToAssignedLidCache.get(user)
                : null;
        var localLid = chatLid != null
                ? chatLid
                : (user != null ? store.findLidByPhone(jid).orElse(null) : null);

        // Rule 8: Primary has a LID for this contact
        if (primaryLid != null) {
            // Rule 8a: No local LID or local matches primary -> use primary
            if (localLid == null || localLid.toUserJid().equals(primaryLid.toUserJid())) {
                return new LidMigrationResolution.Migrate(jid, primaryLid);
            }

            // Rule 8b: LID mismatch between local and primary
            // Gate mismatch check with AB prop
            if (abPropsService.getBool(ABProp.LID_ONE_ON_ONE_MIGRATION_LOG_OUT_ON_MISMATCH)) {
                // Compare timestamps to determine which is fresher
                // Use >= (not >) - local timestamp >= sync timestamp means local is fresher
                var chatTimestamp = chat.conversationTimestamp();
                var effectiveSyncTimestamp = getEffectiveSyncTimestamp();
                if (chatTimestamp.isPresent() && !chatTimestamp.get().isBefore(effectiveSyncTimestamp)) {
                    // Local data is fresher than or equal to primary sync -> primary mappings are obsolete
                    throw new WhatsAppLidMigrationException.PrimaryMappingsObsolete();
                }
            }

            // Primary is fresher or mismatch logging out is disabled -> trust primary
            return new LidMigrationResolution.Migrate(jid, primaryLid);
        }

        // Rule 9: Primary has no LID, but local does -> use local
        // Inline split thread check: if the local LID already exists as a separate thread, abort
        // Matches WA Web's inline check: isThreadExistsWithChatJid ? logout(SplitThreadMismatch) : migrate
        if (localLid != null) {
            if (existingLidThreads.contains(localLid.toUserJid())) {
                throw new WhatsAppLidMigrationException.SplitThreadMismatch();
            }
            return new LidMigrationResolution.Migrate(jid, localLid);
        }

        // Rule 9b: originalLid fallback — check the cache of LIDs set at chat creation time
        // Matches WA Web's chat.originalLid check in getResolvedThreadAccountLid
        var cachedOriginalLid = user != null ? originalLidCache.get(user) : null;
        if (cachedOriginalLid != null) {
            return new LidMigrationResolution.Migrate(jid, cachedOriginalLid.toUserJid());
        }

        // Rule 10: No LID found - evaluate if chat can be deleted
        if (!canDeleteChat(chat)) {
            // Non-deletable chat with no LID -> abort migration
            throw new WhatsAppLidMigrationException.NoLidAvailable();
        }

        // Chat is eligible for deletion
        return new LidMigrationResolution.Delete(jid, LidMigrationResolution.DeleteReason.NO_LID_MAPPING);
    }

    /**
     * Returns the effective sync timestamp for migration timing comparisons.
     *
     * <p>Returns {@link #chatDbMigrationTimestamp} if non-{@code null}, otherwise
     * falls back to {@link #receiveTimestamp}, and finally to {@link Instant#EPOCH}.
     *
     * @implNote ADAPTED: WAWebLid1x1MigrationPrimaryCache.getPrimaryMigrationTsSec — WA Web returns
     *           raw {@code $4} (primaryMigrationTsSec); Cobalt wraps as {@link Instant} and adds
     *           fallback to {@link #receiveTimestamp} and {@link Instant#EPOCH}
     * @return the effective sync timestamp, never {@code null}
     */
    private Instant getEffectiveSyncTimestamp() {
        if (chatDbMigrationTimestamp != null) {
            return chatDbMigrationTimestamp;
        }
        if (receiveTimestamp != null) {
            return receiveTimestamp;
        }
        return Instant.EPOCH;
    }

    /**
     * Determines if a chat can be deleted during LID migration when no LID
     * mapping is available.
     *
     * <p>This matches WhatsApp Web's deletion logic (function {@code j/K}):
     * <ol>
     *     <li>Compute broadcast exemption: if all messages are safe stubs +
     *         broadcast messages ({@code te()}), AND the pairing timestamp
     *         is at or before the oldest message timestamp, then the chat
     *         is pre-exempted for deletion. (WA Web also checks {@code ne()}
     *         for ContactInfoCard messages, but Cobalt's protobuf model does
     *         not expose the message subtype field needed for that check.)</li>
     *     <li>If NOT pre-exempted AND {@code createdLocally !== true}, the chat
     *         is NOT deletable. Since Cobalt's {@code Chat} model does not
     *         track {@code createdLocally}, this step is skipped and the
     *         remaining checks are applied unconditionally.</li>
     *     <li>Chats with ephemeral settings are NOT deletable, unless the
     *         disappearing mode trigger is {@code ACCOUNT_SETTING} and the
     *         chat contains a disappearing mode system message</li>
     *     <li>Locked chats are NOT deletable</li>
     *     <li>Archived chats are NOT deletable</li>
     *     <li>Muted chats are NOT deletable</li>
     *     <li>Chats where all messages are safe system stubs, or all messages
     *         are safe stubs + call log entries (with at least one call log),
     *         or pre-exempted, are deletable</li>
     *     <li>Otherwise the chat is NOT deletable</li>
     * </ol>
     *
     * @implNote WAWebLid1X1ThreadAccountMigrations.K
     * @param chat the chat to evaluate
     * @return {@code true} if the chat can be safely deleted
     */
    private boolean canDeleteChat(Chat chat) {
        var messages = chat.messages();

        // WAWebLid1X1ThreadAccountMigrations.K: var a = false;
        // (te(r) || ne(r)) && H(getPairingTimestamp(), J(r).oldestMessageTs) === "false" && (a = true)
        // H returns "false" when pairingTimestamp != null && !(oldestMessageTs < pairingTimestamp),
        // i.e., when pairingTimestamp <= oldestMessageTs
        var broadcastExempt = false; // WAWebLid1X1ThreadAccountMigrations.K
        if (allMessagesAreSafeStubsOrBroadcast(messages)) { // WAWebLid1X1ThreadAccountMigrations.te
            var oldestMessageTs = getOldestMessageTimestamp(messages);
            if (oldestMessageTs != null && isPairingTimestampAtOrBefore(oldestMessageTs)) { // WAWebLid1X1ThreadAccountMigrations.H
                broadcastExempt = true;
            }
        }
        // ADAPTED: ne() (contactInfoCard) check is omitted because Cobalt's protobuf-based
        // ChatMessageInfo model does not expose the message subtype field needed to identify
        // ContactInfoCard messages. WA Web's subtype is an internal DB field, not protobuf.

        // ADAPTED: WAWebLid1X1ThreadAccountMigrations.K: !a && e.createdLocally !== true
        // Cobalt's Chat model does not track the createdLocally field.
        // WA Web blocks deletion when !a AND !createdLocally.
        // Since we cannot determine createdLocally, we skip this check and fall through
        // to the remaining guards (ephemeral/locked/archived/muted/message content).
        // This is slightly more permissive than WA Web for chats that are both
        // non-broadcast and non-createdLocally, but the message content check
        // (allSafeStubs || allStubsOrCallLog || broadcastExempt) at the end
        // provides the primary safety net.

        // Ephemeral settings check — NOT deletable unless exempted
        // WAWebLid1X1ThreadAccountMigrations.K: (e.ephemeralDuration != null || e.ephemeralSettingTimestamp != null) && !re(e, r)
        if (hasEphemeralSettings(chat) && !isEphemeralExempt(chat, messages)) {
            return false; // WAWebLid1X1ThreadAccountMigrations.K: "ephemeral_duration"
        }

        // Locked chats are NOT deletable
        // WAWebLid1X1ThreadAccountMigrations.K: e.isLocked
        if (chat.locked()) {
            return false; // WAWebLid1X1ThreadAccountMigrations.K: "locked"
        }

        // Archived chats are NOT deletable
        // WAWebLid1X1ThreadAccountMigrations.K: e.archive
        if (chat.archived()) {
            return false; // WAWebLid1X1ThreadAccountMigrations.K: "archived"
        }

        // Muted chats are NOT deletable
        // WAWebLid1X1ThreadAccountMigrations.K: e.muteExpiration
        if (chat.mute().map(ChatMute::isMuted).orElse(false)) {
            return false; // WAWebLid1X1ThreadAccountMigrations.K: "mute_expiration"
        }

        // WAWebLid1X1ThreadAccountMigrations.K: r.every(X) || ee(r) || a
        // Message content check: deletable if all messages are safe stubs,
        // or all messages are safe stubs + call log entries with at least one call log,
        // or pre-exempted via broadcast check
        return allMessagesAreSafeStubs(messages)
                || allMessagesAreSafeStubsOrCallLog(messages)
                || broadcastExempt; // WAWebLid1X1ThreadAccountMigrations.K
    }

    /**
     * Returns whether the chat has ephemeral settings configured.
     *
     * <p>Matches WA Web's check: {@code e.ephemeralDuration != null || e.ephemeralSettingTimestamp != null}.
     *
     * @implNote WAWebLid1X1ThreadAccountMigrations.flow
     * @param chat the chat to check
     * @return {@code true} if the chat has ephemeral duration or setting timestamp
     */
    private boolean hasEphemeralSettings(Chat chat) {
        return chat.ephemeralExpiration().isPresent() || chat.ephemeralSettingTimestamp().isPresent();
    }

    /**
     * Returns whether the chat is exempt from the ephemeral deletability block.
     *
     * <p>Matches WA Web's {@code re()} function: returns {@code true} if the chat's
     * disappearing mode trigger is {@code ACCOUNT_SETTING} and the message list contains
     * at least one disappearing mode system message.
     *
     * @implNote WAWebLid1X1ThreadAccountMigrations.flow.re
     * @param chat     the chat to check
     * @param messages the chat's messages
     * @return {@code true} if the chat is exempt from the ephemeral block
     */
    private boolean isEphemeralExempt(Chat chat, Collection<ChatMessageInfo> messages) {
        var trigger = chat.disappearingMode()
                .flatMap(ChatDisappearingMode::trigger)
                .orElse(null);
        if (trigger != ChatDisappearingMode.Trigger.ACCOUNT_SETTING) {
            return false;
        }

        return messages.stream().anyMatch(msg -> {
            var stubType = msg.messageStubType().orElse(null);
            return stubType == ChatMessageInfo.StubType.DISAPPEARING_MODE;
        });
    }

    /**
     * Returns whether all messages are either migration-safe stubs or broadcast messages,
     * with at least one broadcast message present.
     *
     * <p>Matches WA Web's {@code te(r)} function which checks
     * {@code r.every(e => X(e) || e.broadcast) && r.some(e => e.broadcast)}.
     *
     * @implNote WAWebLid1X1ThreadAccountMigrations.te
     * @param messages the messages to check
     * @return {@code true} if every message is a safe stub or broadcast, with at least one broadcast
     */
    private boolean allMessagesAreSafeStubsOrBroadcast(Collection<ChatMessageInfo> messages) {
        var hasBroadcast = false; // WAWebLid1X1ThreadAccountMigrations.te
        for (var msg : messages) {
            if (isMigrationSafeStub(msg)) {
                continue;
            }
            if (msg.broadcast()) { // WAWebLid1X1ThreadAccountMigrations.te: e.broadcast
                hasBroadcast = true;
                continue;
            }
            return false;
        }
        return hasBroadcast; // WAWebLid1X1ThreadAccountMigrations.te: r.some(e => e.broadcast)
    }

    /**
     * Returns the oldest message timestamp from the collection.
     *
     * <p>Matches WA Web's {@code J(r).oldestMessageTs} which computes
     * {@code Math.min(...r.map(e => e.t))}.
     *
     * @implNote WAWebLid1X1ThreadAccountMigrations.J
     * @param messages the messages to compute from
     * @return the oldest timestamp, or {@code null} if the collection is empty
     */
    private Instant getOldestMessageTimestamp(Collection<ChatMessageInfo> messages) {
        Instant oldest = null; // WAWebLid1X1ThreadAccountMigrations.J
        for (var msg : messages) {
            var ts = msg.timestamp().orElse(null);
            if (ts != null && (oldest == null || ts.isBefore(oldest))) {
                oldest = ts;
            }
        }
        return oldest; // WAWebLid1X1ThreadAccountMigrations.J: oldestMessageTs
    }

    /**
     * Returns whether the pairing timestamp is at or before the given timestamp.
     *
     * <p>Matches WA Web's {@code H(pairingTimestamp, oldestMessageTs) === "false"}
     * where {@code H(e, t)} returns {@code "false"} when {@code e != null && !(t < e)},
     * meaning {@code pairingTimestamp != null && oldestMessageTs >= pairingTimestamp}.
     *
     * @implNote WAWebLid1X1ThreadAccountMigrations.H
     * @param messageTimestamp the message timestamp to compare against
     * @return {@code true} if pairing timestamp is non-null and at or before the message timestamp
     */
    private boolean isPairingTimestampAtOrBefore(Instant messageTimestamp) {
        var pairingTs = store.pairingTimestamp().orElse(null); // WAWebUserPrefsMultiDevice.getPairingTimestamp
        if (pairingTs == null) {
            return false; // WAWebLid1X1ThreadAccountMigrations.H: e == null -> "unknown"
        }
        // WAWebLid1X1ThreadAccountMigrations.H: t < e ? "true" : "false"
        // We want H(...) === "false", which means !(messageTimestamp < pairingTimestamp),
        // i.e., messageTimestamp >= pairingTimestamp, i.e., !messageTimestamp.isBefore(pairingTimestamp)
        return !messageTimestamp.isBefore(pairingTs); // WAWebLid1X1ThreadAccountMigrations.H
    }

    /**
     * Returns whether all messages in the collection are migration-safe system stubs.
     *
     * <p>Matches WA Web's {@code r.every(X)} check, where {@code X()} returns {@code true}
     * for initial e2e notifications and disappearing mode system messages.
     *
     * @implNote WAWebLid1X1ThreadAccountMigrations.X
     * @param messages the messages to check
     * @return {@code true} if every message is a safe system stub
     */
    private boolean allMessagesAreSafeStubs(Collection<ChatMessageInfo> messages) {
        return messages.stream().allMatch(this::isMigrationSafeStub);
    }

    /**
     * Returns whether all messages are either migration-safe stubs or call log entries,
     * with at least one call log entry present.
     *
     * <p>Matches WA Web's {@code ee(r)} function.
     *
     * @implNote WAWebLid1X1ThreadAccountMigrations.flow.ee
     * @param messages the messages to check
     * @return {@code true} if every message is a safe stub or call log, with at least one call log
     */
    private boolean allMessagesAreSafeStubsOrCallLog(Collection<ChatMessageInfo> messages) {
        var hasCallLog = false;
        for (var msg : messages) {
            if (isMigrationSafeStub(msg)) {
                continue;
            }
            if (isCallLogMessage(msg)) {
                hasCallLog = true;
                continue;
            }
            return false;
        }
        return hasCallLog;
    }

    /**
     * Returns whether a message is a migration-safe system stub that can be ignored.
     *
     * @implNote WAWebLid1X1ThreadAccountMigrations.flow.X
     * @param msg the message to check
     * @return {@code true} if the message is a safe stub
     */
    private boolean isMigrationSafeStub(ChatMessageInfo msg) {
        if (!msg.message().isEmpty()) {
            return false;
        }

        var stubType = msg.messageStubType().orElse(null);
        return stubType != null && MIGRATION_SAFE_STUB_TYPES.contains(stubType);
    }

    /**
     * Returns whether a message is a call log entry.
     *
     * @implNote WAWebLid1X1ThreadAccountMigrations.flow.ee
     * @param msg the message to check
     * @return {@code true} if the message is a call log entry
     */
    private boolean isCallLogMessage(ChatMessageInfo msg) {
        var stubType = msg.messageStubType().orElse(null);
        return stubType != null && CALL_LOG_STUB_TYPES.contains(stubType);
    }


    /**
     * Executes the resolved migrations.
     *
     * @implNote ADAPTED: WAWebLid1X1ThreadAccountMigrations.migrate1x1Chats — WA Web collects
     *           updates into arrays and calls bulkCreateOrMerge/bulkRemove; Cobalt executes
     *           per-resolution for simplicity
     * @param resolutions the resolutions to execute
     */
    private void executeResolutions(List<LidMigrationResolution> resolutions) {
        for (var resolution : resolutions) {
            try {
                switch (resolution) {
                    case LidMigrationResolution.Migrate migrate -> executeMigrate(migrate);
                    case LidMigrationResolution.Delete delete -> executeDelete(delete);
                    case LidMigrationResolution.Keep _ -> {}
                }
            } catch (Throwable throwable) {
                LOGGER.log(System.Logger.Level.ERROR, "Error executing resolution for {0}: {1}",
                        resolution.originalJid(), throwable.getMessage());
            }
        }
    }

    /**
     * Executes a migration resolution by updating the chat to use LID addressing.
     *
     * @implNote ADAPTED: WAWebLid1X1ThreadAccountMigrations.migrate1x1Chats — maps to
     *           WA Web's {@code bulkCreateOrMerge(i)} where each entry has
     *           {@code {id, accountLid, lidOriginType}}
     * @param migrate the migration resolution to execute
     */
    private void executeMigrate(LidMigrationResolution.Migrate migrate) {
        var originalJid = migrate.originalJid();
        var targetLid = migrate.targetLid();

        // Find the chat
        var chat = store.findChatByJid(originalJid).orElse(null);
        if (chat == null) {
            LOGGER.log(System.Logger.Level.WARNING, "Chat not found for migration: {0}", originalJid);
            return;
        }

        // Update the chat's LID
        chat.setLid(targetLid);

        // Store the phoneJid for backward compatibility
        chat.setPhoneNumberJid(originalJid);

        // Register the mapping in the store
        store.registerLidMapping(originalJid, targetLid);

        // Update associated contact if exists
        store.findContactByJid(originalJid).ifPresent(contact -> {
            contact.setLid(targetLid);
        });

        LOGGER.log(System.Logger.Level.DEBUG, "Migrated chat {0} -> {1}", originalJid, targetLid);
    }

    /**
     * Executes a delete resolution by removing the chat from the store.
     *
     * @implNote ADAPTED: WAWebLid1X1ThreadAccountMigrations.migrate1x1Chats — maps to
     *           WA Web's {@code bulkRemove(m)} for chats and {@code bulkRemove(p)} for messages
     * @param delete the delete resolution to execute
     */
    private void executeDelete(LidMigrationResolution.Delete delete) {
        var originalJid = delete.originalJid();

        // Remove the chat from store
        var removed = store.removeChat(originalJid);

        if (removed.isPresent()) {
            LOGGER.log(System.Logger.Level.DEBUG, "Deleted chat {0}: {1}", originalJid, delete.reason());
        }
    }

    /**
     * Handles a LID change notification for a contact.
     *
     * @implNote WAWebLid1X1ThreadAccountMigrations.flow
     * @param phoneJid the phone number JID
     * @param newLid   the new LID
     * @param oldLid   the old LID (may be {@code null})
     */
    public void changeLid(Jid phoneJid, Jid newLid, Jid oldLid) {
        if (phoneJid == null || newLid == null) {
            return;
        }

        // Update primary caches
        if (phoneJid.user() != null) {
            primaryPnToAssignedLidCache.put(phoneJid.user(), newLid);
            primaryPnToLatestLidCache.put(phoneJid.user(), newLid);
        }

        // Update store mappings
        store.registerLidMapping(phoneJid, newLid);

        // Update contact
        store.findContactByJid(phoneJid).ifPresent(contact -> {
            contact.setLid(newLid);
        });

        // Update chat
        store.findChatByJid(phoneJid).ifPresent(chat -> {
            chat.setLid(newLid);
            chat.setPhoneNumberJid(phoneJid);
        });

        LOGGER.log(System.Logger.Level.DEBUG, "LID changed for {0}: {1} -> {2}", phoneJid, oldLid, newLid);
    }

    /**
     * Registers the original LID for a locally-created chat.
     *
     * <p>This should be called from the chat creation logic when a LID mapping
     * is already known at chat creation time and LID migration has not yet
     * completed. Matches WA Web's {@code WAWebCreateChat} behavior where
     * {@code originalLid} is set on the chat object.
     *
     * @implNote WAWebCreateChat — chat.originalLid
     * @param phoneJid the phone number JID of the chat
     * @param lid      the LID known at chat creation time
     */
    public void registerOriginalLid(Jid phoneJid, Jid lid) {
        if (phoneJid == null || lid == null || phoneJid.user() == null) {
            return;
        }

        originalLidCache.put(phoneJid.user(), lid);
    }

    /**
     * Persists primary mappings to the store's bidirectional mapping tables.
     *
     * <p>Matches WA Web's {@code learnMappingsInBulk()} which uses two learning
     * sources with skip logic:
     * <ol>
     *     <li>Skips entries where the assigned LID already matches the store's
     *         current LID for that phone number</li>
     *     <li>Processes "migration-sync-old" mappings first (assigned LID only,
     *         when the latest LID already matches local)</li>
     *     <li>Processes "migration-sync-latest" mappings second (both assigned
     *         and latest LIDs, when the latest LID differs from local)</li>
     * </ol>
     *
     * @implNote WAWebLid1x1MigrationPrimaryCache.learnMappingsInBulk,
     *           WAWebLid1x1MigrationPrimaryCache.$6 (categorization),
     *           WAWebDBCreateLidPnMappings.createLidPnMappings (store registration)
     */
    private void learnMappingsInBulk() {
        var oldMappings = new ArrayList<Map.Entry<Jid, Jid>>();
        var latestMappings = new ArrayList<Map.Entry<Jid, Jid>>();

        for (var entry : primaryPnToAssignedLidCache.entrySet()) {
            var phoneJid = Jid.of(entry.getKey());
            var assignedLid = entry.getValue();

            // Skip if the assigned LID already matches the store's current mapping
            var currentLid = store.findLidByPhone(phoneJid).orElse(null);
            if (assignedLid.toUserJid().equals(currentLid != null ? currentLid.toUserJid() : null)) {
                continue;
            }

            // Check if latestLid matches the current local LID to determine categorization
            var latestLid = primaryPnToLatestLidCache.get(entry.getKey());
            if (latestLid != null && latestLid.toUserJid().equals(currentLid != null ? currentLid.toUserJid() : null)) {
                // Latest matches local -> "migration-sync-old": register assigned only
                oldMappings.add(Map.entry(phoneJid, assignedLid));
            } else {
                // Latest differs from local -> "migration-sync-latest": register assigned + latest
                latestMappings.add(Map.entry(phoneJid, assignedLid));
                if (latestLid != null) {
                    latestMappings.add(Map.entry(phoneJid, latestLid));
                }
            }
        }

        // Process old mappings first, then latest (ordering matters for conflict resolution)
        for (var mapping : oldMappings) {
            store.registerLidMapping(mapping.getKey(), mapping.getValue());
        }
        for (var mapping : latestMappings) {
            store.registerLidMapping(mapping.getKey(), mapping.getValue());
        }

        LOGGER.log(System.Logger.Level.INFO,
                "Bulk-registered LID mappings: {0} old, {1} latest",
                oldMappings.size(), latestMappings.size());
    }

    /**
     * Determines if migration should auto-start after mappings are received.
     *
     * <p>WhatsApp Web's {@code WAWebLid1x1MigrationManager.executeMigrationIfNeeded()}
     * unconditionally calls {@code shouldMigrateNow()} (which checks state == READY ||
     * IN_PROGRESS) and then {@code migrate1x1Chats()}. Since Cobalt's state machine is
     * already in READY at the point this method is called from
     * {@link #processProtocolMessage(LIDMigrationMappingSyncPayload)}, this always
     * returns {@code true}.
     *
     * <p>The dependent task registry ({@code addDependentMigrationTask} / {@code $1})
     * from the WA Web {@code ThreadMigrationManager} singleton is intentionally omitted
     * because those tasks are WA Web-specific IndexedDB migrations (favorites, labels,
     * carts, blocklist, PNH threads) that do not apply to Cobalt's flat store model.
     *
     * @implNote ADAPTED: WAWebLid1x1MigrationManager.executeMigrationIfNeeded,
     *           WAWebLid1X1ThreadAccountMigrations.shouldMigrateNow (function O)
     * @return {@code true}
     */
    private boolean shouldAutoStartMigration() {
        return true; // WAWebLid1x1MigrationManager.executeMigrationIfNeeded
    }

    /**
     * Handles a migration error by setting the state to {@link LidMigrationState#FAILED}
     * and delegating to the client's error handler.
     *
     * @implNote ADAPTED: WAWebLid1X1MigrationGating — WA Web uses inline recovery, Cobalt throws
     * @param error the migration exception
     */
    private void handleError(WhatsAppLidMigrationException error) {
        state.set(LidMigrationState.FAILED);
        LOGGER.log(System.Logger.Level.ERROR, "LID migration failed: {0}", error.getMessage());
        whatsapp.handleFailure(error);
    }

    /**
     * Resets the migration service state for a new session.
     * Called when the client disconnects and reconnects.
     *
     * @implNote ADAPTED: WAWebLid1x1MigrationPrimaryCache.clear — WA Web's {@code clear()} resets
     *           {@code $1} and clears both maps; Cobalt preserves caches across reconnects since
     *           {@code clear()} is only called from {@code updateCacheIfEmpty()} (re-population
     *           guard) and {@code WAWebTestUtilRollbackLidThreadMigration} (debug utility)
     */
    public void reset() {
        var timeout = mappingTimeoutFuture;
        if (timeout != null) {
            timeout.cancel(false);
            mappingTimeoutFuture = null;
        }
        var currentState = state.get();
        // Only reset if not in a terminal state
        if (!currentState.isTerminal()) {
            state.set(LidMigrationState.NOT_STARTED);
        }
        // Don't clear primary caches on reconnect - they persist across sessions
    }

    /**
     * Looks up a LID for a phone number JID.
     * Checks primary assigned cache first, then store mappings.
     *
     * @implNote ADAPTED: WAWebLid1x1MigrationPrimaryCache.getLidForPn — WA Web returns only from
     *           {@code $2} (assigned cache); Cobalt additionally falls back to {@code store.findLidByPhone}
     *           for mappings learned outside the primary cache
     * @param phoneJid the phone number JID
     * @return the LID if found
     */
    public Optional<Jid> lookupLid(Jid phoneJid) {
        if (phoneJid == null || phoneJid.user() == null) {
            return Optional.empty();
        }

        // Check primary assigned cache
        var cached = primaryPnToAssignedLidCache.get(phoneJid.user());
        if (cached != null) {
            return Optional.of(cached);
        }

        // Check store mappings (these don't expire)
        return store.findLidByPhone(phoneJid);
    }

    /**
     * Determines whether to use LID addressing mode for a recipient.
     *
     * @implNote ADAPTED: WAWebLid1X1MigrationGating.isLidMigrated — checks state machine instead of UserPrefs
     * @param recipientJid the recipient's JID
     * @return {@code true} if LID addressing should be used
     */
    public boolean shouldUseLidAddressing(Jid recipientJid) {
        if (recipientJid == null) {
            return false;
        }

        // Already using LID
        if (recipientJid.hasLidServer()) {
            return true;
        }

        // Groups, newsletters, broadcasts don't use LID addressing
        if (recipientJid.hasGroupOrCommunityServer() ||
            recipientJid.hasNewsletterServer() ||
            recipientJid.hasBroadcastServer()) {
            return false;
        }

        // Check migration state
        var currentState = state.get();
        if (currentState != LidMigrationState.COMPLETE && currentState != LidMigrationState.IN_PROGRESS) {
            return false;
        }

        // Check if we have a LID for this recipient
        return lookupLid(recipientJid).isPresent();
    }

    /**
     * Returns whether a JID should have an account LID.
     *
     * <p>A JID should have an account LID when the LID migration has completed
     * and the JID belongs to a regular user (not a bot, not the PSA/announcements
     * account). This matches WA Web's {@code shouldHaveAccountLid(wid)} which
     * checks {@code isLidMigrated() && wid.isRegularUser()}, where
     * {@code isRegularUser()} means {@code isUser() && !isPSA() && !isBot()}.
     *
     * @implNote WAWebLidMigrationUtils.shouldHaveAccountLid
     * @param jid the JID to check
     * @return {@code true} if the JID should have an account LID
     */
    public boolean shouldHaveAccountLid(Jid jid) {
        if (jid == null) {
            return false;
        }

        // WAWebLidMigrationUtils.shouldHaveAccountLid: isLidMigrated() && e.isRegularUser()
        // isRegularUser() = isUser() && !isPSA() && !isBot()
        // isUser() = c.us || lid || bot || hosted || hosted.lid
        return isLidMigrated() && isRegularUser(jid); // WAWebLidMigrationUtils.shouldHaveAccountLid
    }

    /**
     * Returns whether a JID represents a regular user.
     *
     * <p>Matches WA Web's {@code Wid.isRegularUser()} which returns
     * {@code this.isUser() && !this.isPSA() && !this.isBot()}, where
     * {@code isUser()} includes servers {@code c.us}, {@code lid}, {@code bot},
     * {@code hosted}, and {@code hosted.lid}.
     *
     * @implNote WAWebWid.isRegularUser
     * @param jid the JID to check
     * @return {@code true} if the JID is a regular user
     */
    private static boolean isRegularUser(Jid jid) {
        // WAWebWid.isUser: c.us || lid || bot || hosted || hosted.lid
        if (!jid.hasUserServer() && !jid.hasLidServer() && !jid.hasBotServer()
                && !jid.hasHostedServer() && !jid.hasHostedLidServer()) {
            return false; // WAWebWid.isRegularUser
        }

        // WAWebWid.isPSA: user === "0" && server === "c.us"
        if (jid.equals(Jid.announcementsAccount())) {
            return false; // WAWebWid.isRegularUser
        }

        // WAWebWid.isBot: isPnBot() || isFbidBot()
        if (jid.isBot()) {
            return false; // WAWebWid.isRegularUser
        }

        return true; // WAWebWid.isRegularUser
    }

    /**
     * Converts a JID to its phone number (PN) representation.
     *
     * <p>If the JID is not a LID, returns it unchanged. If the JID is a LID,
     * looks up the corresponding phone number from the store. Returns
     * {@code null} if no phone number mapping is found.
     *
     * <p>This matches WA Web's {@code toPn(wid)} function which returns the wid
     * unchanged if not a LID, or looks up the phone number via
     * {@code getPhoneNumber(wid)}.
     *
     * @implNote WAWebLidMigrationUtils.toPn
     * @param jid the JID to convert
     * @return the phone number JID, the original JID if not a LID,
     *         or {@code null} if no mapping is found
     */
    public Jid toPn(Jid jid) {
        if (jid == null) {
            return null;
        }

        // WAWebLidMigrationUtils.toPn: if(!t.isLid()) return t
        if (!jid.hasLidServer()) {
            return jid; // WAWebLidMigrationUtils.toPn
        }

        // WAWebLidMigrationUtils.toPn: var n = getPhoneNumber(t); return n
        return store.findPhoneByLid(jid).orElse(null); // WAWebLidMigrationUtils.toPn
    }

    /**
     * Converts a JID to its LID representation.
     *
     * <p>If the JID is already a LID, returns it unchanged. Otherwise, coerces
     * the JID to a user JID (stripping device/agent) and looks up the
     * corresponding LID from the store. Returns {@code null} if no LID mapping
     * is found.
     *
     * <p>This matches WA Web's {@code toLid(wid)} function which returns the wid
     * unchanged if already a LID, or calls {@code getCurrentLid(asUserWidOrThrow(wid))}
     * to look up the LID.
     *
     * @implNote WAWebLidMigrationUtils.toLid
     * @param jid the JID to convert
     * @return the LID JID, the original JID if already a LID,
     *         or {@code null} if no mapping is found
     */
    public Jid toLid(Jid jid) {
        if (jid == null) {
            return null;
        }

        // WAWebLidMigrationUtils.toLid: if(e.isLid()) return e
        if (jid.hasLidServer()) {
            return jid; // WAWebLidMigrationUtils.toLid
        }

        // WAWebLidMigrationUtils.toLid: var t = getCurrentLid(asUserWidOrThrow(e))
        // asUserWidOrThrow strips device/agent, getCurrentLid looks up store
        var userJid = jid.toUserJid(); // WAWebWidFactory.asUserWidOrThrow
        return store.findLidByPhone(userJid).orElse(null); // WAWebLidMigrationUtils.toLid
    }

    /**
     * Converts a JID to its user LID representation.
     *
     * <p>First coerces the JID to a user JID (stripping device/agent data).
     * If the resulting JID is already a LID, returns it. Otherwise, looks up the
     * corresponding LID from the store. Returns {@code null} if no mapping is found.
     *
     * <p>This matches WA Web's {@code toUserLid(wid)} function which calls
     * {@code asUserWidOrThrow(wid)} first, then returns the wid if it is a LID,
     * or calls {@code getCurrentLid(t)} otherwise.
     *
     * @implNote WAWebLidMigrationUtils.toUserLid
     * @param jid the JID to convert
     * @return the user LID JID, or {@code null} if no mapping is found
     */
    public Jid toUserLid(Jid jid) {
        if (jid == null) {
            return null;
        }

        // WAWebLidMigrationUtils.toUserLid: var t = asUserWidOrThrow(e)
        var userJid = jid.toUserJid(); // WAWebWidFactory.asUserWidOrThrow

        // WAWebLidMigrationUtils.toUserLid: return t.isLid() ? t : getCurrentLid(t)
        if (userJid.hasLidServer()) {
            return userJid; // WAWebLidMigrationUtils.toUserLid
        }

        return store.findLidByPhone(userJid).orElse(null); // WAWebLidMigrationUtils.toUserLid
    }

    /**
     * Converts a JID to its user LID representation, throwing if no LID is found.
     *
     * <p>This is the throwing variant of {@link #toUserLid(Jid)}. Matches WA Web's
     * {@code toUserLidOrThrow(wid)} which calls {@code toUserLid(wid)} and throws
     * {@code Error("No LID for user")} if the result is {@code null}.
     *
     * @implNote WAWebLidMigrationUtils.toUserLidOrThrow
     * @param jid the JID to convert
     * @return the user LID JID, never {@code null}
     * @throws IllegalStateException if no LID mapping is found for the JID
     */
    public Jid toUserLidOrThrow(Jid jid) {
        var result = toUserLid(jid); // WAWebLidMigrationUtils.toUserLidOrThrow
        if (result == null) {
            // WAWebLidMigrationUtils.toUserLidOrThrow: throw err("No LID for user")
            throw new IllegalStateException("No LID for user"); // WAWebLidMigrationUtils.toUserLidOrThrow
        }
        return result; // WAWebLidMigrationUtils.toUserLidOrThrow
    }

    /**
     * Converts a JID to its phone number (PN) representation, throwing if not found.
     *
     * <p>This is the throwing variant of {@link #toPn(Jid)}. Matches WA Web's
     * {@code toPnOrThrow(wid)} which calls {@code toPn(wid)} and throws
     * {@code Error("No PN for user")} if the result is {@code null}.
     *
     * @implNote WAWebLidMigrationUtils.toPnOrThrow
     * @param jid the JID to convert
     * @return the phone number JID, never {@code null}
     * @throws IllegalStateException if no phone number mapping is found for the JID
     */
    public Jid toPnOrThrow(Jid jid) {
        var result = toPn(jid); // WAWebLidMigrationUtils.toPnOrThrow
        if (result == null) {
            // WAWebLidMigrationUtils.toPnOrThrow: throw err("No PN for user")
            throw new IllegalStateException("No PN for user"); // WAWebLidMigrationUtils.toPnOrThrow
        }
        return result; // WAWebLidMigrationUtils.toPnOrThrow
    }

    /**
     * Returns the appropriate addressing mode conversion function based on whether
     * LID addressing is active.
     *
     * <p>When {@code isLid} is {@code true}, returns the {@link #toLid(Jid)} function.
     * When {@code isLid} is {@code false}, returns the {@link #toPn(Jid)} function.
     *
     * <p>This matches WA Web's {@code toAddressingModeFactory(isLid)} which returns
     * the {@code toLid} function reference when {@code isLid === true}, or the
     * {@code toPn} function reference otherwise.
     *
     * @implNote WAWebLidMigrationUtils.toAddressingModeFactory
     * @param isLid {@code true} to return the LID conversion function,
     *              {@code false} to return the PN conversion function
     * @return the addressing mode conversion function
     */
    public Function<Jid, Jid> toAddressingModeFactory(boolean isLid) {
        // WAWebLidMigrationUtils.toAddressingModeFactory: return e ? f : _
        return isLid ? this::toLid : this::toPn; // WAWebLidMigrationUtils.toAddressingModeFactory
    }

    /**
     * Normalizes two JIDs to a common addressing mode by converting one to match
     * the other's addressing mode (LID or PN).
     *
     * <p>If both JIDs are non-null users with different addressing modes (one is LID,
     * the other is PN), tries to find an alternate JID for either side so they match.
     * First tries to find the alternate for the first JID; if that succeeds, returns
     * the alternate paired with the second JID. Otherwise tries the second JID.
     *
     * <p>This matches WA Web's {@code toCommonAddressingMode(e, t)} function which
     * calls {@code getAlternateUserWid()} on each side in order to resolve addressing
     * mode mismatches.
     *
     * @implNote WAWebLidMigrationUtils.toCommonAddressingMode
     * @param first  the first JID, or {@code null}
     * @param second the second JID, or {@code null}
     * @return a two-element array with the (possibly converted) JIDs
     */
    public Jid[] toCommonAddressingMode(Jid first, Jid second) {
        // WAWebLidMigrationUtils.toCommonAddressingMode
        if (first != null && second != null
                && isUserWid(first) && isUserWid(second) // WAWebLidMigrationUtils.toCommonAddressingMode: e.isUser() && t.isUser()
                && first.hasLidServer() != second.hasLidServer()) { // WAWebLidMigrationUtils.toCommonAddressingMode: e.isLid() !== t.isLid()

            // WAWebLidMigrationUtils.toCommonAddressingMode: var n = getAlternateUserWid(asUserWidOrThrow(e))
            var alternateFirst = getAlternateUserWid(first.toUserJid());
            if (alternateFirst != null) {
                return new Jid[]{alternateFirst, second}; // WAWebLidMigrationUtils.toCommonAddressingMode
            }

            // WAWebLidMigrationUtils.toCommonAddressingMode: var r = getAlternateUserWid(asUserWidOrThrow(t))
            var alternateSecond = getAlternateUserWid(second.toUserJid());
            if (alternateSecond != null) {
                return new Jid[]{first, alternateSecond}; // WAWebLidMigrationUtils.toCommonAddressingMode
            }
        }
        return new Jid[]{first, second}; // WAWebLidMigrationUtils.toCommonAddressingMode
    }

    /**
     * Creates an alternate message key by looking up the alternate user JID for the
     * relevant participant or remote JID.
     *
     * <p>For group, status, or broadcast messages, the participant JID is alternated.
     * For 1:1 user messages, the remote JID is alternated. Returns {@code null} if
     * no alternate JID can be found.
     *
     * <p>This matches WA Web's {@code getAlternateMsgKey(msgKey)} function which
     * delegates to internal helpers for group/status/broadcast keys (alternating
     * participant) and user keys (alternating remote).
     *
     * @implNote WAWebLidMigrationUtils.getAlternateMsgKey
     * @param msgKey the message key to create an alternate for
     * @return the alternate message key, or {@code null} if no alternate is available
     */
    public MessageKey getAlternateMsgKey(MessageKey msgKey) {
        if (msgKey == null) {
            return null;
        }

        var remote = msgKey.parentJid().orElse(null); // WAWebLidMigrationUtils.getAlternateMsgKey
        if (remote == null) {
            return null;
        }

        // WAWebLidMigrationUtils.getAlternateMsgKey: if(e.remote.isGroup()||e.remote.isStatus()||e.remote.isBroadcastList())
        // isStatus() || isBroadcastList() covers all broadcast-server JIDs
        if (remote.hasGroupOrCommunityServer() || remote.hasBroadcastServer()) {
            // WAWebLidMigrationUtils.getAlternateMsgKey -> S(e) (group/status/broadcast path)
            return getAlternateMsgKeyForGroup(msgKey); // WAWebLidMigrationUtils.getAlternateMsgKey
        }

        // WAWebLidMigrationUtils.getAlternateMsgKey: if(e.remote.isUser())
        if (isUserWid(remote)) {
            // WAWebLidMigrationUtils.getAlternateMsgKey -> R(e) (1:1 user path)
            return getAlternateMsgKeyForUser(msgKey); // WAWebLidMigrationUtils.getAlternateMsgKey
        }

        return null; // WAWebLidMigrationUtils.getAlternateMsgKey
    }

    /**
     * Creates an alternate message key for a group/status/broadcast message by
     * alternating the participant JID.
     *
     * @implNote WAWebLidMigrationUtils.S
     * @param msgKey the message key with a group, status, or broadcast remote
     * @return the alternate message key, or {@code null} if no alternate participant is found
     */
    private MessageKey getAlternateMsgKeyForGroup(MessageKey msgKey) {
        // WAWebLidMigrationUtils.S: e.participant != null ? getAlternateUserWid(...) : null
        var participant = getRawParticipant(msgKey); // WAWebLidMigrationUtils.S
        if (participant == null) {
            return null; // WAWebLidMigrationUtils.S: e.participant != null ? ... : null
        }

        // WAWebLidMigrationUtils.S: getAlternateUserWid(asUserWidOrThrow(e.participant))
        var alternateParticipant = getAlternateUserWid(participant.toUserJid());
        if (alternateParticipant == null) {
            return null; // WAWebLidMigrationUtils.S
        }

        // WAWebLidMigrationUtils.S: new WAWebMsgKey({fromMe, remote, id, participant: t})
        var remote = msgKey.parentJid().orElse(null);
        var id = msgKey.id().orElse(null);
        return new MessageKeyBuilder() // WAWebLidMigrationUtils.S
                .fromMe(msgKey.fromMe())
                .parentJid(remote)
                .id(id)
                .senderJid(alternateParticipant)
                .build();
    }

    /**
     * Creates an alternate message key for a 1:1 user message by alternating
     * the remote JID.
     *
     * @implNote WAWebLidMigrationUtils.R
     * @param msgKey the message key with a user remote
     * @return the alternate message key, or {@code null} if no alternate remote is found
     */
    private MessageKey getAlternateMsgKeyForUser(MessageKey msgKey) {
        var remote = msgKey.parentJid().orElse(null); // WAWebLidMigrationUtils.R
        if (remote == null) {
            return null;
        }

        // WAWebLidMigrationUtils.R: getAlternateUserWid(asUserWidOrThrow(e.remote))
        var alternateRemote = getAlternateUserWid(remote.toUserJid());
        if (alternateRemote == null) {
            return null; // WAWebLidMigrationUtils.R
        }

        // WAWebLidMigrationUtils.R: new WAWebMsgKey({fromMe, remote: t, id, participant})
        var id = msgKey.id().orElse(null);
        var participant = getRawParticipant(msgKey); // WAWebLidMigrationUtils.R: e.participant (raw field, not fallback)
        return new MessageKeyBuilder() // WAWebLidMigrationUtils.R
                .fromMe(msgKey.fromMe())
                .parentJid(alternateRemote)
                .id(id)
                .senderJid(participant)
                .build();
    }

    /**
     * Extracts the raw participant JID from a message key, without the fallback to
     * parentJid that {@link MessageKey#senderJid()} provides.
     *
     * <p>This corresponds to WA Web's raw {@code e.participant} field access, which
     * returns {@code null} when no participant is set (unlike Cobalt's
     * {@code senderJid()} accessor which falls back to {@code parentJid}).
     *
     * @implNote ADAPTED: WAWebMsgKey.participant — raw field access without fallback
     * @param msgKey the message key
     * @return the raw participant JID, or {@code null} if not set
     */
    private static Jid getRawParticipant(MessageKey msgKey) {
        // senderJid() falls back to parentJid when senderJid is null.
        // If senderJid() returns the same as parentJid(), then the raw senderJid was null.
        var sender = msgKey.senderJid().orElse(null); // ADAPTED: WAWebMsgKey.participant
        var parent = msgKey.parentJid().orElse(null);
        if (sender != null && sender.equals(parent)) {
            return null; // Raw participant was null, senderJid() fell back to parentJid
        }
        return sender;
    }

    /**
     * Translate type for the {@link #getMeUserLidOrJidForChat(Chat, TranslateMsgKeyType)}
     * method, controlling which addressing mode is used for the current user's identity.
     *
     * <p>Matches WA Web's {@code WAWebMsgKeyUtils.TranslateMsgKeyType} enum with values
     * {@code Addon}, {@code Message}, and {@code EditMessage}.
     *
     * @implNote WAWebMsgKeyUtils.TranslateMsgKeyType
     */
    public enum TranslateMsgKeyType {
        /**
         * Used for addon messages (reactions, receipts, etc.).
         *
         * @implNote WAWebMsgKeyUtils.TranslateMsgKeyType.Addon
         */
        ADDON,

        /**
         * Used for regular messages.
         *
         * @implNote WAWebMsgKeyUtils.TranslateMsgKeyType.Message
         */
        MESSAGE,

        /**
         * Used for edit messages.
         *
         * @implNote WAWebMsgKeyUtils.TranslateMsgKeyType.EditMessage
         */
        EDIT_MESSAGE
    }

    /**
     * Returns the current user's identity (as LID or PN) appropriate for the given
     * chat and translate type.
     *
     * <p>The logic determines whether to use the LID or PN identity based on:
     * <ul>
     *     <li>Whether the chat JID is a LID</li>
     *     <li>Whether the chat is a Community Announcement Group (CAG)</li>
     *     <li>Whether the chat's group metadata has LID addressing mode enabled</li>
     *     <li>The translate type (Addon, Message, EditMessage)</li>
     * </ul>
     *
     * <p>For {@code Addon} type: returns LID if the chat is LID, CAG, or has LID
     * addressing mode; otherwise returns PN. For {@code Message} and {@code EditMessage}:
     * if CAG, returns LID only when group has LID addressing mode; otherwise uses the
     * same LID/PN decision as Addon.
     *
     * @implNote WAWebLidMigrationUtils.getMeUserLidOrJidForChat
     * @param chat          the chat for which to determine the user identity
     * @param translateType the type of message key translation
     * @return the current user's JID in the appropriate addressing mode
     * @throws IllegalStateException if the store has no JID or LID configured
     */
    public Jid getMeUserLidOrJidForChat(Chat chat, TranslateMsgKeyType translateType) {
        var chatJid = chat.jid(); // WAWebLidMigrationUtils.getMeUserLidOrJidForChat
        var isLid = chatJid.hasLidServer(); // WAWebLidMigrationUtils.getMeUserLidOrJidForChat: e.id.isLid()

        // WAWebLidMigrationUtils.getMeUserLidOrJidForChat: e.isCAG()
        // isCAG = isGroup && groupType === LINKED_ANNOUNCEMENT_GROUP
        var chatMetadata = store.findChatMetadata(chatJid).orElse(null);
        var isGroup = chatJid.hasGroupOrCommunityServer(); // WAWebChatGetters.getIsGroup
        var isCAG = isGroup && chatMetadata instanceof com.github.auties00.cobalt.model.chat.group.GroupMetadata gm
                && gm.isDefaultSubgroup(); // WAWebLidMigrationUtils.getMeUserLidOrJidForChat

        // WAWebLidMigrationUtils.getMeUserLidOrJidForChat: getIsGroup(e) && groupMetadata?.isLidAddressingMode
        var isLidAddressingMode = isGroup
                && chatMetadata != null
                && chatMetadata.isLidAddressingMode(); // WAWebLidMigrationUtils.getMeUserLidOrJidForChat

        // WAWebLidMigrationUtils.getMeUserLidOrJidForChat: switch(t)
        return switch (translateType) {
            case ADDON -> {
                // WAWebLidMigrationUtils.getMeUserLidOrJidForChat: r || a || i ? getMeLidUserOrThrow() : getMePnUserOrThrow()
                if (isLid || isCAG || isLidAddressingMode) {
                    yield getMeLidUserOrThrow(); // WAWebLidMigrationUtils.getMeUserLidOrJidForChat
                } else {
                    yield getMePnUserOrThrow(); // WAWebLidMigrationUtils.getMeUserLidOrJidForChat
                }
            }
            case MESSAGE, EDIT_MESSAGE -> {
                // WAWebLidMigrationUtils.getMeUserLidOrJidForChat: a ? (i ? getMeLidUserOrThrow() : getMePnUserOrThrow()) : (r || i ? getMeLidUserOrThrow() : getMePnUserOrThrow())
                if (isCAG) {
                    if (isLidAddressingMode) {
                        yield getMeLidUserOrThrow(); // WAWebLidMigrationUtils.getMeUserLidOrJidForChat
                    } else {
                        yield getMePnUserOrThrow(); // WAWebLidMigrationUtils.getMeUserLidOrJidForChat
                    }
                } else {
                    if (isLid || isLidAddressingMode) {
                        yield getMeLidUserOrThrow(); // WAWebLidMigrationUtils.getMeUserLidOrJidForChat
                    } else {
                        yield getMePnUserOrThrow(); // WAWebLidMigrationUtils.getMeUserLidOrJidForChat
                    }
                }
            }
        };
    }

    /**
     * Returns the LID and PN pair for a JID so both addressing modes can be updated.
     *
     * <p>If the JID is a LID, finds the corresponding PN and returns {@code [lid, pn]}.
     * If the JID is a PN, finds the corresponding LID and returns {@code [pn, lid]}.
     * If no mapping is found, returns a single-element list with just the original JID.
     *
     * <p>This matches WA Web's {@code getPnAndLidToUpdate(wid)} function which returns
     * a two-element array {@code [lid, pn]} when both are known, or a single-element
     * array when the alternate cannot be found.
     *
     * @implNote WAWebLidMigrationUtils.getPnAndLidToUpdate
     * @param jid the JID to find the PN/LID pair for
     * @return a list containing both addressing mode JIDs, or just the original if no
     *         alternate is found
     */
    public List<Jid> getPnAndLidToUpdate(Jid jid) {
        if (jid == null) {
            return List.of(); // NO_WA_BASIS — defensive null check
        }

        // WAWebLidMigrationUtils.getPnAndLidToUpdate: if(e.isLid()) { var n = toPn(e); if(n != null) return [e, n] }
        if (jid.hasLidServer()) {
            var pn = toPn(jid); // WAWebLidMigrationUtils.getPnAndLidToUpdate
            if (pn != null) {
                return List.of(jid, pn); // WAWebLidMigrationUtils.getPnAndLidToUpdate
            }
        } else {
            // WAWebLidMigrationUtils.getPnAndLidToUpdate: var t = toLid(e); if(t != null) return [e, t]
            var lid = toLid(jid); // WAWebLidMigrationUtils.getPnAndLidToUpdate
            if (lid != null) {
                return List.of(jid, lid); // WAWebLidMigrationUtils.getPnAndLidToUpdate
            }
        }

        return List.of(jid); // WAWebLidMigrationUtils.getPnAndLidToUpdate
    }

    /**
     * Returns whether a chat uses LID addressing mode.
     *
     * <p>A chat uses LID addressing if its JID is on the LID server, or if it is a
     * group whose group metadata has {@code isLidAddressingMode} enabled. This matches
     * WA Web's {@code chatIsLid(chat)} function which checks
     * {@code chat.id.isLid() || (isGroup && groupMetadata?.isLidAddressingMode)}.
     *
     * @implNote WAWebLidMigrationUtils.chatIsLid
     * @param chat the chat to check
     * @return {@code true} if the chat uses LID addressing mode
     */
    public boolean chatIsLid(Chat chat) {
        if (chat == null) {
            return false; // NO_WA_BASIS — defensive null check
        }

        var chatJid = chat.jid(); // WAWebLidMigrationUtils.chatIsLid

        // WAWebLidMigrationUtils.chatIsLid: n.isLid()
        if (chatJid.hasLidServer()) {
            return true; // WAWebLidMigrationUtils.chatIsLid
        }

        // WAWebLidMigrationUtils.chatIsLid: n.isGroup() && groupMetadata?.isLidAddressingMode
        if (chatJid.hasGroupOrCommunityServer()) {
            var chatMetadata = store.findChatMetadata(chatJid).orElse(null);
            return chatMetadata != null && chatMetadata.isLidAddressingMode(); // WAWebLidMigrationUtils.chatIsLid
        }

        return false; // WAWebLidMigrationUtils.chatIsLid
    }

    /**
     * Returns the alternate user JID for the given JID.
     *
     * <p>For a LID, returns the phone number. For a phone number, returns the LID.
     * Handles the "me" user specially: if the given JID matches the current user's
     * PN, returns the current user's LID (and vice versa), before falling back to
     * store lookups.
     *
     * <p>This matches WA Web's {@code WAWebApiContact.getAlternateUserWid(wid)} which
     * dispatches to {@code getPhoneNumber()} for LIDs and {@code getCurrentLid()} for
     * PNs, with special handling for the "me" user.
     *
     * @implNote ADAPTED: WAWebApiContact.getAlternateUserWid — WA Web uses separate
     *           {@code getCurrentLid} and {@code getPhoneNumber} functions with
     *           me-user special casing; Cobalt consolidates into a single method
     *           using {@code store.jid()} and {@code store.lid()} for me-user checks
     * @param userJid the user JID (must already be stripped of device/agent data)
     * @return the alternate JID, or {@code null} if not found
     */
    private Jid getAlternateUserWid(Jid userJid) {
        if (userJid == null) {
            return null;
        }

        // WAWebApiContact.getAlternateUserWid: e.isLid() ? getPhoneNumber(e) : getCurrentLid(e)
        if (userJid.hasLidServer()) {
            // WAWebApiContact.A (getPhoneNumber): check me-user first
            var meLid = store.lid().map(Jid::toUserJid).orElse(null);
            var mePn = store.jid().map(Jid::toUserJid).orElse(null);
            if (mePn != null && meLid != null && userJid.equals(meLid)) {
                return mePn; // WAWebApiContact.getAlternateUserWid
            }
            return store.findPhoneByLid(userJid).orElse(null); // WAWebApiContact.getAlternateUserWid
        } else {
            // WAWebApiContact.w (getCurrentLid): check me-user first
            var mePn = store.jid().map(Jid::toUserJid).orElse(null);
            var meLid = store.lid().map(Jid::toUserJid).orElse(null);
            if (meLid != null && mePn != null && userJid.equals(mePn)) {
                return meLid; // WAWebApiContact.getAlternateUserWid
            }
            return store.findLidByPhone(userJid).orElse(null); // WAWebApiContact.getAlternateUserWid
        }
    }

    /**
     * Returns the current user's LID user JID, throwing if not available.
     *
     * @implNote ADAPTED: WAWebUserPrefsMeUser.getMeLidUserOrThrow — Cobalt uses
     *           {@code store.lid()} instead of UserPrefs
     * @return the current user's LID
     * @throws IllegalStateException if no LID is configured for the current user
     */
    private Jid getMeLidUserOrThrow() {
        return store.lid() // WAWebUserPrefsMeUser.getMeLidUserOrThrow
                .map(Jid::toUserJid)
                .orElseThrow(() -> new IllegalStateException("No LID for current user")); // WAWebUserPrefsMeUser.getMeLidUserOrThrow
    }

    /**
     * Returns the current user's phone number (PN) user JID, throwing if not available.
     *
     * @implNote ADAPTED: WAWebUserPrefsMeUser.getMePnUserOrThrow_DO_NOT_USE — Cobalt uses
     *           {@code store.jid()} instead of UserPrefs
     * @return the current user's PN JID
     * @throws IllegalStateException if no JID is configured for the current user
     */
    private Jid getMePnUserOrThrow() {
        return store.jid() // WAWebUserPrefsMeUser.getMePnUserOrThrow_DO_NOT_USE
                .map(Jid::toUserJid)
                .orElseThrow(() -> new IllegalStateException("No PN for current user")); // WAWebUserPrefsMeUser.getMePnUserOrThrow_DO_NOT_USE
    }

    /**
     * Returns whether a JID is a "user" wid in the WA Web sense.
     *
     * <p>WA Web's {@code Wid.isUser()} returns {@code true} for servers
     * {@code c.us}, {@code lid}, {@code bot}, {@code hosted}, and {@code hosted.lid}.
     * In Cobalt, {@code c.us} is normalized to {@code s.whatsapp.net}.
     *
     * @implNote WAWebWid.isUser
     * @param jid the JID to check
     * @return {@code true} if the JID is a user wid
     */
    private static boolean isUserWid(Jid jid) {
        // WAWebWid.isUser: c.us || lid || bot || hosted || hosted.lid
        return jid.hasUserServer()
                || jid.hasLidServer()
                || jid.hasBotServer()
                || jid.hasHostedServer()
                || jid.hasHostedLidServer(); // WAWebWid.isUser
    }
}
