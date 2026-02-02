package com.github.auties00.cobalt.device.fanout;

/**
 * Participant hash version.
 */
public enum DevicePhashVersion {
    /**
     * Version 1: SHA-1 based phash with "1:" prefix.
     * Does not support Meta AI bot injection.
     */
    V1("SHA-1", "1:", false),

    /**
     * Version 2: SHA-256 based phash with "2:" prefix (current).
     * Supports Meta AI bot injection for open groups.
     */
    V2("SHA-256", "2:", true);

    private final String algorithm;
    private final String prefix;
    private final boolean supportsMetaBot;

    DevicePhashVersion(String algorithm, String prefix, boolean supportsMetaBot) {
        this.algorithm = algorithm;
        this.prefix = prefix;
        this.supportsMetaBot = supportsMetaBot;
    }

    /**
     * Returns the hash algorithm name for this version.
     *
     * @return the algorithm name (e.g., "SHA-1", "SHA-256")
     */
    public String algorithm() {
        return algorithm;
    }

    /**
     * Returns the prefix string for this version.
     *
     * @return the prefix (e.g., "1:", "2:")
     */
    public String prefix() {
        return prefix;
    }

    /**
     * Returns whether this version supports Meta AI bot injection.
     *
     * @return true if Meta AI bot can be injected, false otherwise
     */
    public boolean supportsMetaBot() {
        return supportsMetaBot;
    }
}
