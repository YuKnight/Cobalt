package com.github.auties00.cobalt.device.fanout;

import com.github.auties00.cobalt.model.jid.Jid;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Collection;
import java.util.Comparator;

/**
 * Calculates participant hash (phash) for group messages.
 * <p>
 * The phash is used to verify that the sender and server agree on the
 * list of participants/devices that should receive a group message.
 */
public final class DevicePhashCalculator {
    private static final int HASH_BYTES_TO_USE = 6;

    private DevicePhashCalculator() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }

    /**
     * Calculates the phash for a collection of device JIDs.
     * The Meta AI bot can be injected for open groups (only applicable for V2).
     *
     * @param deviceJids     the device JIDs to include in the hash
     * @param version        the phash version to use
     * @param includeMetaBot whether to include the Meta AI bot (only for V2, ignored for V1)
     * @return the phash string (e.g., "1:q83vEjRW" or "2:q83vEjRW")
     * @throws NoSuchAlgorithmException if the hash algorithm is not available
     */
    // TODO: Cache hash for better performance
    //       The cache should not be shared between sessions
    public static String calculate(
            Collection<Jid> deviceJids,
            DevicePhashVersion version,
            boolean includeMetaBot
    ) throws NoSuchAlgorithmException {
        var jidsToHash = new ArrayList<>(deviceJids);

        // Add Meta AI bot for open groups if version supports it
        if (includeMetaBot && version.supportsMetaBot()) {
            jidsToHash.add(Jid.metaAiBot());
        }

        var legacyJids = jidsToHash.stream()
                .map(jid -> toLegacyJidString(jid, version))
                .sorted(Comparator.naturalOrder())
                .toList();

        var digest = MessageDigest.getInstance(version.algorithm());
        for (var legacyJid : legacyJids) {
            digest.update(legacyJid.getBytes(StandardCharsets.UTF_8));
        }
        var hash = digest.digest();

        var truncated = new byte[HASH_BYTES_TO_USE];
        System.arraycopy(hash, 0, truncated, 0, HASH_BYTES_TO_USE);

        var base64 = Base64.getEncoder().encodeToString(truncated);
        return version.prefix() + base64;
    }

    /**
     * Converts a device JID to legacy format for phash calculation.
     * <p>
     * Per WhatsApp Web:
     * - V1: uses {@code toString({legacy: true})} → "user@server"
     * - V2: uses {@code toString({legacy: true, formatFull: true})} → "user:device@server"
     *
     * @param jid     the JID to convert
     * @param version the phash version (determines format)
     * @return the legacy JID string
     */
    private static String toLegacyJidString(Jid jid, DevicePhashVersion version) {
        var user = jid.user();
        var server = jid.server().address();
        var device = jid.device();

        return switch (version) {
            // V1: Simple legacy format without device ID
            case V1 -> user + "@" + server;

            // V2: Full legacy format with device ID (formatFull in WhatsApp Web)
            // Format: user:device@server (device is omitted if 0)
            case V2 -> device == 0
                    ? user + "@" + server
                    : user + ":" + device + "@" + server;
        };
    }
}
