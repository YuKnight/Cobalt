package com.github.auties00.cobalt.device.fanout;

import com.github.auties00.cobalt.meta.annotation.WhatsAppWebExport;
import com.github.auties00.cobalt.meta.annotation.WhatsAppWebModule;
import com.github.auties00.cobalt.meta.model.WhatsAppAdaptation;
import com.github.auties00.cobalt.model.jid.Jid;

import java.util.Collections;
import java.util.Objects;
import java.util.Set;

/**
 * Carries the output of a group message fanout computation: the set of target devices and
 * the matching participant hash.
 *
 * <p>When Cobalt sends a group message it must both decide which companion devices to
 * encrypt for and attach a {@code phash} stanza attribute so the server can detect whether
 * the sender has a stale membership view. This record bundles the two values together so
 * callers cannot accidentally use one without the other.
 *
 * <p>Produced by
 * {@link com.github.auties00.cobalt.device.DeviceService#getGroupFanout(Jid, Jid)} and
 * consumed by the outbound group message encoder.
 *
 * @implNote WAWebDBDeviceListFanout.getFanOutList: returns the device list.
 * WAWebPhashUtils.phashV2: calculates the participant hash. Cobalt groups the two into
 * a single result object whereas WA Web passes them as separate locals at the call site.
 */
@WhatsAppWebModule(moduleName = "WAWebDBDeviceListFanout")
@WhatsAppWebModule(moduleName = "WAWebPhashUtils")
public final class DeviceGroupFanoutResult {

    /**
     * The set of device JIDs to send the message to.
     *
     * @implNote WAWebDBDeviceListFanout.getFanOutList: the filtered device list from
     * fanout calculation.
     */
    private final Set<Jid> devices;

    /**
     * The participant hash for server-side message delivery verification.
     *
     * @implNote WAWebPhashUtils.phashV2: hash of sorted participant JIDs used by the
     * server to verify that the client has an up-to-date view of the group.
     */
    private final String phash;

    /**
     * Creates a new device group fanout result.
     *
     * @param devices the collection of device JIDs to send to
     * @param phash   the calculated participant hash for server verification
     * @implNote ADAPTED: WAWebDBDeviceListFanout.getFanOutList: in WA Web, the device
     * list and phash are computed separately and combined at the call site. Cobalt
     * groups them into this result object for type safety.
     */
    @WhatsAppWebExport(moduleName = "WAWebDBDeviceListFanout",
            exports = "getFanOutList",
            adaptation = WhatsAppAdaptation.ADAPTED)
    @WhatsAppWebExport(moduleName = "WAWebPhashUtils",
            exports = "phashV2",
            adaptation = WhatsAppAdaptation.ADAPTED)
    public DeviceGroupFanoutResult(Set<Jid> devices, String phash) {
        this.devices = Objects.requireNonNull(devices, "devices cannot be null");
        this.phash = Objects.requireNonNull(phash, "phash cannot be null");
    }

    /**
     * Returns the set of device JIDs to send to.
     *
     * @return an unmodifiable view of the set of device JIDs
     * @implNote WAWebDBDeviceListFanout.getFanOutList: the return value of the fanout
     * calculation after identity change filtering.
     */
    @WhatsAppWebExport(moduleName = "WAWebDBDeviceListFanout",
            exports = "getFanOutList",
            adaptation = WhatsAppAdaptation.DIRECT)
    public Set<Jid> devices() {
        return Collections.unmodifiableSet(devices);
    }

    /**
     * Returns the calculated participant hash for server verification.
     *
     * @return the participant hash string
     * @implNote WAWebPhashUtils.phashV2: the computed hash value used in the message
     * stanza's {@code phash} attribute.
     */
    @WhatsAppWebExport(moduleName = "WAWebPhashUtils",
            exports = "phashV2",
            adaptation = WhatsAppAdaptation.DIRECT)
    public String phash() {
        return phash;
    }
}
