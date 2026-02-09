package com.github.auties00.cobalt.model.message.common;

import com.alibaba.fastjson2.JSONObject;
import com.github.auties00.cobalt.model.button.template.highlyStructured.HighlyStructuredMessage;
import com.github.auties00.cobalt.model.info.DeviceContextInfo;
import com.github.auties00.cobalt.model.message.button.*;
import com.github.auties00.cobalt.model.message.payment.*;
import com.github.auties00.cobalt.model.message.server.*;
import com.github.auties00.cobalt.model.message.standard.*;
import com.github.auties00.cobalt.model.message.standard.AlbumMessage;
import com.github.auties00.cobalt.model.message.standard.BCallMessage;
import com.github.auties00.cobalt.model.message.standard.CallLogMessage;
import com.github.auties00.cobalt.model.message.standard.NewsletterFollowerInviteMessage;
import com.github.auties00.cobalt.model.message.standard.PlaceholderMessage;
import com.github.auties00.cobalt.model.message.standard.QuestionResponseMessage;
import com.github.auties00.cobalt.model.message.standard.ScheduledCallCreationMessage;
import com.github.auties00.cobalt.model.message.standard.ScheduledCallEditMessage;
import com.github.auties00.cobalt.model.message.standard.StatusNotificationMessage;
import com.github.auties00.cobalt.model.message.standard.StatusQuestionAnswerMessage;
import com.github.auties00.cobalt.model.message.standard.StatusQuotedMessage;
import com.github.auties00.cobalt.model.message.standard.StatusStickerInteractionMessage;
import com.github.auties00.cobalt.model.message.standard.StickerPackMessage;
import it.auties.protobuf.annotation.ProtobufMessage;
import it.auties.protobuf.annotation.ProtobufProperty;
import it.auties.protobuf.model.ProtobufType;

import java.util.Objects;
import java.util.Optional;

/**
 * A container for all types of messages known currently to WhatsappWeb.
 * <p>
 * Only one of these properties is populated usually, but it is possible to have multiple after a message retry for example
 * <p>
 * There are several categories of messages:
 * <ul>
 * <li>Server messages</li>
 * <li>Button messages</li>
 * <li>Product messages</li>
 * <li>Payment messages</li>
 * <li>Standard messages</li>
 * </ul>
 */
@ProtobufMessage(name = "Message")
public final class MessageContainer {
    private static final EmptyMessage EMPTY_MESSAGE = new EmptyMessage();

    @ProtobufProperty(index = 1, type = ProtobufType.STRING)
    final String textWithNoContextMessage;

    @ProtobufProperty(index = 2, type = ProtobufType.MESSAGE)
    final SenderKeyDistributionMessage senderKeyDistributionMessage;

    @ProtobufProperty(index = 3, type = ProtobufType.MESSAGE)
    final ImageMessage imageMessage;

    @ProtobufProperty(index = 4, type = ProtobufType.MESSAGE)
    final ContactMessage contactMessage;

    @ProtobufProperty(index = 5, type = ProtobufType.MESSAGE)
    final LocationMessage locationMessage;

    @ProtobufProperty(index = 6, type = ProtobufType.MESSAGE)
    final TextMessage textMessage;

    @ProtobufProperty(index = 7, type = ProtobufType.MESSAGE)
    final DocumentMessage documentMessage;

    @ProtobufProperty(index = 8, type = ProtobufType.MESSAGE)
    final AudioMessage audioMessage;

    @ProtobufProperty(index = 9, type = ProtobufType.MESSAGE)
    final VideoOrGifMessage videoMessage;

    @ProtobufProperty(index = 10, type = ProtobufType.MESSAGE)
    final CallMessage callMessage;

    @ProtobufProperty(index = 12, type = ProtobufType.MESSAGE)
    final ProtocolMessage protocolMessage;

    @ProtobufProperty(index = 13, type = ProtobufType.MESSAGE)
    final ContactsMessage contactsArrayMessage;

    @ProtobufProperty(index = 14, type = ProtobufType.MESSAGE)
    final HighlyStructuredMessage highlyStructuredMessage;

    @ProtobufProperty(index = 16, type = ProtobufType.MESSAGE)
    final SendPaymentMessage sendPaymentMessage;

    @ProtobufProperty(index = 18, type = ProtobufType.MESSAGE)
    final LiveLocationMessage liveLocationMessage;

    @ProtobufProperty(index = 22, type = ProtobufType.MESSAGE)
    final RequestPaymentMessage requestPaymentMessage;

    @ProtobufProperty(index = 23, type = ProtobufType.MESSAGE)
    final DeclinePaymentRequestMessage declinePaymentRequestMessage;

    @ProtobufProperty(index = 24, type = ProtobufType.MESSAGE)
    final CancelPaymentRequestMessage cancelPaymentRequestMessage;

    @ProtobufProperty(index = 25, type = ProtobufType.MESSAGE)
    final TemplateMessage templateMessage;

    @ProtobufProperty(index = 26, type = ProtobufType.MESSAGE)
    final StickerMessage stickerMessage;

    @ProtobufProperty(index = 28, type = ProtobufType.MESSAGE)
    final GroupInviteMessage groupInviteMessage;

    @ProtobufProperty(index = 29, type = ProtobufType.MESSAGE)
    final TemplateReplyMessage templateReplyMessage;

    @ProtobufProperty(index = 30, type = ProtobufType.MESSAGE)
    final ProductMessage productMessage;

    @ProtobufProperty(index = 31, type = ProtobufType.MESSAGE)
    final DeviceSentMessage deviceSentMessage;

    @ProtobufProperty(index = 32, type = ProtobufType.MESSAGE)
    final DeviceSyncMessage deviceSyncMessage;

    @ProtobufProperty(index = 36, type = ProtobufType.MESSAGE)
    final ListMessage listMessage;

    @ProtobufProperty(index = 37, type = ProtobufType.MESSAGE)
    final FutureMessageContainer viewOnceMessage;

    @ProtobufProperty(index = 38, type = ProtobufType.MESSAGE)
    final PaymentOrderMessage orderMessage;

    @ProtobufProperty(index = 39, type = ProtobufType.MESSAGE)
    final ListResponseMessage listResponseMessage;

    @ProtobufProperty(index = 40, type = ProtobufType.MESSAGE)
    final FutureMessageContainer ephemeralMessage;

    @ProtobufProperty(index = 41, type = ProtobufType.MESSAGE)
    final PaymentInvoiceMessage invoiceMessage;

    @ProtobufProperty(index = 42, type = ProtobufType.MESSAGE)
    final ButtonsMessage buttonsMessage;

    @ProtobufProperty(index = 43, type = ProtobufType.MESSAGE)
    final ButtonsResponseMessage buttonsResponseMessage;

    @ProtobufProperty(index = 44, type = ProtobufType.MESSAGE)
    final PaymentInviteMessage paymentInviteMessage;

    @ProtobufProperty(index = 45, type = ProtobufType.MESSAGE)
    final InteractiveMessage interactiveMessage;

    @ProtobufProperty(index = 46, type = ProtobufType.MESSAGE)
    final ReactionMessage reactionMessage;

    @ProtobufProperty(index = 47, type = ProtobufType.MESSAGE)
    final StickerSyncRMRMessage stickerSyncMessage;

    @ProtobufProperty(index = 48, type = ProtobufType.MESSAGE)
    final InteractiveResponseMessage interactiveResponseMessage;

    @ProtobufProperty(index = 49, type = ProtobufType.MESSAGE)
    final PollCreationMessage pollCreationMessage;

    @ProtobufProperty(index = 50, type = ProtobufType.MESSAGE)
    final PollUpdateMessage pollUpdateMessage;

    @ProtobufProperty(index = 51, type = ProtobufType.MESSAGE)
    final KeepInChatMessage keepInChatMessage;

    @ProtobufProperty(index = 53, type = ProtobufType.MESSAGE)
    final FutureMessageContainer documentWithCaptionMessage;

    @ProtobufProperty(index = 54, type = ProtobufType.MESSAGE)
    final RequestPhoneNumberMessage requestPhoneNumberMessage;

    @ProtobufProperty(index = 55, type = ProtobufType.MESSAGE)
    final FutureMessageContainer viewOnceV2Message;

    @ProtobufProperty(index = 56, type = ProtobufType.MESSAGE)
    final EncryptedReactionMessage encryptedReactionMessage;

    @ProtobufProperty(index = 58, type = ProtobufType.MESSAGE)
    final FutureMessageContainer editedMessage;

    @ProtobufProperty(index = 59, type = ProtobufType.MESSAGE)
    final FutureMessageContainer viewOnceV2ExtensionMessage;

    @ProtobufProperty(index = 78, type = ProtobufType.MESSAGE)
    final NewsletterAdminInviteMessage newsletterAdminInviteMessage;

    @ProtobufProperty(index = 63, type = ProtobufType.MESSAGE)
    final PinInChatMessage pinInChatMessage;

