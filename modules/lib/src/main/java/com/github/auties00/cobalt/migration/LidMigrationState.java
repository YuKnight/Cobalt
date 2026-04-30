package com.github.auties00.cobalt.migration;

import com.github.auties00.cobalt.meta.annotation.WhatsAppWebExport;
import com.github.auties00.cobalt.meta.annotation.WhatsAppWebModule;
import com.github.auties00.cobalt.meta.model.WhatsAppAdaptation;
import it.auties.protobuf.annotation.ProtobufEnum;
import it.auties.protobuf.annotation.ProtobufEnumIndex;

/**
 * Represents the position of a single account inside the 1:1 LID migration
 * pipeline.
 *
 * <p>The pipeline starts when a paired client receives the AB prop that
 * enables migration, waits for the primary device to deliver its mapping
 * tables, runs the rewrite over the local chat store, and finally records
 * the account as fully migrated. This enum encodes the same six values that
 * WhatsApp Web's {@code LidThreadMigrationStatus} enum exposes, plus three
 * Cobalt-only states ({@link #NOT_STARTED}, {@link #FAILED},
 * {@link #DISABLED}) that exist because Cobalt drives the migration through
 * an explicit state machine rather than through ad-hoc UserPrefs flags and
 * because the Cobalt error model surfaces failures as terminal states.
 */
@ProtobufEnum
@WhatsAppWebModule(moduleName = "WAWebLid1X1ThreadAccountMigrations.flow")
public enum LidMigrationState {
    /**
     * Initial state before any migration activity has begun.
     */
    NOT_STARTED(0),

    /**
     * Waiting for the AB prop that enables LID migration to flip on.
     */
    @WhatsAppWebExport(moduleName = "WAWebLid1X1ThreadAccountMigrations.flow",
            exports = "LidThreadMigrationStatus", adaptation = WhatsAppAdaptation.DIRECT)
    WAITING_PROP(1),

    /**
     * Waiting for the primary device to send the LID mapping sync message.
     */
    @WhatsAppWebExport(moduleName = "WAWebLid1X1ThreadAccountMigrations.flow",
            exports = "LidThreadMigrationStatus", adaptation = WhatsAppAdaptation.DIRECT)
    WAITING_MAPPINGS(2),

    /**
     * Mappings have been received and validated, the migration is ready to
     * run.
     */
    @WhatsAppWebExport(moduleName = "WAWebLid1X1ThreadAccountMigrations.flow",
            exports = "LidThreadMigrationStatus", adaptation = WhatsAppAdaptation.DIRECT)
    READY(3),

    /**
     * Migration is currently rewriting threads from PN to LID addressing.
     */
    @WhatsAppWebExport(moduleName = "WAWebLid1X1ThreadAccountMigrations.flow",
            exports = "LidThreadMigrationStatus", adaptation = WhatsAppAdaptation.DIRECT)
    IN_PROGRESS(4),

    /**
     * Migration finished successfully and every eligible thread has been
     * rewritten.
     */
    @WhatsAppWebExport(moduleName = "WAWebLid1X1ThreadAccountMigrations.flow",
            exports = "LidThreadMigrationStatus", adaptation = WhatsAppAdaptation.DIRECT)
    COMPLETE(5),

    /**
     * Migration aborted because of a fatal error surfaced through the
     * configurable error handler.
     */
    FAILED(6),

    /**
     * Migration is disabled and will not run for this session.
     */
    DISABLED(7);

    /**
     * The protobuf wire index assigned to this state.
     */
    final int index;

    /**
     * Constructs a new state with the given protobuf index.
     *
     * @param index the protobuf wire index
     */
    LidMigrationState(@ProtobufEnumIndex int index) {
        this.index = index;
    }

    /**
     * Returns whether this state is terminal, meaning the migration will
     * not progress further without an explicit external transition.
     *
     * @return {@code true} if this state is {@link #COMPLETE},
     *         {@link #FAILED}, or {@link #DISABLED}
     */
    public boolean isTerminal() {
        return this == DISABLED || this == COMPLETE || this == FAILED;
    }
}
