package com.github.auties00.cobalt.migration;

/**
 * Represents the migration status for an individual chat thread.
 * <p>
 * During LID migration, each thread is evaluated to determine whether it
 * should be migrated, kept as-is, or deleted based on available LID mappings.
 */
public enum LidThreadMigrationStatus {
    /**
     * Thread should be migrated from PN to LID addressing.
     * A valid LID mapping exists for this thread's JID.
     */
    MIGRATE,

    /**
     * Thread should be kept as-is without migration.
     * This applies to threads that are already using LID addressing,
     * or threads that don't require migration (e.g., groups, newsletters).
     */
    KEEP,

    /**
     * Thread should be deleted because no valid LID mapping exists.
     * This typically means the contact has not been migrated on the primary device.
     */
    DELETE,

    /**
     * Thread evaluation encountered an error.
     * The thread's migration status could not be determined.
     */
    ERROR;

    /**
     * Returns whether this status indicates the thread will be modified.
     *
     * @return true if the thread will be migrated or deleted
     */
    public boolean willModify() {
        return this == MIGRATE || this == DELETE;
    }

    /**
     * Returns whether this status indicates a successful resolution.
     *
     * @return true if the status is not an error
     */
    public boolean isResolved() {
        return this != ERROR;
    }
}
