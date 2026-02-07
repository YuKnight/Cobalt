package com.github.auties00.cobalt.message.send;

import com.github.auties00.cobalt.client.WhatsAppClient;
import com.github.auties00.cobalt.device.DeviceService;
import com.github.auties00.cobalt.message.send.crypto.MessageEncryption;
import com.github.auties00.cobalt.message.send.crypto.MessageEncryptedPayload;
import com.github.auties00.cobalt.message.send.crypto.MessageSignalEncryptionType;
import com.github.auties00.cobalt.message.send.ack.AckParser;
import com.github.auties00.cobalt.message.send.ack.AckResult;
import com.github.auties00.cobalt.message.send.senderkey.SenderKeyDistribution;
import com.github.auties00.cobalt.message.send.stanza.MetaStanza;
import com.github.auties00.cobalt.message.send.stanza.ParticipantsStanza;
import com.github.auties00.cobalt.message.send.stanza.ReportingStanza;
import com.github.auties00.cobalt.model.info.ChatMessageInfo;
import com.github.auties00.cobalt.model.jid.Jid;
import com.github.auties00.cobalt.model.message.common.MessageContainer;
import com.github.auties00.cobalt.model.message.common.MessageContainerSpec;
import com.github.auties00.cobalt.model.message.server.ProtocolMessage;
import com.github.auties00.cobalt.model.privacy.PrivacySettingType;
import com.github.auties00.cobalt.node.Node;
import com.github.auties00.cobalt.node.NodeBuilder;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

/**
 * Sends status updates ({@code status@broadcast}).
 *
 * <p>Status messages use sender-key encryption, similar to group messages,
 * but are addressed to the status broadcast JID.  The audience is
 * determined by the user's status privacy list (contacts, allowlist,
 * or denylist).
 *
 * <p>The stanza includes a {@code <meta status_setting="...">} attribute
 * indicating the privacy setting.
 *
 * @apiNote WAWebEncryptAndSendStatusMsg.encryptAndSendStatusMsg: builds
 * the status stanza with sender-key encryption, SK distribution for
 * new devices, and optional status_setting meta attribute.
 */
final class StatusMessageSender extends MessageSender<ChatMessageInfo> {
    private static final System.Logger LOGGER = System.getLogger("StatusMessageSender");

    private final MessageEncryption encryption;
    private final DeviceService deviceService;
    private final SenderKeyDistribution senderKeyDistribution;
    private final MetaStanza metaStanza;
    private final ReportingStanza reportingStanza;

    StatusMessageSender(
            WhatsAppClient client,
            MessageEncryption encryption,
            DeviceService deviceService,
            SenderKeyDistribution senderKeyDistribution,
            MetaStanza metaStanza,
            ReportingStanza reportingStanza
    ) {
        super(client);
        this.encryption = Objects.requireNonNull(encryption, "encryption");
        this.deviceService = Objects.requireNonNull(deviceService, "deviceService");
        this.senderKeyDistribution = Objects.requireNonNull(senderKeyDistribution, "senderKeyDistribution");
        this.metaStanza = Objects.requireNonNull(metaStanza, "metaStanza");
        this.reportingStanza = Objects.requireNonNull(reportingStanza, "reportingStanza");
    }

