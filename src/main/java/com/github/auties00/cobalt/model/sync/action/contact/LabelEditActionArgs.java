package com.github.auties00.cobalt.model.sync.action.contact;


import com.github.auties00.cobalt.model.sync.SyncActionArgs;

/**
 * Index arguments for {@link LabelEditAction}.
 *
 * <p>The sync index produced is {@code ["label_edit", labelId]}.
 *
 * @param labelId the unique identifier of the label being created, edited, or deleted
 */
public record LabelEditActionArgs(String labelId) implements SyncActionArgs {
    /**
     * {@inheritDoc}
     *
     * @return a single-element array containing the label identifier
     */
    @Override
    public String[] toIndexArgs() {
        return new String[]{labelId};
    }
}