    @ProtobufProperty(index = 76, type = ProtobufType.MESSAGE)
    final EncryptedEventResponseMessage encEventResponseMessage;

    @ProtobufProperty(index = 75, type = ProtobufType.MESSAGE)
    final EventMessage eventMessage;

    @ProtobufProperty(index = 82, type = ProtobufType.MESSAGE)
    final SecretEncryptedMessage secretEncryptedMessage;

    @ProtobufProperty(index = 88, type = ProtobufType.MESSAGE)
    final PollResultSnapshotMessage pollResultSnapshotMessage;

    @ProtobufProperty(index = 115, type = ProtobufType.MESSAGE)
    final PollResultSnapshotMessage pollResultSnapshotMessageV3;

    @ProtobufProperty(index = 67, type = ProtobufType.MESSAGE)
    final FutureMessageContainer botInvokeMessage;

    @ProtobufProperty(index = 35, type = ProtobufType.MESSAGE)
    final DeviceContextInfo deviceInfo;

    @ProtobufProperty(index = 15, type = ProtobufType.MESSAGE)
    final SenderKeyDistributionMessage fastRatchetKeySenderKeyDistributionMessage;

    @ProtobufProperty(index = 60, type = ProtobufType.MESSAGE)
    final PollCreationMessage pollCreationMessageV2;

    @ProtobufProperty(index = 61, type = ProtobufType.MESSAGE)
    final ScheduledCallCreationMessage scheduledCallCreationMessage;

    @ProtobufProperty(index = 62, type = ProtobufType.MESSAGE)
    final FutureMessageContainer groupMentionedMessage;

    @ProtobufProperty(index = 64, type = ProtobufType.MESSAGE)
    final PollCreationMessage pollCreationMessageV3;

    @ProtobufProperty(index = 65, type = ProtobufType.MESSAGE)
    final ScheduledCallEditMessage scheduledCallEditMessage;

    @ProtobufProperty(index = 66, type = ProtobufType.MESSAGE)
    final VideoOrGifMessage ptvMessage;

    @ProtobufProperty(index = 69, type = ProtobufType.MESSAGE)
    final CallLogMessage callLogMessage;

    @ProtobufProperty(index = 70, type = ProtobufType.MESSAGE)
    final MessageHistoryBundle messageHistoryBundle;

    @ProtobufProperty(index = 71, type = ProtobufType.MESSAGE)
    final EncryptedCommentMessage encCommentMessage;

    @ProtobufProperty(index = 72, type = ProtobufType.MESSAGE)
    final BCallMessage bcallMessage;

    @ProtobufProperty(index = 74, type = ProtobufType.MESSAGE)
    final FutureMessageContainer lottieStickerMessage;

    @ProtobufProperty(index = 77, type = ProtobufType.MESSAGE)
    final CommentMessage commentMessage;

    @ProtobufProperty(index = 80, type = ProtobufType.MESSAGE)
    final PlaceholderMessage placeholderMessage;

    @ProtobufProperty(index = 83, type = ProtobufType.MESSAGE)
    final AlbumMessage albumMessage;

    @ProtobufProperty(index = 85, type = ProtobufType.MESSAGE)
    final FutureMessageContainer eventCoverImage;

    @ProtobufProperty(index = 86, type = ProtobufType.MESSAGE)
    final StickerPackMessage stickerPackMessage;

    @ProtobufProperty(index = 87, type = ProtobufType.MESSAGE)
    final FutureMessageContainer statusMentionMessage;

    @ProtobufProperty(index = 90, type = ProtobufType.MESSAGE)
    final FutureMessageContainer pollCreationOptionImageMessage;

    @ProtobufProperty(index = 91, type = ProtobufType.MESSAGE)
    final FutureMessageContainer associatedChildMessage;

    @ProtobufProperty(index = 92, type = ProtobufType.MESSAGE)
    final FutureMessageContainer groupStatusMentionMessage;

    @ProtobufProperty(index = 93, type = ProtobufType.MESSAGE)
    final FutureMessageContainer pollCreationMessageV4;

    @ProtobufProperty(index = 95, type = ProtobufType.MESSAGE)
    final FutureMessageContainer statusAddYours;

    @ProtobufProperty(index = 96, type = ProtobufType.MESSAGE)
    final FutureMessageContainer groupStatusMessage;

    @ProtobufProperty(index = 98, type = ProtobufType.MESSAGE)
    final StatusNotificationMessage statusNotificationMessage;

    @ProtobufProperty(index = 99, type = ProtobufType.MESSAGE)
    final FutureMessageContainer limitSharingMessage;

    @ProtobufProperty(index = 100, type = ProtobufType.MESSAGE)
    final FutureMessageContainer botTaskMessage;

    @ProtobufProperty(index = 101, type = ProtobufType.MESSAGE)
    final FutureMessageContainer questionMessage;

    @ProtobufProperty(index = 102, type = ProtobufType.MESSAGE)
    final MessageHistoryNotice messageHistoryNotice;

    @ProtobufProperty(index = 103, type = ProtobufType.MESSAGE)
    final FutureMessageContainer groupStatusMessageV2;

    @ProtobufProperty(index = 104, type = ProtobufType.MESSAGE)
    final FutureMessageContainer botForwardedMessage;

    @ProtobufProperty(index = 105, type = ProtobufType.MESSAGE)
    final StatusQuestionAnswerMessage statusQuestionAnswerMessage;

    @ProtobufProperty(index = 106, type = ProtobufType.MESSAGE)
    final FutureMessageContainer questionReplyMessage;

    @ProtobufProperty(index = 107, type = ProtobufType.MESSAGE)
    final QuestionResponseMessage questionResponseMessage;

    @ProtobufProperty(index = 109, type = ProtobufType.MESSAGE)
    final StatusQuotedMessage statusQuotedMessage;

    @ProtobufProperty(index = 110, type = ProtobufType.MESSAGE)
    final StatusStickerInteractionMessage statusStickerInteractionMessage;

    @ProtobufProperty(index = 111, type = ProtobufType.MESSAGE)
    final PollCreationMessage pollCreationMessageV5;

    @ProtobufProperty(index = 113, type = ProtobufType.MESSAGE)
    final NewsletterFollowerInviteMessage newsletterFollowerInviteMessageV2;

    @ProtobufProperty(index = 116, type = ProtobufType.MESSAGE)
    final FutureMessageContainer newsletterAdminProfileMessage;

    @ProtobufProperty(index = 117, type = ProtobufType.MESSAGE)
    final FutureMessageContainer newsletterAdminProfileMessageV2;

