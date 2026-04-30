package com.github.auties00.cobalt.message.send.icdc;

import com.github.auties00.cobalt.device.icdc.IcdcResult;
import com.github.auties00.cobalt.meta.annotation.WhatsAppWebExport;
import com.github.auties00.cobalt.meta.annotation.WhatsAppWebModule;
import com.github.auties00.cobalt.meta.model.WhatsAppAdaptation;
import com.github.auties00.cobalt.model.chat.ChatMessageContextInfoBuilder;
import com.github.auties00.cobalt.model.device.DeviceListMetadataBuilder;
import com.github.auties00.cobalt.model.message.MessageContainer;

/**
 * Populates ICDC (Identity Change Detection Consistency) metadata on outgoing
 * message containers so recipients can detect changes in the sender's or
 * recipient's device list since the last key exchange.
 */
@WhatsAppWebModule(moduleName = "WAWebE2EProtoGenerator")
@WhatsAppWebModule(moduleName = "WAWebICDCMetaApi")
public final class IcdcEnricher {
    /**
     * Prevents instantiation of this utility class.
     *
     * @throws UnsupportedOperationException always
     */
    private IcdcEnricher() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }

    /**
     * Returns a copy of the container with ICDC metadata merged into its
     * {@code messageContextInfo}.
     *
     * <p>Performs a spread-equivalent merge that preserves every existing field
     * on the container's {@code ChatMessageContextInfo} while adding or
     * replacing the {@code deviceListMetadata} and setting
     * {@code deviceListMetadataVersion} to {@code 2}.
     *
     * @param container     the original message container
     * @param senderIcdc    the sender's ICDC result, or {@code null}
     * @param recipientIcdc the recipient's ICDC result, or {@code null}
     * @return the enriched container, unchanged when both ICDC inputs are
     *         {@code null}
     */
    @WhatsAppWebExport(moduleName = "WAWebE2EProtoGenerator", exports = "populateMessageContextInfo",
            adaptation = WhatsAppAdaptation.DIRECT)
    @WhatsAppWebExport(moduleName = "WAWebICDCMetaApi", exports = "populateICDCMeta",
            adaptation = WhatsAppAdaptation.DIRECT)
    public static MessageContainer enrich(
            MessageContainer container,
            IcdcResult senderIcdc,
            IcdcResult recipientIcdc
    ) {
        if (senderIcdc == null && recipientIcdc == null) {
            return container;
        }

        var metadataBuilder = new DeviceListMetadataBuilder();
        if (senderIcdc != null) {
            senderIcdc.keyHash().ifPresent(metadataBuilder::senderKeyHash);
            senderIcdc.timestamp().ifPresent(metadataBuilder::senderTimestamp);
            metadataBuilder.senderKeyIndexes(senderIcdc.keyIndexes());
            senderIcdc.accountType().ifPresent(metadataBuilder::senderAccountType);
        }
        if (recipientIcdc != null) {
            recipientIcdc.keyHash().ifPresent(metadataBuilder::recipientKeyHash);
            recipientIcdc.timestamp().ifPresent(metadataBuilder::recipientTimestamp);
            metadataBuilder.recipientKeyIndexes(recipientIcdc.keyIndexes());
            recipientIcdc.accountType().ifPresent(metadataBuilder::receiverAccountType);
        }

        var existing = container.messageContextInfo().orElse(null);
        var infoBuilder = new ChatMessageContextInfoBuilder()
                .deviceListMetadata(metadataBuilder.build())
                .deviceListMetadataVersion(2);
        if (existing != null) {
            existing.messageSecret().ifPresent(infoBuilder::messageSecret);
            existing.paddingBytes().ifPresent(infoBuilder::paddingBytes);
            existing.botMessageSecret().ifPresent(infoBuilder::botMessageSecret);
            existing.botMetadata().ifPresent(infoBuilder::botMetadata);
            infoBuilder.capiCreatedGroup(existing.capiCreatedGroup());
            existing.supportPayload().ifPresent(infoBuilder::supportPayload);
            infoBuilder.threadId(existing.threadId());
            existing.messageAddOnExpiryType().ifPresent(infoBuilder::messageAddOnExpiryType);
            existing.messageAssociation().ifPresent(infoBuilder::messageAssociation);
            existing.limitSharing().ifPresent(infoBuilder::limitSharing);
            existing.limitSharingV2().ifPresent(infoBuilder::limitSharingV2);
            existing.weblinkRenderConfig().ifPresent(infoBuilder::weblinkRenderConfig);
            existing.reportingTokenVersion().ifPresent(version -> infoBuilder.reportingTokenVersion(version));
            existing.messageAddOnDurationInSecs().ifPresent(secs -> infoBuilder.messageAddOnDurationInSecs(secs));
        }

        return container.withMessageContextInfo(infoBuilder.build());
    }
}
