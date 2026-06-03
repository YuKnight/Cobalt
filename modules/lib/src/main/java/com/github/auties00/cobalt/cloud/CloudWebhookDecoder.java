package com.github.auties00.cobalt.cloud;

import com.alibaba.fastjson2.JSONObject;
import com.github.auties00.cobalt.model.chat.ChatMessageInfo;
import com.github.auties00.cobalt.model.chat.ChatMessageInfoBuilder;
import com.github.auties00.cobalt.model.cloud.CloudMessageStatus;
import com.github.auties00.cobalt.model.jid.Jid;
import com.github.auties00.cobalt.model.jid.JidServer;
import com.github.auties00.cobalt.model.message.MessageContainer;
import com.github.auties00.cobalt.model.message.MessageKey;
import com.github.auties00.cobalt.model.message.MessageKeyBuilder;
import com.github.auties00.cobalt.model.message.MessageStatus;
import com.github.auties00.cobalt.model.message.commerce.ButtonsResponseMessage;
import com.github.auties00.cobalt.model.message.commerce.ButtonsResponseMessageBuilder;
import com.github.auties00.cobalt.model.message.list.ListResponseMessage;
import com.github.auties00.cobalt.model.message.list.ListResponseMessageBuilder;
import com.github.auties00.cobalt.model.message.list.ListResponseMessageSingleSelectReplyBuilder;
import com.github.auties00.cobalt.model.message.location.LocationMessageBuilder;
import com.github.auties00.cobalt.model.message.media.AudioMessageBuilder;
import com.github.auties00.cobalt.model.message.media.DocumentMessageBuilder;
import com.github.auties00.cobalt.model.message.media.ImageMessageBuilder;
import com.github.auties00.cobalt.model.message.media.StickerMessageBuilder;
import com.github.auties00.cobalt.model.message.media.VideoMessageBuilder;
import com.github.auties00.cobalt.model.message.text.ExtendedTextMessageBuilder;
import com.github.auties00.cobalt.model.message.text.ReactionMessageBuilder;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Translates inbound WhatsApp Cloud API webhook change values into Cobalt's universal message model.
 *
 * <p>The decoder is the inverse of {@link CloudMessageEncoder}: it reads the {@code messages[]},
 * {@code statuses[]}, and {@code contacts[]} arrays of a webhook change {@code value} and produces
 * {@link ChatMessageInfo} instances for inbound messages and {@link StatusUpdate} records for outbound
 * delivery receipts. Inbound media is referenced by its Cloud media id, stored in the media message's
 * url field so a later download resolves it through the media edge.
 */
public final class CloudWebhookDecoder {
    /**
     * Private constructor; the decoder exposes only static behaviour.
     */
    private CloudWebhookDecoder() {

    }

    /**
     * A single outbound message status transition decoded from a {@code statuses[]} entry.
     *
     * @param key    the key of the message whose status changed
     * @param status the new status
     */
    public record StatusUpdate(MessageKey key, CloudMessageStatus status) {
    }

    /**
     * Decodes the inbound messages of a webhook change value.
     *
     * @param value the webhook change {@code value}
     * @return the decoded inbound messages, empty when the change carried none
     */
    public static List<ChatMessageInfo> decodeMessages(JSONObject value) {
        var messages = value.getJSONArray("messages");
        if (messages == null || messages.isEmpty()) {
            return List.of();
        }
        var pushNames = pushNamesByWaId(value);
        var result = new ArrayList<ChatMessageInfo>();
        for (var index = 0; index < messages.size(); index++) {
            var message = messages.getJSONObject(index);
            result.add(decodeMessage(message, pushNames));
        }
        return result;
    }

    /**
     * Decodes the outbound status transitions of a webhook change value.
     *
     * @param value the webhook change {@code value}
     * @return the decoded status updates, empty when the change carried none
     */
    public static List<StatusUpdate> decodeStatuses(JSONObject value) {
        var statuses = value.getJSONArray("statuses");
        if (statuses == null || statuses.isEmpty()) {
            return List.of();
        }
        var result = new ArrayList<StatusUpdate>();
        for (var index = 0; index < statuses.size(); index++) {
            var status = statuses.getJSONObject(index);
            var key = new MessageKeyBuilder()
                    .id(status.getString("id"))
                    .parentJid(userJid(status.getString("recipient_id")))
                    .fromMe(true)
                    .build();
            result.add(new StatusUpdate(key, CloudMessageStatus.of(status.getString("status"))));
        }
        return result;
    }

