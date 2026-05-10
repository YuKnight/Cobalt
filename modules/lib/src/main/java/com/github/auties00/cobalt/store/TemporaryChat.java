package com.github.auties00.cobalt.store;

import com.github.auties00.cobalt.model.chat.Chat;
import com.github.auties00.cobalt.model.chat.ChatMessageInfo;
import com.github.auties00.cobalt.model.jid.Jid;
import com.github.auties00.collections.ConcurrentLinkedHashMap;

import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;

/**
 * In-memory {@link Chat}
 * subclass used as the value type of the {@link TemporaryStore#chats}
 * map. Backs every message accessor with a single
 * {@link ConcurrentLinkedHashMap} keyed by message id.
 */
final class TemporaryChat extends Chat {
    /**
     * The in-memory message store, keyed by message id, preserving
     * insertion order.
     */
    private final ConcurrentLinkedHashMap<String, ChatMessageInfo> messages;

    /**
     * Constructs a new in-memory chat with the given JID and every
     * other metadata field defaulted to {@code null}.
     *
     * @param jid the chat JID
     */
    TemporaryChat(Jid jid) {
        super(jid, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null);
        this.messages = new ConcurrentLinkedHashMap<>();
    }

    @Override
    public Stream<ChatMessageInfo> messages() {
        return messages.sequencedValues().stream();
    }

    @Override
    public int messageCount() {
        return messages.size();
    }

    @Override
    public void addMessage(ChatMessageInfo info) {
        Objects.requireNonNull(info, "info cannot be null");
        info.key().id().ifPresent(id -> messages.put(id, info));
    }

    @Override
    public boolean removeMessage(String id) {
        return id != null && messages.remove(id) != null;
    }

    @Override
    public void removeMessages() {
        messages.clear();
    }

    @Override
    public Optional<ChatMessageInfo> getMessageById(String id) {
        return id == null ? Optional.empty() : Optional.ofNullable(messages.get(id));
    }

    @Override
    public Optional<ChatMessageInfo> newestMessage() {
        var entry = messages.lastEntry();
        return entry == null ? Optional.empty() : Optional.of(entry.getValue());
    }

    @Override
    public Optional<ChatMessageInfo> oldestMessage() {
        var entry = messages.firstEntry();
        return entry == null ? Optional.empty() : Optional.of(entry.getValue());
    }
}