    MessageContainer(String textWithNoContextMessage, SenderKeyDistributionMessage senderKeyDistributionMessage, ImageMessage imageMessage, ContactMessage contactMessage, LocationMessage locationMessage, TextMessage textMessage, DocumentMessage documentMessage, AudioMessage audioMessage, VideoOrGifMessage videoMessage, CallMessage callMessage, ProtocolMessage protocolMessage, ContactsMessage contactsArrayMessage, HighlyStructuredMessage highlyStructuredMessage, SendPaymentMessage sendPaymentMessage, LiveLocationMessage liveLocationMessage, RequestPaymentMessage requestPaymentMessage, DeclinePaymentRequestMessage declinePaymentRequestMessage, CancelPaymentRequestMessage cancelPaymentRequestMessage, TemplateMessage templateMessage, StickerMessage stickerMessage, GroupInviteMessage groupInviteMessage, TemplateReplyMessage templateReplyMessage, ProductMessage productMessage, DeviceSentMessage deviceSentMessage, DeviceSyncMessage deviceSyncMessage, ListMessage listMessage, FutureMessageContainer viewOnceMessage, PaymentOrderMessage orderMessage, ListResponseMessage listResponseMessage, FutureMessageContainer ephemeralMessage, PaymentInvoiceMessage invoiceMessage, ButtonsMessage buttonsMessage, ButtonsResponseMessage buttonsResponseMessage, PaymentInviteMessage paymentInviteMessage, InteractiveMessage interactiveMessage, ReactionMessage reactionMessage, StickerSyncRMRMessage stickerSyncMessage, InteractiveResponseMessage interactiveResponseMessage, PollCreationMessage pollCreationMessage, PollUpdateMessage pollUpdateMessage, KeepInChatMessage keepInChatMessage, FutureMessageContainer documentWithCaptionMessage, RequestPhoneNumberMessage requestPhoneNumberMessage, FutureMessageContainer viewOnceV2Message, EncryptedReactionMessage encryptedReactionMessage, FutureMessageContainer editedMessage, FutureMessageContainer viewOnceV2ExtensionMessage, NewsletterAdminInviteMessage newsletterAdminInviteMessage, PinInChatMessage pinInChatMessage, EncryptedEventResponseMessage encEventResponseMessage, EventMessage eventMessage, SecretEncryptedMessage secretEncryptedMessage, PollResultSnapshotMessage pollResultSnapshotMessage, PollResultSnapshotMessage pollResultSnapshotMessageV3, FutureMessageContainer botInvokeMessage, DeviceContextInfo deviceInfo, SenderKeyDistributionMessage fastRatchetKeySenderKeyDistributionMessage, PollCreationMessage pollCreationMessageV2, ScheduledCallCreationMessage scheduledCallCreationMessage, FutureMessageContainer groupMentionedMessage, PollCreationMessage pollCreationMessageV3, ScheduledCallEditMessage scheduledCallEditMessage, VideoOrGifMessage ptvMessage, CallLogMessage callLogMessage, MessageHistoryBundle messageHistoryBundle, EncryptedCommentMessage encCommentMessage, BCallMessage bcallMessage, FutureMessageContainer lottieStickerMessage, CommentMessage commentMessage, PlaceholderMessage placeholderMessage, AlbumMessage albumMessage, FutureMessageContainer eventCoverImage, StickerPackMessage stickerPackMessage, FutureMessageContainer statusMentionMessage, FutureMessageContainer pollCreationOptionImageMessage, FutureMessageContainer associatedChildMessage, FutureMessageContainer groupStatusMentionMessage, FutureMessageContainer pollCreationMessageV4, FutureMessageContainer statusAddYours, FutureMessageContainer groupStatusMessage, StatusNotificationMessage statusNotificationMessage, FutureMessageContainer limitSharingMessage, FutureMessageContainer botTaskMessage, FutureMessageContainer questionMessage, MessageHistoryNotice messageHistoryNotice, FutureMessageContainer groupStatusMessageV2, FutureMessageContainer botForwardedMessage, StatusQuestionAnswerMessage statusQuestionAnswerMessage, FutureMessageContainer questionReplyMessage, QuestionResponseMessage questionResponseMessage, StatusQuotedMessage statusQuotedMessage, StatusStickerInteractionMessage statusStickerInteractionMessage, PollCreationMessage pollCreationMessageV5, NewsletterFollowerInviteMessage newsletterFollowerInviteMessageV2, FutureMessageContainer newsletterAdminProfileMessage, FutureMessageContainer newsletterAdminProfileMessageV2) {
        this.textWithNoContextMessage = textWithNoContextMessage;
        this.senderKeyDistributionMessage = senderKeyDistributionMessage;
        this.imageMessage = imageMessage;
        this.contactMessage = contactMessage;
        this.locationMessage = locationMessage;
        this.textMessage = textMessage;
        this.documentMessage = documentMessage;
        this.audioMessage = audioMessage;
        this.videoMessage = videoMessage;
        this.callMessage = callMessage;
        this.protocolMessage = protocolMessage;
        this.contactsArrayMessage = contactsArrayMessage;
        this.highlyStructuredMessage = highlyStructuredMessage;
        this.sendPaymentMessage = sendPaymentMessage;
        this.liveLocationMessage = liveLocationMessage;
        this.requestPaymentMessage = requestPaymentMessage;
        this.declinePaymentRequestMessage = declinePaymentRequestMessage;
        this.cancelPaymentRequestMessage = cancelPaymentRequestMessage;
        this.templateMessage = templateMessage;
        this.stickerMessage = stickerMessage;
        this.groupInviteMessage = groupInviteMessage;
        this.templateReplyMessage = templateReplyMessage;
        this.productMessage = productMessage;
        this.deviceSentMessage = deviceSentMessage;
        this.deviceSyncMessage = deviceSyncMessage;
        this.listMessage = listMessage;
        this.viewOnceMessage = viewOnceMessage;
        this.orderMessage = orderMessage;
        this.listResponseMessage = listResponseMessage;
        this.ephemeralMessage = ephemeralMessage;
        this.invoiceMessage = invoiceMessage;
        this.buttonsMessage = buttonsMessage;
        this.buttonsResponseMessage = buttonsResponseMessage;
        this.paymentInviteMessage = paymentInviteMessage;
        this.interactiveMessage = interactiveMessage;
        this.reactionMessage = reactionMessage;
        this.stickerSyncMessage = stickerSyncMessage;
        this.interactiveResponseMessage = interactiveResponseMessage;
        this.pollCreationMessage = pollCreationMessage;
        this.pollUpdateMessage = pollUpdateMessage;
        this.keepInChatMessage = keepInChatMessage;
        this.documentWithCaptionMessage = documentWithCaptionMessage;
        this.requestPhoneNumberMessage = requestPhoneNumberMessage;
        this.viewOnceV2Message = viewOnceV2Message;
        this.encryptedReactionMessage = encryptedReactionMessage;
        this.editedMessage = editedMessage;
        this.viewOnceV2ExtensionMessage = viewOnceV2ExtensionMessage;
        this.newsletterAdminInviteMessage = newsletterAdminInviteMessage;
        this.pinInChatMessage = pinInChatMessage;
        this.encEventResponseMessage = encEventResponseMessage;
        this.eventMessage = eventMessage;
        this.secretEncryptedMessage = secretEncryptedMessage;
        this.pollResultSnapshotMessage = pollResultSnapshotMessage;
        this.pollResultSnapshotMessageV3 = pollResultSnapshotMessageV3;
        this.botInvokeMessage = botInvokeMessage;
        this.deviceInfo = deviceInfo;
        this.fastRatchetKeySenderKeyDistributionMessage = fastRatchetKeySenderKeyDistributionMessage;
        this.pollCreationMessageV2 = pollCreationMessageV2;
        this.scheduledCallCreationMessage = scheduledCallCreationMessage;
        this.groupMentionedMessage = groupMentionedMessage;
        this.pollCreationMessageV3 = pollCreationMessageV3;
        this.scheduledCallEditMessage = scheduledCallEditMessage;
        this.ptvMessage = ptvMessage;
        this.callLogMessage = callLogMessage;
        this.messageHistoryBundle = messageHistoryBundle;
        this.encCommentMessage = encCommentMessage;
        this.bcallMessage = bcallMessage;
        this.lottieStickerMessage = lottieStickerMessage;
        this.commentMessage = commentMessage;
        this.placeholderMessage = placeholderMessage;
        this.albumMessage = albumMessage;
        this.eventCoverImage = eventCoverImage;
        this.stickerPackMessage = stickerPackMessage;
        this.statusMentionMessage = statusMentionMessage;
        this.pollCreationOptionImageMessage = pollCreationOptionImageMessage;
        this.associatedChildMessage = associatedChildMessage;
        this.groupStatusMentionMessage = groupStatusMentionMessage;
        this.pollCreationMessageV4 = pollCreationMessageV4;
        this.statusAddYours = statusAddYours;
        this.groupStatusMessage = groupStatusMessage;
        this.statusNotificationMessage = statusNotificationMessage;
        this.limitSharingMessage = limitSharingMessage;
        this.botTaskMessage = botTaskMessage;
        this.questionMessage = questionMessage;
        this.messageHistoryNotice = messageHistoryNotice;
        this.groupStatusMessageV2 = groupStatusMessageV2;
        this.botForwardedMessage = botForwardedMessage;
        this.statusQuestionAnswerMessage = statusQuestionAnswerMessage;
        this.questionReplyMessage = questionReplyMessage;
        this.questionResponseMessage = questionResponseMessage;
        this.statusQuotedMessage = statusQuotedMessage;
        this.statusStickerInteractionMessage = statusStickerInteractionMessage;
        this.pollCreationMessageV5 = pollCreationMessageV5;
        this.newsletterFollowerInviteMessageV2 = newsletterFollowerInviteMessageV2;
        this.newsletterAdminProfileMessage = newsletterAdminProfileMessage;
        this.newsletterAdminProfileMessageV2 = newsletterAdminProfileMessageV2;
    }

