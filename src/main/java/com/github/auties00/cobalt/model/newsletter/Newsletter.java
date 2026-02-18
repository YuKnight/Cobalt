package com.github.auties00.cobalt.model.newsletter;

import com.github.auties00.cobalt.model.jid.Jid;
import com.github.auties00.cobalt.model.jid.JidProvider;
import com.github.auties00.cobalt.model.mixin.InstantSecondsMixin;
import com.github.auties00.collections.ConcurrentLinkedHashMap;
import it.auties.protobuf.annotation.ProtobufMessage;
import it.auties.protobuf.annotation.ProtobufProperty;
import it.auties.protobuf.model.ProtobufType;

import java.time.Instant;
import java.util.*;

/**
 * A WhatsApp newsletter (channel), containing its JID, state, metadata,
 * viewer metadata, and a collection of messages.
 *
 * <p>Newsletters are not end-to-end encrypted. Message content is
 * received as plaintext protobuf and decoded using the standard
 * {@code Message} protobuf specification.
 *
 */
@ProtobufMessage
public final class Newsletter implements JidProvider {
    @ProtobufProperty(index = 1, type = ProtobufType.STRING)
    Jid jid;

    @ProtobufProperty(index = 2, type = ProtobufType.MESSAGE)
    NewsletterState state;

    @ProtobufProperty(index = 3, type = ProtobufType.MESSAGE)
    NewsletterMetadata metadata;

    @ProtobufProperty(index = 4, type = ProtobufType.MESSAGE)
    NewsletterViewerMetadata viewerMetadata;


    @ProtobufProperty(index = 5, type = ProtobufType.MESSAGE)
    final Messages messages;

    @ProtobufProperty(index = 6, type = ProtobufType.INT32)
    int unreadMessagesCount;

    @ProtobufProperty(index = 7, type = ProtobufType.UINT64, mixins = InstantSecondsMixin.class)
    Instant timestamp;

    /**
     * Constructs a new {@code Newsletter} with the specified fields.
     *
     * @param jid                 the newsletter JID, must not be {@code null}
     * @param state               the newsletter state, may be {@code null}
     * @param metadata            the newsletter metadata, may be {@code null}
     * @param viewerMetadata      the viewer's metadata, may be {@code null}
     * @param messages            the message collection, may be {@code null}
     * @param unreadMessagesCount the number of unread messages
     * @param timestamp    the timestamp in seconds of the last activity
     * @throws NullPointerException if {@code jid} is {@code null}
     */
    Newsletter(Jid jid, NewsletterState state, NewsletterMetadata metadata, NewsletterViewerMetadata viewerMetadata, Messages messages, int unreadMessagesCount, Instant timestamp) {
        this.jid = Objects.requireNonNull(jid, "value cannot be null");
        this.state = state;
        this.metadata = metadata;
        this.viewerMetadata = viewerMetadata;
        this.messages = messages;
        this.unreadMessagesCount = unreadMessagesCount;
        this.timestamp = timestamp;
    }

    /**
     * Adds a message to this newsletter's message collection.
     *
     * @param info the message to add
     */
    public void addMessage(NewsletterMessageInfo info) {
        messages.add(info);
    }

    /**
     * Removes a message from this newsletter by its identifier.
     *
     * @param messageId the message identifier
     * @return {@code true} if the message was removed
     */
    public boolean removeMessage(String messageId) {
        return messages.removeMessageInfoById(messageId);
    }

    /**
     * Removes all messages from this newsletter.
     */
    public void removeMessages() {
        messages.clear();
    }

    /**
     * Returns an unmodifiable sequenced view of the messages.
     *
     * @return the messages view, never {@code null}
     */
    public SequencedCollection<NewsletterMessageInfo> messages() {
        return messages.messageInfoView();
    }

    /**
     * Finds a message by its identifier.
     *
     * @param messageId the message identifier
     * @return an {@link Optional} containing the message, or empty if not found
     */
    public Optional<NewsletterMessageInfo> getMessageById(String messageId) {
        return messages.getMessageInfoById(messageId);
    }

    /**
     * Returns the oldest message in this newsletter, if any.
     *
     * @return an {@link Optional} containing the oldest message,
     *         or empty if there are no messages
     */
    public Optional<NewsletterMessageInfo> oldestMessage() {
        return messages.getOldestMessageInfo();
    }

