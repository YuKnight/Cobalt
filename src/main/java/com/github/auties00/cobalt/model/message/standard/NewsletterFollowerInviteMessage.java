package com.github.auties00.cobalt.model.message.standard;

import com.github.auties00.cobalt.model.info.ContextInfo;
import com.github.auties00.cobalt.model.message.common.ContextualMessage;
import com.github.auties00.cobalt.model.message.common.Message;
import it.auties.protobuf.annotation.ProtobufMessage;
import it.auties.protobuf.annotation.ProtobufProperty;
import it.auties.protobuf.model.ProtobufType;

import java.util.Objects;
import java.util.Optional;

/**
 * A model class that represents a newsletter follower invite message.
 */
@ProtobufMessage(name = "Message.NewsletterFollowerInviteMessage")
public final class NewsletterFollowerInviteMessage implements ContextualMessage {
    @ProtobufProperty(index = 1, type = ProtobufType.STRING)
    final String newsletterJid;

    @ProtobufProperty(index = 2, type = ProtobufType.STRING)
    final String newsletterName;

    @ProtobufProperty(index = 3, type = ProtobufType.BYTES)
    final byte[] jpegThumbnail;

    @ProtobufProperty(index = 4, type = ProtobufType.STRING)
    final String caption;

    @ProtobufProperty(index = 5, type = ProtobufType.MESSAGE)
    ContextInfo contextInfo;

    NewsletterFollowerInviteMessage(String newsletterJid, String newsletterName, byte[] jpegThumbnail, String caption, ContextInfo contextInfo) {
        this.newsletterJid = newsletterJid;
        this.newsletterName = newsletterName;
        this.jpegThumbnail = jpegThumbnail;
        this.caption = caption;
        this.contextInfo = contextInfo;
    }

    public Optional<String> newsletterJid() {
        return Optional.ofNullable(newsletterJid);
    }

    public Optional<String> newsletterName() {
        return Optional.ofNullable(newsletterName);
    }

    public Optional<byte[]> jpegThumbnail() {
        return Optional.ofNullable(jpegThumbnail);
    }

    public Optional<String> caption() {
        return Optional.ofNullable(caption);
    }

    @Override
    public Optional<ContextInfo> contextInfo() {
        return Optional.ofNullable(contextInfo);
    }

    @Override
    public void setContextInfo(ContextInfo contextInfo) {
        this.contextInfo = contextInfo;
    }

    @Override
    public Type type() {
        return Type.NEWSLETTER_FOLLOWER_INVITE;
    }

    @Override
    public Category category() {
        return Category.STANDARD;
    }

    @Override
    public boolean equals(Object o) {
        return o instanceof NewsletterFollowerInviteMessage that
                && Objects.equals(newsletterJid, that.newsletterJid)
                && Objects.equals(newsletterName, that.newsletterName)
                && Objects.equals(caption, that.caption)
                && Objects.equals(contextInfo, that.contextInfo);
    }

    @Override
    public int hashCode() {
        return Objects.hash(newsletterJid, newsletterName, caption, contextInfo);
    }

    @Override
    public String toString() {
        return "NewsletterFollowerInviteMessage[name=" + newsletterName + ']';
    }
}
