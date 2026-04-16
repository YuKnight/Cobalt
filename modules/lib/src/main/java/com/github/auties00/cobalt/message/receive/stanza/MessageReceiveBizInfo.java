package com.github.auties00.cobalt.message.receive.stanza;

import com.github.auties00.cobalt.meta.annotation.WhatsAppWebExport;
import com.github.auties00.cobalt.meta.annotation.WhatsAppWebModule;
import com.github.auties00.cobalt.meta.model.WhatsAppAdaptation;

import java.util.Optional;

/**
 * Holds the business-related information parsed from an incoming
 * message stanza.
 *
 * <p>Combines data from the {@code <biz>} child node, the
 * {@code verified_name} attribute/child, and the {@code verified_level}
 * attribute. This metadata identifies verified business accounts and
 * carries privacy mode information for business-hosted messaging (for
 * example when a message is handled by Meta's BSP infrastructure).
 * Cobalt uses it to drive business template rendering, business-verified
 * badges, and to apply privacy policy behavior.
 *
 * @implNote WAWebHandleMsgParser function v(): parses verified_name,
 * verified_level, biz node (actual_actors, host_storage, privacy_mode_ts,
 * native_flow_name, campaign_id, button/list/hsm envelope flags).
 */
@WhatsAppWebModule(moduleName = "WAWebHandleMsgParser")
public final class MessageReceiveBizInfo {
    /**
     * The raw bytes of the {@code <verified_name>} child node containing
     * the serialized verified name certificate.
     *
     * @implNote WAWebHandleMsgParser function v(): {@code verifiedNameCert}
     * extracted via {@code e.maybeChild("verified_name")?.contentBytes()}.
     */
    private final byte[] verifiedNameCert;

    /**
     * The serial number from the {@code verified_name} stanza attribute,
     * defaulting to {@code -1} when the attribute is absent.
     *
     * @implNote WAWebHandleMsgParser function v(): {@code verifiedNameSerial}.
     */
    private final int verifiedNameSerial;

    /**
     * The {@code verified_level} attribute of the stanza, identifying
     * the business verification tier.
     *
     * @implNote WAWebHandleMsgParser function v(): {@code verifiedLevel}.
     */
    private final String verifiedLevel;

    /**
     * The native flow name that identifies the type of business flow
     * the message belongs to (for example {@code "shops"},
     * {@code "appointment_booking"}).
     *
     * @implNote WAWebHandleMsgParser function v(): {@code nativeFlowName}.
     */
    private final String nativeFlowName;

    /**
     * The {@code campaign_id} attribute identifying the business campaign
     * that originated the message.
     *
     * @implNote WAWebHandleMsgParser function v(): {@code campaignId}.
     */
    private final String campaignId;

    /**
     * The number of actual actors involved in a business-hosted
     * conversation (privacy mode metadata).
     *
     * @implNote WAWebHandleMsgParser function v(): {@code actualActors}.
     */
    private final Integer actualActors;

    /**
     * The host storage indicator for business-hosted conversations
     * (privacy mode metadata).
     *
     * @implNote WAWebHandleMsgParser function v(): {@code hostStorage}.
     */
    private final Integer hostStorage;

    /**
     * The timestamp at which the current privacy mode took effect for
     * the conversation.
     *
     * @implNote WAWebHandleMsgParser function v(): {@code privacyModeTs}.
     */
    private final Integer privacyModeTs;

    /**
     * Whether the message is wrapped in a verified buttons envelope.
     *
     * @implNote WAWebHandleMsgParser function v(): {@code verifiedButtonsEnvelope},
     * set when the biz node has a {@code <buttons>} child.
     */
    private final boolean verifiedButtonsEnvelope;

    /**
     * Whether the message is wrapped in a verified list envelope.
     *
     * @implNote WAWebHandleMsgParser function v(): {@code verifiedListEnvelope},
     * set when the biz node has a {@code <list>} child.
     */
    private final boolean verifiedListEnvelope;

    /**
     * Whether the message is wrapped in a verified highly-structured-message
     * envelope.
     *
     * @implNote WAWebHandleMsgParser function v(): {@code verifiedHsmEnvelope},
     * set when the message has an {@code <hsm>} child.
     */
    private final boolean verifiedHsmEnvelope;