    @Override
    public boolean equals(Object o) {
        return o instanceof MessageContainer that
                && Objects.equals(textWithNoContextMessage, that.textWithNoContextMessage)
                && Objects.equals(senderKeyDistributionMessage, that.senderKeyDistributionMessage)
                && Objects.equals(imageMessage, that.imageMessage)
                && Objects.equals(contactMessage, that.contactMessage)
                && Objects.equals(locationMessage, that.locationMessage)
                && Objects.equals(textMessage, that.textMessage)
                && Objects.equals(documentMessage, that.documentMessage)
                && Objects.equals(audioMessage, that.audioMessage)
                && Objects.equals(videoMessage, that.videoMessage)
                && Objects.equals(callMessage, that.callMessage)
                && Objects.equals(protocolMessage, that.protocolMessage)
                && Objects.equals(contactsArrayMessage, that.contactsArrayMessage)
                && Objects.equals(highlyStructuredMessage, that.highlyStructuredMessage)
                && Objects.equals(sendPaymentMessage, that.sendPaymentMessage)
                && Objects.equals(liveLocationMessage, that.liveLocationMessage)
                && Objects.equals(requestPaymentMessage, that.requestPaymentMessage)
                && Objects.equals(declinePaymentRequestMessage, that.declinePaymentRequestMessage)
                && Objects.equals(cancelPaymentRequestMessage, that.cancelPaymentRequestMessage)
                && Objects.equals(templateMessage, that.templateMessage)
                && Objects.equals(stickerMessage, that.stickerMessage)
                && Objects.equals(groupInviteMessage, that.groupInviteMessage)
                && Objects.equals(templateReplyMessage, that.templateReplyMessage)
                && Objects.equals(productMessage, that.productMessage)
                && Objects.equals(deviceSentMessage, that.deviceSentMessage)
                && Objects.equals(deviceSyncMessage, that.deviceSyncMessage)
                && Objects.equals(listMessage, that.listMessage)
                && Objects.equals(viewOnceMessage, that.viewOnceMessage)
                && Objects.equals(orderMessage, that.orderMessage)
                && Objects.equals(listResponseMessage, that.listResponseMessage)
                && Objects.equals(ephemeralMessage, that.ephemeralMessage)
                && Objects.equals(invoiceMessage, that.invoiceMessage)
                && Objects.equals(buttonsMessage, that.buttonsMessage)
                && Objects.equals(buttonsResponseMessage, that.buttonsResponseMessage)
                && Objects.equals(paymentInviteMessage, that.paymentInviteMessage)
                && Objects.equals(interactiveMessage, that.interactiveMessage)
                && Objects.equals(reactionMessage, that.reactionMessage)
                && Objects.equals(stickerSyncMessage, that.stickerSyncMessage)
                && Objects.equals(interactiveResponseMessage, that.interactiveResponseMessage)
                && Objects.equals(pollCreationMessage, that.pollCreationMessage)
                && Objects.equals(pollUpdateMessage, that.pollUpdateMessage)
                && Objects.equals(keepInChatMessage, that.keepInChatMessage)
                && Objects.equals(documentWithCaptionMessage, that.documentWithCaptionMessage)
                && Objects.equals(requestPhoneNumberMessage, that.requestPhoneNumberMessage)
                && Objects.equals(viewOnceV2Message, that.viewOnceV2Message)
                && Objects.equals(encryptedReactionMessage, that.encryptedReactionMessage)
                && Objects.equals(editedMessage, that.editedMessage)
                && Objects.equals(viewOnceV2ExtensionMessage, that.viewOnceV2ExtensionMessage)
                && Objects.equals(newsletterAdminInviteMessage, that.newsletterAdminInviteMessage)
                && Objects.equals(pinInChatMessage, that.pinInChatMessage)
                && Objects.equals(encEventResponseMessage, that.encEventResponseMessage)
                && Objects.equals(eventMessage, that.eventMessage)
                && Objects.equals(secretEncryptedMessage, that.secretEncryptedMessage)
                && Objects.equals(pollResultSnapshotMessage, that.pollResultSnapshotMessage)
                && Objects.equals(pollResultSnapshotMessageV3, that.pollResultSnapshotMessageV3)
                && Objects.equals(botInvokeMessage, that.botInvokeMessage)
                && Objects.equals(deviceInfo, that.deviceInfo);
    }

    @Override
    public int hashCode() {
        return Objects.hash(
                textWithNoContextMessage,
                senderKeyDistributionMessage,
                imageMessage,
                contactMessage,
                locationMessage,
                textMessage,
                documentMessage,
                audioMessage,
                videoMessage,
                callMessage,
                protocolMessage,
                contactsArrayMessage,
                highlyStructuredMessage,
                sendPaymentMessage,
                liveLocationMessage,
                requestPaymentMessage,
                declinePaymentRequestMessage,
                cancelPaymentRequestMessage,
                templateMessage,
                stickerMessage,
                groupInviteMessage,
                templateReplyMessage,
                productMessage,
                deviceSentMessage,
                deviceSyncMessage,
                listMessage,
                viewOnceMessage,
                orderMessage,
                listResponseMessage,
                ephemeralMessage,
                invoiceMessage,
                buttonsMessage,
                buttonsResponseMessage,
                paymentInviteMessage,
                interactiveMessage,
                reactionMessage,
                stickerSyncMessage,
                interactiveResponseMessage,
                pollCreationMessage,
                pollUpdateMessage,
                keepInChatMessage,
                documentWithCaptionMessage,
                requestPhoneNumberMessage,
                viewOnceV2Message,
                encryptedReactionMessage,
                editedMessage,
                viewOnceV2ExtensionMessage,
                newsletterAdminInviteMessage,
                pinInChatMessage,
                encEventResponseMessage,
                eventMessage,
                secretEncryptedMessage,
                pollResultSnapshotMessage,
                pollResultSnapshotMessageV3,
                botInvokeMessage,
                deviceInfo
        );
    }

    /**
     * Returns an empty message container
     *
     * @return a non-null container
     */
    public static MessageContainer empty() {
        return new MessageContainerBuilder().build();
    }

    public static Optional<MessageContainer> ofJson(JSONObject jsonObject) {
        // TODO: Implement me
        return Optional.empty();
    }

    /**
     * Constructs a new MessageContainer from a message of any type
     *
     * @param message the message that the new container should wrap
     * @param <T>     the type of the message
     * @return a non-null container
     */
    public static <T extends Message> MessageContainer of(T message) {
        return ofBuilder(message).build();
    }

    /**
     * Constructs a new MessageContainerBuilder from a message of any type
     *
     * @param message the message that the new container should wrap
     * @param <T>     the type of the message
     * @return a non-null builder
     */
    public static <T extends Message> MessageContainerBuilder ofBuilder(T message) {
        var builder = new MessageContainerBuilder();
        switch (message) {
            case SenderKeyDistributionMessage senderKeyDistribution ->
                    builder.senderKeyDistributionMessage(senderKeyDistribution);
            case ImageMessage image -> builder.imageMessage(image);
            case ContactMessage contact -> builder.contactMessage(contact);
            case LocationMessage location -> builder.locationMessage(location);
            case TextMessage text -> builder.textMessage(text);
            case DocumentMessage document -> builder.documentMessage(document);
            case AudioMessage audio -> builder.audioMessage(audio);
            case VideoOrGifMessage video -> builder.videoMessage(video);
            case ProtocolMessage protocol -> builder.protocolMessage(protocol);
            case ContactsMessage contactsArray -> builder.contactsArrayMessage(contactsArray);
            case HighlyStructuredMessage highlyStructured ->
                    builder.highlyStructuredMessage(highlyStructured);
            case SendPaymentMessage sendPayment -> builder.sendPaymentMessage(sendPayment);
            case LiveLocationMessage liveLocation -> builder.liveLocationMessage(liveLocation);
            case RequestPaymentMessage requestPayment -> builder.requestPaymentMessage(requestPayment);
            case DeclinePaymentRequestMessage declinePaymentRequest ->
                    builder.declinePaymentRequestMessage(declinePaymentRequest);
            case CancelPaymentRequestMessage cancelPaymentRequest ->
                    builder.cancelPaymentRequestMessage(cancelPaymentRequest);
            case TemplateMessage template -> builder.templateMessage(template);
            case StickerMessage sticker -> builder.stickerMessage(sticker);
            case GroupInviteMessage groupInvite -> builder.groupInviteMessage(groupInvite);
            case TemplateReplyMessage templateButtonReply ->
                    builder.templateReplyMessage(templateButtonReply);
            case ProductMessage product -> builder.productMessage(product);
            case DeviceSyncMessage deviceSync -> builder.deviceSyncMessage(deviceSync);
            case ListMessage buttonsList -> builder.listMessage(buttonsList);
            case PaymentOrderMessage order -> builder.orderMessage(order);
            case ListResponseMessage listResponse -> builder.listResponseMessage(listResponse);
            case PaymentInvoiceMessage invoice -> builder.invoiceMessage(invoice);
            case ButtonsMessage buttons -> builder.buttonsMessage(buttons);
            case ButtonsResponseMessage buttonsResponse -> builder.buttonsResponseMessage(buttonsResponse);
            case PaymentInviteMessage paymentInvite -> builder.paymentInviteMessage(paymentInvite);
            case InteractiveMessage interactive -> builder.interactiveMessage(interactive);
            case ReactionMessage reaction -> builder.reactionMessage(reaction);
            case StickerSyncRMRMessage stickerSync -> builder.stickerSyncMessage(stickerSync);
            case DeviceSentMessage deviceSent -> builder.deviceSentMessage(deviceSent);
            case InteractiveResponseMessage interactiveResponseMessage ->
                    builder.interactiveResponseMessage(interactiveResponseMessage);
            case PollCreationMessage pollCreationMessage ->
                    builder.pollCreationMessage(pollCreationMessage);
            case PollUpdateMessage pollUpdateMessage -> builder.pollUpdateMessage(pollUpdateMessage);
            case KeepInChatMessage keepInChatMessage -> builder.keepInChatMessage(keepInChatMessage);
            case RequestPhoneNumberMessage requestPhoneNumberMessage ->
                    builder.requestPhoneNumberMessage(requestPhoneNumberMessage);
            case EncryptedReactionMessage encReactionMessage ->
                    builder.encryptedReactionMessage(encReactionMessage);
            case CallMessage callMessage -> builder.callMessage(callMessage);
            case NewsletterAdminInviteMessage newsletterAdminInviteMessage ->
                    builder.newsletterAdminInviteMessage(newsletterAdminInviteMessage);
            case PinInChatMessage pinInChatMessage ->
                    builder.pinInChatMessage(pinInChatMessage);
            case EncryptedEventResponseMessage encEventResponseMessage ->
                    builder.encEventResponseMessage(encEventResponseMessage);
            case EventMessage eventMessage ->
                    builder.eventMessage(eventMessage);
            case SecretEncryptedMessage secretEncryptedMessage ->
                    builder.secretEncryptedMessage(secretEncryptedMessage);
            case PollResultSnapshotMessage pollResultSnapshotMessage ->
                    builder.pollResultSnapshotMessage(pollResultSnapshotMessage);
            case CommentMessage commentMessage -> builder.commentMessage(commentMessage);
            case EncryptedCommentMessage encCommentMessage -> builder.encCommentMessage(encCommentMessage);
            case ScheduledCallCreationMessage scheduledCallCreationMessage ->
                    builder.scheduledCallCreationMessage(scheduledCallCreationMessage);
            case ScheduledCallEditMessage scheduledCallEditMessage ->
                    builder.scheduledCallEditMessage(scheduledCallEditMessage);
            case CallLogMessage callLogMessage -> builder.callLogMessage(callLogMessage);
            case BCallMessage bcallMessage -> builder.bcallMessage(bcallMessage);
            case PlaceholderMessage placeholderMessage -> builder.placeholderMessage(placeholderMessage);
            case AlbumMessage albumMessage -> builder.albumMessage(albumMessage);
            case StickerPackMessage stickerPackMessage -> builder.stickerPackMessage(stickerPackMessage);
            case StatusNotificationMessage statusNotificationMessage ->
                    builder.statusNotificationMessage(statusNotificationMessage);
            case StatusQuestionAnswerMessage statusQuestionAnswerMessage ->
                    builder.statusQuestionAnswerMessage(statusQuestionAnswerMessage);
            case QuestionResponseMessage questionResponseMessage ->
                    builder.questionResponseMessage(questionResponseMessage);
            case StatusQuotedMessage statusQuotedMessage -> builder.statusQuotedMessage(statusQuotedMessage);
            case StatusStickerInteractionMessage statusStickerInteractionMessage ->
                    builder.statusStickerInteractionMessage(statusStickerInteractionMessage);
            case NewsletterFollowerInviteMessage newsletterFollowerInviteMessage ->
                    builder.newsletterFollowerInviteMessageV2(newsletterFollowerInviteMessage);
            case MessageHistoryBundle messageHistoryBundle -> builder.messageHistoryBundle(messageHistoryBundle);
            case MessageHistoryNotice messageHistoryNotice -> builder.messageHistoryNotice(messageHistoryNotice);
            default -> {
            }
        }
        return builder;
    }

