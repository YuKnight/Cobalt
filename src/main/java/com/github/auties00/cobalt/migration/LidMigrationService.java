package com.github.auties00.cobalt.migration;

import com.github.auties00.cobalt.client.WhatsAppClient;
import com.github.auties00.cobalt.exception.LidMigrationException;
import com.github.auties00.cobalt.model.chat.Chat;
import com.github.auties00.cobalt.model.jid.Jid;
import com.github.auties00.cobalt.model.sync.LIDMigrationMapping;
import com.github.auties00.cobalt.model.sync.LIDMigrationMappingSyncPayload;
import com.github.auties00.cobalt.store.WhatsAppStore;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

import static com.github.auties00.cobalt.client.WhatsAppClientErrorHandler.Location.LID_MIGRATION;

/**
 * Service responsible for orchestrating LID (Long ID) migration.
 * <p>
 * LID migration is a process where WhatsApp transitions from phone number-based
 * addressing to LID-based addressing for improved privacy.
 */
public final class LidMigrationService {
    private static final System.Logger LOGGER = System.getLogger("LidMigrationService");

    /**
     * Timeout for waiting for primary mappings (24 hours in milliseconds).
     */
    private static final long PRIMARY_MAPPINGS_TIMEOUT_MS = 24 * 60 * 60 * 1000L;

    private final WhatsAppClient whatsapp;
    private final WhatsAppStore store;

    /**
     * Current migration state.
     */
    private final AtomicReference<LidMigrationState> state;

    /**
     * Primary device's PN to LID mappings cache.
     * Maps phone number (as numeric string) to LID JID.
     */
    private final ConcurrentHashMap<String, Jid> primaryPnToLidCache;

    /**
     * Timestamp when mappings were received from primary.
     */
    private volatile long primaryMappingsTimestamp;

    /**
     * Chat DB migration timestamp from primary device.
     */
    private volatile long chatDbMigrationTimestamp;

    /**
     * Creates a new LID migration service.
     *
     * @param whatsapp the WhatsApp client instance
     */
    public LidMigrationService(WhatsAppClient whatsapp) {
        this.whatsapp = whatsapp;
        this.store = whatsapp.store();
        this.state = new AtomicReference<>(LidMigrationState.NOT_STARTED);
        this.primaryPnToLidCache = new ConcurrentHashMap<>();
        this.primaryMappingsTimestamp = 0;
        this.chatDbMigrationTimestamp = 0;
    }

    /**
     * Initializes the migration service.
     * Should be called after connection is established.
     */
    public void initialize() {
        if (state.compareAndSet(LidMigrationState.NOT_STARTED, LidMigrationState.WAITING_PROP)) {
            LOGGER.log(System.Logger.Level.INFO, "LID migration initialized, waiting for AB prop");
        }
    }

    /**
     * Called when the AB prop indicates LID migration is enabled.
     * Transitions from WAITING_PROP to WAITING_MAPPINGS state.
     */
    public void onMigrationEnabled() {
        if (state.compareAndSet(LidMigrationState.WAITING_PROP, LidMigrationState.WAITING_MAPPINGS)) {
            LOGGER.log(System.Logger.Level.INFO, "LID migration enabled, waiting for mappings from primary");
        }
    }

    /**
     * Processes LID migration mappings received from the primary device.
     * <p>
     * This method:
     * <ol>
     *     <li>Validates the payload</li>
     *     <li>Populates the primary mapping cache</li>
     *     <li>Updates contacts with LID information</li>
     *     <li>Transitions to READY state</li>
     * </ol>
     *
     * @param payload the decoded mapping payload from primary device
     */
    public void onMappingsReceived(LIDMigrationMappingSyncPayload payload) {
        if (payload == null) {
            handleError(new LidMigrationException.FailedToParseMappings("null payload"));
            return;
        }

        var currentState = state.get();
        if (currentState != LidMigrationState.WAITING_MAPPINGS && currentState != LidMigrationState.WAITING_PROP) {
            LOGGER.log(System.Logger.Level.DEBUG, "Ignoring mappings in state: {0}", currentState);
            return;
        }

        try {
            // Store migration timestamp
            this.chatDbMigrationTimestamp = payload.chatDbMigrationTimestamp();
            this.primaryMappingsTimestamp = System.currentTimeMillis();

            // Process mappings
            var mappings = payload.pnToLidMappings();
            if (mappings == null || mappings.isEmpty()) {
                LOGGER.log(System.Logger.Level.WARNING, "Received empty LID mappings from primary");
                // Empty mappings might be valid (no contacts to migrate)
                state.set(LidMigrationState.READY);
                return;
            }

            LOGGER.log(System.Logger.Level.INFO, "Processing {0} LID mappings from primary", mappings.size());

            // Populate primary cache and update contacts
            for (var mapping : mappings) {
                processSingleMapping(mapping);
            }

            // Transition to READY state
            state.set(LidMigrationState.READY);
            LOGGER.log(System.Logger.Level.INFO, "LID migration ready with {0} mappings", primaryPnToLidCache.size());

            // Check if we should auto-start migration
            if (shouldAutoStartMigration()) {
                executeMigration();
            }

        } catch (Throwable throwable) {
            handleError(new LidMigrationException.FailedToParseMappings("error processing mappings", throwable));
        }
    }