    /**
     * Constructs a new business information record with all parsed fields.
     *
     * @param verifiedNameCert        the verified-name certificate bytes, or {@code null}
     * @param verifiedNameSerial      the verified-name serial number, or {@code -1} if absent
     * @param verifiedLevel           the verification tier, or {@code null}
     * @param nativeFlowName          the native flow name, or {@code null}
     * @param campaignId              the campaign identifier, or {@code null}
     * @param actualActors            the actor count for privacy mode, or {@code null}
     * @param hostStorage             the host storage indicator for privacy mode, or {@code null}
     * @param privacyModeTs           the privacy mode timestamp, or {@code null}
     * @param verifiedButtonsEnvelope whether the buttons envelope is present
     * @param verifiedListEnvelope    whether the list envelope is present
     * @param verifiedHsmEnvelope     whether the HSM envelope is present
     *
     * @implNote WAWebHandleMsgParser function v(): constructs the bizInfo
     * object with all parsed fields.
     */
    @WhatsAppWebExport(moduleName = "WAWebHandleMsgParser", exports = "incomingMsgParser",
            adaptation = WhatsAppAdaptation.DIRECT)
    public MessageReceiveBizInfo(
            byte[] verifiedNameCert,
            int verifiedNameSerial,
            String verifiedLevel,
            String nativeFlowName,
            String campaignId,
            Integer actualActors,
            Integer hostStorage,
            Integer privacyModeTs,
            boolean verifiedButtonsEnvelope,
            boolean verifiedListEnvelope,
            boolean verifiedHsmEnvelope
    ) {
        this.verifiedNameCert = verifiedNameCert;
        this.verifiedNameSerial = verifiedNameSerial;
        this.verifiedLevel = verifiedLevel;
        this.nativeFlowName = nativeFlowName;
        this.campaignId = campaignId;
        this.actualActors = actualActors;
        this.hostStorage = hostStorage;
        this.privacyModeTs = privacyModeTs;
        this.verifiedButtonsEnvelope = verifiedButtonsEnvelope;
        this.verifiedListEnvelope = verifiedListEnvelope;
        this.verifiedHsmEnvelope = verifiedHsmEnvelope;
    }

    /**
     * Returns the raw bytes of the verified-name certificate, when present.
     *
     * @return an {@link Optional} wrapping the certificate bytes
     * @implNote WAWebHandleMsgParser function v(): {@code verifiedNameCert}.
     */
    public Optional<byte[]> verifiedNameCert() {
        return Optional.ofNullable(verifiedNameCert);
    }

    /**
     * Returns the verified-name serial number, or {@code -1} if the
     * {@code verified_name} attribute was absent from the stanza.
     *
     * @return the serial number
     * @implNote WAWebHandleMsgParser function v(): {@code verifiedNameSerial}.
     */
    public int verifiedNameSerial() {
        return verifiedNameSerial;
    }

    /**
     * Returns the business verification tier from the
     * {@code verified_level} attribute, when present.
     *
     * @return an {@link Optional} wrapping the level identifier
     * @implNote WAWebHandleMsgParser function v(): {@code verifiedLevel}.
     */
    public Optional<String> verifiedLevel() {
        return Optional.ofNullable(verifiedLevel);
    }

    /**
     * Returns the business native flow name, when present.
     *
     * @return an {@link Optional} wrapping the native flow name
     * @implNote WAWebHandleMsgParser function v(): {@code nativeFlowName}.
     */
    public Optional<String> nativeFlowName() {
        return Optional.ofNullable(nativeFlowName);
    }

    /**
     * Returns the business campaign identifier, when present.
     *
     * @return an {@link Optional} wrapping the campaign identifier
     * @implNote WAWebHandleMsgParser function v(): {@code campaignId}.
     */
    public Optional<String> campaignId() {
        return Optional.ofNullable(campaignId);
    }

    /**
     * Returns the number of actors for business-hosted privacy mode.
     *
     * @return an {@link Optional} wrapping the actor count
     * @implNote WAWebHandleMsgParser function v(): {@code actualActors}.
     */
    public Optional<Integer> actualActors() {
        return Optional.ofNullable(actualActors);
    }

    /**
     * Returns the host storage indicator for business-hosted privacy
     * mode.
     *
     * @return an {@link Optional} wrapping the host storage value
     * @implNote WAWebHandleMsgParser function v(): {@code hostStorage}.
     */
    public Optional<Integer> hostStorage() {
        return Optional.ofNullable(hostStorage);
    }

    /**
     * Returns the timestamp at which the current privacy mode took
     * effect, when present.
     *
     * @return an {@link Optional} wrapping the privacy mode timestamp
     * @implNote WAWebHandleMsgParser function v(): {@code privacyModeTs}.
     */
    public Optional<Integer> privacyModeTs() {
        return Optional.ofNullable(privacyModeTs);
    }

    /**
     * Returns whether the message carries a verified buttons envelope.
     *
     * @return {@code true} if the buttons envelope is present
     * @implNote WAWebHandleMsgParser function v(): {@code verifiedButtonsEnvelope}.
     */
    public boolean verifiedButtonsEnvelope() {
        return verifiedButtonsEnvelope;
    }

    /**
     * Returns whether the message carries a verified list envelope.
     *
     * @return {@code true} if the list envelope is present
     * @implNote WAWebHandleMsgParser function v(): {@code verifiedListEnvelope}.
     */
    public boolean verifiedListEnvelope() {
        return verifiedListEnvelope;
    }

    /**
     * Returns whether the message carries a verified highly-structured
     * message envelope.
     *
     * @return {@code true} if the HSM envelope is present
     * @implNote WAWebHandleMsgParser function v(): {@code verifiedHsmEnvelope}.
     */
    public boolean verifiedHsmEnvelope() {
        return verifiedHsmEnvelope;
    }

    /**
     * Returns whether all three privacy mode fields are present,
     * indicating that the message participates in business-hosted
     * privacy mode.
     *
     * @return {@code true} if this is a business-hosted message with
     *         privacy mode metadata
     * @implNote Convenience predicate over {@code actualActors},
     * {@code hostStorage}, and {@code privacyModeTs}.
     */
    public boolean hasPrivacyMode() {
        return actualActors != null && hostStorage != null && privacyModeTs != null;
    }
}