    @Override
    AckResult send(Jid statusJid, ChatMessageInfo messageInfo) {
        waitForOfflineDelivery();
        var container = messageInfo.message();
        var selfJid = requireSelfJid();

        // WAWebEncryptAndSendStatusMsg: get status audience fanout
        // For revoke messages, narrow the audience to the original status recipients
        // WAWebEncryptAndSendStatusMsg: calculateRevokeSenderList uses the
        // original message's receipt records to determine who received it
        var fanout = deviceService.getGroupFanout(statusJid);
        var allDevices = narrowAudienceForRevoke(container, fanout.devices());

        // WAWebEncryptAndSendStatusMsg: split into SK distribution and existing
        var skDistribDevices = new ArrayList<Jid>();
        var skExistingDevices = new ArrayList<Jid>();
        for (var device : allDevices) {
            if (store.hasSenderKeyDistributed(statusJid, device)) {
                skExistingDevices.add(device);
            } else {
                skDistribDevices.add(device);
            }
        }

        // WAWebUserPrefsStatus.getStatusSkDistribList: rotate sender key
        // when viewers have been removed from the status audience
        var rotateKey = store.checkAndClearSenderKeyRotationNeeded(statusJid);
        if (rotateKey) {
            encryption.rotateSenderKey(statusJid, selfJid);
            skDistribDevices.addAll(skExistingDevices);
            skExistingDevices.clear();
        }

        // WAWebEncryptAndSendStatusMsg: encrypt with sender key
        // The status JID acts as the "group" for sender-key purposes
        var plaintext = MessageContainerSpec.encode(container);
        var skmsgPayload = encryption.encryptForGroup(statusJid, selfJid, plaintext);
        var senderKeyBytes = encryption.getSenderKeyBytes(statusJid, selfJid);

        // WAWebEncryptAndSendStatusMsg: encrypt SK distribution for new devices
        // WAWebGetGroupKeyDistributionMsg: populates ICDC per device
        var skDistPayloads = skDistribDevices.isEmpty()
                ? List.<MessageEncryptedPayload>of()
                : senderKeyDistribution.encrypt(statusJid, senderKeyBytes, skDistribDevices);

        // WAWebEncryptAndSendStatusMsg: build participants node
        // Contains both SK distribution <to> and existing device <to> nodes
        var participantsChildren = new ArrayList<Node>();
        for (var payload : skDistPayloads) {
            if (payload.recipientJid() == null) {
                continue;
            }
            // WAWebEncryptAndSendStatusMsg: SK distribution <to> with <enc>
            var encNode = new NodeBuilder()
                    .description("enc")
                    .attribute("v", String.valueOf(MessageEncryption.CIPHERTEXT_VERSION))
                    .attribute("type", payload.type().protocolValue())
                    .content(payload.ciphertext())
                    .build();
            participantsChildren.add(new NodeBuilder()
                    .description("to")
                    .attribute("jid", payload.recipientJid())
                    .content(encNode)
                    .build());
        }
        // WAWebEncryptAndSendStatusMsg: existing SK device <to> nodes (no enc)
        for (var device : skExistingDevices) {
            participantsChildren.add(new NodeBuilder()
                    .description("to")
                    .attribute("jid", device)
                    .build());
        }

        var participantsNode = participantsChildren.isEmpty() ? null : new NodeBuilder()
                .description("participants")
                .content(participantsChildren)
                .build();

        // WAWebEncryptAndSendStatusMsg: SKMSG <enc> node
        var skmsgEncNode = new NodeBuilder()
                .description("enc")
                .attribute("v", String.valueOf(MessageEncryption.CIPHERTEXT_VERSION))
                .attribute("type", MessageSignalEncryptionType.SKMSG.protocolValue())
                .content(skmsgPayload.ciphertext())
                .build();

        // WAWebEncryptAndSendStatusMsg: identity node when any pkmsg
        var identityNode = ParticipantsStanza.requiresIdentityNode(skDistPayloads)
                ? buildIdentityNode() : null;


        // WAWebEncryptAndSendStatusMsg: status_setting from user's
        // status privacy preference (contacts/allowlist/denylist)
        var statusSetting = resolveStatusSetting();
        var metaNode = metaStanza.build(statusJid, container, statusSetting);

        var reportingNode = reportingStanza.build(messageInfo, selfJid, statusJid);

        // WAWebEncryptAndSendStatusMsg: build the stanza
        var stanza = new NodeBuilder()
                .description("message")
                .attribute("id", messageInfo.key().id())
                .attribute("to", statusJid)
                .attribute("type", resolveStanzaType(container))
                .attribute("edit", resolveEditAttribute(container))
                .content(
                        participantsNode,
                        skmsgEncNode,
                        identityNode,
                        metaNode,
                        reportingNode
                );

        flushStore();
        var ackNode = client.sendNode(stanza);
        var ack = AckParser.parse(ackNode);

        // WAWebEncryptAndSendStatusMsg: mark SK as distributed on success
        if (ack.isSuccess()) {
            for (var device : skDistribDevices) {
                store.markSenderKeyDistributed(statusJid, device);
            }
        }

        return ack;
    }

    /**
     * Resolves the {@code status_setting} meta attribute from the user's
     * status privacy preference.
     *
     * @return {@code "contacts"}, {@code "allowlist"}, {@code "denylist"},
     *         or {@code null} if unavailable
     *
     * @apiNote WAWebEncryptAndSendStatusMsg: maps
     * {@code StatusPrivacySettingType.Contact → "contacts"},
     * {@code AllowList → "allowlist"}, {@code DenyList → "denylist"}.
     */
    private String resolveStatusSetting() {
        var entry = store.findPrivacySetting(PrivacySettingType.STATUS)
                .orElse(null);
        if (entry == null) {
            return null;
        }
        return switch (entry.value()) {
            case CONTACTS -> "contacts";
            case CONTACTS_ONLY -> "allowlist";
            case CONTACTS_EXCEPT -> "denylist";
            default -> null;
        };
    }

    /**
     * Narrows the audience for status revoke messages to only include
     * devices that received the original status.
     *
     * <p>When revoking a status, the revoke should only be sent to
     * recipients who actually received the original status, not the
     * full current audience list (which may have changed since).
     *
     * @param container  the message container
     * @param allDevices the full current audience device list
     * @return the narrowed device list for revokes, or the original
     *         list for non-revoke messages
     *
     * @apiNote WAWebEncryptAndSendStatusMsg: calls
     * calculateRevokeSenderList to determine the intersection of the
     * original recipients and the current audience.
     */
    private Collection<Jid> narrowAudienceForRevoke(
            MessageContainer container,
            Collection<Jid> allDevices
    ) {
        if (!(container.content() instanceof ProtocolMessage pm)
            || pm.protocolType() != ProtocolMessage.Type.REVOKE) {
            return allDevices;
        }

        // WAWebEncryptAndSendStatusMsg: calculateRevokeSenderList retrieves
        // the receipt records for the original status message to determine
        // which devices received it
        var originalKey = pm.key().orElse(null);
        if (originalKey == null) {
            return allDevices;
        }

        var originalRecipients = store.findReceiptRecords(originalKey.id());
        if (originalRecipients.isEmpty()) {
            // No receipt data available — fall back to full audience
            return allDevices;
        }

        // WAWebEncryptAndSendStatusMsg: intersection of original recipients
        // and current audience
        var narrowed = allDevices.stream()
                .filter(originalRecipients::contains)
                .toList();
        return narrowed.isEmpty() ? allDevices : narrowed;
    }
}
