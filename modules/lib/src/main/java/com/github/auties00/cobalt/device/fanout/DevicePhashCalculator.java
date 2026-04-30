package com.github.auties00.cobalt.device.fanout;

import com.github.auties00.cobalt.meta.annotation.WhatsAppWebExport;
import com.github.auties00.cobalt.meta.annotation.WhatsAppWebModule;
import com.github.auties00.cobalt.meta.model.WhatsAppAdaptation;
import com.github.auties00.cobalt.model.jid.Jid;
import com.github.auties00.cobalt.model.jid.JidServer;
import com.github.auties00.cobalt.props.ABProp;
import com.github.auties00.cobalt.props.ABPropsService;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;

/**
 * Computes the participant hash (phash) that WhatsApp servers use to confirm the
 * sender's view of a group's device membership.
 *
 * <p>Group messages carry a {@code phash} attribute on the stanza; the server
 * compares it to its own hash of the participant device JIDs and, on mismatch,
 * either rejects the stanza or instructs the client to resync. This calculator
 * reproduces both supported hash formats (SHA-1 V1 and SHA-256 V2) and is also
 * used by {@link com.github.auties00.cobalt.device.DeviceService} to short-circuit
 * device list syncs when the server pre-announces an expected phash.
 *
 * <p>V2 additionally supports injecting the Meta AI bot JIDs into the hashed set
 * when the corresponding feature flags are active.
 */
@WhatsAppWebModule(moduleName = "WAWebPhashUtils")
public final class DevicePhashCalculator {

    /**
     * Logger for phash computation diagnostics.
     */
    private static final System.Logger LOGGER = System.getLogger(DevicePhashCalculator.class.getName());

    /**
     * Number of hash bytes retained after truncation. 6 bytes encode to 8 base64
     * characters once the version prefix is prepended.
     */
    @WhatsAppWebExport(moduleName = "WAWebPhashUtils",
            exports = {"phashV1", "phashV2"},
            adaptation = WhatsAppAdaptation.DIRECT)
    private static final int HASH_BYTES_TO_USE = 6;

    /**
     * The JID of the Meta AI TEE (Trusted Execution Environment) bot account.
     */
    @WhatsAppWebExport(moduleName = "WAWebBotUtils",
            exports = "META_BOT_TEE_FBID_WID",
            adaptation = WhatsAppAdaptation.DIRECT)
    private static final Jid META_AI_TEE_BOT_ACCOUNT = new Jid("1273596044787272", JidServer.bot());

    /**
     * The AB props service used to gate Meta AI bot injection.
     */
    private final ABPropsService abPropsService;

    /**
     * Constructs a new phash calculator.
     *
     * @param abPropsService the AB props service for feature flag checks
     * @throws NullPointerException if {@code abPropsService} is {@code null}
     */
    @WhatsAppWebExport(moduleName = "WAWebPhashUtils",
            exports = {"phashV1", "phashV2"},
            adaptation = WhatsAppAdaptation.ADAPTED)
    public DevicePhashCalculator(ABPropsService abPropsService) {
        this.abPropsService = Objects.requireNonNull(abPropsService, "abPropsService cannot be null");
    }

    /**
     * Calculates the phash for the given device JIDs without TEE bot injection.
     *
     * <p>Convenience overload that passes {@code false} for the TEE bot flag.
     *
     * @param deviceJids          the device JIDs to hash
     * @param version             the phash version
     * @param allowIncludeOpenBot whether the open Meta AI group bot may be added (V2 only)
     * @return the encoded phash, including the version prefix
     * @throws NoSuchAlgorithmException if the underlying hash algorithm is unavailable
     */
    @WhatsAppWebExport(moduleName = "WAWebPhashUtils",
            exports = {"phashV1", "phashV2"},
            adaptation = WhatsAppAdaptation.ADAPTED)
    public String calculate(
            Set<Jid> deviceJids,
            DevicePhashVersion version,
            boolean allowIncludeOpenBot
    ) throws NoSuchAlgorithmException {
        return calculate(deviceJids, version, allowIncludeOpenBot, false);
    }

