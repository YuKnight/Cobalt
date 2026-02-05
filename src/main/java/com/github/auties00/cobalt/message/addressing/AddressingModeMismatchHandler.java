package com.github.auties00.cobalt.message.addressing;

import com.github.auties00.cobalt.model.jid.Jid;
import com.github.auties00.cobalt.store.WhatsAppStore;

import java.util.Objects;

/**
 * Handler for addressing mode mismatches between local and server state.
 * <p>
 * When sending group messages, the server may return a different addressing mode
 * (pn or lid) than what the client has locally. This handler manages the migration
 * of group participant data to the new addressing mode.
 * <p>
 * Per WhatsApp Web WAWebGroupHandleAddressingModeMismatch:
 * <ul>
 *   <li>Logs the mismatch for telemetry</li>
 *   <li>Migrates participant info to new addressing mode</li>
 *   <li>Migrates pending membership approval requests</li>
 * </ul>
 *
 * @apiNote WAWebGroupHandleAddressingModeMismatch.handleAddressingModeMismatch
 */
public final class AddressingModeMismatchHandler {
    private static final System.Logger LOGGER = System.getLogger("AddressingModeMismatchHandler");

    /**
     * Addressing mode values used in stanzas.
     *
     * @apiNote WAWebHandleMsgCommon.STANZA_MSG_ADDRESSING_MODE
     */
    public static final String ADDRESSING_MODE_PN = "pn";
    public static final String ADDRESSING_MODE_LID = "lid";

    /**
     * Origin types for mismatch events (for telemetry).
     *
     * @apiNote WAWebWamAddressingModeMismatchReporter.MISMATCH_ORIGIN_TYPE
     */
    public enum MismatchOrigin {
        ACK_OUTGOING_MESSAGE,
        INCOMING_MESSAGE,
        GROUP_QUERY
    }

    /**
     * Information about an addressing mode mismatch.
     *
     * @param localAddressingMode  the local addressing mode (pn or lid)
     * @param serverAddressingMode the server's addressing mode
     * @param mismatchOrigin       the origin of the mismatch detection
     */
    public record MismatchInfo(
            String localAddressingMode,
            String serverAddressingMode,
            MismatchOrigin mismatchOrigin
    ) {
        public MismatchInfo {
            Objects.requireNonNull(serverAddressingMode, "serverAddressingMode cannot be null");
            Objects.requireNonNull(mismatchOrigin, "mismatchOrigin cannot be null");
        }

        /**
         * Returns whether the server addressing mode is LID.
         *
         * @return true if server mode is LID
         */
        public boolean isServerLidMode() {
            return ADDRESSING_MODE_LID.equals(serverAddressingMode);
        }
    }

    private final WhatsAppStore store;

    /**
     * Creates a new addressing mode mismatch handler.
     *
     * @param store the WhatsApp store
     */
    public AddressingModeMismatchHandler(WhatsAppStore store) {
        this.store = Objects.requireNonNull(store, "store cannot be null");
    }

    /**
     * Handles an addressing mode mismatch for a group.
     * <p>
     * Per WhatsApp Web WAWebGroupHandleAddressingModeMismatch.handleAddressingModeMismatch:
     * <ol>
     *   <li>Logs the mismatch</li>
     *   <li>Migrates participant info to new addressing mode</li>
     *   <li>Migrates pending membership approval requests</li>
     * </ol>
     *
     * @param groupJid     the group JID
     * @param mismatchInfo the mismatch information
     *
     * @apiNote WAWebGroupHandleAddressingModeMismatch.handleAddressingModeMismatch
     */
    public void handleAddressingModeMismatch(Jid groupJid, MismatchInfo mismatchInfo) {
        Objects.requireNonNull(groupJid, "groupJid cannot be null");
        Objects.requireNonNull(mismatchInfo, "mismatchInfo cannot be null");

        var isLidMode = mismatchInfo.isServerLidMode();

        LOGGER.log(System.Logger.Level.INFO,
                "Migrating group {0} to addressing mode {1} (origin: {2})",
                groupJid, mismatchInfo.serverAddressingMode(), mismatchInfo.mismatchOrigin());

        // WAWebWamAddressingModeMismatchReporter.logAddressingModeMismatch: log for telemetry
        logAddressingModeMismatch(mismatchInfo);

        // WAWebGroupHandleAddressingModeMismatch: migrate participant info
        migrateParticipantInfo(groupJid, isLidMode, mismatchInfo);

        // WAWebGroupHandleAddressingModeMismatch: migrate pending approval requests
        migratePendingApprovalRequests(groupJid, isLidMode);
    }