    /**
     * Decodes a single inbound message into a {@link ChatMessageInfo}.
     *
     * @param message   the inbound message object
     * @param pushNames the profile names indexed by sender wa_id
     * @return the decoded message
     */
    private static ChatMessageInfo decodeMessage(JSONObject message, Map<String, String> pushNames) {
        var from = message.getString("from");
        var senderJid = userJid(from);
        var key = new MessageKeyBuilder()
                .id(message.getString("id"))
                .parentJid(senderJid)
                .fromMe(false)
                .build();
        var container = decodeContent(message);
        var builder = new ChatMessageInfoBuilder()
                .key(key)
                .message(container)
                .senderJid(senderJid)
                .status(MessageStatus.DELIVERED);
        var timestamp = message.getLong("timestamp");
        if (timestamp != null) {
            builder.timestamp(Instant.ofEpochSecond(timestamp));
        }
        var pushName = pushNames.get(from);
        if (pushName != null) {
            builder.pushName(pushName);
        }
        return builder.build();
    }

    /**
     * Decodes the content of an inbound message into a {@link MessageContainer}.
     *
     * @param message the inbound message object
     * @return the decoded container
     */
    private static MessageContainer decodeContent(JSONObject message) {
        var type = message.getString("type");
        if (type == null) {
            return MessageContainer.empty();
        }
        return switch (type) {
            case "text" -> MessageContainer.of(new ExtendedTextMessageBuilder()
                    .text(textBody(message))
                    .build());
            case "image" -> MessageContainer.of(new ImageMessageBuilder()
                    .mediaUrl(mediaId(message, "image"))
                    .mimetype(mediaMime(message, "image"))
                    .caption(mediaCaption(message, "image"))
                    .build());
            case "video" -> MessageContainer.of(new VideoMessageBuilder()
                    .mediaUrl(mediaId(message, "video"))
                    .mimetype(mediaMime(message, "video"))
                    .caption(mediaCaption(message, "video"))
                    .build());
            case "audio" -> MessageContainer.of(new AudioMessageBuilder()
                    .mediaUrl(mediaId(message, "audio"))
                    .mimetype(mediaMime(message, "audio"))
                    .build());
            case "document" -> MessageContainer.of(new DocumentMessageBuilder()
                    .mediaUrl(mediaId(message, "document"))
                    .mimetype(mediaMime(message, "document"))
                    .caption(mediaCaption(message, "document"))
                    .build());
            case "sticker" -> MessageContainer.of(new StickerMessageBuilder()
                    .mediaUrl(mediaId(message, "sticker"))
                    .mimetype(mediaMime(message, "sticker"))
                    .build());
            case "location" -> decodeLocation(message);
            case "reaction" -> decodeReaction(message);
            case "interactive" -> decodeInteractiveReply(message);
            case "button" -> decodeButtonReply(message);
            default -> MessageContainer.empty();
        };
    }

    /**
     * Decodes an inbound location message.
     *
     * @param message the inbound message object
     * @return the decoded container
     */
    private static MessageContainer decodeLocation(JSONObject message) {
        var location = message.getJSONObject("location");
        var builder = new LocationMessageBuilder();
        if (location != null) {
            var latitude = location.getDouble("latitude");
            var longitude = location.getDouble("longitude");
            if (latitude != null) {
                builder.degreesLatitude(latitude);
            }
            if (longitude != null) {
                builder.degreesLongitude(longitude);
            }
            builder.name(location.getString("name"));
            builder.address(location.getString("address"));
        }
        return MessageContainer.of(builder.build());
    }

    /**
     * Decodes an inbound reaction message.
     *
     * @param message the inbound message object
     * @return the decoded container
     */
    private static MessageContainer decodeReaction(JSONObject message) {
        var reaction = message.getJSONObject("reaction");
        var builder = new ReactionMessageBuilder();
        if (reaction != null) {
            builder.text(reaction.getString("emoji"));
            var targetId = reaction.getString("message_id");
            if (targetId != null) {
                builder.key(new MessageKeyBuilder().id(targetId).fromMe(true).build());
            }
        }
        return MessageContainer.of(builder.build());
    }