    /**
     * Processes a single LID mapping entry.
     */
    private void processSingleMapping(LIDMigrationMapping mapping) {
        if (mapping == null) {
            return;
        }

        var user = mapping.phoneNumber()
                .map(Jid::user)
                .orElse(null);
        if(user == null) {
            return;
        }

        // Get the effective LID (prefer latest over assigned)
        var effectiveLid = mapping.effectiveLid()
                .orElse(null);
        if (effectiveLid == null) {
            return;
        }

        // Store in primary cache
        primaryPnToLidCache.put(user, effectiveLid);

        // Update contact if exists
        mapping.phoneNumber().ifPresent(phoneJid -> {
            store.findContactByJid(phoneJid).ifPresent(contact -> {
                contact.setLid(effectiveLid);
                store.registerLidMapping(phoneJid, effectiveLid);
            });
        });
    }

    /**
     * Executes the LID migration for all eligible chat threads.
     */
    public void executeMigration() {
        if (!state.compareAndSet(LidMigrationState.READY, LidMigrationState.IN_PROGRESS)) {
            var currentState = state.get();
            LOGGER.log(System.Logger.Level.WARNING, "Cannot start migration in state: {0}", currentState);
            return;
        }

        // Verify primary mappings haven't expired before starting migration
        if (!primaryPnToLidCache.isEmpty() && !arePrimaryMappingsValid()) {
            LOGGER.log(System.Logger.Level.ERROR, "Primary mappings have expired (older than 24 hours)");
            handleError(new LidMigrationException.PrimaryMappingsObsolete());
            return;
        }

        LOGGER.log(System.Logger.Level.INFO, "Starting LID migration execution");

        try {
            var resolutions = new ArrayList<LidMigrationResolution>();
            var chatsToProcess = new ArrayList<>(store.chats());

            // Phase 1: Resolve all threads
            for (var chat : chatsToProcess) {
                var resolution = resolveThread(chat);
                resolutions.add(resolution);
            }

            // Phase 2: Check for split thread issues
            checkForSplitThreads(resolutions);

            // Phase 3: Execute migrations
            executeResolutions(resolutions);

            // Phase 4: Bulk-register all primary mappings in the store
            learnMappingsInBulk();

            // Phase 5: Mark complete
            state.set(LidMigrationState.COMPLETE);
            LOGGER.log(System.Logger.Level.INFO, "LID migration completed");

        } catch (LidMigrationException e) {
            handleError(e);
        } catch (Throwable throwable) {
            handleError(new LidMigrationException.FailedToParseMappings("migration execution failed", throwable));
        }
    }