    /**
     * Constructs a new MessageContainer from a text message
     *
     * @param message the text message with no context
     */
    public static MessageContainer of(String message) {
        return new MessageContainerBuilder()
                .textMessage(TextMessage.of(message))
                .build();
    }

    /**
     * Constructs a new MessageContainer from a message of any type that can only be seen once
     *
     * @param message the message that the new container should wrap
     * @param <T>     the type of the message
     */
    public static <T extends Message> MessageContainer ofViewOnce(T message) {
        var futureMessageContainer = new FutureMessageContainerBuilder()
                .value(MessageContainer.of(message))
                .build();
        return new MessageContainerBuilder()
                .viewOnceMessage(futureMessageContainer)
                .build();
    }

    /**
     * Constructs a new MessageContainer from a message of any type that can only be seen once(version
     * v2)
     *
     * @param message the message that the new container should wrap
     * @param <T>     the type of the message
     */
    public static <T extends Message> MessageContainer ofViewOnceV2(T message) {
        var futureMessageContainer = new FutureMessageContainerBuilder()
                .value(MessageContainer.of(message))
                .build();
        return new MessageContainerBuilder()
                .viewOnceV2Message(futureMessageContainer)
                .build();
    }

    /**
     * Constructs a new MessageContainer from a message of any type marking it as ephemeral
     *
     * @param message the message that the new container should wrap
     * @param <T>     the type of the message
     */
    public static <T extends Message> MessageContainer ofEphemeral(T message) {
        var futureMessageContainer = new FutureMessageContainerBuilder()
                .value(MessageContainer.of(message))
                .build();
        return new MessageContainerBuilder()
                .ephemeralMessage(futureMessageContainer)
                .build();
    }

    /**
     * Constructs a new MessageContainer from an edited message
     *
     * @param message the message that the new container should wrap
     * @param <T>     the type of the message
     */
    public static <T extends Message> MessageContainer ofEditedMessage(T message) {
        var futureMessageContainer = new FutureMessageContainerBuilder()
                .value(MessageContainer.of(message))
                .build();
        return new MessageContainerBuilder()
                .editedMessage(futureMessageContainer)
                .build();
    }

    /**
     * Constructs a new MessageContainer from a document with caption message
     *
     * @param message the message that the new container should wrap
     * @param <T>     the type of the message
     */
    public static <T extends Message> MessageContainer ofDocumentWithCaption(T message) {
        var futureMessageContainer = new FutureMessageContainerBuilder()
                .value(MessageContainer.of(message))
                .build();
        return new MessageContainerBuilder()
                .documentWithCaptionMessage(futureMessageContainer)
                .build();
    }

    /**
     * Wraps a message in a bot invoke wrapper.
     */
    public static <T extends Message> MessageContainer ofBotInvoke(T message) {
        var futureMessageContainer = new FutureMessageContainerBuilder()
                .value(MessageContainer.of(message))
                .build();
        return new MessageContainerBuilder()
                .botInvokeMessage(futureMessageContainer)
                .build();
    }

    /**
     * Wraps a message in a group mentioned message wrapper.
     */
    public static <T extends Message> MessageContainer ofGroupMentioned(T message) {
        var futureMessageContainer = new FutureMessageContainerBuilder()
                .value(MessageContainer.of(message))
                .build();
        return new MessageContainerBuilder()
                .groupMentionedMessage(futureMessageContainer)
                .build();
    }

    /**
     * Wraps a message in a lottie sticker wrapper.
     */
    public static <T extends Message> MessageContainer ofLottieSticker(T message) {
        var futureMessageContainer = new FutureMessageContainerBuilder()
                .value(MessageContainer.of(message))
                .build();
        return new MessageContainerBuilder()
                .lottieStickerMessage(futureMessageContainer)
                .build();
    }

    /**
     * Wraps a message in an event cover image wrapper.
     */
    public static <T extends Message> MessageContainer ofEventCoverImage(T message) {
        var futureMessageContainer = new FutureMessageContainerBuilder()
                .value(MessageContainer.of(message))
                .build();
        return new MessageContainerBuilder()
                .eventCoverImage(futureMessageContainer)
                .build();
    }

    /**
     * Wraps a message in a status mention wrapper.
     */
    public static <T extends Message> MessageContainer ofStatusMention(T message) {
        var futureMessageContainer = new FutureMessageContainerBuilder()
                .value(MessageContainer.of(message))
                .build();
        return new MessageContainerBuilder()
                .statusMentionMessage(futureMessageContainer)
                .build();
    }

    /**
     * Wraps a message in a poll creation option image wrapper.
     */
    public static <T extends Message> MessageContainer ofPollCreationOptionImage(T message) {
        var futureMessageContainer = new FutureMessageContainerBuilder()
                .value(MessageContainer.of(message))
                .build();
        return new MessageContainerBuilder()
                .pollCreationOptionImageMessage(futureMessageContainer)
                .build();
    }

    /**
     * Wraps a message in an associated child message wrapper.
     */
    public static <T extends Message> MessageContainer ofAssociatedChild(T message) {
        var futureMessageContainer = new FutureMessageContainerBuilder()
                .value(MessageContainer.of(message))
                .build();
        return new MessageContainerBuilder()
                .associatedChildMessage(futureMessageContainer)
                .build();
    }

    /**
     * Wraps a message in a group status mention wrapper.
     */
    public static <T extends Message> MessageContainer ofGroupStatusMention(T message) {
        var futureMessageContainer = new FutureMessageContainerBuilder()
                .value(MessageContainer.of(message))
                .build();
        return new MessageContainerBuilder()
                .groupStatusMentionMessage(futureMessageContainer)
                .build();
    }

