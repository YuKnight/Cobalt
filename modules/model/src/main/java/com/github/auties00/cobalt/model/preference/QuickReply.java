package com.github.auties00.cobalt.model.preference;

import it.auties.protobuf.annotation.ProtobufMessage;
import it.auties.protobuf.annotation.ProtobufProperty;
import it.auties.protobuf.model.ProtobufType;

import java.util.List;
import java.util.Objects;

/**
 * Represents a business quick reply template stored locally for the linked
 * device.
 *
 * <p>A quick reply consists of a stable {@code id} (the primary key in the
 * WhatsApp Web {@code WAWebSchemaQuickReply} IndexedDB table), a user-visible
 * {@code shortcut} that the user types to expand the template, the
 * {@code message} body that gets sent in the shortcut's place, an optional
 * list of search {@code keywords}, and a usage {@code count} maintained by
 * the WhatsApp Web client.
 *
 * <p>Instances are immutable and built via the generated
 * {@code QuickReplyBuilder}.
 *
 * @implNote WAWebSchemaQuickReply — IndexedDB schema definition for the
 *           {@code quick-reply} table
 */
@ProtobufMessage
public final class QuickReply {
    /**
     * The stable identifier for this quick reply, taken from the second
     * element of the sync index ({@code indexParts[1]}).
     *
     * <p>This is the primary key in WhatsApp Web's {@code WAWebSchemaQuickReply}
     * IndexedDB table and the value used by Cobalt's {@code WhatsAppStore} to
     * key the quick reply map. It is required and must be non-{@code null};
     * mutations that change the {@code shortcut} while preserving the
     * {@code id} are routed to the same store entry, mirroring WA Web's
     * primary-key-based upsert semantics.
     *
     * @implNote WAWebSchemaQuickReply.id (primary key)
     */
    @ProtobufProperty(index = 5, type = ProtobufType.STRING)
    final String id;

    /**
     * The user-typed shortcut text that triggers the quick reply expansion.
     *
     * @implNote WAWebSchemaQuickReply.shortcut
     */
    @ProtobufProperty(index = 1, type = ProtobufType.STRING)
    final String shortcut;

    /**
     * The message body that is sent in place of the {@code shortcut}.
     *
     * @implNote WAWebSchemaQuickReply.message
     */
    @ProtobufProperty(index = 2, type = ProtobufType.STRING)
    final String message;

    /**
     * The list of search keywords associated with this quick reply.
     *
     * @implNote WAWebSchemaQuickReply.keywords
     */
    @ProtobufProperty(index = 3, type = ProtobufType.STRING)
    final List<String> keywords;

    /**
     * The usage counter maintained by the WhatsApp Web client.
     *
     * @implNote WAWebSchemaQuickReply.count
     */
    @ProtobufProperty(index = 4, type = ProtobufType.INT32)
    final int count;

    /**
     * Constructs a new quick reply with the given field values.
     *
     * @param id       the non-{@code null} stable identifier
     * @param shortcut the user-typed shortcut text
     * @param message  the message body
     * @param keywords the search keywords
     * @param count    the usage counter
     * @implNote WAWebSchemaQuickReply — table row constructor
     */
    QuickReply(String id, String shortcut, String message, List<String> keywords, int count) {
        this.id = Objects.requireNonNull(id, "id cannot be null");
        this.shortcut = shortcut;
        this.message = message;
        this.keywords = keywords;
        this.count = count;
    }

    /**
     * Returns the stable identifier of this quick reply.
     *
     * <p>This value is the primary key used by Cobalt's {@code WhatsAppStore}
     * and corresponds to the {@code id} column of WhatsApp Web's
     * {@code WAWebSchemaQuickReply} IndexedDB table.
     *
     * @return the non-{@code null} stable identifier
     * @implNote WAWebSchemaQuickReply.id (primary key)
     */
    public String id() {
        return id;
    }

    /**
     * Returns the user-typed shortcut text that triggers this quick reply.
     *
     * @return the shortcut text
     * @implNote WAWebSchemaQuickReply.shortcut
     */
    public String shortcut() {
        return shortcut;
    }

    /**
     * Returns the message body that is sent in place of the shortcut.
     *
     * @return the message body
     * @implNote WAWebSchemaQuickReply.message
     */
    public String message() {
        return message;
    }

    /**
     * Returns the search keywords associated with this quick reply.
     *
     * @return the keyword list
     * @implNote WAWebSchemaQuickReply.keywords
     */
    public List<String> keywords() {
        return keywords;
    }

    /**
     * Returns the usage counter maintained by the WhatsApp Web client.
     *
     * @return the usage counter
     * @implNote WAWebSchemaQuickReply.count
     */
    public int count() {
        return count;
    }
}
