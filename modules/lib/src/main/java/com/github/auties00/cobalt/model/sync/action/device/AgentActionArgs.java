package com.github.auties00.cobalt.model.sync.action.device;


import com.github.auties00.cobalt.model.sync.SyncActionArgs;

/**
 * Index arguments for {@link AgentAction}.
 *
 * <p>The sync index produced is {@code ["deviceAgent", agentId]}.
 *
 * @param agentId the device-agent identifier string used as the index key
 */
public record AgentActionArgs(String agentId) implements SyncActionArgs {
    /**
     * {@inheritDoc}
     *
     * @return a single-element array containing the agent identifier
     */
    @Override
    public String[] toIndexArgs() {
        return new String[]{agentId};
    }
}