    /**
     * Wraps a message in a status add yours wrapper.
     */
    public static <T extends Message> MessageContainer ofStatusAddYours(T message) {
        var futureMessageContainer = new FutureMessageContainerBuilder()
                .value(MessageContainer.of(message))
                .build();
        return new MessageContainerBuilder()
                .statusAddYours(futureMessageContainer)
                .build();
    }

    /**
     * Wraps a message in a group status wrapper.
     */
    public static <T extends Message> MessageContainer ofGroupStatus(T message) {
        var futureMessageContainer = new FutureMessageContainerBuilder()
                .value(MessageContainer.of(message))
                .build();
        return new MessageContainerBuilder()
                .groupStatusMessage(futureMessageContainer)
                .build();
    }

    /**
     * Wraps a message in a group status wrapper (version 2).
     */
    public static <T extends Message> MessageContainer ofGroupStatusV2(T message) {
        var futureMessageContainer = new FutureMessageContainerBuilder()
                .value(MessageContainer.of(message))
                .build();
        return new MessageContainerBuilder()
                .groupStatusMessageV2(futureMessageContainer)
                .build();
    }

    /**
     * Wraps a message in a limit sharing wrapper.
     */
    public static <T extends Message> MessageContainer ofLimitSharing(T message) {
        var futureMessageContainer = new FutureMessageContainerBuilder()
                .value(MessageContainer.of(message))
                .build();
        return new MessageContainerBuilder()
                .limitSharingMessage(futureMessageContainer)
                .build();
    }

    /**
     * Wraps a message in a bot task wrapper.
     */
    public static <T extends Message> MessageContainer ofBotTask(T message) {
        var futureMessageContainer = new FutureMessageContainerBuilder()
                .value(MessageContainer.of(message))
                .build();
        return new MessageContainerBuilder()
                .botTaskMessage(futureMessageContainer)
                .build();
    }

    /**
     * Wraps a message in a newsletter question wrapper.
     */
    public static <T extends Message> MessageContainer ofQuestion(T message) {
        var futureMessageContainer = new FutureMessageContainerBuilder()
                .value(MessageContainer.of(message))
                .build();
        return new MessageContainerBuilder()
                .questionMessage(futureMessageContainer)
                .build();
    }

    /**
     * Wraps a message in a bot forwarded wrapper.
     */
    public static <T extends Message> MessageContainer ofBotForwarded(T message) {
        var futureMessageContainer = new FutureMessageContainerBuilder()
                .value(MessageContainer.of(message))
                .build();
        return new MessageContainerBuilder()
                .botForwardedMessage(futureMessageContainer)
                .build();
    }

    /**
     * Wraps a message in a newsletter question reply wrapper.
     */
    public static <T extends Message> MessageContainer ofQuestionReply(T message) {
        var futureMessageContainer = new FutureMessageContainerBuilder()
                .value(MessageContainer.of(message))
                .build();
        return new MessageContainerBuilder()
                .questionReplyMessage(futureMessageContainer)
                .build();
    }

    /**
     * Wraps a message in a newsletter admin profile wrapper.
     */
    public static <T extends Message> MessageContainer ofNewsletterAdminProfile(T message) {
        var futureMessageContainer = new FutureMessageContainerBuilder()
                .value(MessageContainer.of(message))
                .build();
        return new MessageContainerBuilder()
                .newsletterAdminProfileMessage(futureMessageContainer)
                .build();
    }

    /**
     * Wraps a message in a newsletter admin profile wrapper (version 2).
     */
    public static <T extends Message> MessageContainer ofNewsletterAdminProfileV2(T message) {
        var futureMessageContainer = new FutureMessageContainerBuilder()
                .value(MessageContainer.of(message))
                .build();
        return new MessageContainerBuilder()
                .newsletterAdminProfileMessageV2(futureMessageContainer)
                .build();
    }

    /**
     * Converts this message to a bot invoke wrapper.
     */
    public MessageContainer toBotInvoke() {
        if (type() == Message.Type.BOT_INVOKE) {
            return this;
        }

        var futureMessageContainer = new FutureMessageContainerBuilder()
                .value(MessageContainer.of(content()))
                .build();
        return new MessageContainerBuilder()
                .botInvokeMessage(futureMessageContainer)
                .deviceInfo(deviceInfo)
                .build();
    }

    /**
     * Converts this message to a group mentioned wrapper.
     */
    public MessageContainer toGroupMentioned() {
        if (type() == Message.Type.GROUP_MENTIONED) {
            return this;
        }

        var futureMessageContainer = new FutureMessageContainerBuilder()
                .value(MessageContainer.of(content()))
                .build();
        return new MessageContainerBuilder()
                .groupMentionedMessage(futureMessageContainer)
                .deviceInfo(deviceInfo)
                .build();
    }

    /**
     * Converts this message to a lottie sticker wrapper.
     */
    public MessageContainer toLottieSticker() {
        if (type() == Message.Type.LOTTIE_STICKER) {
            return this;
        }

        var futureMessageContainer = new FutureMessageContainerBuilder()
                .value(MessageContainer.of(content()))
                .build();
        return new MessageContainerBuilder()
                .lottieStickerMessage(futureMessageContainer)
                .deviceInfo(deviceInfo)
                .build();
    }

    /**
     * Converts this message to an event cover image wrapper.
     */
    public MessageContainer toEventCoverImage() {
        if (type() == Message.Type.EVENT_COVER_IMAGE) {
            return this;
        }

        var futureMessageContainer = new FutureMessageContainerBuilder()
                .value(MessageContainer.of(content()))
                .build();
        return new MessageContainerBuilder()
                .eventCoverImage(futureMessageContainer)
                .deviceInfo(deviceInfo)
                .build();
    }

    /**
     * Converts this message to a status mention wrapper.
     */
    public MessageContainer toStatusMention() {
        if (type() == Message.Type.STATUS_MENTION) {
            return this;
        }

        var futureMessageContainer = new FutureMessageContainerBuilder()
                .value(MessageContainer.of(content()))
                .build();
        return new MessageContainerBuilder()
                .statusMentionMessage(futureMessageContainer)
                .deviceInfo(deviceInfo)
                .build();
    }

    /**
     * Converts this message to a poll creation option image wrapper.
     */
    public MessageContainer toPollCreationOptionImage() {
        if (type() == Message.Type.POLL_CREATION_OPTION_IMAGE) {
            return this;
        }

        var futureMessageContainer = new FutureMessageContainerBuilder()
                .value(MessageContainer.of(content()))
                .build();
        return new MessageContainerBuilder()
                .pollCreationOptionImageMessage(futureMessageContainer)
                .deviceInfo(deviceInfo)
                .build();
    }

    /**
     * Converts this message to an associated child wrapper.
     */
    public MessageContainer toAssociatedChild() {
        if (type() == Message.Type.ASSOCIATED_CHILD) {
            return this;
        }

        var futureMessageContainer = new FutureMessageContainerBuilder()
                .value(MessageContainer.of(content()))
                .build();
        return new MessageContainerBuilder()
                .associatedChildMessage(futureMessageContainer)
                .deviceInfo(deviceInfo)
                .build();
    }

    /**
     * Converts this message to a group status mention wrapper.
     */
    public MessageContainer toGroupStatusMention() {
        if (type() == Message.Type.GROUP_STATUS_MENTION) {
            return this;
        }

        var futureMessageContainer = new FutureMessageContainerBuilder()
                .value(MessageContainer.of(content()))
                .build();
        return new MessageContainerBuilder()
                .groupStatusMentionMessage(futureMessageContainer)
                .deviceInfo(deviceInfo)
                .build();
    }

    /**
     * Converts this message to a status add yours wrapper.
     */
    public MessageContainer toStatusAddYours() {
        if (type() == Message.Type.STATUS_ADD_YOURS) {
            return this;
        }

        var futureMessageContainer = new FutureMessageContainerBuilder()
                .value(MessageContainer.of(content()))
                .build();
        return new MessageContainerBuilder()
                .statusAddYours(futureMessageContainer)
                .deviceInfo(deviceInfo)
                .build();
    }

    /**
     * Converts this message to a group status wrapper.
     */
    public MessageContainer toGroupStatus() {
        if (type() == Message.Type.GROUP_STATUS) {
            return this;
        }

        var futureMessageContainer = new FutureMessageContainerBuilder()
                .value(MessageContainer.of(content()))
                .build();
        return new MessageContainerBuilder()
                .groupStatusMessage(futureMessageContainer)
                .deviceInfo(deviceInfo)
                .build();
    }

    /**
     * Converts this message to a group status wrapper (version 2).
     */
    public MessageContainer toGroupStatusV2() {
        if (type() == Message.Type.GROUP_STATUS) {
            return this;
        }

        var futureMessageContainer = new FutureMessageContainerBuilder()
                .value(MessageContainer.of(content()))
                .build();
        return new MessageContainerBuilder()
                .groupStatusMessageV2(futureMessageContainer)
                .deviceInfo(deviceInfo)
                .build();
    }

