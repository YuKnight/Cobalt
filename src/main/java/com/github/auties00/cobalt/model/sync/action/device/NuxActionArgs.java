package com.github.auties00.cobalt.model.sync.action.device;


import com.github.auties00.cobalt.model.sync.SyncActionArgs;

/**
 * Index arguments for {@link NuxAction}.
 *
 * <p>The sync index produced is {@code ["nux", nuxKey]}.
 * The NUX (New User Experience) key identifies which onboarding step or
 * feature-discovery prompt this action refers to.
 *
 * @param nuxKey the NUX identifier key (e.g. a feature or onboarding step name)
 */
public record NuxActionArgs(String nuxKey) implements SyncActionArgs {
    /**
     * {@inheritDoc}
     *
     * @return a single-element array containing the NUX key
     */
    @Override
    public String[] toIndexArgs() {
        return new String[]{nuxKey};
    }
}