    /**
     * Calculates the phash for the given device JIDs.
     *
     * <p>V1 validates every JID as a user WID and serialises it to simple legacy
     * format. V2 serialises each JID to full legacy format including the agent and
     * device components. Both versions sort the resulting strings, hash them,
     * truncate the hash to 6 bytes, and prepend the version prefix to the
     * base64-encoded value.
     *
     * @param deviceJids          the device JIDs to hash
     * @param version             the phash version
     * @param allowIncludeOpenBot whether the open Meta AI group bot may be added (V2 only)
     * @param allowIncludeTeeBot  whether the TEE Meta AI group bot may be added (V2 only)
     * @return the encoded phash, including the version prefix
     * @throws NoSuchAlgorithmException if the underlying hash algorithm is unavailable
     */
    @WhatsAppWebExport(moduleName = "WAWebPhashUtils",
            exports = {"phashV1", "phashV2"},
            adaptation = WhatsAppAdaptation.DIRECT)
    public String calculate(
            Set<Jid> deviceJids,
            DevicePhashVersion version,
            boolean allowIncludeOpenBot,
            boolean allowIncludeTeeBot
    ) throws NoSuchAlgorithmException {
        var jidsToHash = new ArrayList<>(deviceJids);

        if (allowIncludeOpenBot && version.supportsMetaBot() && isOpenGroupBotParticipantAddEnabled()) {
            jidsToHash.add(Jid.metaAiBotAccount());
        }

        if (allowIncludeTeeBot && version.supportsMetaBot() && isTEEGroupBotParticipantAddEnabled()) {
            jidsToHash.add(META_AI_TEE_BOT_ACCOUNT);
        }

        var legacyJids = jidsToHash.stream()
                .map(jid -> toLegacyJidString(jid, version))
                .sorted(Comparator.naturalOrder())
                .toList();

        LOGGER.log(System.Logger.Level.TRACE,
                "[{0}] calculating phash for {1}",
                version.prefix(),
                String.join(",", legacyJids));

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
     * Returns whether the open group Meta AI bot may be added to phash inputs.
     *
     * @return {@code true} when both {@code web_ai_group_open_support} and
     *         {@code ai_group_participation_enabled} are set
     */
    @WhatsAppWebExport(moduleName = "WAWebBotGroupGatingUtils",
            exports = "isOpenGroupBotParticipantAddEnabled",
            adaptation = WhatsAppAdaptation.ADAPTED)
    private boolean isOpenGroupBotParticipantAddEnabled() {
        var webAiGroupOpenSupport = abPropsService.getBool(ABProp.WEB_AI_GROUP_OPEN_SUPPORT);
        var aiGroupParticipationEnabled = abPropsService.getBool(ABProp.AI_GROUP_PARTICIPATION_ENABLED);
        return webAiGroupOpenSupport && aiGroupParticipationEnabled;
    }

    /**
     * Returns whether the TEE group Meta AI bot may be added to phash inputs.
     *
     * @return {@code true} when both {@code web_ai_group_open_support} and
     *         {@code ai_group_participation_add_tee_enabled} are set
     */
    @WhatsAppWebExport(moduleName = "WAWebBotGroupGatingUtils",
            exports = "isTEEGroupBotParticipantAddEnabled",
            adaptation = WhatsAppAdaptation.ADAPTED)
    private boolean isTEEGroupBotParticipantAddEnabled() {
        var webAiGroupOpenSupport = abPropsService.getBool(ABProp.WEB_AI_GROUP_OPEN_SUPPORT);
        var aiGroupParticipationAddTeeEnabled = abPropsService.getBool(ABProp.AI_GROUP_PARTICIPATION_ADD_TEE_ENABLED);
        return webAiGroupOpenSupport && aiGroupParticipationAddTeeEnabled;
    }

    /**
     * Serialises a device JID to the legacy string form used for phash computation.
     *
     * <p>V1 emits {@code user@server} after stripping device information. V2 emits
     * {@code user.0:device@server}, including the agent and device components.
     *
     * @param jid     the JID to serialise
     * @param version the phash version that selects the format
     * @return the serialised legacy string
     */
    @WhatsAppWebExport(moduleName = "WAWebPhashUtils",
            exports = {"phashV1", "phashV2"},
            adaptation = WhatsAppAdaptation.ADAPTED)
    private static String toLegacyJidString(Jid jid, DevicePhashVersion version) {
        var user = jid.user();
        var device = jid.device();
        var legacyServer = toLegacyServer(jid.server());
        return switch (version) {
            case V1 -> user + "@" + legacyServer;
            case V2 -> user + ".0:" + device + "@" + legacyServer;
        };
    }

    /**
     * Maps a server to its legacy form, replacing {@code c.us} with
     * {@code s.whatsapp.net}.
     *
     * @param server the server to map
     * @return the legacy server
     */
    @WhatsAppWebExport(moduleName = "WAWebWid",
            exports = "toString",
            adaptation = WhatsAppAdaptation.ADAPTED)
    private static JidServer toLegacyServer(JidServer server) {
        if (server.equals(JidServer.legacyUser())) {
            return JidServer.user();
        }
        return server;
    }
}
