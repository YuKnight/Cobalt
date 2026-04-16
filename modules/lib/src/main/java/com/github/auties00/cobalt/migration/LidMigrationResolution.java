package com.github.auties00.cobalt.migration;

import com.github.auties00.cobalt.meta.annotation.WhatsAppWebModule;
import com.github.auties00.cobalt.model.jid.Jid;

/**
 * Represents the decision taken for a single chat thread during the LID
 * migration sweep.
 *
 * <p>When the LID migration runs over the local chat store, each chat is
 * classified into one of three mutually exclusive outcomes:
 * <ul>
 *     <li>{@link Migrate} - the chat is rewritten to use LID addressing,</li>
 *     <li>{@link Keep} - the chat is left untouched because it is either
 *         already on LID, an unsupported type (group, newsletter, broadcast,
 *         bot), or has a duplicate LID thread that will absorb it,</li>
 *     <li>{@link Delete} - the chat is removed because no LID mapping is
 *         known and the chat is judged safe to delete.</li>
 * </ul>
 *
 * <p>This type mirrors the return shape of WA Web's
 * {@code getResolvedThreadAccountLid} helper and is consumed by the
 * executor phase of {@link LidMigrationService}.
 *
 * @implNote WAWebLid1X1ThreadAccountMigrations.getResolvedThreadAccountLid:
 *           maps the JS return shapes to Cobalt types. {@code {threadLid}}
 *           becomes {@link Migrate}; an already-LID or non-regular JID
 *           becomes {@link Keep}; {@code {deleteChat: true}} becomes
 *           {@link Delete}; the {@code {logoutReason}} shape causes a
 *           {@code WhatsAppLidMigrationException} to be thrown instead.
 */
@WhatsAppWebModule(moduleName = "WAWebLid1X1ThreadAccountMigrations")
public sealed interface LidMigrationResolution
        permits LidMigrationResolution.Migrate,
                LidMigrationResolution.Keep,
                LidMigrationResolution.Delete{
    /**
     * Returns the original JID of the chat thread before any migration
     * rewrite is applied.
     *
     * @return the original JID, never {@code null}
     */
    Jid originalJid();

    /**
     * Resolution indicating the thread should be migrated to LID addressing.
     *
     * <p>When executed, the chat is re-keyed to {@code targetLid} and its
     * phone-number JID is preserved as metadata for backwards compatibility.
     *
     * @param originalJid the original phone number JID of the chat
     * @param targetLid   the LID the chat should be re-keyed to
     */
    record Migrate(Jid originalJid, Jid targetLid) implements LidMigrationResolution {

    }

    /**
     * Resolution indicating the thread should be kept as-is.
     *
     * <p>Keep is emitted for chats that either already use LID addressing,
     * are of a type not covered by 1:1 migration, or whose duplicate LID
     * thread will absorb them during the sweep.
     *
     * @param originalJid the original JID (may be PN or LID)
     * @param reason      the reason the chat is being kept unchanged
     */
    record Keep(Jid originalJid, KeepReason reason) implements LidMigrationResolution {

    }

    /**
     * Resolution indicating the thread should be deleted.
     *
     * <p>Delete is emitted when no LID mapping is available for a 1:1 chat
     * and the chat passes the deletability heuristics (no ephemeral/locked/
     * archived/muted state, only safe system messages, etc.).
     *
     * @param originalJid the original phone number JID of the chat
     * @param reason      the reason the chat is being deleted
     */
    record Delete(Jid originalJid, DeleteReason reason) implements LidMigrationResolution {

    }

    /**
     * Enumerates the reasons a chat may be kept unchanged during LID
     * migration.
     */
    enum KeepReason {
        /**
         * Thread is already using LID addressing.
         */
        ALREADY_LID,

        /**
         * Thread is a group or community (not subject to LID migration).
         */
        GROUP_OR_COMMUNITY,

        /**
         * Thread is a newsletter (not subject to LID migration).
         */
        NEWSLETTER,

        /**
         * Thread is a broadcast list (not subject to LID migration).
         */
        BROADCAST,

        /**
         * Thread is the status broadcast (not subject to LID migration).
         */
        STATUS_BROADCAST,

        /**
         * Thread belongs to a bot account.
         */
        BOT,

        /**
         * Thread has a duplicate LID thread that will be merged.
         */
        DUPLICATE_WILL_MERGE
    }

    /**
     * Enumerates the reasons a chat may be deleted during LID migration.
     */
    enum DeleteReason {
        /**
         * No LID mapping found in primary device's cache.
         */
        NO_LID_MAPPING,

        /**
         * Contact has not completed LID migration on their end.
         */
        CONTACT_NOT_MIGRATED,

        /**
         * Split thread detected - would result in duplicate after migration.
         */
        SPLIT_THREAD_MISMATCH
    }
}