    /**
     * Extracts the body of an inbound text message.
     *
     * @param message the inbound message object
     * @return the message body, or the empty string when absent
     */
    private static String textBody(JSONObject message) {
        var text = message.getJSONObject("text");
        return text == null ? "" : text.getString("body");
    }

    /**
     * Decodes an inbound interactive reply (a reply button or a list selection) into a typed response
     * message.
     *
     * @param message the inbound message object
     * @return the decoded container
     */
    private static MessageContainer decodeInteractiveReply(JSONObject message) {
        var interactive = message.getJSONObject("interactive");
        if (interactive != null) {
            var buttonReply = interactive.getJSONObject("button_reply");
            if (buttonReply != null) {
                return MessageContainer.of(new ButtonsResponseMessageBuilder()
                        .selectedButtonId(buttonReply.getString("id"))
                        .selectedDisplayText(buttonReply.getString("title"))
                        .type(ButtonsResponseMessage.Type.DISPLAY_TEXT)
                        .build());
            }
            var listReply = interactive.getJSONObject("list_reply");
            if (listReply != null) {
                var reply = new ListResponseMessageSingleSelectReplyBuilder()
                        .selectedRowId(listReply.getString("id"))
                        .build();
                return MessageContainer.of(new ListResponseMessageBuilder()
                        .title(listReply.getString("title"))
                        .description(listReply.getString("description"))
                        .singleSelectReply(reply)
                        .listType(ListResponseMessage.ListType.SINGLE_SELECT)
                        .build());
            }
        }
        return MessageContainer.empty();
    }

    /**
     * Decodes an inbound legacy quick-reply button tap into a typed response message.
     *
     * @param message the inbound message object
     * @return the decoded container
     */
    private static MessageContainer decodeButtonReply(JSONObject message) {
        var builder = new ButtonsResponseMessageBuilder()
                .type(ButtonsResponseMessage.Type.DISPLAY_TEXT);
        var button = message.getJSONObject("button");
        if (button != null) {
            builder.selectedButtonId(button.getString("payload"))
                    .selectedDisplayText(button.getString("text"));
        }
        return MessageContainer.of(builder.build());
    }

    /**
     * Extracts the media id of an inbound media message.
     *
     * @param message the inbound message object
     * @param type    the media type key, for example {@code "image"}
     * @return the media id, or {@code null} when absent
     */
    private static String mediaId(JSONObject message, String type) {
        var media = message.getJSONObject(type);
        return media == null ? null : media.getString("id");
    }

    /**
     * Extracts the MIME type of an inbound media message.
     *
     * @param message the inbound message object
     * @param type    the media type key
     * @return the MIME type, or {@code null} when absent
     */
    private static String mediaMime(JSONObject message, String type) {
        var media = message.getJSONObject(type);
        return media == null ? null : media.getString("mime_type");
    }

    /**
     * Extracts the caption of an inbound media message.
     *
     * @param message the inbound message object
     * @param type    the media type key
     * @return the caption, or {@code null} when absent
     */
    private static String mediaCaption(JSONObject message, String type) {
        var media = message.getJSONObject(type);
        return media == null ? null : media.getString("caption");
    }

    /**
     * Indexes the inbound profile names by sender wa_id.
     *
     * @param value the webhook change {@code value}
     * @return a map of wa_id to profile name
     */
    private static Map<String, String> pushNamesByWaId(JSONObject value) {
        var result = new HashMap<String, String>();
        var contacts = value.getJSONArray("contacts");
        if (contacts == null) {
            return result;
        }
        for (var index = 0; index < contacts.size(); index++) {
            var contact = contacts.getJSONObject(index);
            var waId = contact.getString("wa_id");
            var profile = contact.getJSONObject("profile");
            if (waId != null && profile != null) {
                result.put(waId, profile.getString("name"));
            }
        }
        return result;
    }

    /**
     * Builds a user JID from an E.164 phone number.
     *
     * @param phoneNumber the phone number in E.164 form, without a leading plus
     * @return the user JID
     */
    private static Jid userJid(String phoneNumber) {
        return Jid.of(phoneNumber, JidServer.user());
    }
}
