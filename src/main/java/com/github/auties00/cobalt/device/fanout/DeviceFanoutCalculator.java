package com.github.auties00.cobalt.device.fanout;

import com.github.auties00.cobalt.device.info.DeviceList;
import com.github.auties00.cobalt.model.jid.Jid;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Calculates which devices should receive a message (fanout list).
 */
public final class DeviceFanoutCalculator {
    private DeviceFanoutCalculator() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }

    /**
     * Calculates the fanout list for any number of device lists.
     *
     * @param senderJid the jid of the sender
     * @param deviceLists the users' device list
     * @return set of device JIDs to send to
     */
    public static Set<Jid> calculate(Jid senderJid, List<DeviceList> deviceLists) {
        var results = new HashSet<Jid>();
        for(var deviceList : deviceLists) {
            var userJid = deviceList.userJid();
            for (var device : deviceList.devices()) {
                if (device.isHosted()) {
                    continue;
                }

                var deviceJid = device.toDeviceJid(userJid.user(), userJid.server());
                if (Objects.equals(deviceJid, senderJid)) {
                    continue;
                }

                results.add(deviceJid);
            }
        }
        return Collections.unmodifiableSet(results);
    }

    /**
     * Filters out devices with unconfirmed identity changes.
     * These devices should be excluded from fanout until the user confirms the identity change.
     *
     * @param devices         the list of devices to filter
     * @param changedIdentities the set of devices with unconfirmed identity changes
     * @return filtered list excluding devices with identity changes
     */
    public static Set<Jid> filterIdentityChanges(Set<Jid> devices, Set<Jid> changedIdentities) {
        return changedIdentities.isEmpty() ? devices : devices.stream()
                .filter(jid -> !changedIdentities.contains(jid))
                .collect(Collectors.toUnmodifiableSet());
    }
}