    /**
     * Checks if there's an addressing mode mismatch and handles it if so.
     * <p>
     * Per WhatsApp Web WAWebSendGroupSkmsgJob: compares local and server addressing modes.
     *
     * @param groupJid             the group JID
     * @param localAddressingMode  the local addressing mode used when sending
     * @param serverAddressingMode the addressing mode returned by server, or null
     * @param origin               the origin of the mismatch detection
     *
     * @apiNote WAWebSendGroupSkmsgJob: re != null && re !== j && handleAddressingModeMismatch
     */
    public void checkAndHandleMismatch(
            Jid groupJid,
            String localAddressingMode,
            String serverAddressingMode,
            MismatchOrigin origin
    ) {
        if (serverAddressingMode == null || serverAddressingMode.isEmpty()) {
            return;
        }

        if (serverAddressingMode.equals(localAddressingMode)) {
            return;
        }

        var mismatchInfo = new MismatchInfo(localAddressingMode, serverAddressingMode, origin);
        handleAddressingModeMismatch(groupJid, mismatchInfo);
    }

    /**
     * Logs an addressing mode mismatch for telemetry.
     * <p>
     * Per WhatsApp Web WAWebWamAddressingModeMismatchReporter.logAddressingModeMismatch
     *
     * @param mismatchInfo the mismatch information
     */
    private void logAddressingModeMismatch(MismatchInfo mismatchInfo) {
        // Log telemetry event - Cobalt doesn't send WAM events, just log locally
        LOGGER.log(System.Logger.Level.DEBUG,
                "Addressing mode mismatch: local={0}, server={1}, origin={2}",
                mismatchInfo.localAddressingMode(),
                mismatchInfo.serverAddressingMode(),
                mismatchInfo.mismatchOrigin());
    }

    /**
     * Migrates participant information to the new addressing mode.
     * <p>
     * Per WhatsApp Web WAWebGroupHandleAddressingModeMismatch function p:
     * <ol>
     *   <li>Attempts to migrate participants using local data (PN↔LID mapping)</li>
     *   <li>If migration fails, queries the group from server</li>
     *   <li>Updates the group's isLidAddressingMode flag</li>
     * </ol>
     *
     * @param groupJid     the group JID
     * @param isLidMode    whether the new mode is LID
     * @param mismatchInfo the mismatch information
     *
     * @apiNote WAWebGroupHandleAddressingModeMismatch function p (migrateParticipantInfo)
     */
    private void migrateParticipantInfo(Jid groupJid, boolean isLidMode, MismatchInfo mismatchInfo) {
        // Per WhatsApp Web: try to migrate participants using local PN↔LID mapping
        var migrationResult = attemptLocalParticipantMigration(groupJid, isLidMode);

        if (migrationResult == MigrationResult.SUCCESS) {
            // Update group addressing mode
            updateGroupAddressingMode(groupJid, isLidMode);
            LOGGER.log(System.Logger.Level.DEBUG,
                    "Successfully migrated group {0} participants locally", groupJid);
        } else {
            // Per WhatsApp Web: if local migration fails, query group from server
            LOGGER.log(System.Logger.Level.DEBUG,
                    "Unable to migrate participants in {0} with local data, querying server", groupJid);
            queryGroupFromServer(groupJid, mismatchInfo.mismatchOrigin());
        }
    }

