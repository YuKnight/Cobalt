package com.github.auties00.cobalt.migration;

import com.github.auties00.cobalt.meta.annotation.WhatsAppWebModule;
import com.github.auties00.cobalt.model.jid.Jid;

/**
 * Represents the decision taken for a single chat thread during the LID
 * migration sweep.
 *
 * <p>Every chat in the local store is classified into exactly one of three
 * mutually exclusive outcomes. {@link Migrate} rewrites the chat to use LID
 * addressing, {@link Keep} leaves it untouched (because it is already on
 * LID, because its type does not participate in 1:1 migration, or because a
 * duplicate LID thread will absorb it), and {@link Delete} removes it
 * (because no LID can be resolved and the chat passes the deletability
 * heuristics).
 *
 * <p>This type mirrors the discriminated return shape of WhatsApp Web's
 * {@code getResolvedThreadAccountLid} helper. The {@code {threadLid}}
 * variant becomes {@link Migrate}, the already-LID and ignore variants
 * become {@link Keep}, the {@code {deleteChat: true}} variant becomes
 * {@link Delete}, and the {@code {logoutReason}} variant is converted into
 * a {@code WhatsAppLidMigrationException} thrown by
 * {@link LidMigrationService}.
 */
@WhatsAppWebModule(moduleName = "WAWebLid1X1ThreadAccountMigrations")
public sealed interface LidMigrationResolution
        permits LidMigrationResolution.Migrate,
                LidMigrationResolution.Keep,
                LidMigrationResolution.Delete{
    /**
     * Returns the JID the chat had before any migration rewrite was
     * applied.
     *
     * @return the original JID of the chat
     */
    Jid originalJid();

    /**
     * Resolution that re-keys a chat to LID addressing.
     *
     * <p>When executed by the migration service the chat is moved to
     * {@code targetLid} and its original phone-number JID is preserved as
     * metadata so historical references continue to resolve.
     *
     * @param originalJid the phone-number JID the chat previously used
     * @param targetLid   the LID the chat is rewritten to
     */
    record Migrate(Jid originalJid, Jid targetLid) implements LidMigrationResolution {

    }

    /**
     * Resolution that leaves a chat untouched.
     *
     * <p>Emitted when the chat already uses LID addressing, when the chat
     * type is not part of 1:1 migration (group, community, newsletter,
     * broadcast, bot), or when a duplicate LID thread will absorb the
     * chat during the sweep.
     *
     * @param originalJid the JID currently held by the chat
     * @param reason      the reason the chat is being kept as is
     */
    record Keep(Jid originalJid, KeepReason reason) implements LidMigrationResolution {

    }

    /**
     * Resolution that removes a chat from the store.
     *
     * <p>Emitted only when no LID mapping can be resolved for a 1:1 chat
     * and the chat passes the deletability heuristics that classify it as
     * safe to drop (no ephemeral or locked or archived or muted state, and
     * only safe stub messages or call-log entries).
     *
     * @param originalJid the phone-number JID of the chat being removed
     * @param reason      the reason the chat is being deleted
     */
    record Delete(Jid originalJid, DeleteReason reason) implements LidMigrationResolution {

    }

    /**
     * Enumerates the reasons a chat is left untouched during LID
     * migration.
     */
    enum KeepReason {
        /**
         * The chat already uses LID addressing.
         */
        ALREADY_LID,

        /**
         * The chat is a group or community and does not participate in
         * 1:1 migration.
         */
        GROUP_OR_COMMUNITY,

        /**
         * The chat is a newsletter and does not participate in 1:1
         * migration.
         */
        NEWSLETTER,

        /**
         * The chat is a regular broadcast list and does not participate
         * in 1:1 migration.
         */
        BROADCAST,

        /**
         * The chat is the dedicated status broadcast and does not
         * participate in 1:1 migration.
         */
        STATUS_BROADCAST,

        /**
         * The chat belongs to a bot account.
         */
        BOT,

        /**
         * The chat has a duplicate LID thread that will be merged into
         * it during the sweep.
         */
        DUPLICATE_WILL_MERGE
    }

    /**
     * Enumerates the reasons a chat is removed during LID migration.
     */
    enum DeleteReason {
        /**
         * No LID mapping was found in the primary device's cache or in
         * the local store.
         */
        NO_LID_MAPPING,

        /**
         * The contact has not yet completed LID migration on their end.
         */
        CONTACT_NOT_MIGRATED,

        /**
         * A split thread was detected that would result in a duplicate
         * after migration.
         */
        SPLIT_THREAD_MISMATCH
    }
}
