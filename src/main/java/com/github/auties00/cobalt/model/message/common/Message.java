package com.github.auties00.cobalt.model.message.common;

import com.github.auties00.cobalt.model.message.standard.*;

/**
 * A model interface that represents a message sent by a contact or by Whatsapp.
 */
public sealed interface Message
        permits ButtonMessage, ContextualMessage, EncryptedMessage, PaymentMessage, ServerMessage, CallMessage, CommentMessage, EmptyMessage, EventResponseMessage, KeepInChatMessage, NewsletterAdminInviteMessage, PollUpdateMessage, ReactionMessage,
        BCallMessage, CallLogMessage, PlaceholderMessage, QuestionResponseMessage, ScheduledCallCreationMessage, ScheduledCallEditMessage, StatusNotificationMessage, StatusQuestionAnswerMessage, StatusQuotedMessage, StatusStickerInteractionMessage {
    /**
     * Return message type
     *
     * @return a non-null message type
     */
    Type type();

    /**
     * Return message category
     *
     * @return a non-null message category
     */
    Category category();

    /**
     * The constants of this enumerated type describe the various types of messages that a
     * {@link MessageContainer} can wrap
     */
    enum Type {
        /**
         * Empty
         */
        EMPTY,
        /**
         * Text
         */
        TEXT,
        /**
         * Sender key distribution
         */
        SENDER_KEY_DISTRIBUTION,
        /**
         * Image
         */
        IMAGE,
        /**
         * Contact
         */
        CONTACT,
        /**
         * Location
         */
        LOCATION,
        /**
         * Document
         */
        DOCUMENT,
        /**
         * Audio
         */
        AUDIO,
        /**
         * Video
         */
        VIDEO,
        /**
         * Protocol
         */
        PROTOCOL,
        /**
         * Contact array
         */
        CONTACT_ARRAY,
        /**
         * Highly structured
         */
        HIGHLY_STRUCTURED,
        /**
         * Send payment
         */
        SEND_PAYMENT,
        /**
         * Live location
         */
        LIVE_LOCATION,
        /**
         * Request payment
         */
        REQUEST_PAYMENT,
        /**
         * Decline payment request
         */
        DECLINE_PAYMENT_REQUEST,
        /**
         * Cancel payment request
         */
        CANCEL_PAYMENT_REQUEST,
        /**
         * Template
         */
        TEMPLATE,
        /**
         * Sticker
         */
        STICKER,
        /**
         * Group invite
         */
        GROUP_INVITE,
        /**
         * Template reply
         */
        TEMPLATE_REPLY,
        /**
         * Product
         */
        PRODUCT,
        /**
         * Device sent
         */
        DEVICE_SENT,
        /**
         * Device sync
         */
        DEVICE_SYNC,
        /**
         * List
         */
        LIST,
        /**
         * View once
         */
        VIEW_ONCE,
        /**
         * Order
         */
        PAYMENT_ORDER,
        /**
         * List newsletters
         */
        LIST_RESPONSE,
        /**
         * Ephemeral
         */
        EPHEMERAL,
        /**
         * Payment invoice
         */
        PAYMENT_INVOICE,
        /**
         * Buttons newsletters
         */
        BUTTONS,
        /**
         * Buttons newsletters
         */
        BUTTONS_RESPONSE,
        /**
         * Payment invite
         */
        PAYMENT_INVITE,
        /**
         * Interactive
         */
        INTERACTIVE,
        /**
         * Reaction
         */
        REACTION,
        /**
         * Interactive newsletters
         */
        INTERACTIVE_RESPONSE,
        /**
         * Native flow newsletters
         */
        NATIVE_FLOW_RESPONSE,
        /**
         * Keep in chat
         */
        KEEP_IN_CHAT,
        /**
         * Poll creation
         */
        POLL_CREATION,
        /**
         * Poll update
         */
        POLL_UPDATE,
        /**
         * Request phone value
         */
        REQUEST_PHONE_NUMBER,
        /**
         * Encrypted reaction
         */
        ENCRYPTED_REACTION,
        /**
         * A call
         */
        CALL,
        /**
         * Sticker sync
         */
        STICKER_SYNC,
        /**
         * Text edit
         */
        EDITED,
        /**
         * Newsletter admin invite
         */
        NEWSLETTER_ADMIN_INVITE,
        /**
         * Pin in chat
         */
        PIN_IN_CHAT,
        /**
         * Encrypted event response
         */
        ENCRYPTED_EVENT_RESPONSE,
        /**
         * Secret encrypted message
         */
        SECRET_ENCRYPTED,
        /**
         * Calendar event message
         */
        EVENT,
        /**
         * Event response
         */
        EVENT_RESPONSE,
        /**
         * Poll result snapshot message
         */
        POLL_RESULT_SNAPSHOT,
        /**
         * Comment message
         */
        COMMENT,
        /**
         * Encrypted comment message
         */
        ENCRYPTED_COMMENT,
        /**
         * Scheduled call creation
         */
        SCHEDULED_CALL_CREATION,
        /**
         * Scheduled call edit
         */
        SCHEDULED_CALL_EDIT,
        /**
         * Call log
         */
        CALL_LOG,
        /**
         * Message history bundle
         */
        MESSAGE_HISTORY_BUNDLE,
        /**
         * Business call
         */
        BCALL,
        /**
         * Placeholder
         */
        PLACEHOLDER,
        /**
         * Album
         */
        ALBUM,
        /**
         * Sticker pack
         */
        STICKER_PACK,
        /**
         * Status notification
         */
        STATUS_NOTIFICATION,
        /**
         * Message history notice
         */
        MESSAGE_HISTORY_NOTICE,
        /**
         * Status question answer
         */
        STATUS_QUESTION_ANSWER,
        /**
         * Question response
         */
        QUESTION_RESPONSE,
        /**
         * Status quoted
         */
        STATUS_QUOTED,
        /**
         * Status sticker interaction
         */
        STATUS_STICKER_INTERACTION,
        /**
         * Newsletter follower invite
         */
        NEWSLETTER_FOLLOWER_INVITE,
        /**
         * Newsletter question wrapper
         */
        NEWSLETTER_QUESTION,
        /**
         * Newsletter question reply wrapper
         */
        NEWSLETTER_QUESTION_REPLY,
        /**
         * Document with caption wrapper
         */
        DOCUMENT_WITH_CAPTION,
        /**
         * Bot invoke wrapper
         */
        BOT_INVOKE,
        /**
         * Group mentioned wrapper
         */
        GROUP_MENTIONED,
        /**
         * Lottie sticker wrapper
         */
        LOTTIE_STICKER,
        /**
         * Event cover image wrapper
         */
        EVENT_COVER_IMAGE,
        /**
         * Status mention wrapper
         */
        STATUS_MENTION,
        /**
         * Poll creation option image wrapper
         */
        POLL_CREATION_OPTION_IMAGE,
        /**
         * Associated child message wrapper
         */
        ASSOCIATED_CHILD,
        /**
         * Group status mention wrapper
         */
        GROUP_STATUS_MENTION,
        /**
         * Status add yours wrapper
         */
        STATUS_ADD_YOURS,
        /**
         * Group status wrapper
         */
        GROUP_STATUS,
        /**
         * Limit sharing wrapper
         */
        LIMIT_SHARING,
        /**
         * Bot task wrapper
         */
        BOT_TASK,
        /**
         * Bot forwarded wrapper
         */
        BOT_FORWARDED,
        /**
         * Newsletter admin profile wrapper
         */
        NEWSLETTER_ADMIN_PROFILE
    }

    /**
     * The constants of this enumerated type describe the various categories of messages that a
     * {@link MessageContainer} can wrap
     */
    enum Category {
        /**
         * Device message
         */
        BUTTON,
        /**
         * Payment message
         */
        PAYMENT,
        /**
         * Payment message
         */
        MEDIA,
        /**
         * Server message
         */
        SERVER,
        /**
         * Encrypted
         */
        ENCRYPTED,
        /**
         * Standard message
         */
        STANDARD
    }
}
