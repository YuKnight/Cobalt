package com.github.auties00.cobalt.store.inMemory;

import com.github.auties00.cobalt.model.jid.Jid;
import com.github.auties00.cobalt.model.newsletter.*;
import it.auties.protobuf.annotation.ProtobufMessage;

import java.time.Instant;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.SequencedCollection;

@ProtobufMessage
final class InMemoryNewsletter extends Newsletter {
    InMemoryNewsletter(Jid jid, NewsletterState state, NewsletterMetadata metadata, NewsletterViewerMetadata viewerMetadata, int unreadMessagesCount, Instant timestamp) {
        this.jid = Objects.requireNonNull(jid, "jid cannot be null");
        this.state = state;
        this.metadata = metadata;
        this.viewerMetadata = viewerMetadata;
        this.unreadMessagesCount = unreadMessagesCount;
        this.timestamp = timestamp;
    }

    @Override
    public void addMessage(NewsletterMessageInfo info) {
        Objects.requireNonNull(info, "info cannot be null");
    }

    @Override
    public boolean removeMessage(String messageId) {
        return false;
    }

    @Override
    public void removeMessages() {

    }

    @Override
    public SequencedCollection<NewsletterMessageInfo> messages() {
        return List.of();
    }

    @Override
    public Optional<NewsletterMessageInfo> getMessageById(String messageId) {
        return Optional.empty();
    }

    @Override
    public Optional<NewsletterMessageInfo> oldestMessage() {
        return Optional.empty();
    }

    @Override
    public Optional<NewsletterMessageInfo> newestMessage() {
        return Optional.empty();
    }
}
