package com.github.auties00.cobalt.device.fanout;

import com.github.auties00.cobalt.meta.annotation.WhatsAppWebExport;
import com.github.auties00.cobalt.meta.annotation.WhatsAppWebModule;
import com.github.auties00.cobalt.meta.model.WhatsAppAdaptation;
import com.github.auties00.cobalt.model.device.info.DeviceList;
import com.github.auties00.cobalt.model.jid.Jid;
import com.github.auties00.cobalt.props.ABProp;
import com.github.auties00.cobalt.props.ABPropsService;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Computes the set of recipient device JIDs ("fanout") for a WhatsApp message.
 *
 * <p>When sending either a 1:1 or a group message, the sender must decide exactly
 * which companion devices receive an encrypted copy. This calculator walks the
 * resolved per-user device lists and removes the sender's own device, applies the
 * hosted-device gating rules (business coexistence), and falls back to the primary
 * device when no device record exists. It also filters devices with unconfirmed
 * identity changes so Cobalt never silently re-encrypts to a device whose key
 * rotation the user has not acknowledged.
 *
 * <p>Invoked by
 * {@link com.github.auties00.cobalt.device.DeviceService#getUserFanout(Jid, String)}
 * and {@link com.github.auties00.cobalt.device.DeviceService#getGroupFanout(Jid, Jid)}.
 *
 * @implNote WAWebDBDeviceListFanout.getFanOutList: retrieves device identifiers for
 * specified users, filtering out the sender's own device and applying hosted device
 * logic. Hosted inclusion gated via WAWebBizCoexGatingUtils.bizHostedDevicesEnabled.
 * Identity change filtering is WAWebSendMsgCommonApi.filterDeviceWithChangedIdentity.
 */
@WhatsAppWebModule(moduleName = "WAWebDBDeviceListFanout")
public final class DeviceFanoutCalculator {

    /**
     * Logger for fanout diagnostics.
     */
    private static final System.Logger LOGGER = System.getLogger(DeviceFanoutCalculator.class.getName());

    /**
     * The AB props service used to gate hosted-device inclusion.
     */
    private final ABPropsService abPropsService;

    /**
     * Constructs a new fanout calculator.
     *
     * @param abPropsService the AB props service
     * @throws NullPointerException if {@code abPropsService} is {@code null}
     */
    @WhatsAppWebExport(moduleName = "WAWebDBDeviceListFanout",
            exports = "getFanOutList",
            adaptation = WhatsAppAdaptation.ADAPTED)
    public DeviceFanoutCalculator(ABPropsService abPropsService) {
        this.abPropsService = Objects.requireNonNull(abPropsService, "abPropsService cannot be null");
    }

    /**
     * Calculates the fanout list for the given device lists.
     *
     * <p>For each user's device list, iterates over all devices and includes them in
     * the fanout unless they are hosted (and hosted inclusion is not enabled) or they
     * represent the sender's own device. When no device list exists for a user, falls
     * back to the user's primary JID unless the user is the sender's own account.
     *
     * <p>Hosted devices are included only when all three conditions are met:
     * <ol>
     *   <li>{@code bizHostedDevicesEnabled()} returns {@code true}</li>
     *   <li>{@code includeHostedForOneToOneChatJid} is non-null</li>
     *   <li>{@code includeHostedForOneToOneChatJid} is a user-type JID</li>
     * </ol>
     *
     * @param senderJid                       the JID of the sender (exact device JID)
     * @param deviceLists                     the users' device lists
     * @param includeHostedForOneToOneChatJid JID for which hosted devices should be included, or {@code null}
     * @return unmodifiable set of device JIDs to send to
     * @implNote WAWebDBDeviceListFanout.getFanOutList: filters out sender's own device via
     * {@code isMeDevice}, skips hosted devices unless {@code bizHostedDevicesEnabled}
     * and the chat is a 1:1 user chat ({@code chatWidSetToIncludeHostedInFanoutOneToOneChatOnly.isUser()}).
     * Uses a {@code Map} keyed by {@code toString()} for deduplication; Cobalt uses a
     * {@code HashSet} which achieves the same deduplication via {@code equals}/{@code hashCode}.
     */
    @WhatsAppWebExport(moduleName = "WAWebDBDeviceListFanout",
            exports = "getFanOutList",
            adaptation = WhatsAppAdaptation.DIRECT)
    public Set<Jid> calculate(
            Jid senderJid,
            Set<DeviceList> deviceLists,
            Jid includeHostedForOneToOneChatJid
    ) {
        var results = new HashSet<Jid>();
        var includeHosted = isBizHostedDevicesEnabled()
                && includeHostedForOneToOneChatJid != null
                && isUserJid(includeHostedForOneToOneChatJid);
        var fallbackWids = new ArrayList<String>();

        for (var deviceList : deviceLists) {
            var userJid = deviceList.userJid();

            if (deviceList.devices().isEmpty()) {
                var primaryJid = userJid.toUserJid();
                if (fallbackWids.size() < 3) {
                    fallbackWids.add(primaryJid.toString());
                }
                if (!isSameAccount(primaryJid, senderJid)) {
                    results.add(primaryJid);
                }
                continue;
            }

            for (var device : deviceList.devices()) {
                if (device.isHosted() && !includeHosted) {
                    continue;
                }
                var deviceJid = device.toDeviceJid(userJid.user(), userJid.server());
                if (isSameDevice(deviceJid, senderJid)) {
                    continue;
                }
                results.add(deviceJid);
            }
        }

        if (!fallbackWids.isEmpty()) {
            LOGGER.log(System.Logger.Level.DEBUG,
                    "[getFanOutList] no device for {0} wids => primary {1}",
                    fallbackWids.size(),
                    fallbackWids);
        }

        return Collections.unmodifiableSet(results);
    }

    /**
     * Returns whether the hosted-devices feature is enabled.
     *
     * @return {@code true} when {@code adv_accept_hosted_devices} is set
     */
    @WhatsAppWebExport(moduleName = "WAWebBizCoexGatingUtils",
            exports = "bizHostedDevicesEnabled",
            adaptation = WhatsAppAdaptation.ADAPTED)
    public boolean isBizHostedDevicesEnabled() {
        return abPropsService.getBool(ABProp.ADV_ACCEPT_HOSTED_DEVICES);
    }

    /**
     * Returns whether the given JID addresses an individual user account.
     *
     * @param jid the JID to test
     * @return {@code true} for user-type servers ({@code c.us}, {@code lid},
     *         {@code bot}, {@code hosted}, {@code hosted.lid})
     */
    @WhatsAppWebExport(moduleName = "WAWebWid",
            exports = "isUser",
            adaptation = WhatsAppAdaptation.ADAPTED)
    private static boolean isUserJid(Jid jid) {
        return jid.hasUserServer()
                || jid.hasLidServer()
                || jid.hasBotServer()
                || jid.hasHostedServer()
                || jid.hasHostedLidServer();
    }

    /**
     * Returns whether the two JIDs reference the same device.
     *
     * @param a the first JID
     * @param b the second JID
     * @return {@code true} if both refer to the same device
     */
    @WhatsAppWebExport(moduleName = "WAWebUserPrefsMeUser",
            exports = "isMeDevice",
            adaptation = WhatsAppAdaptation.ADAPTED)
    private static boolean isSameDevice(Jid a, Jid b) {
        return Objects.equals(a, b);
    }

    /**
     * Returns whether the two JIDs reference the same account.
     *
     * <p>The {@code hosted} server maps to {@code c.us} and {@code hosted.lid} to
     * {@code lid} when comparing via {@link Jid#toUserJid()}.
     *
     * @param a the first JID
     * @param b the second JID
     * @return {@code true} if both reference the same account
     */
    @WhatsAppWebExport(moduleName = "WAWebUserPrefsMeUser",
            exports = "isMeAccount",
            adaptation = WhatsAppAdaptation.ADAPTED)
    private static boolean isSameAccount(Jid a, Jid b) {
        if (a == null || b == null) {
            return false;
        }
        return Objects.equals(a.toUserJid(), b.toUserJid());
    }

    /**
     * Returns the device subset whose identity keys have not been flagged as changed.
     *
     * <p>Devices with pending identity-change confirmations are removed from the
     * fanout so messages are never silently re-encrypted against an unverified key.
     *
     * @param devices           the candidate devices
     * @param changedIdentities the devices with pending identity changes
     * @return the filtered device set
     */
    @WhatsAppWebExport(moduleName = "WAWebSendMsgCommonApi",
            exports = "filterDeviceWithChangedIdentity",
            adaptation = WhatsAppAdaptation.ADAPTED)
    public Set<Jid> filterIdentityChanges(Set<Jid> devices, Set<Jid> changedIdentities) {
        if (changedIdentities.isEmpty()) {
            return devices;
        }

        return devices.stream()
                .filter(jid -> !changedIdentities.contains(jid))
                .collect(Collectors.toUnmodifiableSet());
    }
}
