package com.github.auties00.cobalt.store;

import com.github.auties00.cobalt.model.jid.Jid;
import com.github.auties00.cobalt.model.newsletter.Newsletter;
import com.github.auties00.cobalt.model.newsletter.NewsletterMessageInfo;
import com.github.auties00.collections.ConcurrentLinkedHashMap;

import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;

/**
 * In-memory {@link Newsletter}
 * subclass used as the value type of the
 * {@link TemporaryStore#newsletters} map. Mirrors {@link TemporaryChat} but
 * keys messages by the message-key id since
 * {@link NewsletterMessageInfo}
 * exposes both an id-string and a numeric server id.
 */
final class TemporaryNewsletter extends Newsletter {
    /**
     * The in-memory message store, keyed by message-key id (or the
     * stringified server id when the key id is absent), preserving
     * insertion order.
     */
    private final ConcurrentLinkedHashMap<String, NewsletterMessageInfo> messages;

    /**
     * Constructs a new in-memory newsletter with the given JID and
     * every other metadata field defaulted.
     *
     * @param jid the newsletter JID
     */
    TemporaryNewsletter(Jid jid) {
        super(jid, null, null, null, 0, null);
        this.messages = new ConcurrentLinkedHashMap<>();
    }

    @Override
    public void addMessage(NewsletterMessageInfo info) {
        Objects.requireNonNull(info, "info cannot be null");
        info.key().id().ifPresent(id -> messages.put(id, info));
    }

    @Override
    public boolean removeMessage(String messageId) {
        return messageId != null && messages.remove(messageId) != null;
    }

    @Override
    public void removeMessages() {
        messages.clear();
    }

    @Override
    public Stream<NewsletterMessageInfo> messages() {
        return messages.sequencedValues().stream();
    }

    @Override
    public int messageCount() {
        return messages.size();
    }

    @Override
    public Optional<NewsletterMessageInfo> getMessageById(String messageId) {
        if (messageId == null) {
            return Optional.empty();
        }
        var byId = messages.get(messageId);
        return Optional.ofNullable(byId);
    }

    @Override
    public Optional<NewsletterMessageInfo> oldestMessage() {
        var entry = messages.firstEntry();
        return entry == null ? Optional.empty() : Optional.of(entry.getValue());
    }

    @Override
    public Optional<NewsletterMessageInfo> newestMessage() {
        var entry = messages.lastEntry();
        return entry == null ? Optional.empty() : Optional.of(entry.getValue());
    }
}
