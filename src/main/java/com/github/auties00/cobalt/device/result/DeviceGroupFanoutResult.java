package com.github.auties00.cobalt.device.result;

import com.github.auties00.cobalt.model.jid.Jid;

import java.util.Collection;
import java.util.Collections;
import java.util.Objects;

/**
 * Result of group message fanout calculation containing target devices and participant hash.
 *
 * @apiNote WAWebDBDeviceListFanout.getFanOutList: returns the device list.
 * WAWebPhashUtils.phashV2: calculates the participant hash.
 */
public final class DeviceGroupFanoutResult {
    private final Collection<Jid> devices;
    private final String phash;

    /**
     * Creates a new device group fanout result.
     *
     * @param devices the collection of device JIDs to send to
     * @param phash   the calculated participant hash for server verification
     */
    public DeviceGroupFanoutResult(Collection<Jid> devices, String phash) {
        this.devices = Objects.requireNonNull(devices, "devices cannot be null");
        this.phash = Objects.requireNonNull(phash, "phash cannot be null");
    }

    /**
     * Returns the set of device JIDs to send to.
     *
     * @return an unmodifiable view of the set of device JIDs
     */
    public Collection<Jid> devices() {
        return Collections.unmodifiableCollection(devices);
    }

    /**
     * Returns the calculated participant hash for server verification.
     *
     * @return the participant hash
     */
    public String phash() {
        return phash;
    }
}