    /**
     * Resolves a single chat thread to determine its migration action.
     *
     * @param chat the chat to resolve
     * @return the resolution for this thread
     */
    public LidMigrationResolution resolveThread(Chat chat) {
        var jid = chat.jid();

        // Rule 1: Already LID → KEEP
        if (jid.hasLidServer()) {
            return new LidMigrationResolution.Keep(jid, LidMigrationResolution.KeepReason.ALREADY_LID);
        }

        // Rule 2: Groups and communities → KEEP (not subject to 1:1 migration)
        if (jid.hasGroupOrCommunityServer()) {
            return new LidMigrationResolution.Keep(jid, LidMigrationResolution.KeepReason.GROUP_OR_COMMUNITY);
        }

        // Rule 3: Newsletters → KEEP
        if (jid.hasNewsletterServer()) {
            return new LidMigrationResolution.Keep(jid, LidMigrationResolution.KeepReason.NEWSLETTER);
        }

        // Rule 4: Broadcast lists → KEEP
        if (jid.hasBroadcastServer()) {
            if (jid.isStatusBroadcastAccount()) {
                return new LidMigrationResolution.Keep(jid, LidMigrationResolution.KeepReason.STATUS_BROADCAST);
            }
            return new LidMigrationResolution.Keep(jid, LidMigrationResolution.KeepReason.BROADCAST);
        }

        // Rule 5: Bot accounts → KEEP
        if (jid.hasBotServer()) {
            return new LidMigrationResolution.Keep(jid, LidMigrationResolution.KeepReason.BOT);
        }

        // Rule 6: Check for split thread flag
        // phoneDuplicateLidThread indicates this PN chat has a duplicate LID thread
        if (chat.phoneDuplicateLidThread()) {
            return new LidMigrationResolution.Keep(jid, LidMigrationResolution.KeepReason.DUPLICATE_WILL_MERGE);
        }

        // Rule 7: Determine local LID (from chat or store) and primary LID (from cache)
        var chatLid = chat.lidJid().orElse(null);
        var user = jid.user();
        var primaryLid = (user != null && arePrimaryMappingsValid())
                ? primaryPnToLidCache.get(user)
                : null;
        var localLid = chatLid != null
                ? chatLid
                : (user != null ? store.findLidByPhone(jid).orElse(null) : null);

        // Rule 8: Primary has a LID for this contact
        if (primaryLid != null) {
            // Rule 8a: No local LID or local matches primary → use primary
            if (localLid == null || localLid.toUserJid().equals(primaryLid.toUserJid())) {
                return new LidMigrationResolution.Migrate(jid, primaryLid);
            }

            // Rule 8b: LID mismatch between local and primary
            // Compare timestamps to determine which is fresher
            var chatTimestamp = chat.timestampSeconds();
            if (chatTimestamp >= chatDbMigrationTimestamp && chatDbMigrationTimestamp > 0) {
                // Local data is fresher than primary sync → primary mappings are obsolete
                throw new LidMigrationException.PrimaryMappingsObsolete();
            }

            // Primary is fresher → trust primary
            return new LidMigrationResolution.Migrate(jid, primaryLid);
        }

        // Rule 9: Primary has no LID, but local does → use local
        if (localLid != null) {
            return new LidMigrationResolution.Migrate(jid, localLid);
        }

        // Rule 10: No LID found - evaluate if chat can be deleted
        if (shouldPreserveChat(chat)) {
            LOGGER.log(System.Logger.Level.WARNING,
                    "No LID mapping for chat {0}, but preserving due to user data", jid);
            return new LidMigrationResolution.Keep(jid, LidMigrationResolution.KeepReason.NO_LID_BUT_HAS_DATA);
        }

        // Chat is eligible for deletion
        return new LidMigrationResolution.Delete(jid, LidMigrationResolution.DeleteReason.NO_LID_MAPPING);
    }

    /**
     * Determines if a chat should be preserved even without a LID mapping.
     *
     * @param chat the chat to evaluate
     * @return true if the chat should be preserved
     */
    private boolean shouldPreserveChat(Chat chat) {
        // Preserve archived chats - user explicitly saved this
        if (chat.archived()) {
            return true;
        }

        // Preserve muted chats - user made a choice about notifications
        if (chat.mute() != null && chat.mute().isMuted()) {
            return true;
        }

        // Preserve pinned chats
        if (chat.pinnedTimestamp().isPresent()) {
            return true;
        }

        // Preserve chats with messages
        return !chat.messages().isEmpty();
    }

    /**
     * Checks for split thread issues that would cause conflicts after migration.
     *
     * @param resolutions the list of resolutions to check
     * @throws LidMigrationException.SplitThreadMismatch if a critical split thread issue is detected
     */
    private void checkForSplitThreads(List<LidMigrationResolution> resolutions) {
        // Collect all existing LID threads that are being kept as ALREADY_LID
        var existingLidThreads = new HashSet<Jid>();
        for (var resolution : resolutions) {
            if (resolution instanceof LidMigrationResolution.Keep(var originalJid, var reason) && reason == LidMigrationResolution.KeepReason.ALREADY_LID) {
                existingLidThreads.add(originalJid.toUserJid());
            }
        }

        // Build a map of target LIDs from migrations to detect duplicates
        var targetLidCounts = new HashMap<Jid, List<LidMigrationResolution.Migrate>>();
        for (var resolution : resolutions) {
            if (resolution instanceof LidMigrationResolution.Migrate migrate) {
                targetLidCounts
                        .computeIfAbsent(migrate.targetLid().toUserJid(), _ -> new ArrayList<>())
                        .add(migrate);
            }
        }

        for (var entry : targetLidCounts.entrySet()) {
            var targetLid = entry.getKey();
            var migrations = entry.getValue();

            // Check 1: Multiple PN threads would migrate to the same LID
            if (migrations.size() > 1) {
                var duplicates = migrations.stream()
                        .map(LidMigrationResolution.Migrate::originalJid)
                        .map(Jid::toString)
                        .collect(Collectors.joining(", "));

                LOGGER.log(System.Logger.Level.WARNING,
                        "Split thread detected: {0} threads would migrate to {1}: [{2}]",
                        migrations.size(), targetLid, duplicates);
                throw new LidMigrationException.SplitThreadMismatch();
            }

            // Check 2: A PN thread would migrate to a LID that already exists as a separate thread
            if (existingLidThreads.contains(targetLid)) {
                var pnThread = migrations.getFirst().originalJid();
                LOGGER.log(System.Logger.Level.WARNING,
                        "Split thread detected: PN thread {0} would collide with existing LID thread {1}",
                        pnThread, targetLid);
                throw new LidMigrationException.SplitThreadMismatch();
            }
        }
    }