    /**
     * Converts this message to a limit sharing wrapper.
     */
    public MessageContainer toLimitSharing() {
        if (type() == Message.Type.LIMIT_SHARING) {
            return this;
        }

        var futureMessageContainer = new FutureMessageContainerBuilder()
                .value(MessageContainer.of(content()))
                .build();
        return new MessageContainerBuilder()
                .limitSharingMessage(futureMessageContainer)
                .deviceInfo(deviceInfo)
                .build();
    }

    /**
     * Converts this message to a bot task wrapper.
     */
    public MessageContainer toBotTask() {
        if (type() == Message.Type.BOT_TASK) {
            return this;
        }

        var futureMessageContainer = new FutureMessageContainerBuilder()
                .value(MessageContainer.of(content()))
                .build();
        return new MessageContainerBuilder()
                .botTaskMessage(futureMessageContainer)
                .deviceInfo(deviceInfo)
                .build();
    }

    /**
     * Converts this message to a newsletter question wrapper.
     */
    public MessageContainer toQuestion() {
        if (type() == Message.Type.NEWSLETTER_QUESTION) {
            return this;
        }

        var futureMessageContainer = new FutureMessageContainerBuilder()
                .value(MessageContainer.of(content()))
                .build();
        return new MessageContainerBuilder()
                .questionMessage(futureMessageContainer)
                .deviceInfo(deviceInfo)
                .build();
    }

    /**
     * Converts this message to a bot forwarded wrapper.
     */
    public MessageContainer toBotForwarded() {
        if (type() == Message.Type.BOT_FORWARDED) {
            return this;
        }

        var futureMessageContainer = new FutureMessageContainerBuilder()
                .value(MessageContainer.of(content()))
                .build();
        return new MessageContainerBuilder()
                .botForwardedMessage(futureMessageContainer)
                .deviceInfo(deviceInfo)
                .build();
    }

    /**
     * Converts this message to a newsletter question reply wrapper.
     */
    public MessageContainer toQuestionReply() {
        if (type() == Message.Type.NEWSLETTER_QUESTION_REPLY) {
            return this;
        }

        var futureMessageContainer = new FutureMessageContainerBuilder()
                .value(MessageContainer.of(content()))
                .build();
        return new MessageContainerBuilder()
                .questionReplyMessage(futureMessageContainer)
                .deviceInfo(deviceInfo)
                .build();
    }

    /**
     * Converts this message to a newsletter admin profile wrapper.
     */
    public MessageContainer toNewsletterAdminProfile() {
        if (type() == Message.Type.NEWSLETTER_ADMIN_PROFILE) {
            return this;
        }

        var futureMessageContainer = new FutureMessageContainerBuilder()
                .value(MessageContainer.of(content()))
                .build();
        return new MessageContainerBuilder()
                .newsletterAdminProfileMessage(futureMessageContainer)
                .deviceInfo(deviceInfo)
                .build();
    }

    /**
     * Converts this message to a newsletter admin profile wrapper (version 2).
     */
    public MessageContainer toNewsletterAdminProfileV2() {
        if (type() == Message.Type.NEWSLETTER_ADMIN_PROFILE) {
            return this;
        }

        var futureMessageContainer = new FutureMessageContainerBuilder()
                .value(MessageContainer.of(content()))
                .build();
        return new MessageContainerBuilder()
                .newsletterAdminProfileMessageV2(futureMessageContainer)
                .deviceInfo(deviceInfo)
                .build();
    }

    /**
     * Returns the first populated message inside this container. If no message is found,
     * {@link EmptyMessage} is returned
     *
     * @return a non-null message
     */
    public Message content() {
        if (this.textWithNoContextMessage != null) {
            return TextMessage.of(textWithNoContextMessage);
        }
        if (this.imageMessage != null) {
            return imageMessage;
        }
        if (this.contactMessage != null) {
            return contactMessage;
        }
        if (this.locationMessage != null) {
            return locationMessage;
        }
        if (this.textMessage != null) {
            return textMessage;
        }
        if (this.documentMessage != null) {
            return documentMessage;
        }
        if (this.audioMessage != null) {
            return audioMessage;
        }
        if (this.videoMessage != null) {
            return videoMessage;
        }
        if (this.protocolMessage != null) {
            return protocolMessage;
        }
        if (this.contactsArrayMessage != null) {
            return contactsArrayMessage;
        }
        if (this.highlyStructuredMessage != null) {
            return highlyStructuredMessage;
        }
        if (this.sendPaymentMessage != null) {
            return sendPaymentMessage;
        }
        if (this.liveLocationMessage != null) {
            return liveLocationMessage;
        }
        if (this.requestPaymentMessage != null) {
            return requestPaymentMessage;
        }
        if (this.declinePaymentRequestMessage != null) {
            return declinePaymentRequestMessage;
        }
        if (this.cancelPaymentRequestMessage != null) {
            return cancelPaymentRequestMessage;
        }
        if (this.templateMessage != null) {
            return templateMessage;
        }
        if (this.stickerMessage != null) {
            return stickerMessage;
        }
        if (this.groupInviteMessage != null) {
            return groupInviteMessage;
        }
        if (this.templateReplyMessage != null) {
            return templateReplyMessage;
        }
        if (this.productMessage != null) {
            return productMessage;
        }
        if (this.deviceSentMessage != null) {
            return deviceSentMessage.message().content();
        }
        if (this.deviceSyncMessage != null) {
            return deviceSyncMessage;
        }
        if (this.listMessage != null) {
            return listMessage;
        }
        if (this.viewOnceMessage != null) {
            return viewOnceMessage.value().content();
        }
        if (this.orderMessage != null) {
            return orderMessage;
        }
        if (this.listResponseMessage != null) {
            return listResponseMessage;
        }
        if (this.ephemeralMessage != null) {
            return ephemeralMessage.value().content();
        }
        if (this.invoiceMessage != null) {
            return invoiceMessage;
        }
        if (this.buttonsMessage != null) {
            return buttonsMessage;
        }
        if (this.buttonsResponseMessage != null) {
            return buttonsResponseMessage;
        }
        if (this.paymentInviteMessage != null) {
            return paymentInviteMessage;
        }
        if (interactiveMessage != null) {
            return interactiveMessage;
        }
        if (reactionMessage != null) {
            return reactionMessage;
        }
        if (stickerSyncMessage != null) {
            return stickerSyncMessage;
        }
        if (interactiveResponseMessage != null) {
            return interactiveResponseMessage;
        }
        if (pollCreationMessage != null) {
            return pollCreationMessage;
        }
        if (pollUpdateMessage != null) {
            return pollUpdateMessage;
        }
        if (keepInChatMessage != null) {
            return keepInChatMessage;
        }
        if (documentWithCaptionMessage != null) {
            return documentWithCaptionMessage.value().content();
        }
        if (requestPhoneNumberMessage != null) {
            return requestPhoneNumberMessage;
        }
        if (viewOnceV2Message != null) {
            return viewOnceV2Message.value.content();
        }
        if (encryptedReactionMessage != null) {
            return encryptedReactionMessage;
        }
        if (editedMessage != null) {
            return editedMessage.value().content();
        }
        if (viewOnceV2ExtensionMessage != null) {
            return viewOnceV2ExtensionMessage.value().content();
        }
        if (callMessage != null) {
            return callMessage;
        }
        if (newsletterAdminInviteMessage != null) {
            return newsletterAdminInviteMessage;
        }
        if (pinInChatMessage != null) {
            return pinInChatMessage;
        }
        if (encEventResponseMessage != null) {
            return encEventResponseMessage;
        }
        if (eventMessage != null) {
            return eventMessage;
        }
        if (secretEncryptedMessage != null) {
            return secretEncryptedMessage;
        }
        if (pollResultSnapshotMessage != null) {
            return pollResultSnapshotMessage;
        }
        if (pollResultSnapshotMessageV3 != null) {
            return pollResultSnapshotMessageV3;
        }
        if (botInvokeMessage != null) {
            return botInvokeMessage.value().content();
        }
        if (scheduledCallCreationMessage != null) {
            return scheduledCallCreationMessage;
        }
        if (scheduledCallEditMessage != null) {
            return scheduledCallEditMessage;
        }
        if (ptvMessage != null) {
            return ptvMessage;
        }
        if (callLogMessage != null) {
            return callLogMessage;
        }
        if (messageHistoryBundle != null) {
            return messageHistoryBundle;
        }
        if (encCommentMessage != null) {
            return encCommentMessage;
        }
        if (bcallMessage != null) {
            return bcallMessage;
        }
        if (lottieStickerMessage != null) {
            return lottieStickerMessage.value().content();
        }
        if (commentMessage != null) {
            return commentMessage;
        }
        if (placeholderMessage != null) {
            return placeholderMessage;
        }
        if (albumMessage != null) {
            return albumMessage;
        }
        if (eventCoverImage != null) {
            return eventCoverImage.value().content();
        }
        if (stickerPackMessage != null) {
            return stickerPackMessage;
        }
        if (statusMentionMessage != null) {
            return statusMentionMessage.value().content();
        }
        if (statusNotificationMessage != null) {
            return statusNotificationMessage;
        }
        if (messageHistoryNotice != null) {
            return messageHistoryNotice;
        }
        if (statusQuestionAnswerMessage != null) {
            return statusQuestionAnswerMessage;
        }
        if (questionResponseMessage != null) {
            return questionResponseMessage;
        }
        if (statusQuotedMessage != null) {
            return statusQuotedMessage;
        }
        if (statusStickerInteractionMessage != null) {
            return statusStickerInteractionMessage;
        }
        if (newsletterFollowerInviteMessageV2 != null) {
            return newsletterFollowerInviteMessageV2;
        }
        if (questionMessage != null) {
            return questionMessage.value().content();
        }
        if (questionReplyMessage != null) {
            return questionReplyMessage.value().content();
        }
        // This needs to be last
        if (this.senderKeyDistributionMessage != null) {
            return senderKeyDistributionMessage;
        }
        return EMPTY_MESSAGE;
    }

