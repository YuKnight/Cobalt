package com.github.auties00.cobalt.model.sync.action.media;

import com.github.auties00.cobalt.model.jid.Jid;
import com.github.auties00.cobalt.model.sync.SyncAction;
import com.github.auties00.cobalt.model.sync.SyncPatchType;

import it.auties.protobuf.annotation.*;
import it.auties.protobuf.model.*;
import java.util.Optional;
import java.util.OptionalLong;

/**
 * A sync action that propagates the creation, editing, or deletion of a
 * chat-scoped note across linked devices.
 *
 * <p>Notes are short pieces of text the user attaches to a specific chat, such
 * as reminders about a contact or ongoing conversation. The action carries
 * both the note content (or its absence, in case of deletion) and the chat it
 * is attached to. The target note is identified by the identifier carried in
 * the associated {@link NoteEditActionArgs}.
 */
@ProtobufMessage(name = "SyncActionValue.NoteEditAction")
public final class NoteEditAction implements SyncAction<NoteEditActionArgs> {
    /**
     * The app-state action name that identifies this action type on the wire.
     */
    public static final String ACTION_NAME = "note_edit";

    /**
     * The app-state action version that identifies this action revision on the
     * wire.
     */
    public static final int ACTION_VERSION = 7;

    /**
     * The app-state collection that stores this action type.
     */
    public static final SyncPatchType COLLECTION_NAME = SyncPatchType.REGULAR_LOW;

    /**
     * Returns the action name used to route this action through the app-state
     * sync pipeline.
     *
     * @return the canonical action name
     */
    @Override
    public String actionName() {
        return ACTION_NAME;
    }

    /**
     * Returns the action version used to route this action through the
     * app-state sync pipeline.
     *
     * @return the canonical action version
     */
    @Override
    public int actionVersion() {
        return ACTION_VERSION;
    }


    /**
     * The structural category of the note payload.
     */
    @ProtobufProperty(index = 1, type = ProtobufType.ENUM)
    NoteType type;

    /**
     * The JID of the chat this note is attached to.
     */
    @ProtobufProperty(index = 2, type = ProtobufType.STRING)
    Jid chatJid;

    /**
     * The epoch-second timestamp at which the note was created.
     */
    @ProtobufProperty(index = 3, type = ProtobufType.INT64)
    Long createdAt;

    /**
     * Whether this action represents a deletion of the target note rather
     * than a create or update.
     */
    @ProtobufProperty(index = 4, type = ProtobufType.BOOL)
    Boolean deleted;

    /**
     * The note body for {@link NoteType#UNSTRUCTURED} notes.
     */
    @ProtobufProperty(index = 5, type = ProtobufType.STRING)
    String unstructuredContent;


    /**
     * Constructs a new {@code NoteEditAction} carrying the supplied note
     * fields.
     *
     * @param type                the structural category of the note
     * @param chatJid             the chat the note is attached to
     * @param createdAt           the epoch-second creation timestamp
     * @param deleted             whether the action deletes the note
     * @param unstructuredContent the free-text body for unstructured notes
     */
    NoteEditAction(NoteType type, Jid chatJid, Long createdAt, Boolean deleted, String unstructuredContent) {
        this.type = type;
        this.chatJid = chatJid;
        this.createdAt = createdAt;
        this.deleted = deleted;
        this.unstructuredContent = unstructuredContent;
    }

    /**
     * Returns the structural category of the note payload.
     *
     * @return the note type, or {@link Optional#empty()} if unset
     */
    public Optional<NoteType> type() {
        return Optional.ofNullable(type);
    }

    /**
     * Returns the JID of the chat the note is attached to.
     *
     * @return the chat JID, or {@link Optional#empty()} if unset
     */
    public Optional<Jid> chatJid() {
        return Optional.ofNullable(chatJid);
    }

    /**
     * Returns the epoch-second timestamp at which the note was created.
     *
     * @return the creation timestamp, or {@link OptionalLong#empty()} if unset
     */
    public OptionalLong createdAt() {
        return createdAt == null ? OptionalLong.empty() : OptionalLong.of(createdAt);
    }

    /**
     * Returns whether this action represents a deletion of the target note.
     *
     * @return {@code true} if the note is being deleted, {@code false} otherwise
     */
    public boolean deleted() {
        return deleted != null && deleted;
    }

    /**
     * Returns the free-text body for {@link NoteType#UNSTRUCTURED} notes.
     *
     * @return the note body, or {@link Optional#empty()} if unset
     */
    public Optional<String> unstructuredContent() {
        return Optional.ofNullable(unstructuredContent);
    }

    /**
     * Sets the structural category of the note payload.
     *
     * @param type the new note type, or {@code null} to clear it
     */
    public void setType(NoteType type) {
        this.type = type;
    }

    /**
     * Sets the JID of the chat the note is attached to.
     *
     * @param chatJid the new chat JID, or {@code null} to clear it
     */
    public void setChatJid(Jid chatJid) {
        this.chatJid = chatJid;
    }

    /**
     * Sets the epoch-second timestamp at which the note was created.
     *
     * @param createdAt the new creation timestamp, or {@code null} to clear it
     */
    public void setCreatedAt(Long createdAt) {
        this.createdAt = createdAt;
    }

    /**
     * Sets whether this action represents a deletion of the target note.
     *
     * @param deleted the new deletion flag, or {@code null} to clear it
     */
    public void setDeleted(Boolean deleted) {
        this.deleted = deleted;
    }

    /**
     * Sets the free-text body for {@link NoteType#UNSTRUCTURED} notes.
     *
     * @param unstructuredContent the new note body, or {@code null} to clear it
     */
    public void setUnstructuredContent(String unstructuredContent) {
        this.unstructuredContent = unstructuredContent;
    }

    /**
     * The structural category of a chat note payload.
     *
     * <p>Notes come in two flavours: a simple free-text body or a structured
     * representation whose exact shape is defined outside this action.
     */
    @ProtobufEnum(name = "SyncActionValue.NoteEditAction.NoteType")
    public static enum NoteType {
        /**
         * A note whose body is a single free-form text string carried in
         * {@link NoteEditAction#unstructuredContent()}.
         */
        UNSTRUCTURED(1),
        /**
         * A note with a richer, structured payload defined outside the scope
         * of this action.
         */
        STRUCTURED(2);

        /**
         * Constructs a new {@code NoteType} constant with the supplied wire
         * index.
         *
         * @param index the protobuf wire index
         */
        NoteType(@ProtobufEnumIndex int index) {
            this.index = index;
        }

        /**
         * The protobuf wire index for this constant.
         */
        final int index;

        /**
         * Returns the protobuf wire index for this constant.
         *
         * @return the wire index
         */
        public int index() {
            return this.index;
        }
    }


}