    /**
     * Executes the resolved migrations.
     *
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
     * Executes a migration resolution - updates the chat to use LID addressing.
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
        chat.setPhoneJid(originalJid);

        // Register the mapping in the store
        store.registerLidMapping(originalJid, targetLid);

        // Update associated contact if exists
        store.findContactByJid(originalJid).ifPresent(contact -> {
            contact.setLid(targetLid);
        });

        LOGGER.log(System.Logger.Level.DEBUG, "Migrated chat {0} → {1}", originalJid, targetLid);
    }

    /**
     * Executes a delete resolution - removes the chat.
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
     * @param phoneJid the phone number JID
     * @param newLid   the new LID
     * @param oldLid   the old LID (may be null)
     */
    public void onLidChanged(Jid phoneJid, Jid newLid, Jid oldLid) {
        if (phoneJid == null || newLid == null) {
            return;
        }

        // Update primary cache
        if (phoneJid.user() != null) {
            primaryPnToLidCache.put(phoneJid.user(), newLid);
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
            chat.setPhoneJid(phoneJid);
        });

        LOGGER.log(System.Logger.Level.DEBUG, "LID changed for {0}: {1} → {2}", phoneJid, oldLid, newLid);
    }

    /**
     * Persists all primary mappings to the store's bidirectional mapping tables.
     */
    private void learnMappingsInBulk() {
        var registered = 0;
        for (var entry : primaryPnToLidCache.entrySet()) {
            var phoneJid = Jid.of(entry.getKey());
            var lidJid = entry.getValue();
            store.registerLidMapping(phoneJid, lidJid);
            registered++;
        }
        LOGGER.log(System.Logger.Level.INFO, "Bulk-registered {0} PN↔LID mappings", registered);
    }

    /**
     * Determines if migration should auto-start.
     */
    private boolean shouldAutoStartMigration() {
        // For now, auto-start if we have mappings and the timestamp indicates we should migrate
        return chatDbMigrationTimestamp > 0 &&
               System.currentTimeMillis() / 1000 >= chatDbMigrationTimestamp;
    }

    /**
     * Handles a migration error.
     */
    private void handleError(LidMigrationException error) {
        state.set(LidMigrationState.FAILED);
        LOGGER.log(System.Logger.Level.ERROR, "LID migration failed: {0}", error.getMessage());
        whatsapp.handleFailure(LID_MIGRATION, error);
    }

    /**
     * Resets the migration service state for a new session.
     * Called when the client disconnects and reconnects.
     */
    public void reset() {
        var currentState = state.get();
        // Only reset if not in a terminal state
        if (!currentState.isTerminal()) {
            state.set(LidMigrationState.NOT_STARTED);
        }
        // Don't clear primary cache on reconnect - it's still valid for 24 hours
    }

    /**
     * Returns whether the primary mappings cache is still valid.
     * The cache expires after 24 hours per the Design Document.
     *
     * @return true if mappings are valid and haven't expired
     */
    public boolean arePrimaryMappingsValid() {
        if (primaryMappingsTimestamp == 0) {
            return false;
        }
        return System.currentTimeMillis() - primaryMappingsTimestamp < PRIMARY_MAPPINGS_TIMEOUT_MS;
    }

    /**
     * Looks up a LID for a phone number JID.
     * Checks primary cache first (if not expired), then store mappings.
     *
     * @param phoneJid the phone number JID
     * @return the LID if found
     */
    public Optional<Jid> lookupLid(Jid phoneJid) {
        if (phoneJid == null || phoneJid.user() == null) {
            return Optional.empty();
        }

        // Check primary cache only if it hasn't expired
        if (arePrimaryMappingsValid()) {
            var cached = primaryPnToLidCache.get(phoneJid.user());
            if (cached != null) {
                return Optional.of(cached);
            }
        }

        // Check store mappings (these don't expire)
        return store.findLidByPhone(phoneJid);
    }

    /**
     * Determines whether to use LID addressing mode for a recipient.
     *
     * @param recipientJid the recipient's JID
     * @return true if LID addressing should be used
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
     * Gets the effective JID to use for messaging a recipient.
     * <p>
     * If LID addressing should be used and a LID is available, returns the LID.
     * Otherwise returns the original JID.
     *
     * @param recipientJid the recipient's original JID
     * @return the effective JID to use for messaging
     */
    public Jid getEffectiveJid(Jid recipientJid) {
        if (recipientJid == null) {
            return null;
        }

        if (!shouldUseLidAddressing(recipientJid)) {
            return recipientJid;
        }

        return lookupLid(recipientJid).orElse(recipientJid);
    }
}
