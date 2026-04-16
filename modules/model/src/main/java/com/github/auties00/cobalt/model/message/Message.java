package com.github.auties00.cobalt.model.message;

import com.github.auties00.cobalt.model.message.call.BCallMessage;
import com.github.auties00.cobalt.model.message.call.CallLogMessage;
import com.github.auties00.cobalt.model.message.call.ScheduledCallCreationMessage;
import com.github.auties00.cobalt.model.message.call.ScheduledCallEditMessage;
import com.github.auties00.cobalt.model.message.commerce.InvoiceMessage;
import com.github.auties00.cobalt.model.message.context.ContextualMessage;
import com.github.auties00.cobalt.model.message.event.EncEventResponseMessage;
import com.github.auties00.cobalt.model.message.event.EventResponseMessage;
import com.github.auties00.cobalt.model.message.media.MessageVideoEndCard;
import com.github.auties00.cobalt.model.message.media.MessageLinkPreviewMetadata;
import com.github.auties00.cobalt.model.message.media.MessageMMSThumbnailMetadata;
import com.github.auties00.cobalt.model.message.media.MessageURLMetadata;
import com.github.auties00.cobalt.model.message.payment.*;
import com.github.auties00.cobalt.model.message.poll.*;
import com.github.auties00.cobalt.model.message.security.EncCommentMessage;
import com.github.auties00.cobalt.model.message.security.EncReactionMessage;
import com.github.auties00.cobalt.model.message.security.PlaceholderMessage;
import com.github.auties00.cobalt.model.message.security.SecretEncMessage;
import com.github.auties00.cobalt.model.message.status.StatusNotificationMessage;
import com.github.auties00.cobalt.model.message.status.StatusQuestionAnswerMessage;
import com.github.auties00.cobalt.model.message.status.StatusQuotedMessage;
import com.github.auties00.cobalt.model.message.status.StatusStickerInteractionMessage;
import com.github.auties00.cobalt.model.message.system.*;
import com.github.auties00.cobalt.model.message.system.appstate.*;
import com.github.auties00.cobalt.model.message.system.history.*;
import com.github.auties00.cobalt.model.message.system.peer.PeerDataOperationRequestMessage;
import com.github.auties00.cobalt.model.message.system.peer.PeerDataOperationRequestResponseMessage;
import com.github.auties00.cobalt.model.message.text.CommentMessage;
import com.github.auties00.cobalt.model.message.text.HighlyStructuredMessage;
import com.github.auties00.cobalt.model.message.text.ReactionMessage;
import com.github.auties00.cobalt.model.message.group.SenderKeyDistributionMessage;

/**
 * Root marker interface implemented by every concrete WhatsApp message type.
 *
 * <p>This sealed interface defines the complete, exhaustive set of message
 * payloads that a {@link MessageContainer} can hold. Each variant represents
 * a distinct feature of the WhatsApp platform, ranging from the simplest
 * text messages to payment flows, polls, calls, newsletters, interactive
 * bot conversations, and system-level protocol messages.
 *
 * <p>Because the hierarchy is sealed, callers can write exhaustive
 * {@code switch} expressions over a {@code Message} and the compiler will
 * flag any branch that does not cover a permitted subtype. This is the
 * recommended way to branch on message content:
 * <pre>{@code
 *     String display = switch (message) {
 *         case ImageMessage img -> "[image]" + img.caption().orElse("");
 *         case ExtendedTextMessage txt -> txt.text();
 *         case EmptyMessage e -> "";
 *         default -> "[unsupported]";
 *     };
 * }</pre>
 *
 * <p>The {@link EmptyMessage#INSTANCE} singleton is the sentinel used when
 * a container carries no payload; it is always returned by
 * {@link MessageContainer#content()} instead of {@code null}.
 */
public sealed interface Message permits EmptyMessage, BCallMessage, CallLogMessage, ScheduledCallCreationMessage, ScheduledCallEditMessage, InvoiceMessage, ContextualMessage, EncEventResponseMessage, EventResponseMessage, SenderKeyDistributionMessage, MessageLinkPreviewMetadata, MessageMMSThumbnailMetadata, MessageURLMetadata, MessageVideoEndCard, CancelPaymentRequestMessage, DeclinePaymentRequestMessage, PaymentExtendedMetadata, PaymentInviteMessage, PaymentLinkMetadata, RequestPaymentMessage, SendPaymentMessage, PollEncValue, PollUpdateMessage, PollUpdateMessageMetadata, PollVoteMessage, EncCommentMessage, EncReactionMessage, PlaceholderMessage, SecretEncMessage, StatusNotificationMessage, StatusQuestionAnswerMessage, StatusQuotedMessage, StatusStickerInteractionMessage, ChatProtocolMessage, CloudAPIThreadControlNotification, DeviceSentMessage, FutureProofMessage, InitialSecurityNotificationSettingSync, KeepInChatMessage, PinInChatMessage, ProtocolMessage, QuestionResponseMessage, RequestWelcomeMessageMetadata, StickerSyncRMRMessage, AppStateFatalExceptionNotification, AppStateSyncKey, AppStateSyncKeyData, AppStateSyncKeyFingerprint, AppStateSyncKeyId, AppStateSyncKeyRequest, AppStateSyncKeyShare, FullHistorySyncOnDemandRequestMetadata, HistorySyncMessageAccessStatus, HistorySyncNotification, MessageHistoryMetadata, PeerDataOperationRequestMessage, PeerDataOperationRequestResponseMessage, CommentMessage, HighlyStructuredMessage, ReactionMessage {
}