    /**
     * Returns the first populated contextual message inside this container
     *
     * @return a non-null Optional ContextualMessage
     */
    public Optional<ContextualMessage> contentWithContext() {
        return Optional.of(content())
                .filter(entry -> entry instanceof ContextualMessage)
                .map(entry -> (ContextualMessage) entry);
    }

    /**
     * Checks whether the message that this container wraps matches the provided type
     *
     * @param type the non-null type to check against
     * @return a boolean
     */
    public boolean hasType(Message.Type type) {
        return content().type() == type;
    }

    /**
     * Checks whether the message that this container wraps matches the provided category
     *
     * @param category the non-null category to check against
     * @return a boolean
     */
    public boolean hasCategory(Message.Category category) {
        return content().category() == category;
    }

    /**
     * Returns the type of the message
     *
     * @return a non-null type
     */
    public Message.Type type() {
        if (ephemeralMessage != null) {
            return Message.Type.EPHEMERAL;
        }
        if (viewOnceMessage != null || viewOnceV2Message != null || viewOnceV2ExtensionMessage != null) {
            return Message.Type.VIEW_ONCE;
        }
        if (editedMessage != null) {
            return Message.Type.EDITED;
        }
        if (documentWithCaptionMessage != null) {
            return Message.Type.DOCUMENT_WITH_CAPTION;
        }
        if (botInvokeMessage != null) {
            return Message.Type.BOT_INVOKE;
        }
        if (groupMentionedMessage != null) {
            return Message.Type.GROUP_MENTIONED;
        }
        if (lottieStickerMessage != null) {
            return Message.Type.LOTTIE_STICKER;
        }
        if (eventCoverImage != null) {
            return Message.Type.EVENT_COVER_IMAGE;
        }
        if (statusMentionMessage != null) {
            return Message.Type.STATUS_MENTION;
        }
        if (pollCreationOptionImageMessage != null) {
            return Message.Type.POLL_CREATION_OPTION_IMAGE;
        }
        if (associatedChildMessage != null) {
            return Message.Type.ASSOCIATED_CHILD;
        }
        if (groupStatusMentionMessage != null) {
            return Message.Type.GROUP_STATUS_MENTION;
        }
        if (pollCreationMessageV4 != null) {
            return Message.Type.POLL_CREATION;
        }
        if (statusAddYours != null) {
            return Message.Type.STATUS_ADD_YOURS;
        }
        if (groupStatusMessage != null || groupStatusMessageV2 != null) {
            return Message.Type.GROUP_STATUS;
        }
        if (limitSharingMessage != null) {
            return Message.Type.LIMIT_SHARING;
        }
        if (botTaskMessage != null) {
            return Message.Type.BOT_TASK;
        }
        if (questionMessage != null) {
            return Message.Type.NEWSLETTER_QUESTION;
        }
        if (questionReplyMessage != null) {
            return Message.Type.NEWSLETTER_QUESTION_REPLY;
        }
        if (botForwardedMessage != null) {
            return Message.Type.BOT_FORWARDED;
        }
        if (newsletterAdminProfileMessage != null || newsletterAdminProfileMessageV2 != null) {
            return Message.Type.NEWSLETTER_ADMIN_PROFILE;
        }
        return content().type();
    }

    /**
     * Returns the deep type of the message unwrapping ephemeral and view once messages
     *
     * @return a non-null type
     */
    public Message.Type deepType() {
        return content().type();
    }

    /**
     * Returns the category of the message
     *
     * @return a non-null category
     */
    public Message.Category category() {
        return content().category();
    }

    /**
     * Converts this message to an ephemeral message
     *
     * @return a non-null message container
     */
    public MessageContainer toEphemeral() {
        if (type() == Message.Type.EPHEMERAL) {
            return this;
        }

        var futureMessageContainer = new FutureMessageContainerBuilder()
                .value(MessageContainer.of(content()))
                .build();
        return new MessageContainerBuilder()
                .ephemeralMessage(futureMessageContainer)
                .deviceInfo(deviceInfo)
                .build();
    }

    /**
     * Converts this message to a view once message
     *
     * @return a non-null message container
     */
    public MessageContainer toViewOnce() {
        if (type() == Message.Type.VIEW_ONCE) {
            return this;
        }

        var futureMessageContainer = new FutureMessageContainerBuilder()
                .value(MessageContainer.of(content()))
                .build();
        return new MessageContainerBuilder()
                .viewOnceMessage(futureMessageContainer)
                .deviceInfo(deviceInfo)
                .build();
    }

    /**
     * Returns an unboxed message where are all future-proof messages(i.e. ephemeral and view once)
     * have been unboxed
     *
     * @return a non-null message container
     */
    public MessageContainer unbox() {
        if (deviceSentMessage != null) {
            return deviceSentMessage.message();
        }

        if (viewOnceMessage != null) {
            return viewOnceMessage.value();
        }

        if (ephemeralMessage != null) {
            return ephemeralMessage.value();
        }

        if (documentWithCaptionMessage != null) {
            return documentWithCaptionMessage.value();
        }

        if (viewOnceV2Message != null) {
            return viewOnceV2Message.value();
        }

        if (editedMessage != null) {
            return editedMessage.value();
        }

        if (viewOnceV2ExtensionMessage != null) {
            return viewOnceV2ExtensionMessage.value();
        }

        if (botInvokeMessage != null) {
            return botInvokeMessage.value();
        }

        if (questionMessage != null) {
            return questionMessage.value();
        }

        if (questionReplyMessage != null) {
            return questionReplyMessage.value();
        }

        return this;
    }

    public Optional<DeviceContextInfo> deviceInfo() {
        return Optional.ofNullable(deviceInfo);
    }

    /**
     * Returns a copy of this container with a different device info
     *
     * @return a non-null message container
     */
    public MessageContainer withDeviceInfo(DeviceContextInfo deviceInfo) {
        if (deviceSentMessage != null) {
            return ofBuilder(deviceSentMessage)
                    .deviceInfo(deviceInfo)
                    .build();
        }

        if (viewOnceMessage != null) {
            return new MessageContainerBuilder()
                    .viewOnceMessage(viewOnceMessage)
                    .deviceInfo(deviceInfo)
                    .build();
        }

        if (ephemeralMessage != null) {
            return new MessageContainerBuilder()
                    .ephemeralMessage(ephemeralMessage)
                    .deviceInfo(deviceInfo)
                    .build();
        }

        if (documentWithCaptionMessage != null) {
            return new MessageContainerBuilder()
                    .documentWithCaptionMessage(documentWithCaptionMessage)
                    .deviceInfo(deviceInfo)
                    .build();
        }

        if (viewOnceV2Message != null) {
            return new MessageContainerBuilder()
                    .viewOnceV2Message(viewOnceV2Message)
                    .deviceInfo(deviceInfo)
                    .build();
        }

        if (editedMessage != null) {
            return new MessageContainerBuilder()
                    .editedMessage(editedMessage)
                    .deviceInfo(deviceInfo)
                    .build();
        }

        if (viewOnceV2ExtensionMessage != null) {
            return new MessageContainerBuilder()
                    .viewOnceV2ExtensionMessage(viewOnceV2ExtensionMessage)
                    .deviceInfo(deviceInfo)
                    .build();
        }

        return ofBuilder(content())
                .deviceInfo(deviceInfo)
                .build();
    }

    public Optional<SenderKeyDistributionMessage> senderKeyDistributionMessage() {
        return Optional.ofNullable(senderKeyDistributionMessage);
    }

    /**
     * Returns whether this container is empty
     *
     * @return a boolean
     */
    public boolean isEmpty() {
        return hasType(Message.Type.EMPTY);
    }

    /**
     * Converts this container into a String
     *
     * @return a non-null String
     */
    @Override
    public String toString() {
        return Objects.toString(content());
    }
}