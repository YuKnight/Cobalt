package com.github.auties00.cobalt.device.stanza;

import com.github.auties00.cobalt.device.info.DeviceList;
import com.github.auties00.cobalt.model.jid.Jid;

/**
 * Represents the result of parsing a device list from a USync response.
 * Can be either a full device list or an omitted result (delta update confirmation).
 */
public sealed interface DeviceListResult {
    /**
     * Returns the user JID this result is for.
     */
    Jid userJid();

    /**
     * A full device list response from the server.
     *
     * @param deviceList the complete device list
     */
    record Full(DeviceList deviceList) implements DeviceListResult {
        @Override
        public Jid userJid() {
            return deviceList.userJid();
        }
    }

    /**
     * An omitted result indicating the server confirmed the dhash matches.
     * The cached device list should be retained with updated timestamp.
     *
     * @param userJid    the user JID
     * @param timestamp  the server's timestamp (if provided)
     * @param expectedTs the server's expected timestamp (if provided)
     */
    record Omitted(Jid userJid, Long timestamp, Long expectedTs) implements DeviceListResult {

    }
}
