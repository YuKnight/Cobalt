package com.github.auties00.cobalt.device.fanout;

import com.github.auties00.cobalt.meta.annotation.WhatsAppWebExport;
import com.github.auties00.cobalt.meta.annotation.WhatsAppWebModule;
import com.github.auties00.cobalt.meta.model.WhatsAppAdaptation;
import com.github.auties00.cobalt.model.jid.Jid;

import java.util.Collections;
import java.util.Objects;
import java.util.Set;

/**
 * Holds the output of a group message fanout computation, namely the set of target
 * device JIDs and the matching participant hash.
 *
 * <p>When sending a group message Cobalt must decide which companion devices to
 * encrypt for and attach a {@code phash} stanza attribute so the server can detect a
 * stale membership view. This record bundles the two values together so callers
 * cannot accidentally use one without the other.
 *
 * <p>Produced by
 * {@link com.github.auties00.cobalt.device.DeviceService#getGroupFanout(Jid, Jid)}.
 */
@WhatsAppWebModule(moduleName = "WAWebDBDeviceListFanout")
@WhatsAppWebModule(moduleName = "WAWebPhashUtils")
public final class DeviceGroupFanoutResult {

    /**
     * The set of device JIDs the message is encrypted for.
     */
    private final Set<Jid> devices;

    /**
     * The participant hash attached to the stanza for server-side verification.
     */
    private final String phash;

    /**
     * Constructs a new fanout result.
     *
     * @param devices the device JIDs to send to
     * @param phash   the computed participant hash
     * @throws NullPointerException if any argument is {@code null}
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
     * Returns the device JIDs the message is encrypted for.
     *
     * @return an unmodifiable view of the device JID set
     */
    @WhatsAppWebExport(moduleName = "WAWebDBDeviceListFanout",
            exports = "getFanOutList",
            adaptation = WhatsAppAdaptation.DIRECT)
    public Set<Jid> devices() {
        return Collections.unmodifiableSet(devices);
    }

    /**
     * Returns the participant hash for the message stanza's {@code phash} attribute.
     *
     * @return the participant hash
     */
    @WhatsAppWebExport(moduleName = "WAWebPhashUtils",
            exports = "phashV2",
            adaptation = WhatsAppAdaptation.DIRECT)
    public String phash() {
        return phash;
    }
}