    /**
     * Returns the newest message in this newsletter, if any.
     *
     * @return an {@link Optional} containing the newest message,
     *         or empty if there are no messages
     */
    public Optional<NewsletterMessageInfo> newestMessage() {
        return messages.getNewestMessageInfo();
    }

    /**
     * Returns this newsletter's JID.
     *
     * @return the JID, never {@code null}
     */
    @Override
    public Jid toJid() {
        return jid;
    }

    /**
     * Returns this newsletter's JID.
     *
     * @return the JID, never {@code null}
     */
    public Jid jid() {
        return jid;
    }

    /**
     * Sets this newsletter's JID.
     *
     * @param jid the JID, must not be {@code null}
     * @return this instance for chaining
     * @throws NullPointerException if {@code jid} is {@code null}
     */
    public Newsletter setJid(Jid jid) {
        this.jid = Objects.requireNonNull(jid, "jid cannot be null");
        return this;
    }

    /**
     * Returns the newsletter state, if available.
     *
     * @return an {@link Optional} containing the state, or empty if not set
     */
    public Optional<NewsletterState> state() {
        return Optional.ofNullable(state);
    }

    /**
     * Sets the newsletter state.
     *
     * @param state the newsletter state
     * @return this instance for chaining
     */
    public Newsletter setState(NewsletterState state) {
        this.state = state;
        return this;
    }

    /**
     * Sets the newsletter metadata.
     *
     * @param metadata the newsletter metadata
     * @return this instance for chaining
     */
    public Newsletter setMetadata(NewsletterMetadata metadata) {
        this.metadata = metadata;
        return this;
    }

    /**
     * Returns the newsletter metadata, if available.
     *
     * @return an {@link Optional} containing the metadata, or empty if not set
     */
    public Optional<NewsletterMetadata> metadata() {
        return Optional.ofNullable(metadata);
    }

    /**
     * Returns the viewer's metadata, if available.
     *
     * @return an {@link Optional} containing the viewer metadata,
     *         or empty if not set
     */
    public Optional<NewsletterViewerMetadata> viewerMetadata() {
        return Optional.ofNullable(viewerMetadata);
    }

    /**
     * Sets the viewer's metadata.
     *
     * @param viewerMetadata the viewer metadata
     * @return this instance for chaining
     */
    public Newsletter setViewerMetadata(NewsletterViewerMetadata viewerMetadata) {
        this.viewerMetadata = viewerMetadata;
        return this;
    }

    /**
     * Returns the number of unread messages.
     *
     * @return the unread messages count
     */
    public int unreadMessagesCount() {
        return unreadMessagesCount;
    }

    /**
     * Sets the number of unread messages.
     *
     * @param unreadMessagesCount the unread messages count
     * @return this instance for chaining
     */
    public Newsletter setUnreadMessagesCount(int unreadMessagesCount) {
        this.unreadMessagesCount = unreadMessagesCount;
        return this;
    }

    /**
     * Returns the timestamp in seconds of the last activity.
     *
     * @return the timestamp in seconds
     */
    public Optional<Instant> timestampSeconds() {
        return Optional.ofNullable(timestamp);
    }

    /**
     * Sets the timestamp in seconds of the last activity.
     *
     * @param timestamp the timestamp in seconds
     * @return this instance for chaining
     */
    public Newsletter setTimestamp(Instant timestamp) {
        this.timestamp = timestamp;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        return o == this || o instanceof Newsletter that
                            && unreadMessagesCount == that.unreadMessagesCount
                            && Objects.equals(jid, that.jid)
                            && Objects.equals(state, that.state)
                            && Objects.equals(metadata, that.metadata)
                            && Objects.equals(viewerMetadata, that.viewerMetadata)
                            && Objects.equals(messages, that.messages)
                            && Objects.equals(timestamp, that.timestamp);
    }

    @Override
    public int hashCode() {
        return Objects.hash(jid, state, metadata, viewerMetadata, messages, unreadMessagesCount, timestamp);
    }

    /**
     * An ordered collection of {@link NewsletterMessageInfo} backed by a
     * {@link ConcurrentLinkedHashMap}, keyed by message identifier.
     */
    static final class Messages extends AbstractCollection<NewsletterMessageInfo> {
        private final ConcurrentLinkedHashMap<String, NewsletterMessageInfo> backing;

