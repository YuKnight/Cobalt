package com.github.auties00.cobalt.migration;

import com.github.auties00.cobalt.client.WhatsAppClient;
import com.github.auties00.cobalt.model.chat.ChatMetadata;
import com.github.auties00.cobalt.model.chat.community.CommunityMetadata;
import com.github.auties00.cobalt.model.chat.group.GroupMetadata;
import com.github.auties00.cobalt.model.jid.Jid;
import com.github.auties00.cobalt.model.jid.JidServer;
import com.github.auties00.cobalt.props.ABProp;
import com.github.auties00.cobalt.props.ABPropsService;
import com.github.auties00.cobalt.util.SchedulerUtils;

import java.time.Duration;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * A scheduled service that migrates inactive groups to LID addressing mode
 * by re-querying their metadata from the server.
 *
 * <p>When enabled via the {@link ABProp#ENABLE_INACTIVE_GROUP_LID_MIGRATION}
 * AB prop, this service identifies groups that still use phone-number
 * addressing mode ({@code isLidAddressingMode == false}), queries their
 * metadata from the server (which triggers an update of the addressing mode
 * flag), and marks the migration as complete once all groups have been
 * migrated.
 *
 * <p>The service runs once after a 60-second startup delay. If groups
 * remain in PN mode after the initial pass, a follow-up pass is scheduled
 * after 24 hours.
 *
 * @implNote WAWebInactiveGroupLidMigrationJob.migrateInactiveGroupsToLid:
 * finds all non-LID groups where the user is a member, batch-queries
 * their metadata, and marks the migration complete when none remain.
 */
public final class InactiveGroupLidMigrationService {
    /**
     * Logger for migration progress and error messages.
     *
     * @implNote ADAPTED: WAWebInactiveGroupLidMigrationJob uses
     *           {@code WALogger.LOG}/{@code WALogger.ERROR} with tagged
     *           template literals. Cobalt uses {@link System.Logger}.
     */
    private static final System.Logger LOGGER = System.getLogger(InactiveGroupLidMigrationService.class.getName());

    /**
     * The initial delay before the first migration attempt, matching the
     * WhatsApp Web requirement that at least 60 seconds have elapsed
     * since pairing.
     *
     * @implNote ADAPTED: WAWebTasksDefinitions registers the task with
     *           a 60-second post-pairing delay.
     */
    private static final Duration INITIAL_DELAY = Duration.ofSeconds(60);

    /**
     * The delay between retry attempts when groups remain in PN mode
     * after the first pass.
     *
     * @implNote ADAPTED: WAWebTasksDefinitions schedules the task at
     *           {@code DAY_SECONDS} intervals. Cobalt self-schedules
     *           with the same 24-hour delay.
     */
    private static final Duration RETRY_DELAY = Duration.ofDays(1);

    /**
     * The WhatsApp client used for querying group metadata from the server.
     *
     * @implNote WAWebInactiveGroupLidMigrationJob: corresponds to the
     *           module-level import of {@code WAWebQueryAndUpdateGroupMetadataJob}.
     */
    private final WhatsAppClient client;

    /**
     * The AB props service used for checking the
     * {@link ABProp#ENABLE_INACTIVE_GROUP_LID_MIGRATION} feature flag.
     *
     * @implNote WAWebInactiveGroupLidMigrationJob: corresponds to the
     *           module-level import of {@code WAWebABProps}.
     */
    private final ABPropsService abPropsService;

    /**
     * Tracks whether the inactive group LID migration has been completed.
     *
     * <p>In WA Web, this state is persisted via
     * {@code WAWebUserPrefsStore} under the key
     * {@code UserPrefs.InactiveGroupLidMigrationComplete}. In Cobalt,
     * an in-memory {@link AtomicBoolean} is used instead since Cobalt
     * does not replicate the UserPrefs persistence layer.
     *
     * @implNote ADAPTED: WAWebInactiveGroupLidMigration.isInactiveGroupLidMigrationComplete
     *           reads this value; WAWebInactiveGroupLidMigration.setInactiveGroupLidMigrationComplete
     *           writes {@code true} to it.
     */
    private final AtomicBoolean complete;

    /**
     * The currently scheduled migration task, or {@code null} if no task
     * is pending. Used to cancel pending retries on disconnect/reconnect.
     *
     * @implNote NO_WA_BASIS: Cobalt lifecycle field for managing scheduled
     *           task cancellation. WA Web relies on the task scheduler's
     *           own lifecycle management.
     */
    private volatile CompletableFuture<Void> scheduledTask;

    /**
     * Constructs a new inactive group LID migration service.
     *
     * @implNote WAWebInactiveGroupLidMigration: module-level initialization.
     *           The WA Web module initializes by reading
     *           {@code UserPrefs.InactiveGroupLidMigrationComplete}
     *           from {@code WAWebUserPrefsStore}. Cobalt uses constructor
     *           DI with an {@link AtomicBoolean} initialized to {@code false}.
     * @param client        the WhatsApp client for querying group metadata
     * @param abPropsService the AB props service for checking the feature flag
     */
    public InactiveGroupLidMigrationService(WhatsAppClient client, ABPropsService abPropsService) {
        this.client = Objects.requireNonNull(client, "client");
        this.abPropsService = Objects.requireNonNull(abPropsService, "abPropsService");
        this.complete = new AtomicBoolean(false);
    }

    /**
     * Starts the migration service by scheduling the first attempt after
     * the initial delay.
     *
     * <p>This method should be called once after the client has logged in
     * and AB props are available.
     *
     * @implNote ADAPTED: WAWebTasksDefinitions.registerTasks schedules the
     *           migration task via the WA Web task scheduler with a 60-second
     *           initial delay. Cobalt uses {@link SchedulerUtils#scheduleDelayed}
     *           to achieve the same effect.
     */
    public void start() {
        if (complete.get()) {
            LOGGER.log(System.Logger.Level.DEBUG,
                    "[lid-inactive-group-migration] already done, skip");
            return;
        }

        scheduledTask = SchedulerUtils.scheduleDelayed(INITIAL_DELAY, this::run);
    }

    /**
     * Cancels any pending scheduled migration task and resets the service
     * state. Called on disconnect/reconnect.
     *
     * @implNote NO_WA_BASIS: Cobalt lifecycle method for cleaning up
     *           scheduled tasks on disconnect/reconnect. WA Web relies on
     *           the task scheduler's own cleanup mechanism.
     */
    public void reset() {
        var task = scheduledTask;
        if (task != null) {
            task.cancel(true);
            scheduledTask = null;
        }
    }

    /**
     * Executes a single migration pass.
     *
     * <p>Checks the AB prop, finds PN-mode groups, queries their metadata,
     * and either marks migration complete or schedules a retry.
     *
     * @implNote WAWebInactiveGroupLidMigrationJob.migrateInactiveGroupsToLid.
     *           WA Web uses {@code queryAndUpdateAllGroupMetadata} for batch
     *           queries; Cobalt iterates individually via
     *           {@code queryChatMetadata}. WA Web relies on the external task
     *           scheduler for retry (24h interval); Cobalt self-schedules via
     *           {@link SchedulerUtils#scheduleDelayed}. WA Web rethrows
     *           errors to the task scheduler; Cobalt logs and swallows since
     *           the scheduled task handles completion independently.
     */
    private void run() {
        try {
            if (!abPropsService.getBool(ABProp.ENABLE_INACTIVE_GROUP_LID_MIGRATION)) {
                LOGGER.log(System.Logger.Level.DEBUG,
                        "[lid-inactive-group-migration] ABProp disabled, skip");
                return;
            }

            if (complete.get()) {
                LOGGER.log(System.Logger.Level.DEBUG,
                        "[lid-inactive-group-migration] already done, skip");
                return;
            }

            LOGGER.log(System.Logger.Level.INFO,
                    "[lid-inactive-group-migration] starting migration");

            var pnGroups = findPnGroups();
            if (pnGroups.isEmpty()) {
                LOGGER.log(System.Logger.Level.INFO,
                        "[lid-inactive-group-migration] no PN groups, done");
                complete.set(true);
                return;
            }

            LOGGER.log(System.Logger.Level.INFO,
                    "[lid-inactive-group-migration] found {0} PN groups", pnGroups.size());

            for (var groupJid : pnGroups) {
                try {
                    client.queryChatMetadata(groupJid);
                } catch (Exception e) {
                    LOGGER.log(System.Logger.Level.DEBUG,
                            "[lid-inactive-group-migration] failed to query {0}: {1}",
                            groupJid, e.getMessage());
                }
            }

            LOGGER.log(System.Logger.Level.INFO,
                    "[lid-inactive-group-migration] groups queried+updated");

            var remaining = findPnGroups();
            if (remaining.isEmpty()) {
                LOGGER.log(System.Logger.Level.INFO,
                        "[lid-inactive-group-migration] no PN groups left, done");
                complete.set(true);
            } else {
                LOGGER.log(System.Logger.Level.INFO,
                        "[lid-inactive-group-migration] {0} PN groups left, retry later",
                        remaining.size());
                scheduledTask = SchedulerUtils.scheduleDelayed(RETRY_DELAY, this::run);
            }
        } catch (Exception e) {
            LOGGER.log(System.Logger.Level.WARNING,
                    "[lid-inactive-group-migration] Failed to complete migration: {0}",
                    e.getMessage());
        }
    }

    /**
     * Finds all group chats that are still in phone-number addressing mode.
     *
     * <p>Iterates over all chats on the group-or-community server, looks up
     * each chat's metadata, and retains only those where
     * {@code isLidAddressingMode} is {@code false} and the group is neither
     * suspended nor terminated.
     *
     * <p>In WA Web the membership check is explicit via
     * {@code bulkCheckMyMembership}; in Cobalt the store already contains
     * only chats the user is a member of, so the membership filter is
     * implicit.
     *
     * @implNote ADAPTED: WAWebInactiveGroupLidMigrationJob.C (findPnGroups).
     *           WA Web calls {@code getGroupMetadataTable().all()}, filters by
     *           {@code isLidAddressingMode !== true}, then calls
     *           {@code bulkCheckMyMembership} and excludes
     *           {@code suspended}/{@code terminated} groups. Cobalt uses
     *           {@code store.chats()} (which implicitly filters to member
     *           chats) and checks suspended/terminated on the concrete
     *           metadata types.
     * @return the list of group JIDs that have not migrated to LID
     */
    private List<Jid> findPnGroups() {
        var store = client.store();
        return store.chats()
                .stream()
                .map(chat -> chat.jid())
                .filter(jid -> jid.hasServer(JidServer.groupOrCommunity())) // WAWebInactiveGroupLidMigrationJob.C
                .filter(jid -> {
                    var metadata = store.findChatMetadata(jid).orElse(null);
                    return metadata != null
                            && !metadata.isLidAddressingMode() // WAWebInactiveGroupLidMigrationJob.C: isLidAddressingMode !== true
                            && !isSuspendedOrTerminated(metadata); // WAWebInactiveGroupLidMigrationJob.C: !e.suspended && !e.terminated
                })
                .toList();
    }

    /**
     * Checks whether the given chat metadata represents a suspended or
     * terminated group/community.
     *
     * <p>Since {@link ChatMetadata} is a sealed interface that permits
     * {@link GroupMetadata} and {@link CommunityMetadata}, this method
     * uses pattern matching to delegate to the concrete type's
     * {@code isSuspended()} and {@code isTerminated()} accessors.
     *
     * @implNote WAWebInactiveGroupLidMigrationJob.C: {@code !e.suspended && !e.terminated}
     *           filter applied after membership check.
     * @param metadata the non-{@code null} chat metadata to check
     * @return {@code true} if the group or community is suspended or terminated
     */
    private static boolean isSuspendedOrTerminated(ChatMetadata metadata) {
        return switch (metadata) {
            case GroupMetadata gm -> gm.isSuspended() || gm.isTerminated(); // WAWebInactiveGroupLidMigrationJob.C
            case CommunityMetadata cm -> cm.isSuspended() || cm.isTerminated(); // WAWebInactiveGroupLidMigrationJob.C
        };
    }
}
