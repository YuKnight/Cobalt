package com.github.auties00.cobalt.message.receive.stanza;

import com.github.auties00.cobalt.meta.annotation.WhatsAppWebExport;
import com.github.auties00.cobalt.meta.annotation.WhatsAppWebModule;
import com.github.auties00.cobalt.meta.model.WhatsAppAdaptation;

import java.util.Optional;

/**
 * Holds the business-related metadata parsed from an incoming message stanza.
 *
 * <p>Combines data from the {@code <biz>} child node, the {@code verified_name}
 * attribute and child, and the {@code verified_level} attribute. Drives business
 * template rendering, verified-business badging, and privacy-mode behaviour for
 * BSP-hosted conversations.
 */
@WhatsAppWebModule(moduleName = "WAWebHandleMsgParser")
public final class MessageReceiveBizInfo {
    /**
     * Raw bytes of the {@code <verified_name>} child carrying the serialized verified
     * name certificate.
     */
    private final byte[] verifiedNameCert;

    /**
     * Serial number from the stanza's {@code verified_name} attribute, or {@code -1}
     * when the attribute is absent.
     */
    private final int verifiedNameSerial;

    /**
     * {@code verified_level} attribute identifying the business verification tier.
     */
    private final String verifiedLevel;

    /**
     * Native flow name identifying the type of business flow (for example
     * {@code "shops"}, {@code "appointment_booking"}).
     */
    private final String nativeFlowName;

    /**
     * Business campaign identifier from the {@code campaign_id} attribute.
     */
    private final String campaignId;

    /**
     * Number of actual actors involved in a business-hosted conversation (privacy mode
     * metadata).
     */
    private final Integer actualActors;

    /**
     * Host storage indicator for business-hosted conversations (privacy mode metadata).
     */
    private final Integer hostStorage;

    /**
     * Timestamp at which the current privacy mode took effect for the conversation.
     */
    private final Integer privacyModeTs;

    /**
     * Whether the message is wrapped in a verified buttons envelope.
     */
    private final boolean verifiedButtonsEnvelope;

    /**
     * Whether the message is wrapped in a verified list envelope.
     */
    private final boolean verifiedListEnvelope;

    /**
     * Whether the message is wrapped in a verified highly-structured-message envelope.
     */
    private final boolean verifiedHsmEnvelope;

    /**
     * Constructs a new business information record.
     *
     * @param verifiedNameCert        the verified-name certificate bytes, or {@code null}
     * @param verifiedNameSerial      the verified-name serial number, or {@code -1} if absent
     * @param verifiedLevel           the verification tier, or {@code null}
     * @param nativeFlowName          the native flow name, or {@code null}
     * @param campaignId              the campaign identifier, or {@code null}
     * @param actualActors            the actor count for privacy mode, or {@code null}
     * @param hostStorage             the host storage indicator, or {@code null}
     * @param privacyModeTs           the privacy mode timestamp, or {@code null}
     * @param verifiedButtonsEnvelope whether the buttons envelope is present
     * @param verifiedListEnvelope    whether the list envelope is present
     * @param verifiedHsmEnvelope     whether the HSM envelope is present
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
     * Returns the raw verified-name certificate bytes, when present.
     *
     * @return an {@link Optional} wrapping the certificate bytes
     */
    public Optional<byte[]> verifiedNameCert() {
        return Optional.ofNullable(verifiedNameCert);
    }

    /**
     * Returns the verified-name serial number, or {@code -1} if the
     * {@code verified_name} attribute was absent.
     *
     * @return the serial number
     */
    public int verifiedNameSerial() {
        return verifiedNameSerial;
    }

    /**
     * Returns the business verification tier, when present.
     *
     * @return an {@link Optional} wrapping the level identifier
     */
    public Optional<String> verifiedLevel() {
        return Optional.ofNullable(verifiedLevel);
    }

    /**
     * Returns the business native flow name, when present.
     *
     * @return an {@link Optional} wrapping the native flow name
     */
    public Optional<String> nativeFlowName() {
        return Optional.ofNullable(nativeFlowName);
    }

    /**
     * Returns the business campaign identifier, when present.
     *
     * @return an {@link Optional} wrapping the campaign identifier
     */
    public Optional<String> campaignId() {
        return Optional.ofNullable(campaignId);
    }

    /**
     * Returns the number of actors for business-hosted privacy mode.
     *
     * @return an {@link Optional} wrapping the actor count
     */
    public Optional<Integer> actualActors() {
        return Optional.ofNullable(actualActors);
    }

    /**
     * Returns the host storage indicator for business-hosted privacy mode.
     *
     * @return an {@link Optional} wrapping the host storage value
     */
    public Optional<Integer> hostStorage() {
        return Optional.ofNullable(hostStorage);
    }

    /**
     * Returns the timestamp at which the current privacy mode took effect.
     *
     * @return an {@link Optional} wrapping the privacy mode timestamp
     */
    public Optional<Integer> privacyModeTs() {
        return Optional.ofNullable(privacyModeTs);
    }

    /**
     * Returns whether the message carries a verified buttons envelope.
     *
     * @return {@code true} if present
     */
    public boolean verifiedButtonsEnvelope() {
        return verifiedButtonsEnvelope;
    }

    /**
     * Returns whether the message carries a verified list envelope.
     *
     * @return {@code true} if present
     */
    public boolean verifiedListEnvelope() {
        return verifiedListEnvelope;
    }

    /**
     * Returns whether the message carries a verified highly-structured message envelope.
     *
     * @return {@code true} if present
     */
    public boolean verifiedHsmEnvelope() {
        return verifiedHsmEnvelope;
    }

    /**
     * Returns whether all three privacy mode fields are present, indicating that the
     * message participates in business-hosted privacy mode.
     *
     * @return {@code true} if this is a business-hosted message with privacy mode
     *         metadata
     */
    public boolean hasPrivacyMode() {
        return actualActors != null && hostStorage != null && privacyModeTs != null;
    }
}