        /**
         * Constructs an empty {@code Messages} collection.
         */
        Messages() {
            this.backing = new ConcurrentLinkedHashMap<>();
        }

        @Override
        public boolean add(NewsletterMessageInfo msg) {
            if (msg == null || msg.message == null) {
                return false;
            }
            var id = msg.key().id();
            if(id.isEmpty()) {
                return false;
            }
            backing.put(id.get(), msg);
            return true;
        }

        /**
         * Finds a message by its identifier.
         *
         * @param id the message identifier
         * @return an {@link Optional} containing the message, or empty if not found
         */
        Optional<NewsletterMessageInfo> getMessageInfoById(String id) {
            if (id == null) {
                return Optional.empty();
            }
            var msg = backing.get(id);
            return Optional.ofNullable(msg);
        }

        /**
         * Removes a message by its identifier.
         *
         * @param id the message identifier
         * @return {@code true} if the message was removed
         */
        boolean removeMessageInfoById(String id) {
            return id != null && backing.remove(id) != null;
        }

        /**
         * Returns the newest message, if any.
         *
         * @return an {@link Optional} containing the newest message,
         *         or empty if the collection is empty
         */
        Optional<NewsletterMessageInfo> getNewestMessageInfo() {
            var entry = backing.lastEntry();
            return entry != null ? Optional.ofNullable(entry.getValue()) : Optional.empty();
        }

        /**
         * Returns the oldest message, if any.
         *
         * @return an {@link Optional} containing the oldest message,
         *         or empty if the collection is empty
         */
        Optional<NewsletterMessageInfo> getOldestMessageInfo() {
            var entry = backing.firstEntry();
            return entry != null ? Optional.ofNullable(entry.getValue()) : Optional.empty();
        }

        /**
         * Returns an unmodifiable sequenced view of the messages.
         *
         * @return the sequenced view, never {@code null}
         */
        SequencedCollection<NewsletterMessageInfo> messageInfoView() {
            return messageInfoView(backing.sequencedValues());
        }

        private SequencedCollection<NewsletterMessageInfo> messageInfoView(SequencedCollection<NewsletterMessageInfo> data) {
            return new SequencedCollection<>() {
                @Override
                public SequencedCollection<NewsletterMessageInfo> reversed() {
                    return messageInfoView(data.reversed());
                }

                @Override
                public int size() {
                    return data.size();
                }

                @Override
                public boolean isEmpty() {
                    return data.isEmpty();
                }

                @Override
                public boolean contains(Object o) {
                    return o instanceof NewsletterMessageInfo info
                           && info.key().id().isPresent()
                           && backing.containsKey(info.key().id().get());
                }

                @Override
                public Iterator<NewsletterMessageInfo> iterator() {
                    var delegate = data.iterator();
                    return new Iterator<>() {
                        @Override
                        public boolean hasNext() {
                            return delegate.hasNext();
                        }

                        @Override
                        public NewsletterMessageInfo next() {
                            return delegate.next();
                        }
                    };
                }

                @Override
                public Object[] toArray() {
                    return data.toArray();
                }

                @Override
                public <T> T[] toArray(T[] a) {
                    return data.toArray(a);
                }

                @Override
                public boolean add(NewsletterMessageInfo info) {
                    throw new UnsupportedOperationException();
                }

                @Override
                public boolean remove(Object o) {
                    throw new UnsupportedOperationException();
                }

                @Override
                public boolean containsAll(Collection<?> c) {
                    for (var entry : c) {
                        if (!contains(entry)) {
                            return false;
                        }
                    }
                    return true;
                }

                @Override
                public boolean addAll(Collection<? extends NewsletterMessageInfo> c) {
                    throw new UnsupportedOperationException();
                }

                @Override
                public boolean removeAll(Collection<?> c) {
                    throw new UnsupportedOperationException();
                }

                @Override
                public boolean retainAll(Collection<?> c) {
                    throw new UnsupportedOperationException();
                }

                @Override
                public void clear() {
                    throw new UnsupportedOperationException();
                }
            };
        }

        @Override
        public Iterator<NewsletterMessageInfo> iterator() {
            return backing.sequencedValues().iterator();
        }

        @Override
        public int size() {
            return backing.size();
        }
    }
}