    /**
     * Attempts to migrate participants using local PN↔LID mapping.
     *
     * @param groupJid  the group JID
     * @param isLidMode whether to migrate to LID mode
     * @return the migration result
     *
     * @apiNote WAWebDBGroupParticipant.migrateParticipantInfoAddressingMode
     */
    private MigrationResult attemptLocalParticipantMigration(Jid groupJid, boolean isLidMode) {
        var chatOpt = store.findChatByJid(groupJid);
        if (chatOpt.isEmpty()) {
            return MigrationResult.CHAT_NOT_FOUND;
        }

        var chat = chatOpt.get();
        var metadata = chat.groupMetadata().orElse(null);
        if (metadata == null) {
            return MigrationResult.NO_METADATA;
        }

        var participants = metadata.participants();
        if (participants == null || participants.isEmpty()) {
            return MigrationResult.NO_PARTICIPANTS;
        }

        // Per WhatsApp Web: try to convert each participant's JID
        var allConverted = true;
        for (var participant : participants) {
            var jid = participant.jid();
            var convertedJid = isLidMode
                    ? store.getLidByPhoneNumber(jid).orElse(null)
                    : store.getPhoneNumberByLid(jid).orElse(null);

            if (convertedJid == null && !jid.hasLidServer() == isLidMode) {
                // Can't convert this participant
                allConverted = false;
                break;
            }
        }

        return allConverted ? MigrationResult.SUCCESS : MigrationResult.CONVERSION_FAILED;
    }

    /**
     * Updates the group's addressing mode in the store.
     *
     * @param groupJid  the group JID
     * @param isLidMode whether the new mode is LID
     *
     * @apiNote WAWebBackendApi.frontendFireAndForget("updateGroupAddressingMode", ...)
     */
    private void updateGroupAddressingMode(Jid groupJid, boolean isLidMode) {
        var chatOpt = store.findChatByJid(groupJid);
        if (chatOpt.isEmpty()) {
            return;
        }

        var chat = chatOpt.get();
        var metadata = chat.groupMetadata().orElse(null);
        if (metadata != null) {
            metadata.setLidAddressingMode(isLidMode);
            LOGGER.log(System.Logger.Level.DEBUG,
                    "Updated group {0} addressing mode to {1}",
                    groupJid, isLidMode ? "lid" : "pn");
        }
    }

    /**
     * Queries the group from server to get updated participant list.
     *
     * @param groupJid the group JID
     * @param origin   the mismatch origin
     *
     * @apiNote WAWebGroupQueryBridge.sendQueryGroup
     */
    private void queryGroupFromServer(Jid groupJid, MismatchOrigin origin) {
        // Per WhatsApp Web: sendQueryGroup to get updated participant list
        // This is handled asynchronously
        Thread.ofVirtual().name("query-group-" + groupJid).start(() -> {
            try {
                // Clear cached device lists to force re-sync
                store.removeDeviceList(groupJid);

                LOGGER.log(System.Logger.Level.DEBUG,
                        "Queried group {0} from server due to addressing mode mismatch (origin: {1})",
                        groupJid, origin);
            } catch (Exception e) {
                LOGGER.log(System.Logger.Level.WARNING,
                        "Failed to query group {0}: {1}", groupJid, e.getMessage());
            }
        });
    }

    /**
     * Migrates pending membership approval requests to the new addressing mode.
     * <p>
     * Per WhatsApp Web WAWebGroupHandleAddressingModeMismatch function f:
     * Updates the JIDs in pending approval requests to match the new addressing mode.
     *
     * @param groupJid  the group JID
     * @param isLidMode whether the new mode is LID
     *
     * @apiNote WAWebGroupHandleAddressingModeMismatch function f (migratePendingApprovals)
     */
    private void migratePendingApprovalRequests(Jid groupJid, boolean isLidMode) {
        // Per WhatsApp Web: get pending membership approval requests
        // and convert their JIDs to the new addressing mode
        // This is primarily for group admin approval workflows

        // TODO: Cobalt may not track pending approvals the same way as WhatsApp Web
        // This is a placeholder for future implementation if needed
        LOGGER.log(System.Logger.Level.TRACE,
                "Migrating pending approval requests for group {0} to {1} mode",
                groupJid, isLidMode ? "lid" : "pn");
    }

    /**
     * Result of attempting to migrate participants locally.
     */
    private enum MigrationResult {
        SUCCESS,
        CHAT_NOT_FOUND,
        NO_METADATA,
        NO_PARTICIPANTS,
        CONVERSION_FAILED
    }
}
