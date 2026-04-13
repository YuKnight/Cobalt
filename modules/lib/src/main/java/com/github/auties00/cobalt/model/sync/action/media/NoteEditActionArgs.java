package com.github.auties00.cobalt.model.sync.action.media;


import com.github.auties00.cobalt.model.sync.SyncActionArgs;

/**
 * Index arguments for {@link NoteEditAction}.
 *
 * <p>The sync index produced is {@code ["note_edit", noteId]}.
 *
 * @param noteId the unique identifier of the note being created, edited, or deleted
 */
public record NoteEditActionArgs(String noteId) implements SyncActionArgs {
    /**
     * {@inheritDoc}
     *
     * @return a single-element array containing the note identifier
     */
    @Override
    public String[] toIndexArgs() {
        return new String[]{noteId};
    }
}
